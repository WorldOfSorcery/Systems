package me.hektortm.woSSystems.utils.dataclasses;

import java.util.UUID;

public class Challenge {
    private final UUID challenger;
    private final int amount;
    private final String choice;

    public Challenge(UUID challenger, int amount, String choice) {
        this.challenger = challenger;
        this.amount = amount;
        this.choice = choice;
    }

    public UUID getChallenger() {
        return challenger;
    }

    public int getAmount() {
        return amount;
    }

    public String getChoice() {
        return choice;
    }
}