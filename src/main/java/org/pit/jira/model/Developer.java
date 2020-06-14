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
    private String avatar;

    /**
     * Total number of issues that are currently being analyzed.
     */
    @XmlElement
    private Integer count;

    /**
     * Analyzed issues by issue type.
     */
    @XmlElement
    private List<IssueCategory> types;

    /**
     * Analyzed issues by priority.
     */
    @XmlElement
    private List<IssueCategory> priorities;

    /**
     * Total amount of time that has been estimated for analyzed issues in seconds.
     */
    @XmlElement
    private Long totalEstimateSeconds;

    /**
     * The text representation for the total amount of time that has been estimated for analyzed issues.
     * e.g.: "1w 2d 5h 30m".
     */
    @XmlElement
    private String estimate;

    public void setTotalEstimateSeconds(Long totalEstimateSeconds) {
        this.estimate = getEstimateTextRepresentation(totalEstimateSeconds);
        this.totalEstimateSeconds = totalEstimateSeconds;
    }

    /**
     * Convert the estimate in seconds into a text representation. A day in Jira is equal to 8h. A week has 5 days.
     * e.g.: "1w 2d 5h 30m".
     *
     * @param seconds the total estimate in seconds
     * @return the text representation
     */
    private String getEstimateTextRepresentation(Long seconds) {
        String estimateText = "";

        if (seconds >= 144000) {
            int weeks = (int) (seconds / 144000);
            estimateText += weeks + "w";
            seconds -= weeks * 144000;
        }
        if (seconds >= 28800) {
            int days = (int) (seconds / 28800);
            estimateText += " " + days + "d";
            seconds -= days * 28800;
        }
        if (seconds >= 3600) {
            long hours = TimeUnit.SECONDS.toHours(seconds);
            estimateText += " " + hours + "h";
            seconds -= hours * 3600;
        }
        if (seconds >= 60) {
            long minutes = TimeUnit.SECONDS.toMinutes(seconds);
            estimateText += " " + minutes + "m";
        }

        return estimateText.trim();
    }
}
