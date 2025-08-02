package me.hektortm.woSSystems.family;

import java.util.List;
import java.util.UUID;

public class Family {

    private final String id;
    private final String name;
    private final UUID owner;
    private final List<FamilyMember> members;


    public Family(String id, String name, UUID owner, List<FamilyMember> members) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.members = members;
    }

    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public UUID getOwner() {
        return owner;
    }
    public List<FamilyMember> getMembers() {
        return members;
    }
}
