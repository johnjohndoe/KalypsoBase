package org.kalypso.service.ods.operation;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.chart.factory.ChartExtensionLoader;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.exception.AxisProviderException;
import org.kalypso.chart.factory.configuration.exception.ConfigChartNotFoundException;
import org.kalypso.chart.factory.provider.IAxisProvider;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.registry.impl.MapperRegistry;
import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.ODSChartManipulation;
import org.kalypso.service.ods.util.ODSConfigurationLoader;
import org.kalypso.service.ods.util.ODSScene;
import org.kalypso.service.ods.util.XMLOutput;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.ogc.exception.OWSException.ExceptionCode;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.chart.factory.ChartType;
import org.ksp.chart.factory.LayerType;
import org.ksp.chart.factory.RefType;
import org.ksp.service.ods.AxesInfoDocument;
import org.ksp.service.ods.AxesInfoType;
import org.ksp.service.ods.AxisDirectionType;
import org.ksp.service.ods.AxisPositionType;

public class GetAxesInfo implements IODSOperation {

	private ChartConfigurationLoader m_cl;
	private ResponseBean m_responseBean;
	private RequestBean m_requestBean;
	private Map<String, AxisType> m_axes4Chart;
	private MapperRegistry m_mapperRegistry;

	public void operate(RequestBean requestBean, ResponseBean responseBean)
			throws OWSException {
		
		m_requestBean=requestBean;
		m_responseBean=responseBean;
		
		String reqName=m_requestBean.getParameterValue( "NAME" );
		if (reqName!=null)
		{
			String sceneId=m_requestBean.getParameterValue( "SCENE" );
			ODSConfigurationLoader ocl=ODSConfigurationLoader.getInstance();
			ODSScene scene = ocl.getSceneById(sceneId);
			m_cl=new ChartConfigurationLoader(scene.getChartConfiguration());
			ChartConfigurationDocument dcDocument = m_cl.getChartConfigurationDocument();
			ChartConfigurationType chartConfiguration = dcDocument.getChartConfiguration();
			ChartType[] chartArray = chartConfiguration.getChartArray();
			for (ChartType chart  : chartArray) {
				if (chart.getId().equals(reqName.trim()))
				{
					createAxesInfo(chart);
					break;
				}
			}
		}
		else
		{
			//TODO: Fehlermeldung: kein Chart gefunden
			throw new OWSException(ExceptionCode.INVALID_PARAMETER_VALUE, "There's no chart by the name '"+reqName+"'", requestBean.getUrl() );
		}

	}
	
	private void createAxesInfo(ChartType chart)
	{
		AxesInfoDocument aid=AxesInfoDocument.Factory.newInstance();
		AxesInfoType ait = aid.addNewAxesInfo();
		
		m_axes4Chart=new HashMap<String, AxisType>();
		/*
		 * die Achsen können von mehreren Layern referenziert werden; daher HashMap
		 * aufbauen und immer überprüfen, ob die Achse schon da ist
		 */ 
		RefType[] layerRefArray = chart.getLayers().getLayerRefArray();
		for (RefType layerRef : layerRefArray) {
			String ref = layerRef.getRef();
			LayerType layer = (LayerType) m_cl.resolveReference(ref);
			String domainAxisRef = layer.getMapper().getDomainAxisRef().getRef();
			if (!m_axes4Chart.containsKey(domainAxisRef))
			{
				AxisType domainAxisConfig=(AxisType) m_cl.resolveReference(domainAxisRef);
				m_axes4Chart.put(domainAxisRef, domainAxisConfig);
			}
			String targetAxisRef = layer.getMapper().getTargetAxisRef().getRef();
			if (!m_axes4Chart.containsKey(targetAxisRef))
			{
				AxisType targetAxisConfig=(AxisType) m_cl.resolveReference(targetAxisRef);
				m_axes4Chart.put(targetAxisRef, targetAxisConfig);
			}
			
		}
		/**
		 * MapperRegistry erzeugen
		 */
		m_mapperRegistry=new MapperRegistry();
		
		/*
		 * jetzt die individuellen Achsen abbilden
		 */
		for (String axisId: m_axes4Chart.keySet())
		{
			toAxisInfo(ait, m_axes4Chart.get(axisId));
		}
		
		XMLOutput.xmlResponse(m_responseBean, aid);
	}
	
	private <T> void toAxisInfo(AxesInfoType ait,  org.ksp.chart.factory.AxisType atConf)
	{
		
		org.ksp.service.ods.AxisType atInfo = ait.addNewAxis();
		//id
		atInfo.setId(atConf.getId());
		//title
		atInfo.setTitle(atConf.getLabel());
		
		//Type
		Class<?> type=null;
		IAxis<T> axis=null;
		try {
			String epId=atConf.getProvider().getEpid();
			IAxisProvider provider = ChartExtensionLoader.createAxisProvider(epId);
			provider.init(atConf);
			axis = provider.getAxis();
			m_mapperRegistry.addMapper(axis);
			type=provider.getDataClass();
			
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AxisProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (type!=null)
			atInfo.setType(type.getCanonicalName());

		
		//request parameters can overwrite configuration parameters
		ODSChartManipulation.setAxesRange(m_mapperRegistry, m_requestBean);
		
		
		//Position
		org.ksp.chart.factory.AxisType.Position.Enum position = atConf.getPosition();
		if (position == (org.ksp.chart.factory.AxisType.Position.BOTTOM))
			atInfo.setPosition(AxisPositionType.BOTTOM);
		else if (position == (org.ksp.chart.factory.AxisType.Position.LEFT))
			atInfo.setPosition(AxisPositionType.LEFT);
		else if (position == (org.ksp.chart.factory.AxisType.Position.RIGHT))
			atInfo.setPosition(AxisPositionType.RIGHT);
		else if (position == (org.ksp.chart.factory.AxisType.Position.TOP))
			atInfo.setPosition(AxisPositionType.TOP);
		
		//Direction
		org.ksp.chart.factory.AxisType.Direction.Enum direction = atConf.getDirection();
		if (direction == org.ksp.chart.factory.AxisType.Direction.NEGATIVE)
			atInfo.setDirection(AxisDirectionType.NEGATIVE);
		else if (direction == org.ksp.chart.factory.AxisType.Direction.POSITIVE)
			atInfo.setDirection(AxisDirectionType.POSITIVE);
		
		String minVal="";
		String maxVal="";
		
		if (axis != null)
		{
			IDataOperator<T> dataOperator = axis.getDataOperator();
			IDataRange<T> dataRange = axis.getDataRange();
			//this has to be done in order to evaluate dynamic values for the axis range
			T dataMin=dataRange.getMin();
			T dataMax=dataRange.getMax();
			minVal=dataOperator.logicalToString( dataMin );
			maxVal=dataOperator.logicalToString( dataMax );
		}
		//Should never be the case - otherwise the configuration file has errors
		else
		{
			minVal=atConf.getMinVal();
			maxVal=atConf.getMaxVal();
		}
		
		//minval
		atInfo.setMinVal(minVal);
		
		//maxval
		atInfo.setMaxVal(maxVal);
	}

}
