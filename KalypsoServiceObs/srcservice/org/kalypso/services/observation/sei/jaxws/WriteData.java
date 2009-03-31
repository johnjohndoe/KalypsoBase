
package org.kalypso.services.observation.sei.jaxws;

import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "writeData", namespace = "http://sei.observation.services.kalypso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "writeData", namespace = "http://sei.observation.services.kalypso.org/", propOrder = {
    "arg0",
    "arg1"
})
public class WriteData {

    @XmlElement(name = "arg0", namespace = "")
    private org.kalypso.services.observation.sei.ObservationBean arg0;
    @XmlElement(name = "arg1", namespace = "")
    private DataHandler arg1;

    /**
     * 
     * @return
     *     returns ObservationBean
     */
    public org.kalypso.services.observation.sei.ObservationBean getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(org.kalypso.services.observation.sei.ObservationBean arg0) {
        this.arg0 = arg0;
    }

    /**
     * 
     * @return
     *     returns DataHandler
     */
    public DataHandler getArg1() {
        return this.arg1;
    }

    /**
     * 
     * @param arg1
     *     the value for the arg1 property
     */
    public void setArg1(DataHandler arg1) {
        this.arg1 = arg1;
    }

}
