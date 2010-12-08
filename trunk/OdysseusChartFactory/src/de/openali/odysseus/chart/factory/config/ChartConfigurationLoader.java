package de.openali.odysseus.chart.factory.config;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.util.IOUtils;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.AreaStyleDocument;
import de.openali.odysseus.chartconfig.x020.AreaStyleType;
import de.openali.odysseus.chartconfig.x020.AxisDocument;
import de.openali.odysseus.chartconfig.x020.AxisRendererDocument;
import de.openali.odysseus.chartconfig.x020.AxisRendererType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x020.ChartDocument;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerDocument;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LineStyleDocument;
import de.openali.odysseus.chartconfig.x020.LineStyleType;
import de.openali.odysseus.chartconfig.x020.MapperDocument;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.PointStyleDocument;
import de.openali.odysseus.chartconfig.x020.PointStyleType;
import de.openali.odysseus.chartconfig.x020.RoleReferencingType;
import de.openali.odysseus.chartconfig.x020.ScreenAxisDocument;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;
import de.openali.odysseus.chartconfig.x020.TextStyleDocument;
import de.openali.odysseus.chartconfig.x020.TextStyleType;

/**
 * @author alibu
 */
public class ChartConfigurationLoader implements IReferenceResolver
{
  private ChartConfigurationDocument m_document;

  private HashMap<String, XmlObject> m_idMap = null;

  public ChartConfigurationLoader( final URL url ) throws XmlException, IOException
  {
    m_document = ChartConfigurationDocument.Factory.parse( url );
  }

  public ChartConfigurationLoader( final InputStream is ) throws XmlException, IOException
  {
    m_document = ChartConfigurationDocument.Factory.parse( is );
  }

  /**
   * creates a ConfigurationLoader based on the given ChartConfigurationDocument
   * 
   * @param doc
   *          ChartConfigurationDocument
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
    }
    catch( final ExceptionInInitializerError e )
    {
      e.printStackTrace();
      throw new IOException( e.getLocalizedMessage(), e );
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

  public ChartConfigurationLoader( final ChartType chartType )
  {
    final ChartConfigurationDocument document = ChartConfigurationDocument.Factory.newInstance();
    final ChartConfigurationType cct = document.addNewChartConfiguration();
    cct.setChartArray( new ChartType[] { chartType } );
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

  public LayerType[] getLayers( final ChartType chart )
  {
    return chart.getLayers().getLayerArray();
  }

  public AxisType getDomainAxis( final LayerType layer )
  {
    return (AxisType) resolveReference( layer.getMapperRefs().getDomainAxisRef().getRef() );
  }

  public AxisType getTargetAxis( final LayerType layer )
  {
    return (AxisType) resolveReference( layer.getMapperRefs().getTargetAxisRef().getRef() );
  }

  public Map<String, AbstractStyleType> getStyles( final LayerType layer )
  {
    final Styles styles = layer.getStyles();
    final AreaStyleType[] asa = styles.getAreaStyleArray();
    final PointStyleType[] psa = styles.getPointStyleArray();
    final LineStyleType[] lsa = styles.getLineStyleArray();
    final TextStyleType[] tsa = styles.getTextStyleArray();

    final Map<String, AbstractStyleType> styleMap = new TreeMap<String, AbstractStyleType>();
    for( final AreaStyleType element : asa )
      styleMap.put( element.getRole(), element );
    for( final TextStyleType element : tsa )
      styleMap.put( element.getRole(), element );
    for( final LineStyleType element : lsa )
      styleMap.put( element.getRole(), element );
    for( final PointStyleType element : psa )
      styleMap.put( element.getRole(), element );

    return styleMap;
  }

  public Map<String, MapperType> getMappers( final LayerType layer )
  {
    final TreeMap<String, MapperType> map = new TreeMap<String, MapperType>();
    final RoleReferencingType[] mra = layer.getMapperRefs().getMapperRefArray();
    for( final RoleReferencingType refType : mra )
      map.put( refType.getRole(), (MapperType) resolveReference( refType.getRef() ) );
    return map;
  }

  public AxisRendererType getAxisRenderer( final AxisType axis )
  {
    return (AxisRendererType) resolveReference( axis.getRendererRef().getRef() );
  }

  public ChartType getChartById( final String id )
  {
    final ChartType[] charts = getCharts();
    for( final ChartType chart : charts )
      if( chart.getId().equals( id ) )
        return chart;
    return null;
  }

  public String[] getChartIds( )
  {
    final ChartType[] charts = getCharts();
    final String[] chartIds = new String[charts.length];
    for( int i = 0; i < chartIds.length; i++ )
      chartIds[i] = charts[i].getId();
    return chartIds;
  }

  /**
   * as xmlbeans does not support idrefs in xml schema, we have to find them by ourselves; this function searches the
   * DOM tree for an id that equals the given idref and checks if the found element is of the desired type
   * 
   * @param idref
   * @param myClass
   * @return
   */
  @Override
  public XmlObject resolveReference( final String idref )
  {
    final Map<String, XmlObject> idMap = getIdMap();

    return idMap.get( idref );
  }

