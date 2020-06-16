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
 * The grant object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "grant")
@XmlAccessorType(XmlAccessType.FIELD)
public class Grant implements Serializable {

    @XmlElement
    private Boolean granted;
}
