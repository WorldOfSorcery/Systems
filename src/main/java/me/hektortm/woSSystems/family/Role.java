package me.hektortm.woSSystems.family;

public enum Role {

    HEAD("Head of Family"),
    ELDER("Elder"),
    MEMBER("Member");

    private final String displayName;
    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Role fromString(String role) {
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(role)) {
                return r;
            }
        }
        return null; // or throw an exception if preferred
    }

}
