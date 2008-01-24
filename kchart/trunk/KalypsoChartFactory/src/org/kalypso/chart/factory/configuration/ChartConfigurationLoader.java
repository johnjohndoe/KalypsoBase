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
import org.apache.xmlbeans.XmlOptions;
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
  private ChartConfigurationDocument m_document;

  private HashMap<String, XmlObject> m_idMap = null;

  /**
   * @param path
   *            file system path where the configuration file can be found
   * @throws XmlException
   * @throws IOException
   */
  public ChartConfigurationLoader( final URL url ) throws XmlException, IOException
  {
    m_document = ChartConfigurationDocument.Factory.parse( url );
  }

  /**
   * creates a ConfigurationLoader based on the given ChartConfigurationDocument
   * 
   * @param doc
   *            ChartConfigurationDocument
   */
  public ChartConfigurationLoader( final ChartConfigurationDocument doc )
  {
    m_document = doc;
  }

  /**
   * creates a ConfigurationLoader from an InputStream provided by IStorage
   * 
   * @param configFile
   * @throws CoreException
   * @throws IOException
   * @throws XmlException
   */
  public ChartConfigurationLoader( final IStorage configFile ) throws CoreException, XmlException, IOException
  {
    InputStream is = null;
    try
    {
      is = new BufferedInputStream( configFile.getContents() );
      m_document = ChartConfigurationDocument.Factory.parse( is );
      is.close();
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  public ChartConfigurationLoader( final ChartConfigurationType chartConfiguration )
  {
    final ChartConfigurationDocument document = ChartConfigurationDocument.Factory.newInstance();
    document.setChartConfiguration( chartConfiguration );
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

  public AxisType[] getAxes( )
  {
    return m_document.getChartConfiguration().getAxisArray();
  }

  /**
   * as xmlbeans does not support idrefs in xml schema, we have to find them by ourselves; this function searches the
   * DOM tree an id equals to the given idref and checks if the found element is of the desired type
   * 
   * @param idref
   * @param myClass
   * @return
   */
  public XmlObject resolveReference( final String idref )
  {
    final Map<String, XmlObject> idMap = getIdMap();

    return idMap.get( idref );
  }

  private synchronized Map<String, XmlObject> getIdMap( )
  {
    if( m_idMap != null && false )
    {
      return m_idMap;
    }
    else
    {
      m_idMap = new HashMap<String, XmlObject>();
      fillIdMap( m_document.getDomNode() );
      return m_idMap;
    }
  }

  private void fillIdMap( final Node node )
  {
    final NamedNodeMap atts = node.getAttributes();
    if( atts != null )
    {
      final Node idAtt = atts.getNamedItem( "id" );
      if( idAtt != null )
      {
        try
        {
          m_idMap.put( idAtt.getNodeValue(), createXmlObjectFromNode( node ) );
        }
        catch( final DOMException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( final XmlException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    if( node.hasChildNodes() )
    {
      final NodeList childNodes = node.getChildNodes();
      final int length = childNodes.getLength();
      for( int i = 0; i < length; i++ )
      {
        final Node child = childNodes.item( i );
        fillIdMap( child );
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
  private XmlObject createXmlObjectFromNode( final Node node ) throws XmlException
  {
    /**
     * hier muss man die Document-Factories zum Parsen verwenden ( z.B. ChartDocument.Factory statt ChartType.Factory),
     * weil sonst das erzeugt XmlObject nur null-Objekte enthält
     */

    XmlObject obj = null;
    if( node.getNodeName().equals( "Chart" ) )
    {
      final ChartDocument doc = ChartDocument.Factory.parse( node );
      obj = doc.getChart();
    }
    else if( node.getNodeName().equals( "Layer" ) )
    {
      final LayerDocument doc = LayerDocument.Factory.parse( node );
      obj = doc.getLayer();
    }
    else if( node.getNodeName().equals( "Axis" ) )
    {
      final AxisDocument doc = AxisDocument.Factory.parse( node );
      obj = doc.getAxis();
    }
    else if( node.getNodeName().equals( "Mapper" ) )
    {
      final MapperDocument doc = MapperDocument.Factory.parse( node );
      obj = doc.getMapper();
    }
    else if( node.getNodeName().equals( "Style" ) )
    {
      final StyleDocument doc = StyleDocument.Factory.parse( node );
      obj = doc.getStyle();
    }
    else if( node.getNodeName().equals( "AxisRenderer" ) )
    {
      final AxisRendererDocument doc = AxisRendererDocument.Factory.parse( node );
      obj = doc.getAxisRenderer();
    }
    else
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Cannot identify configuration node named: " + node.getNodeName() );
    return obj;
  }

  public static XmlOptions configureXmlOptions( final String charset )
  {
    final XmlOptions options = new XmlOptions();
    final Map<String, String> prefixes = new HashMap<String, String>();
    prefixes.put( "http://www.ksp.org/chart/factory", "" );
    options.setSaveSuggestedPrefixes( prefixes );
    options.setSavePrettyPrint();
    options.setCharacterEncoding( charset );

    return options;
  }

}
