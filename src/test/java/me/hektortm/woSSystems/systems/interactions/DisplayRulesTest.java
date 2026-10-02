package me.hektortm.woSSystems.systems.interactions;

import me.hektortm.woSSystems.systems.interactions.DisplayRules.Box;
import me.hektortm.woSSystems.systems.interactions.DisplayRules.Pose;
import me.hektortm.woSSystems.systems.interactions.DisplayRules.Quat;
import me.hektortm.woSSystems.systems.interactions.DisplaySettings.Kind;
import me.hektortm.woSSystems.systems.interactions.DisplaySettings.Vec3;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/** Unit tests for {@link DisplaySettings} (reading the JSON) and {@link DisplayRules} (the maths). */
class DisplayRulesTest {

    private static final double EPS = 1e-5;

    private static void assertQuat(Quat q, double x, double y, double z, double w) {
        assertThat((double) q.x()).isCloseTo(x, within(EPS));
        assertThat((double) q.y()).isCloseTo(y, within(EPS));
        assertThat((double) q.z()).isCloseTo(z, within(EPS));
        assertThat((double) q.w()).isCloseTo(w, within(EPS));
    }

    /** Where a rotation puts a point: q · v · q⁻¹. */
    private static Vec3 rotate(Quat q, Vec3 v) {
        Quat p = DisplayRules.multiply(DisplayRules.multiply(q, new Quat((float) v.x(), (float) v.y(), (float) v.z(), 0)),
                new Quat(-q.x(), -q.y(), -q.z(), q.w()));
        return new Vec3(p.x(), p.y(), p.z());
    }

    private static void assertVec(Vec3 v, double x, double y, double z) {
        assertThat(v.x()).isCloseTo(x, within(EPS));
        assertThat(v.y()).isCloseTo(y, within(EPS));
        assertThat(v.z()).isCloseTo(z, within(EPS));
    }

    @Nested
    class Settings {

        @Test
        void nothingStoredGivesTheDefaults() {
            for (String json : new String[]{null, "", "not json", "[]", "{}"}) {
                DisplaySettings s = DisplaySettings.parse(json);
                assertThat(s.kind()).isEqualTo(Kind.ITEM);
                assertThat(s.item()).isEqualTo("minecraft:stone");
                assertThat(s.scale()).isEqualTo(Vec3.ONE);
                assertThat(s.offset()).isEqualTo(Vec3.ZERO);
                assertThat(s.brightness()).isNull();
                assertThat(s.itemModel()).isEmpty();
                assertThat(s.head()).isEmpty();
                assertThat(s.viewRange()).isEqualTo(1f);
                assertThat(s.animated()).isFalse();
                assertThat(s.touch().enabled()).isFalse();
            }
        }

        @Test
        void readsEveryPart() {
            DisplaySettings s = DisplaySettings.parse("""
                    {"kind":"citem","citem":"wand","item_display":"ground","offset":[0,1.5,0],"yaw":90,"pitch":-10,
                     "transformation":{"translation":[0.1,0.2,0.3],"scale":[2,2,2],"left_rotation_deg":[0,45,0],"right_rotation_deg":[10,0,0]},
                     "billboard":"vertical","brightness":{"block":3,"sky":99},"glowing":true,"glow_color_override":16711680,
                     "view_range":2,"shadow_radius":0.5,"shadow_strength":0.8,"width":3,"height":4,
                     "animation":{"bob":{"enabled":true,"height":0.5,"period":2},"spin":{"enabled":true,"period":1.5,"axis":"Z","reverse":true}},
                     "touch":{"enabled":true,"width":1.5,"height":2}}
                    """);
            assertThat(s.kind()).isEqualTo(Kind.CITEM);
            assertThat(s.citem()).isEqualTo("wand");
            assertThat(s.itemDisplay()).isEqualTo("ground");
            assertThat(s.offset()).isEqualTo(new Vec3(0, 1.5, 0));
            assertThat(s.yaw()).isEqualTo(90f);
            assertThat(s.pitch()).isEqualTo(-10f);
            assertThat(s.translation()).isEqualTo(new Vec3(0.1, 0.2, 0.3));
            assertThat(s.scale()).isEqualTo(new Vec3(2, 2, 2));
            assertThat(s.leftRotationDeg()).isEqualTo(new Vec3(0, 45, 0));
            assertThat(s.rightRotationDeg()).isEqualTo(new Vec3(10, 0, 0));
            assertThat(s.billboard()).isEqualTo("vertical");
            assertThat(s.brightness()).isEqualTo(new DisplaySettings.Brightness(3, 15)); // 99 is cut to 15
            assertThat(s.glowing()).isTrue();
            assertThat(s.glowColor()).isEqualTo(0xFF0000);
            assertThat(s.bob()).isEqualTo(new DisplaySettings.Bob(true, 0.5, 40));
            assertThat(s.spin()).isEqualTo(new DisplaySettings.Spin(true, 30, 'z', true));
            assertThat(s.touch()).isEqualTo(new DisplaySettings.Touch(true, 1.5, 2));
            assertThat(s.animated()).isTrue();
        }

