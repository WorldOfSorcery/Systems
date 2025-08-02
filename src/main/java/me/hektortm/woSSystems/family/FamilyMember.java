package me.hektortm.woSSystems.family;

import java.util.UUID;

public class FamilyMember {

    private final UUID uuid;
    private final Role role;
    private final String prefix;


    public FamilyMember(UUID uuid, Role role, String prefix) {
        this.uuid = uuid;
        this.role = role;
        this.prefix = prefix;
    }

    public UUID getUuid() {
        return uuid;
    }
    public Role getRole() {
        return role;
    }
    public String getPrefix() {
        return prefix;
    }
}
