package com.studyprogram.ui.map;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapModelTest {

    @Test
    void everyTopicGetsExactlyOneNode() {
        List<MapModel.Node> nodes = MapModel.build(new StudentProfile("s"));
        assertEquals(Topic.values().length, nodes.size());
        assertEquals(Topic.values().length,
                nodes.stream().map(MapModel.Node::topic).distinct().count());
    }

    @Test
    void columnsMatchLevelBands() {
        for (MapModel.Node n : MapModel.build(new StudentProfile("s"))) {
            assertEquals(n.topic().baseLevel - 1, n.col());
            assertTrue(n.row() >= 0);
        }
    }

    @Test
    void blankProfileStates() {
        StudentProfile p = new StudentProfile("s");
        assertEquals(MapModel.NodeState.AVAILABLE, MapModel.stateOf(Topic.VARIABLES, p));
        assertEquals(MapModel.NodeState.LOCKED, MapModel.stateOf(Topic.LOOPS, p));
        assertEquals(MapModel.NodeState.SECRET, MapModel.stateOf(Topic.MACHINE_LEARNING, p));
    }

    @Test
    void progressChangesStates() {
        StudentProfile p = new StudentProfile("s");
        var perf = p.getOrCreatePerformance(Topic.VARIABLES);
        perf.setAttempts(5);
        perf.setMasteryScore(0.5);
        assertEquals(MapModel.NodeState.IN_PROGRESS, MapModel.stateOf(Topic.VARIABLES, p));
        assertEquals(MapModel.NodeState.LOCKED, MapModel.stateOf(Topic.LOOPS, p),
                "IF_CASE prereq not met yet");
        p.getOrCreatePerformance(Topic.IF_CASE).setMasteryScore(0.5);
        assertEquals(MapModel.NodeState.AVAILABLE, MapModel.stateOf(Topic.LOOPS, p));
        perf.setMasteryScore(0.9);
        assertEquals(MapModel.NodeState.MASTERED, MapModel.stateOf(Topic.VARIABLES, p));
    }

    @Test
    void secretNodesHideTheirName() {
        StudentProfile p = new StudentProfile("s");
        MapModel.Node secret = MapModel.build(p).stream()
                .filter(n -> n.topic() == Topic.MACHINE_LEARNING).findFirst().orElseThrow();
        assertEquals("? ? ?", secret.label());
    }
}