        @Test
        void readsAnItemWithItsModelOrHead() {
            DisplaySettings model = DisplaySettings.parse("{\"kind\":\"item\",\"item\":\"PAPER\",\"item_model\":\"wand\"}");
            assertThat(model.item()).isEqualTo("PAPER");
            assertThat(model.itemModel()).isEqualTo("wand");

            DisplaySettings head = DisplaySettings.parse("{\"kind\":\"item\",\"item\":\"PLAYER_HEAD\",\"head\":\" {player_name} \"}");
            assertThat(head.head()).isEqualTo("{player_name}");
        }

        @Test
        void wrongValuesFallBackInsteadOfFailing() {
            DisplaySettings s = DisplaySettings.parse("""
                    {"kind":"hologram","block":7,"offset":[1,2],"yaw":"north","transformation":"big","head":5,
                     "animation":{"bob":{"enabled":"yes","period":0},"spin":{"enabled":true,"period":0.01,"axis":"q"}},
                     "touch":{"enabled":true,"width":-3}}
                    """);
            assertThat(s.kind()).isEqualTo(Kind.ITEM);
            assertThat(s.block()).isEqualTo("minecraft:stone");
            assertThat(s.offset()).isEqualTo(Vec3.ZERO);
            assertThat(s.yaw()).isEqualTo(0f);
            assertThat(s.scale()).isEqualTo(Vec3.ONE);
            assertThat(s.head()).isEmpty();
            assertThat(s.bob().enabled()).isFalse();
            // Never faster than half a second per turn.
            assertThat(s.spin()).isEqualTo(new DisplaySettings.Spin(true, 10, 'y', false));
            assertThat(s.touch().width()).isEqualTo(0);
        }
    }

    @Nested
    class Rotation {

        @Test
        void noDegreesIsNoRotation() {
            assertQuat(DisplayRules.fromDegrees(Vec3.ZERO), 0, 0, 0, 1);
        }

        @Test
        void aQuarterTurnAroundEachAxis() {
            double h = Math.sqrt(0.5);
            assertQuat(DisplayRules.fromDegrees(new Vec3(90, 0, 0)), h, 0, 0, h);
            assertQuat(DisplayRules.fromDegrees(new Vec3(0, 90, 0)), 0, h, 0, h);
            assertQuat(DisplayRules.fromDegrees(new Vec3(0, 0, 90)), 0, 0, h, h);
        }

        @Test
        void xIsAppliedFirstThenYThenZ() {
            // Up (0,1,0): 90° around x lays it along z, 90° around y then turns that to x.
            assertVec(rotate(DisplayRules.fromDegrees(new Vec3(90, 90, 0)), new Vec3(0, 1, 0)), 1, 0, 0);
            // … and 90° around z then stands it up again.
            assertVec(rotate(DisplayRules.fromDegrees(new Vec3(90, 90, 90)), new Vec3(0, 1, 0)), 0, 1, 0);
        }
    }

    @Nested
    class Animation {

