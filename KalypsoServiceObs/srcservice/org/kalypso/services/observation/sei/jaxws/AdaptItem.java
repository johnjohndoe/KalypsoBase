
package org.kalypso.services.observation.sei.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "adaptItem", namespace = "http://sei.observation.services.kalypso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adaptItem", namespace = "http://sei.observation.services.kalypso.org/")
public class AdaptItem {

    @XmlElement(name = "arg0", namespace = "")
    private org.kalypso.services.observation.sei.ItemBean arg0;

    /**
     * 
     * @return
     *     returns ItemBean
     */
    public org.kalypso.services.observation.sei.ItemBean getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(org.kalypso.services.observation.sei.ItemBean arg0) {
        this.arg0 = arg0;
    }

}
