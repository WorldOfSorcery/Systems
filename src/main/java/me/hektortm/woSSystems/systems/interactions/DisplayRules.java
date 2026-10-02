package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.systems.interactions.DisplaySettings.Kind;
import me.hektortm.woSSystems.systems.interactions.DisplaySettings.Vec3;

import java.util.Set;

/**
 * The server-free rules of interaction displays: rotation maths, where an
 * animated display is at a given tick, how often it needs a new keyframe, and
 * the touch box. Unit-tested in {@code DisplayRulesTest}.
 */
public final class DisplayRules {
    private DisplayRules() {}

    /** A rotation as the client wants it: x, y, z, w. */
    public record Quat(float x, float y, float z, float w) {
        public static final Quat IDENTITY = new Quat(0, 0, 0, 1);
    }

    /** What changes while a display is animated: its translation and left rotation. */
    public record Pose(Vec3 translation, Quat leftRotation) {}

    /** An axis-aligned box, by its lowest and highest corner. */
    public record Box(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        public boolean overlaps(Box o) {
            return minX < o.maxX && maxX > o.minX
                    && minY < o.maxY && maxY > o.minY
                    && minZ < o.maxZ && maxZ > o.minZ;
        }
    }

    // ── rotation ───────────────────────────────────────────────────────────────

    /** A turn of {@code degrees} around one axis ('x', 'y' or 'z'). */
    public static Quat turn(char axis, double degrees) {
        double half = Math.toRadians(degrees) / 2;
        float s = (float) Math.sin(half);
        float c = (float) Math.cos(half);
        if (axis == 'x') return new Quat(s, 0, 0, c);
        if (axis == 'z') return new Quat(0, 0, s, c);
        return new Quat(0, s, 0, c);
    }

    /** The rotation "first b, then a". */
    public static Quat multiply(Quat a, Quat b) {
        return new Quat(
                a.w() * b.x() + a.x() * b.w() + a.y() * b.z() - a.z() * b.y(),
                a.w() * b.y() - a.x() * b.z() + a.y() * b.w() + a.z() * b.x(),
                a.w() * b.z() + a.x() * b.y() - a.y() * b.x() + a.z() * b.w(),
                a.w() * b.w() - a.x() * b.x() - a.y() * b.y() - a.z() * b.z());
    }

    /** Degrees around x, y and z (applied in that order) as one rotation. */
    public static Quat fromDegrees(Vec3 deg) {
        return multiply(turn('z', deg.z()), multiply(turn('y', deg.y()), turn('x', deg.x())));
    }

    // ── animation ──────────────────────────────────────────────────────────────

    /**
     * Ticks between two keyframes, 0 for a display that is not animated. The
     * client moves smoothly from one keyframe to the next; a spin must stay well
     * under half a turn per keyframe (a rotation is interpolated the short way)
     * and a bob needs enough keyframes to look like a wave. Always even, because
     * the animation task runs every second tick.
     */
    public static int keyframeInterval(DisplaySettings s) {
        if (!s.animated()) return 0;
        int interval = 10;
        if (s.spin().enabled()) interval = Math.min(interval, s.spin().periodTicks() / 4);
        if (s.bob().enabled()) interval = Math.min(interval, s.bob().periodTicks() / 8);
        return Math.max(2, interval - interval % 2);
    }

    /** Translation and left rotation of a display at a server tick. */
    public static Pose poseAt(DisplaySettings s, long tick) {
        Vec3 t = s.translation();
        if (s.bob().enabled()) {
            double phase = 2 * Math.PI * Math.floorMod(tick, (long) s.bob().periodTicks()) / s.bob().periodTicks();
            t = new Vec3(t.x(), t.y() + s.bob().height() / 2 * Math.sin(phase), t.z());
        }
        Quat left = fromDegrees(s.leftRotationDeg());
        if (s.spin().enabled()) {
            double angle = 360.0 * Math.floorMod(tick, (long) s.spin().periodTicks()) / s.spin().periodTicks();
            left = multiply(turn(s.spin().axis(), s.spin().reverse() ? -angle : angle), left);
        }
        return new Pose(t, left);
    }

    // ── touch ──────────────────────────────────────────────────────────────────

    /**
     * The box a player must walk into or click, for a display whose entity is at x, y, z.
     * It is centred on the resting model: the entity position plus the
     * translation, and for a block (drawn from its corner) plus half its size.
     */
    public static Box touchBox(DisplaySettings s, double x, double y, double z) {
        Vec3 t = s.translation();
        double cx = x + t.x(), cy = y + t.y(), cz = z + t.z();
        if (s.kind() == Kind.BLOCK) {
            cx += s.scale().x() / 2;
            cy += s.scale().y() / 2;
            cz += s.scale().z() / 2;
        }
        double w = s.touch().width() / 2, h = s.touch().height() / 2;
        return new Box(cx - w, cy - h, cz - w, cx + w, cy + h, cz + w);
    }

    /**
     * Keeps track of who is inside which box. Returns true only at the moment of
     * walking in: staying inside does not count again, leaving resets it.
     */
    public static boolean entered(Set<String> inside, String key, boolean overlapping) {
        if (!overlapping) {
            inside.remove(key);
            return false;
        }
        return inside.add(key);
    }

    // ── values the client wants as numbers ─────────────────────────────────────

    /** Block and sky light (0–15 each) packed the way the client reads a brightness override. */
    public static int packBrightness(int block, int sky) {
        return block << 4 | sky << 20;
    }

    public static byte billboard(String name) {
        switch (name.toLowerCase()) {
            case "vertical": return 1;
            case "horizontal": return 2;
            case "center": return 3;
            default: return 0;
        }
    }

    /** Minecraft's item display modes, in the order of their ids. */
    private static final String[] ITEM_DISPLAYS = {
            "none", "thirdperson_lefthand", "thirdperson_righthand", "firstperson_lefthand",
            "firstperson_righthand", "head", "gui", "ground", "fixed"
    };

    public static byte itemDisplay(String name) {
        for (byte i = 0; i < ITEM_DISPLAYS.length; i++) {
            if (ITEM_DISPLAYS[i].equalsIgnoreCase(name)) return i;
        }
        return 0;
    }
}
