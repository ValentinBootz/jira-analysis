package org.pit.jira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The developer object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Developer {

    private String name;

    private String avatarUrl;

    /**
     * Number of issues that are not with the status "COMPLETE".
     */
    private Integer openIssueCount;
}