        private DisplaySettings settings(String animation) {
            return DisplaySettings.parse("{\"transformation\":{\"translation\":[1,2,3],\"left_rotation_deg\":[90,0,0]},\"animation\":" + animation + "}");
        }

        @Test
        void aStillDisplayNeedsNoKeyframes() {
            DisplaySettings s = settings("{}");
            assertThat(DisplayRules.keyframeInterval(s)).isZero();
            Pose pose = DisplayRules.poseAt(s, 1234);
            assertThat(pose.translation()).isEqualTo(new Vec3(1, 2, 3));
            assertQuat(pose.leftRotation(), Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
        }

        @Test
        void bobMovesUpAndDownAroundTheRestingPlace() {
            DisplaySettings s = settings("{\"bob\":{\"enabled\":true,\"height\":0.5,\"period\":2}}"); // 40 ticks
            assertVec(DisplayRules.poseAt(s, 0).translation(), 1, 2, 3);
            assertVec(DisplayRules.poseAt(s, 10).translation(), 1, 2.25, 3);
            assertVec(DisplayRules.poseAt(s, 20).translation(), 1, 2, 3);
            assertVec(DisplayRules.poseAt(s, 30).translation(), 1, 1.75, 3);
            assertVec(DisplayRules.poseAt(s, 40).translation(), 1, 2, 3);
            // The rotation is left alone.
            assertQuat(DisplayRules.poseAt(s, 10).leftRotation(), Math.sqrt(0.5), 0, 0, Math.sqrt(0.5));
        }

        @Test
        void spinTurnsOnTopOfTheSetRotation() {
            DisplaySettings s = settings("{\"spin\":{\"enabled\":true,\"period\":2}}"); // 40 ticks, around y
            // The set rotation lays "up" along z; a quarter turn around y then points it along x.
            assertVec(rotate(DisplayRules.poseAt(s, 0).leftRotation(), new Vec3(0, 1, 0)), 0, 0, 1);
            assertVec(rotate(DisplayRules.poseAt(s, 10).leftRotation(), new Vec3(0, 1, 0)), 1, 0, 0);
            assertVec(rotate(DisplayRules.poseAt(s, 50).leftRotation(), new Vec3(0, 1, 0)), 1, 0, 0);
            assertThat(DisplayRules.poseAt(s, 10).translation()).isEqualTo(new Vec3(1, 2, 3));

            DisplaySettings back = settings("{\"spin\":{\"enabled\":true,\"period\":2,\"reverse\":true}}");
            assertVec(rotate(DisplayRules.poseAt(back, 10).leftRotation(), new Vec3(0, 1, 0)), -1, 0, 0);
        }

        @Test
        void bothRunAtTheirOwnSpeed() {
            DisplaySettings s = settings("{\"bob\":{\"enabled\":true,\"height\":1,\"period\":1},\"spin\":{\"enabled\":true,\"period\":4}}");
            Pose pose = DisplayRules.poseAt(s, 5); // a quarter of the bob, a sixteenth of the turn
            assertVec(pose.translation(), 1, 2.5, 3);
            assertVec(rotate(pose.leftRotation(), new Vec3(0, 1, 0)), Math.sin(Math.toRadians(22.5)), 0, Math.cos(Math.toRadians(22.5)));
        }

        @Test
        void keyframesAreFrequentEnoughAndEven() {
            assertThat(DisplayRules.keyframeInterval(settings("{\"spin\":{\"enabled\":true,\"period\":4}}"))).isEqualTo(10);
            assertThat(DisplayRules.keyframeInterval(settings("{\"spin\":{\"enabled\":true,\"period\":1}}"))).isEqualTo(4);
            assertThat(DisplayRules.keyframeInterval(settings("{\"spin\":{\"enabled\":true,\"period\":0.5}}"))).isEqualTo(2);
            assertThat(DisplayRules.keyframeInterval(settings("{\"bob\":{\"enabled\":true,\"period\":3}}"))).isEqualTo(6);
            assertThat(DisplayRules.keyframeInterval(settings("{\"bob\":{\"enabled\":true,\"period\":0.5}}"))).isEqualTo(2);
            // The faster of the two decides.
            assertThat(DisplayRules.keyframeInterval(settings(
                    "{\"bob\":{\"enabled\":true,\"period\":10},\"spin\":{\"enabled\":true,\"period\":1}}"))).isEqualTo(4);
        }

        @Test
        void aSpinNeverTurnsHalfwayBetweenTwoKeyframes() {
            for (double period : new double[]{0.5, 0.7, 1, 1.3, 2, 4, 30}) {
                DisplaySettings s = settings("{\"spin\":{\"enabled\":true,\"period\":" + period + "}}");
                double step = 360.0 * DisplayRules.keyframeInterval(s) / s.spin().periodTicks();
                assertThat(step).as("period %s", period).isLessThanOrEqualTo(90);
            }
        }
    }

