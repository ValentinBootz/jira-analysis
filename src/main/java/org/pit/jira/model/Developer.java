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
import java.util.concurrent.TimeUnit;

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
     * Total amount of time that has been estimated for open issues in seconds.
     */
    @XmlElement
    private Long totalOpenEstimate;

    /**
     * The text representation for the total amount of time that has been estimated for open issues.
     * e.g.: "1w 2d 5h 30m".
     */
    @XmlElement
    private String totalOpenEstimateText;

    public void setTotalOpenEstimate(Long totalOpenEstimate) {
        this.totalOpenEstimateText = getEstimateTextRepresentation(totalOpenEstimate);
        this.totalOpenEstimate = totalOpenEstimate;
    }

    /**
     * Convert the estimate in seconds into a text representation.
     * e.g.: "1w 2d 5h 30m".
     *
     * @param seconds the total estimate in seconds
     * @return the text representation
     */
    private String getEstimateTextRepresentation(Long seconds) {
        int weeks = (int) (TimeUnit.SECONDS.toDays(seconds) / 7);
        int days = (int) (TimeUnit.SECONDS.toDays(seconds) - 7 * weeks);
        long hours = TimeUnit.SECONDS.toHours(seconds) - TimeUnit.DAYS.toHours(days) - TimeUnit.DAYS.toHours(7 * weeks);
        long minutes = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);

        return weeks + "w " + days + "d " + hours + "h " + minutes + "m";
    }
}
