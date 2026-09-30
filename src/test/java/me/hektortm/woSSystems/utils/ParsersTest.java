package me.hektortm.woSSystems.utils;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link Parsers} — the codec that (de)serialises {@link Location}s to the
 * {@code world,x,y,z[,yaw,pitch]} strings stored in {@code inter_blocks}/{@code placed_citems},
 * plus the pure colour/duration/text helpers. The location round-trip is load-bearing: the
 * same string form is what the T12 Postgres migration transforms, so a regression here is a
 * silent data-corruption class.
 */
class ParsersTest {

    // ---- location codec ------------------------------------------------------------------

    @Test
    void locationRoundTripPreservesAllComponents() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world");

        Location loc = new Location(world, 10.5, 64.0, -20.25, 90.0f, -45.0f);
        String serialized = Parsers.locationToString(loc);
        assertThat(serialized).isEqualTo("world,10.5,64.0,-20.25,90.0,-45.0");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            Location parsed = Parsers.stringToLocation(serialized);
            assertThat(parsed).isNotNull();
            assertThat(parsed.getWorld()).isSameAs(world);
            assertThat(parsed.getX()).isEqualTo(10.5);
            assertThat(parsed.getY()).isEqualTo(64.0);
            assertThat(parsed.getZ()).isEqualTo(-20.25);
            assertThat(parsed.getYaw()).isEqualTo(90.0f);
            assertThat(parsed.getPitch()).isEqualTo(-45.0f);
        }
    }

    @Test
    void locationCodecHandlesOddWorldNamesAndNegativeCoords() {
        World world = mock(World.class);
        when(world.getName()).thenReturn("world_the_end-1");

        Location loc = new Location(world, -0.5, 5.0, -4096.75);
        String serialized = Parsers.locationToString(loc);
        // No yaw/pitch set → default 0.0
        assertThat(serialized).isEqualTo("world_the_end-1,-0.5,5.0,-4096.75,0.0,0.0");

        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world_the_end-1")).thenReturn(world);
            Location parsed = Parsers.stringToLocation(serialized);
            assertThat(parsed).isNotNull();
            assertThat(parsed.getX()).isEqualTo(-0.5);
            assertThat(parsed.getZ()).isEqualTo(-4096.75);
        }
    }

    @Test
    void stringToLocationDefaultsYawAndPitchWhenOmitted() {
        World world = mock(World.class);
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("world")).thenReturn(world);

            Location parsed = Parsers.stringToLocation("world,1.0,2.0,3.0");
            assertThat(parsed).isNotNull();
            assertThat(parsed.getYaw()).isEqualTo(0.0f);
            assertThat(parsed.getPitch()).isEqualTo(0.0f);
        }
    }

    @Test
    void stringToLocationReturnsNullForUnloadedWorld() {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class)) {
            bukkit.when(() -> Bukkit.getWorld("ghost")).thenReturn(null);
            assertThat(Parsers.stringToLocation("ghost,1.0,2.0,3.0")).isNull();
        }
    }

    @Test
    void stringToLocationReturnsNullForMalformedInput() {
        // Fewer than 4 parts, and a non-numeric coordinate: both are caught → null.
        assertThat(Parsers.stringToLocation("world,1.0,2.0")).isNull();
        assertThat(Parsers.stringToLocation("world,x,2.0,3.0")).isNull();
    }

    // ---- cooldown formatting -------------------------------------------------------------

    @Test
    void formatCooldownTimeChoosesTheRightGranularity() {
        assertThat(Parsers.formatCooldownTime(0)).isEqualTo("00");
        assertThat(Parsers.formatCooldownTime(59)).isEqualTo("59");
        assertThat(Parsers.formatCooldownTime(60)).isEqualTo("01:00");
        assertThat(Parsers.formatCooldownTime(3599)).isEqualTo("59:59");
        assertThat(Parsers.formatCooldownTime(3600)).isEqualTo("01:00:00");
        assertThat(Parsers.formatCooldownTime(86400)).isEqualTo("1d 00:00:00");
        assertThat(Parsers.formatCooldownTime(90061)).isEqualTo("1d 01:01:01");
    }

    // ---- colour parsing ------------------------------------------------------------------

    @Test
    void parseColorFromStringParsesArgbHex() {
        Color c = Parsers.parseColorFromString("Color:[argb0xFFFCBA03]");
        assertThat(c.getRed()).isEqualTo(252);
        assertThat(c.getGreen()).isEqualTo(186);
        assertThat(c.getBlue()).isEqualTo(3);
    }

    @Test
    void parseColorFromStringHandlesNullEmptyAndGarbage() {
        assertThat(Parsers.parseColorFromString(null)).isNull();
        assertThat(Parsers.parseColorFromString("")).isNull();
        // Unrecognised format falls through to the WHITE fallback.
        assertThat(Parsers.parseColorFromString("not a color")).isEqualTo(Color.WHITE);
    }

    @Test
    void hexToBukkitColorAcceptsWithAndWithoutHash() {
        Color withHash = Parsers.hexToBukkitColor("#FF8800");
        Color without = Parsers.hexToBukkitColor("FF8800");
        assertThat(withHash.getRed()).isEqualTo(255);
        assertThat(withHash.getGreen()).isEqualTo(136);
        assertThat(withHash.getBlue()).isEqualTo(0);
        assertThat(without).isEqualTo(withHash);
    }

    @Test
    void hexToBukkitColorRejectsWrongLengthAndBadHex() {
        assertThat(Parsers.hexToBukkitColor("FFF")).isNull();
        assertThat(Parsers.hexToBukkitColor("GGGGGG")).isNull();
    }

    // ---- stylised text -------------------------------------------------------------------

    @Test
    void parseUniStaticMapsKnownCharsAndPassesThroughUnknown() {
        assertThat(Parsers.parseUniStatic("abc")).isEqualTo("ᴀʙᴄ");
        assertThat(Parsers.parseUniStatic("A")).isEqualTo("ᴀ");
        // A digit is remapped to a stylised glyph...
        assertThat(Parsers.parseUniStatic("5")).isNotEqualTo("5");
        // ...but a character absent from the table is left untouched.
        assertThat(Parsers.parseUniStatic("é")).isEqualTo("é");
    }
}
