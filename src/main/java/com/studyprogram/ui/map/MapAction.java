package com.studyprogram.ui.map;

import com.studyprogram.model.Topic;

/**
 * What the student asked for by closing the map: study a topic, fight a world's boss, or nothing.
 * The map is a navigation surface, so it hands the request back to the CLI rather than driving
 * the session itself.
 */
public record MapAction(Kind kind, Topic topic, int world) {

    public enum Kind { NONE, STUDY_TOPIC, FIGHT_BOSS }

    public static MapAction none()                { return new MapAction(Kind.NONE, null, 0); }
    public static MapAction study(Topic topic)    { return new MapAction(Kind.STUDY_TOPIC, topic, 0); }
    public static MapAction boss(int world)       { return new MapAction(Kind.FIGHT_BOSS, null, world); }

    public boolean isNone() { return kind == Kind.NONE; }
}