  private synchronized Map<String, XmlObject> getIdMap( )
  {
    m_idMap = new HashMap<String, XmlObject>();
    fillIdMap( m_document.getDomNode() );
    return m_idMap;
  }

  private void fillIdMap( final Node node )
  {
    final NamedNodeMap atts = node.getAttributes();
    if( atts != null )
    {
      final Node idAtt = atts.getNamedItem( "id" );
      if( idAtt != null )
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
    final String nodeName = node.getLocalName();

    if( "Chart".equals( nodeName ) ) //$NON-NLS-1$
    {
      final ChartDocument doc = ChartDocument.Factory.parse( node );

      return doc.getChart();
    }
    else if( "Layer".equals( nodeName ) ) //$NON-NLS-1$
    {
      final LayerDocument doc = LayerDocument.Factory.parse( node );

      return doc.getLayer();
    }
    else if( "Axis".equals( nodeName ) ) //$NON-NLS-1$
    {
      final AxisDocument doc = AxisDocument.Factory.parse( node );

      return doc.getAxis();
    }
    else if( "ScreenAxis".equals( nodeName ) ) //$NON-NLS-1$
    {
      final ScreenAxisDocument doc = ScreenAxisDocument.Factory.parse( node );

      return doc.getScreenAxis();
    }
    else if( "Mapper".equals( nodeName ) ) //$NON-NLS-1$
    {
      final MapperDocument doc = MapperDocument.Factory.parse( node );

      return doc.getMapper();
    }
    else if( "LineStyle".equals( nodeName ) ) //$NON-NLS-1$
    {
      final LineStyleDocument doc = LineStyleDocument.Factory.parse( node );

      return doc.getLineStyle();
    }
    else if( "PointStyle".equals( nodeName ) ) //$NON-NLS-1$
    {
      final PointStyleDocument doc = PointStyleDocument.Factory.parse( node );

      return doc.getPointStyle();
    }
    else if( "AreaStyle".equals( nodeName ) ) //$NON-NLS-1$
    {
      final AreaStyleDocument doc = AreaStyleDocument.Factory.parse( node );

      return doc.getAreaStyle();
    }
    else if( "TextStyle".equals( nodeName ) ) //$NON-NLS-1$
    {
      final TextStyleDocument doc = TextStyleDocument.Factory.parse( node );

      return doc.getTextStyle();
    }
    else if( "AxisRenderer".equals( nodeName ) ) //$NON-NLS-1$
    {
      final AxisRendererDocument doc = AxisRendererDocument.Factory.parse( node );

      return doc.getAxisRenderer();
    }
    else
      Logger.logError( Logger.TOPIC_LOG_CONFIG, "Cannot identify configuration node named: " + nodeName );

    return null;
  }

  public static XmlOptions configureXmlOptions( final String charset )
  {
    final XmlOptions options = new XmlOptions();
    final Map<String, String> prefixes = new HashMap<String, String>();
    prefixes.put( "http://www.openali.de/odysseus/chartconfig/0.2.0/", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    options.setSaveSuggestedPrefixes( prefixes );
    options.setSavePrettyPrint();
    options.setCharacterEncoding( charset );

    return options;
  }

  public String getDocumentSource( )
  {
    return m_document.documentProperties().getSourceName();
  }

}
