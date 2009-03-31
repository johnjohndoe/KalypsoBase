
package org.kalypso.services.observation.sei.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "adaptItemResponse", namespace = "http://sei.observation.services.kalypso.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "adaptItemResponse", namespace = "http://sei.observation.services.kalypso.org/")
public class AdaptItemResponse {

    @XmlElement(name = "return", namespace = "")
    private org.kalypso.services.observation.sei.ObservationBean _return;

    /**
     * 
     * @return
     *     returns ObservationBean
     */
    public org.kalypso.services.observation.sei.ObservationBean getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(org.kalypso.services.observation.sei.ObservationBean _return) {
        this._return = _return;
    }

}
