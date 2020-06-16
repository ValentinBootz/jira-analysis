package org.pit.jira.access;

/**
 * Enum for accessed data types.
 */
public enum DataType {

    // Issue data types.
    ISSUE_TYPE("issue_type"),
    ISSUE_PRIORITY("issue_priority"),
    ISSUE_STATUS("issue_status"),
    ISSUE_ESTIMATE("issue_estimate"),
    ISSUE_TITLE("issue_title"),
    ISSUE_SUMMARY("issue_summary"),
    ISSUE_LINK("issue_link"),

    // Assignee data types.
    ASSIGNEE_NAME("assignee_name"),
    ASSIGNEE_EMAIL("assignee_email"),
    ASSIGNEE_AVATAR("assignee_avatar");

    private final String dataType;

    DataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDataType() {
        return this.dataType;
    }
}
