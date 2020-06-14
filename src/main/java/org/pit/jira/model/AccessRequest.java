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
 * The access request object.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "accessRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccessRequest implements Serializable {

    @XmlElement()
    private List<String> dataTypes;

    @XmlElement()
    private String justification;

    @XmlElement()
    private String tool;

    @XmlElement()
    private String userRid;

    @XmlElement()
    private List<Owner> owners;

    /**
     * Temporary Owner object. To be removed when the API changes.
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @XmlRootElement(name = "owner")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Owner implements Serializable {

        @XmlElement()
        private String email;
    }
}
