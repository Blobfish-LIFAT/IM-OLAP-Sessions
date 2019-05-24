
package fr.univ_tours.info.im_olap.data.cubeloadBeans;

import java.math.BigInteger;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}Name"/>
 *         &lt;element ref="{}SeedQueries"/>
 *         &lt;element ref="{}MinSessionLength"/>
 *         &lt;element ref="{}MaxSessionLength"/>
 *         &lt;element ref="{}NumberOfSessions"/>
 *         &lt;element ref="{}YearPrompt"/>
 *         &lt;element ref="{}SegregationPredicate"/>
 *       &lt;/sequence>
 *       &lt;attribute name="progressive" type="{http://www.w3.org/2001/XMLSchema}integer" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "name",
    "seedQueries",
    "minSessionLength",
    "maxSessionLength",
    "numberOfSessions",
    "yearPrompt",
    "segregationPredicate"
})
@XmlRootElement(name = "Profile")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Profile {

    @XmlElement(name = "Name", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Name name;
    @XmlElement(name = "SeedQueries", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected SeedQueries seedQueries;
    @XmlElement(name = "MinSessionLength", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MinSessionLength minSessionLength;
    @XmlElement(name = "MaxSessionLength", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected MaxSessionLength maxSessionLength;
    @XmlElement(name = "NumberOfSessions", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected NumberOfSessions numberOfSessions;
    @XmlElement(name = "YearPrompt", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected YearPrompt yearPrompt;
    @XmlElement(name = "SegregationPredicate", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected SegregationPredicate segregationPredicate;
    @XmlAttribute(name = "progressive")
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected BigInteger progressive;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the seedQueries property.
     * 
     * @return
     *     possible object is
     *     {@link SeedQueries }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public SeedQueries getSeedQueries() {
        return seedQueries;
    }

    /**
     * Sets the value of the seedQueries property.
     * 
     * @param value
     *     allowed object is
     *     {@link SeedQueries }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setSeedQueries(SeedQueries value) {
        this.seedQueries = value;
    }

    /**
     * Gets the value of the minSessionLength property.
     * 
     * @return
     *     possible object is
     *     {@link MinSessionLength }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MinSessionLength getMinSessionLength() {
        return minSessionLength;
    }

    /**
     * Sets the value of the minSessionLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link MinSessionLength }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMinSessionLength(MinSessionLength value) {
        this.minSessionLength = value;
    }

    /**
     * Gets the value of the maxSessionLength property.
     * 
     * @return
     *     possible object is
     *     {@link MaxSessionLength }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public MaxSessionLength getMaxSessionLength() {
        return maxSessionLength;
    }

    /**
     * Sets the value of the maxSessionLength property.
     * 
     * @param value
     *     allowed object is
     *     {@link MaxSessionLength }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setMaxSessionLength(MaxSessionLength value) {
        this.maxSessionLength = value;
    }

    /**
     * Gets the value of the numberOfSessions property.
     * 
     * @return
     *     possible object is
     *     {@link NumberOfSessions }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public NumberOfSessions getNumberOfSessions() {
        return numberOfSessions;
    }

    /**
     * Sets the value of the numberOfSessions property.
     * 
     * @param value
     *     allowed object is
     *     {@link NumberOfSessions }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setNumberOfSessions(NumberOfSessions value) {
        this.numberOfSessions = value;
    }

    /**
     * Gets the value of the yearPrompt property.
     * 
     * @return
     *     possible object is
     *     {@link YearPrompt }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public YearPrompt getYearPrompt() {
        return yearPrompt;
    }

    /**
     * Sets the value of the yearPrompt property.
     * 
     * @param value
     *     allowed object is
     *     {@link YearPrompt }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setYearPrompt(YearPrompt value) {
        this.yearPrompt = value;
    }

    /**
     * Gets the value of the segregationPredicate property.
     * 
     * @return
     *     possible object is
     *     {@link SegregationPredicate }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public SegregationPredicate getSegregationPredicate() {
        return segregationPredicate;
    }

    /**
     * Sets the value of the segregationPredicate property.
     * 
     * @param value
     *     allowed object is
     *     {@link SegregationPredicate }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setSegregationPredicate(SegregationPredicate value) {
        this.segregationPredicate = value;
    }

    /**
     * Gets the value of the progressive property.
     * 
     * @return
     *     possible object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public BigInteger getProgressive() {
        return progressive;
    }

    /**
     * Sets the value of the progressive property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigInteger }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2019-05-23T01:58:24+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setProgressive(BigInteger value) {
        this.progressive = value;
    }

}