    @Nested
    class Touch {

        @Test
        void anItemBoxIsCentredOnTheModel() {
            DisplaySettings s = DisplaySettings.parse(
                    "{\"kind\":\"item\",\"transformation\":{\"translation\":[0,1,0]},\"touch\":{\"enabled\":true,\"width\":1,\"height\":2}}");
            assertThat(DisplayRules.touchBox(s, 10, 64, 10)).isEqualTo(new Box(9.5, 64, 9.5, 10.5, 66, 10.5));
        }

        @Test
        void aBlockBoxIsCentredOnTheBlockNotItsCorner() {
            DisplaySettings s = DisplaySettings.parse(
                    "{\"kind\":\"block\",\"transformation\":{\"scale\":[2,2,2]},\"touch\":{\"enabled\":true,\"width\":2,\"height\":2}}");
            assertThat(DisplayRules.touchBox(s, 0, 0, 0)).isEqualTo(new Box(0, 0, 0, 2, 2, 2));
        }

        @Test
        void boxesOverlapOnlyWhenTheyShareSpace() {
            Box box = new Box(0, 0, 0, 1, 1, 1);
            assertThat(box.overlaps(new Box(0.9, 0.9, 0.9, 2, 2, 2))).isTrue();
            assertThat(box.overlaps(new Box(1, 0, 0, 2, 1, 1))).isFalse(); // only touching
            assertThat(box.overlaps(new Box(0.2, 1.5, 0.2, 0.8, 3, 0.8))).isFalse(); // above
        }

        @Test
        void walkingInCountsOnceUntilThePlayerLeaves() {
            Set<String> inside = new HashSet<>();
            assertThat(DisplayRules.entered(inside, "a", false)).isFalse();
            assertThat(DisplayRules.entered(inside, "a", true)).isTrue();
            assertThat(DisplayRules.entered(inside, "a", true)).isFalse();
            assertThat(DisplayRules.entered(inside, "b", true)).isTrue(); // another display counts on its own
            assertThat(DisplayRules.entered(inside, "a", false)).isFalse();
            assertThat(DisplayRules.entered(inside, "a", true)).isTrue();
        }
    }

    @Nested
    class ClientValues {

        @Test
        void brightnessIsPackedAsTheClientReadsIt() {
            assertThat(DisplayRules.packBrightness(0, 0)).isZero();
            assertThat(DisplayRules.packBrightness(15, 15)).isEqualTo(15 << 4 | 15 << 20);
            assertThat(DisplayRules.packBrightness(5, 0)).isEqualTo(80);
        }

        @Test
        void namesBecomeTheirIds() {
            assertThat(DisplayRules.billboard("fixed")).isEqualTo((byte) 0);
            assertThat(DisplayRules.billboard("Center")).isEqualTo((byte) 3);
            assertThat(DisplayRules.billboard("nonsense")).isEqualTo((byte) 0);
            assertThat(DisplayRules.itemDisplay("none")).isEqualTo((byte) 0);
            assertThat(DisplayRules.itemDisplay("ground")).isEqualTo((byte) 7);
            assertThat(DisplayRules.itemDisplay("FIXED")).isEqualTo((byte) 8);
            assertThat(DisplayRules.itemDisplay("nonsense")).isEqualTo((byte) 0);
        }
    }
}
