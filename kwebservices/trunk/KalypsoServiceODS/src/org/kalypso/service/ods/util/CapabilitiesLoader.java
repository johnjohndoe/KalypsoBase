package org.kalypso.service.ods.util;

import java.util.List;
import java.util.Set;

import net.opengis.ows.DomainType;
import net.opengis.ows.MetadataType;
import net.opengis.ows.RequestMethodType;
import net.opengis.ows.DCPDocument.DCP;
import net.opengis.ows.HTTPDocument.HTTP;
import net.opengis.ows.OperationDocument.Operation;
import net.opengis.ows.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlString;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.service.ods.extension.ODSExtensionLoader;
import org.kalypso.service.ods.extension.OperationParameter;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.chart.factory.ChartType;
import org.ksp.chart.factory.LayerType;
import org.ksp.chart.factory.RefType;
import org.ksp.service.ods.ODSCapabilitiesDocument;
import org.ksp.service.ods.ODSCapabilitiesType;
import org.ksp.service.ods.ODSMetaDataDocument;
import org.ksp.service.ods.ChartDocument.Chart;
import org.ksp.service.ods.ChartsDocument.Charts;
import org.ksp.service.ods.LayerDocument.Layer;




/**
 * @author burtscher
 *
 * Loads (marshalls) the capabilities configuration file
 * and supplements the information with infos from configuration file
 * in order to create a getCapabilities-XML-Document
 *
 */
public class CapabilitiesLoader
{
  
private ODSCapabilitiesDocument m_ocd;

private String m_serviceUrl;

private ChartConfigurationLoader m_chartLoader;

private ODSScene m_scene;

private ODSConfigurationLoader m_ocl;



public CapabilitiesLoader(String serviceUrl, String sceneId)
  {
      m_serviceUrl=serviceUrl;
      m_ocl=ODSConfigurationLoader.getInstance();
      m_scene= m_ocl.getSceneById(sceneId);
      m_chartLoader=new ChartConfigurationLoader(m_scene.getChartConfiguration());
      createCapabilitiesDocument();
  }


  
  private void createCapabilitiesDocument()
  {
	  	m_ocd=ODSCapabilitiesDocument.Factory.newInstance();
	  	ODSCapabilitiesType caps = m_ocd.addNewODSCapabilities();
	    
	  	//ServiceProvider aus Scene holen
		ServiceProvider serviceProvider = m_scene.getServiceProvider();
		caps.setServiceProvider(serviceProvider);
		
		//ServiceIdentification aus Scene holen
		ServiceIdentification serviceIdentification = m_scene.getServiceIdentification();
		caps.setServiceIdentification(serviceIdentification);
		
		//operations metadata
		OperationsMetadata metadata = caps.addNewOperationsMetadata();
		createOperationsMetadata(metadata);
		
		//chart offerings
		Charts chartCaps = caps.addNewCharts();
		createChartOfferings(chartCaps);
		
  }


  
  private void createOperationsMetadata(OperationsMetadata metadata)
  {
	  	Set<String> installedOperations = ODSExtensionLoader.getOperationsForService("ODS");
	  	for (String instOp: installedOperations) {
	  		List<OperationParameter> ops = ODSExtensionLoader.getParametersForOperation("ODS", instOp, IODSConstants.ODS_VERSION);
	  		
	  		//Operation
			Operation operation = metadata.addNewOperation();
			operation.setName(instOp);
			
			//DCP
			DCP dcp = operation.addNewDCP();
			HTTP http = dcp.addNewHTTP();
			RequestMethodType get = http.addNewGet();
			String href=m_serviceUrl+"?SERVICE="+"ODS"+"&VERSION="+IODSConstants.ODS_VERSION+"&REQUEST="+instOp;
			get.setHref(href);
			
			//Parameter
			if (ops !=null)
			for (OperationParameter op: ops)
			{
				DomainType param = operation.addNewParameter();
				param.setName(op.getName());
				MetadataType md = param.addNewMetadata();
				ODSMetaDataDocument omd = ODSMetaDataDocument.Factory.newInstance();
				omd.setODSMetaData(op.getDescription());
				md.set(omd);
				
				for (String value: op.getValues())
				{
					XmlString val = param.addNewValue();
					val.setStringValue(value);
				}
			}
		}
  }
  
  private void createChartOfferings(Charts chartCaps)
  {
		ChartConfigurationDocument dcDocument = m_chartLoader.getChartConfigurationDocument();
		ChartConfigurationType diagramConfiguration = dcDocument.getChartConfiguration();
		ChartType[] chartArray = diagramConfiguration.getChartArray();
		for (ChartType chartType : chartArray)
		{
			Chart diagram = chartCaps.addNewChart();
			diagram.setId(chartType.getId());
			diagram.setTitle(chartType.getTitle());
			diagram.setDescription(chartType.getDescription());
			org.ksp.service.ods.LayersDocument.Layers layers = diagram.addNewLayers();
			
			
			RefType[] layerRefArray = chartType.getLayers().getLayerRefArray();
			for (RefType layerRef : layerRefArray)
			{
				Layer layer = layers.addNewLayer();
				LayerType lt=(LayerType) m_chartLoader.resolveReference(layerRef.getRef());
				layer.setId(lt.getId());
				layer.setDescription(lt.getDescription());
				layer.setTitle(lt.getTitle());
			}
		}
  }
  
  
  public ODSCapabilitiesDocument getCapabilitiesDocument()
  {
	  return m_ocd;
  }
}


