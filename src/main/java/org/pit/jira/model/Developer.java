package org.pit.jira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * The developer object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "developer")
@XmlAccessorType(XmlAccessType.FIELD)
public class Developer implements Serializable {

    @XmlElement
    private String name;

    @XmlElement
    private String avatarUrl;

    /**
     * Number of issues that are not with the status "COMPLETE".
     */
    @XmlElement
    private Integer openIssueCount;

    /**
     * Open issues by issue type.
     */
    @XmlElement
    private List<IssueCategory> openIssueTypes;

    /**
     * Open issues by priority.
     */
    @XmlElement
    private List<IssueCategory> openIssuePriorities;

    /**
     * Total amount of time that has been estimated for open issues in hours.
     */
    @XmlElement
    private Long totalOpenEstimate;
}
