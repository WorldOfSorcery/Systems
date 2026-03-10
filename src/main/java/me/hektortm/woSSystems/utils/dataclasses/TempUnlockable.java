package me.hektortm.woSSystems.utils.dataclasses;

/**
 * Convenience wrapper for a temporary {@link Unlockable}.
 * Equivalent to {@code new Unlockable(id, true)}.
 */
public class TempUnlockable extends Unlockable {

    public TempUnlockable(String id) {
        super(id, true);
    }
}
