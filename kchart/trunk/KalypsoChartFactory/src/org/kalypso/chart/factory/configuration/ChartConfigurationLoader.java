package org.kalypso.chart.factory.configuration;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.chart.factory.util.IReferenceResolver;
import org.kalypso.chart.framework.logging.Logger;
import org.ksp.chart.factory.AxisDocument;
import org.ksp.chart.factory.AxisRendererDocument;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartConfigurationType;
import org.ksp.chart.factory.ChartDocument;
import org.ksp.chart.factory.ChartType;
import org.ksp.chart.factory.LayerDocument;
import org.ksp.chart.factory.MapperDocument;
import org.ksp.chart.factory.StyleDocument;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author alibu
 */
public class ChartConfigurationLoader implements IReferenceResolver
{
 // private String m_configPath;
  private ChartConfigurationDocument m_document;
  private HashMap<String, XmlObject> m_idMap=null;
  

  /**
   * @param path file system path where the configuration file can be found
 * @throws XmlException 
   */
  public ChartConfigurationLoader( String path ) throws XmlException 
  {
	  m_document = ChartConfigurationDocument.Factory.parse(path);
  }

  /**
   * @param path file system path where the configuration file can be found
   * @throws XmlException 
 * @throws IOException 
   */
  public ChartConfigurationLoader( URL url ) throws XmlException, IOException 
  {
	  InputStream openStream =null;
	  try{
		  openStream = url.openStream();
		  m_document = ChartConfigurationDocument.Factory.parse(openStream);
	  }
	  finally
	  {
		  if (openStream!=null)
			  openStream.close();
	  }
  }

  /**
   * creates a ConfigurationLoader based on the given ChartConfigurationDocument
   * @param doc ChartConfigurationDocument
   */
	public ChartConfigurationLoader(ChartConfigurationDocument doc)
	{
		m_document = doc;
	}

	/**
	 * creates a ConfigurationLoader from an InputStream provided by IStorage 
	 * @param configFile
	 * @throws CoreException
	 * @throws IOException
	 * @throws XmlException
	 */
	public ChartConfigurationLoader( final IStorage configFile ) throws CoreException, IOException, XmlException
	{
	  InputStream is = null;
	  try
	  {
	    is = new BufferedInputStream( configFile.getContents() );
	    m_document = ChartConfigurationDocument.Factory.parse(is);
	    is.close();
	  }
	  finally
	  {
	    IOUtils.closeQuietly( is );
	  }
	}


	public ChartConfigurationLoader(ChartConfigurationType chartConfiguration)
	{
		ChartConfigurationDocument document = ChartConfigurationDocument.Factory.newInstance();
		document.setChartConfiguration(chartConfiguration);
		m_document = document;
	}

  /**
   * @return marshalled configuration
   */
  public ChartConfigurationDocument getChartConfigurationDocument( )
  {
    return m_document;
  }

  public ChartType[] getCharts( )
  {
	  return m_document.getChartConfiguration().getChartArray();
  }
  
  public AxisType[] getAxes()
  {
	  return m_document.getChartConfiguration().getAxisArray();
  }


  
  /**
   * as xmlbeans does not support idrefs in xml schema, we have to find them by ourselves;
   * this function searches the DOM tree an id equals to the given idref and checks if
   * the found element is of the desired type 
   * 
   * @param idref
   * @param myClass
   * @return
   */
  public XmlObject resolveReference(String idref)
  {
	  Map<String, XmlObject> idMap = getIdMap();
	  
	  return  idMap.get(idref);
  }
  
  private synchronized Map<String, XmlObject> getIdMap()
  {
	  if (m_idMap != null && false)
	  {
		  return m_idMap;
	  }
	  else
	  {
		  m_idMap=new HashMap<String, XmlObject>();
		  fillIdMap(m_document.getDomNode());
		  return m_idMap;
	  }
  }
  
  private void fillIdMap(Node node)
  {
	  NamedNodeMap atts = node.getAttributes();
	  if (atts!=null)
	  {
		  Node idAtt = atts.getNamedItem("id");
		  if (idAtt !=null)
		  {
			  try {
				m_idMap.put(idAtt.getNodeValue(), createXmlObjectFromNode(node));
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
	  }
	  if (node.hasChildNodes())
	  {
		  NodeList childNodes = node.getChildNodes();
		  int length=childNodes.getLength();
		  for (int i=0;i< length ; i++)
		  {
			  Node child=childNodes.item(i);
			  fillIdMap(child);
		  }
	  }
  }
  
  /**
   * creates an XmlObject from a DOM node
   * 
   * @param node
   * @return
   * @throws XmlException
   */
  private XmlObject createXmlObjectFromNode(Node node) throws XmlException
  {
	  /**
	   * hier muss man die Document-Factories zum Parsen verwenden ( z.B. ChartDocument.Factory statt ChartType.Factory),
	   * weil sonst das erzeugt XmlObject nur null-Objekte enthält 
	   */
	  
	  XmlObject obj=null;
	  if (node.getNodeName().equals("Chart"))
	  {
		   ChartDocument doc = ChartDocument.Factory.parse(node);
		   obj=doc.getChart();
	  }
	  else if (node.getNodeName().equals("Layer"))
	  {
		   LayerDocument doc = LayerDocument.Factory.parse(node);
		   obj=doc.getLayer();
	  }
	  else if (node.getNodeName().equals("Axis"))
	  {
		   AxisDocument doc = AxisDocument.Factory.parse(node);
		   obj=doc.getAxis();
	  }
	  else if (node.getNodeName().equals("Mapper"))
	  {
		   MapperDocument doc = MapperDocument.Factory.parse(node);
		   obj=doc.getMapper();
	  }
	  else if (node.getNodeName().equals("Style"))
	  {
		   StyleDocument doc = StyleDocument.Factory.parse(node);
		   obj=doc.getStyle();
	  }
	  else if (node.getNodeName().equals("AxisRenderer"))
	  {
		   AxisRendererDocument doc = AxisRendererDocument.Factory.parse(node);
		   obj=doc.getAxisRenderer();
	  }
	  else
		  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Cannot identify configuration node named: "+node.getNodeName());
	  return obj;
  }


}
