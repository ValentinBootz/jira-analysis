package org.pit.jira.access;

/**
 * Enum for Dashboard Item types.
 */
public enum ItemType {

    EXPERT("expert"),
    LEADERBOARD("leaderboard"),
    HELP("help"),
    SUPPORTER("supporter");

    private final String itemType;

    ItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemType() {
        return this.itemType;
    }
}
