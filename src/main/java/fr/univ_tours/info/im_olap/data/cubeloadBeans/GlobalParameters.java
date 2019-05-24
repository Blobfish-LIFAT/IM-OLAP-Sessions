
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
 *         &lt;element ref="{}NumberOfProfiles"/>
 *         &lt;element ref="{}MaxMeasures"/>
 *         &lt;element ref="{}MinReportSize"/>
 *         &lt;element ref="{}MaxReportSize"/>
 *         &lt;element ref="{}SurprisingQueries"/>
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
    "numberOfProfiles",
    "maxMeasures",
    "minReportSize",
    "maxReportSize",
    "surprisingQueries"
})
@XmlRootElement(name = "GlobalParameters")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class GlobalParameters {

    @XmlElement(name = "NumberOfProfiles", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected NumberOfProfiles numberOfProfiles;
    @XmlElement(name = "MaxMeasures", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MaxMeasures maxMeasures;
    @XmlElement(name = "MinReportSize", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MinReportSize minReportSize;
    @XmlElement(name = "MaxReportSize", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MaxReportSize maxReportSize;
    @XmlElement(name = "SurprisingQueries", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected SurprisingQueries surprisingQueries;

    /**
     * Gets the value of the numberOfProfiles property.
     * 
     * @return
     *     possible object is
     *     {@link NumberOfProfiles }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public NumberOfProfiles getNumberOfProfiles() {
        return numberOfProfiles;
    }

    /**
     * Sets the value of the numberOfProfiles property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberOfProfiles }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setNumberOfProfiles(NumberOfProfiles value) {
        this.numberOfProfiles = value;
    }

    /**
     * Gets the value of the maxMeasures property.
     * 
     * @return
     *     possible object is
     *     {@link MaxMeasures }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MaxMeasures getMaxMeasures() {
        return maxMeasures;
    }

    /**
     * Sets the value of the maxMeasures property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaxMeasures }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMaxMeasures(MaxMeasures value) {
        this.maxMeasures = value;
    }

    /**
     * Gets the value of the minReportSize property.
     * 
     * @return
     *     possible object is
     *     {@link MinReportSize }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MinReportSize getMinReportSize() {
        return minReportSize;
    }

    /**
     * Sets the value of the minReportSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link MinReportSize }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMinReportSize(MinReportSize value) {
        this.minReportSize = value;
    }

    /**
     * Gets the value of the maxReportSize property.
     * 
     * @return
     *     possible object is
     *     {@link MaxReportSize }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MaxReportSize getMaxReportSize() {
        return maxReportSize;
    }

    /**
     * Sets the value of the maxReportSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaxReportSize }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMaxReportSize(MaxReportSize value) {
        this.maxReportSize = value;
    }

    /**
     * Gets the value of the surprisingQueries property.
     * 
     * @return
     *     possible object is
     *     {@link SurprisingQueries }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public SurprisingQueries getSurprisingQueries() {
        return surprisingQueries;
    }

    /**
     * Sets the value of the surprisingQueries property.
     * 
     * @param value
     *     allowed object is
     *     {@link SurprisingQueries }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setSurprisingQueries(SurprisingQueries value) {
        this.surprisingQueries = value;
    }

}
