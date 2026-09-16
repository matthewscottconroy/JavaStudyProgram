package com.studyprogram.ui.map;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The map's behaviour, exercised without a display: selection, keyboard navigation and the
 * actions it hands back to the CLI. Painting is left to the eye, but everything that decides
 * what happens when a student presses a key is ordinary logic and is covered here.
 */
class MapPanelTest {

    private static StudentProfile profileWithProgress() {
        StudentProfile p = new StudentProfile("Ada");
        p.getOrCreatePerformance(Topic.VARIABLES).setMasteryScore(0.9);
        p.getOrCreatePerformance(Topic.VARIABLES).setAttempts(10);
        return p;
    }

    private static void fire(MapPanel panel, String actionKey) {
        Action action = panel.getActionMap().get(actionKey);
        assertNotNull(action, "no action bound for '" + actionKey + "'");
        action.actionPerformed(new ActionEvent(panel, ActionEvent.ACTION_PERFORMED, actionKey));
    }

    @Test
    void enterTogglesTheFocusedTopicIntoTheSessionAndSaves() {
        StudentProfile profile = profileWithProgress();
        AtomicInteger saves = new AtomicInteger();
        MapPanel panel = new MapPanel(profile, saves::incrementAndGet, a -> { });

        int before = profile.getSelectedTopics().size();
        fire(panel, "toggle");
        assertEquals(before + 1, profile.getSelectedTopics().size(), "Enter should select a topic");
        int savesAfterSelect = saves.get();

        fire(panel, "toggle");
        assertEquals(before, profile.getSelectedTopics().size(), "Enter again should deselect");
        assertTrue(saves.get() > savesAfterSelect, "each change must be persisted");
    }

    @Test
    void arrowKeysMoveTheFocusWithoutChangingSelection() {
        StudentProfile profile = profileWithProgress();
        MapPanel panel = new MapPanel(profile, () -> { }, a -> { });

        fire(panel, "next");
        fire(panel, "next");
        fire(panel, "prev");
        fire(panel, "nextCol");
        fire(panel, "prevCol");

        assertTrue(profile.getSelectedTopics().isEmpty(),
                "moving the focus must not select anything");
    }

    @Test
    void pressingSAsksTheProgramToStudyTheFocusedTopic() {
        AtomicReference<MapAction> requested = new AtomicReference<>();
        MapPanel panel = new MapPanel(profileWithProgress(), () -> { }, requested::set);

        fire(panel, "study");

        MapAction action = requested.get();
        assertNotNull(action, "S must hand an action back to the caller");
        assertEquals(MapAction.Kind.STUDY_TOPIC, action.kind());
        assertNotNull(action.topic());
        assertFalse(action.isNone());
    }

    @Test
    void pressingBOnlyFightsABossThatIsActuallyUnlocked() {
        // a blank profile has no world at 50% average mastery, so no boss may be started
        AtomicReference<MapAction> fromBlank = new AtomicReference<>();
        new MapPanelHarness(new StudentProfile("Blank"), fromBlank).press("boss");
        assertNull(fromBlank.get(), "a locked boss must not be startable from the map");

        // give World 1 enough mastery and the same key now starts the fight
        StudentProfile ready = new StudentProfile("Ready");
        for (Topic t : Topic.visibleValues()) {
            if (t.baseLevel == 1) ready.getOrCreatePerformance(t).setMasteryScore(0.9);
        }
        AtomicReference<MapAction> fromReady = new AtomicReference<>();
        new MapPanelHarness(ready, fromReady).press("boss");
        MapAction action = fromReady.get();
        assertNotNull(action, "an unlocked boss should be startable");
        assertEquals(MapAction.Kind.FIGHT_BOSS, action.kind());
        assertEquals(1, action.world());
    }

    @Test
    void firstVisitMarksNewlyUnlockedTopicsAsSeen() {
        StudentProfile profile = new StudentProfile("Ada");
        assertTrue(profile.getRevealedOnMap().isEmpty());

        new MapPanel(profile, () -> { }, a -> { });
        assertFalse(profile.getRevealedOnMap().isEmpty(),
                "opening the map records which topics have been revealed");
        int afterFirst = profile.getRevealedOnMap().size();

        new MapPanel(profile, () -> { }, a -> { });
        assertEquals(afterFirst, profile.getRevealedOnMap().size(),
                "a second visit must not re-celebrate the same topics");
    }

    @Test
    void thePanelDescribesItselfForAssistiveTechnology() {
        MapPanel panel = new MapPanel(profileWithProgress(), () -> { }, a -> { });
        var context = panel.getAccessibleContext();
        assertEquals("Concept map", context.getAccessibleName());
        assertNotNull(context.getAccessibleDescription());
        assertTrue(context.getAccessibleDescription().contains("Arrow keys"),
                "the description must explain how to drive the map by keyboard");
        assertTrue(panel.isFocusable(), "a keyboard-driven panel must be focusable");
    }

    @Test
    void mapActionsAreWellFormed() {
        assertTrue(MapAction.none().isNone());
        assertEquals(Topic.LOOPS, MapAction.study(Topic.LOOPS).topic());
        assertEquals(3, MapAction.boss(3).world());
        assertFalse(MapAction.study(Topic.LOOPS).isNone());
    }

    /** Small helper so a test can press a key on a freshly built panel. */
    private record MapPanelHarness(StudentProfile profile, AtomicReference<MapAction> sink) {
        void press(String actionKey) {
            MapPanel panel = new MapPanel(profile, () -> { }, sink::set);
            fire(panel, actionKey);
        }
    }
}
