package org.kalypso.chart.ext.observation.layer.provider;

import java.net.URL;
import java.util.Calendar;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.ext.observation.layer.TupleResultLineLayer;
import org.kalypso.chart.factory.configuration.exception.LayerProviderException;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;

public class GaugeLayerProvider extends AbstractLayerProvider {

	public IChartLayer getLayer(URL context) throws LayerProviderException {
		
		IChartLayer icl=null;
		
		String href=m_pc.getParameterValue("href", null);

		String observationId = m_pc.getParameterValue("observationId", null);
		String domainComponentName=m_pc.getParameterValue("domainComponent", null);
		String targetComponentName=m_pc.getParameterValue("targetComponent", null);
		
		TupleResultDomainValueData<Calendar, Double> data=null;
		if (href!=null && observationId!=null && domainComponentName!=null && targetComponentName!=null)
			data=new TupleResultDomainValueData<Calendar, Double>(context, href, observationId, domainComponentName, targetComponentName);
		
		
		String domainAxisId=m_lt.getMapper().getDomainAxisRef().getRef();
		String targetAxisId=m_lt.getMapper().getTargetAxisRef().getRef();
		IAxis domainAxis=m_model.getAxisRegistry().getAxis(domainAxisId);
		IAxis targetAxis=m_model.getAxisRegistry().getAxis(targetAxisId);
		
		
		if (data!=null)
			icl=new TupleResultLineLayer(data, domainAxis, targetAxis);
		
		
		return icl;
	}

}
