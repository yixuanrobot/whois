//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.27 at 03:38:16 PM EST 
//


package net.ripe.db.whois.api.whois.rdap.domain.vcard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vcardEntries" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
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
    "name",
    "vcardEntries"
})
@XmlRootElement(name = "vcard")
public class Vcard
    extends net.ripe.db.whois.api.whois.rdap.VcardObject
    implements Serializable
{

    @XmlElement(defaultValue = "vcard")
    protected String name;
    protected List<net.ripe.db.whois.api.whois.rdap.VcardObject> vcardEntries;

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the vcardEntries property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the vcardEntries property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVcardEntries().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link net.ripe.db.whois.api.whois.rdap.VcardObject }
     * 
     * 
     */
    public List<net.ripe.db.whois.api.whois.rdap.VcardObject> getVcardEntries() {
        if (vcardEntries == null) {
            vcardEntries = new ArrayList<net.ripe.db.whois.api.whois.rdap.VcardObject>();
        }
        return this.vcardEntries;
    }

    /**
     * Gets the value of the name property.
     * 
     */
    public String getName() {
        if (null == name) {
            return "vcard";
        }
        return name;
    }

}
