package org.pit.jira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * The issue category object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "issueCategory")
@XmlAccessorType(XmlAccessType.FIELD)
public class IssueCategory implements Serializable {

    /**
     * The name of this category (e.g. priority "high").
     */
    @XmlElement
    private String name;

    @XmlElement
    private String iconUrl;

    /**
     * The number of issues that fall into this category.
     */
    @XmlElement
    private Integer count;
}
