package org.kalypso.chart.factory.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.kalypso.chart.factory.util.IReferenceResolver;
import org.kalypso.chart.framework.logging.Logger;
import org.ksp.chart.factory.AxisDocument;
import org.ksp.chart.factory.AxisRendererDocument;
import org.ksp.chart.factory.ChartConfigurationDocument;
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
public class ConfigLoader implements IReferenceResolver
{
  private String m_configPath;
  private ChartConfigurationDocument m_document;
  private HashMap<String, XmlObject> m_idMap=null;
  

  /**
   * @param path
   *          file system path where the configuration file can be found
   */
  public ConfigLoader( String path ) 
  {
    m_configPath = path;
    loadFile();
  }

  public ConfigLoader(Node node)
{
	  try
	{
		m_document = ChartConfigurationDocument.Factory.parse(node);
	} catch (XmlException e)
	{
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
}

/**
   * reads and loads the configuration file
   */
  private void loadFile( ) 
  {
    File file = new File( m_configPath );
    if( file.exists() )
    {
      try {
		m_document = ChartConfigurationDocument.Factory.parse(file);
	} catch (XmlException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }
    else
    {
      Logger.trace( "ERROR: configuration file could not be resolved. \n" + m_configPath );
    }
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
	  if (node.getNodeName().equals("chart"))
	  {
		   ChartDocument doc = ChartDocument.Factory.parse(node);
		   obj=doc.getChart();
	  }
	  else if (node.getNodeName().equals("layer"))
	  {
		   LayerDocument doc = LayerDocument.Factory.parse(node);
		   obj=doc.getLayer();
	  }
	  else if (node.getNodeName().equals("axis"))
	  {
		   AxisDocument doc = AxisDocument.Factory.parse(node);
		   obj=doc.getAxis();
	  }
	  else if (node.getNodeName().equals("mapper"))
	  {
		   MapperDocument doc = MapperDocument.Factory.parse(node);
		   obj=doc.getMapper();
	  }
	  else if (node.getNodeName().equals("style"))
	  {
		   StyleDocument doc = StyleDocument.Factory.parse(node);
		   obj=doc.getStyle();
	  }
	  else if (node.getNodeName().equals("axisRenderer"))
	  {
		   AxisRendererDocument doc = AxisRendererDocument.Factory.parse(node);
		   obj=doc.getAxisRenderer();
	  }
	  else
		  Logger.logError(Logger.TOPIC_LOG_CONFIG, "Cannot identify configuration node named: "+node.getNodeName());
	  return obj;
  }


}
