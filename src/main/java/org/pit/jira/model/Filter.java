package org.pit.jira.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * The filter object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "filter")
@XmlAccessorType(XmlAccessType.FIELD)
public class Filter {

    /**
     * Filter by usernames.
     */
    @XmlElement
    private List<String> users;

    /**
     * Filter by project names.
     */
    @XmlElement
    private List<String> projects;

    /**
     * Filter by issue type names.
     */
    @XmlElement
    private List<String> types;

    /**
     * Filter by priority names.
     */
    @XmlElement
    private List<String> priorities;
}
