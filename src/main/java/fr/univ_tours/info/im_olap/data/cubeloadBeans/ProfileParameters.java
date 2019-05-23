
package fr.univ_tours.info.im_olap.data.cubeloadBeans;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}Profile"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "profile"
})
@XmlRootElement(name = "ProfileParameters")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class ProfileParameters {

    @XmlElement(name = "Profile", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Profile profile;

    /**
     * Gets the value of the profile property.
     * 
     * @return
     *     possible object is
     *     {@link Profile }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Profile getProfile() {
        return profile;
    }

    /**
     * Sets the value of the profile property.
     * 
     * @param value
     *     allowed object is
     *     {@link Profile }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setProfile(Profile value) {
        this.profile = value;
    }

}
