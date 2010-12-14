package de.openali.odysseus.service.ods.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.opengis.ows.DCPDocument.DCP;
import net.opengis.ows.DomainType;
import net.opengis.ows.HTTPDocument.HTTP;
import net.opengis.ows.MetadataType;
import net.opengis.ows.OperationDocument.Operation;
import net.opengis.ows.OperationsMetadataDocument.OperationsMetadata;
import net.opengis.ows.RequestMethodType;
import net.opengis.ows.ServiceIdentificationDocument.ServiceIdentification;
import net.opengis.ows.ServiceProviderDocument.ServiceProvider;

import org.apache.xmlbeans.XmlString;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.IOrdinalDataOperator;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.service.ods.environment.IODSChart;
import de.openali.odysseus.service.ods.environment.IODSEnvironment;
import de.openali.odysseus.service.ods.x020.AxesOfferingType;
import de.openali.odysseus.service.ods.x020.AxisDirectionType;
import de.openali.odysseus.service.ods.x020.AxisOfferingType;
import de.openali.odysseus.service.ods.x020.AxisPositionType;
import de.openali.odysseus.service.ods.x020.ChartOfferingType;
import de.openali.odysseus.service.ods.x020.ChartsOfferingType;
import de.openali.odysseus.service.ods.x020.DateRangeDocument.DateRange;
import de.openali.odysseus.service.ods.x020.LayerOfferingType;
import de.openali.odysseus.service.ods.x020.LayerOfferingType.DomainAxis;
import de.openali.odysseus.service.ods.x020.LayerOfferingType.TargetAxis;
import de.openali.odysseus.service.ods.x020.LayersOfferingType;
import de.openali.odysseus.service.ods.x020.NumberRangeDocument.NumberRange;
import de.openali.odysseus.service.ods.x020.ODSCapabilitiesDocument;
import de.openali.odysseus.service.ods.x020.ODSCapabilitiesType;
import de.openali.odysseus.service.ods.x020.ODSMetaDataDocument;
import de.openali.odysseus.service.ods.x020.StringRangeDocument.StringRange;
import de.openali.odysseus.service.ods.x020.StringRangeDocument.StringRange.ValueSet;
import de.openali.odysseus.service.ods.x020.SymbolOfferingType;
import de.openali.odysseus.service.ods.x020.SymbolsOfferingType;
import de.openali.odysseus.service.ows.extension.OWSOperationExtensionLoader;
import de.openali.odysseus.service.ows.metadata.OperationParameter;

/**
 * Loads (marshalls) the capabilities configuration file and supplements the information with infos from configuration
 * file in order to create a Service Metadata Document
 * 
 * @author burtscher
 */
public class CapabilitiesLoader
{

  private Map<String, ChartOfferingType[]> m_sceneOfferings;

  private ODSCapabilitiesDocument m_ocd;

  private final IODSEnvironment m_env;

  public CapabilitiesLoader( final IODSEnvironment env )
  {
    m_env = env;
    createCapabilitiesDocument();
  }

  private void createCapabilitiesDocument( )
  {

    m_ocd = ODSCapabilitiesDocument.Factory.newInstance();

    final ODSCapabilitiesType caps = m_ocd.addNewODSCapabilities();

    // ServiceProvider aus Scene holen
    final ServiceProvider serviceProvider = m_env.getConfigLoader().getServiceProvider();
    caps.setServiceProvider( serviceProvider );

    // ServiceIdentification aus Scene holen
    final ServiceIdentification serviceIdentification = m_env.getConfigLoader().getServiceIdentification();
    caps.setServiceIdentification( serviceIdentification );

    // operations metadata
    final OperationsMetadata metadata = caps.addNewOperationsMetadata();
    createOperationsMetadata( metadata );

    createOfferings();
  }

  private void createOperationsMetadata( final OperationsMetadata metadata )
  {
    final Map<String, IConfigurationElement> installedOperations = OWSOperationExtensionLoader.getOperations();
    for( final Entry<String, IConfigurationElement> op : installedOperations.entrySet() )
    {

      final List<OperationParameter> params = OWSOperationExtensionLoader.getParametersForOperation( op.getKey() );

      // Operation
      final Operation operation = metadata.addNewOperation();
      operation.setName( op.getKey() );

      // DCP
      final DCP dcp = operation.addNewDCP();
      final HTTP http = dcp.addNewHTTP();
      final RequestMethodType get = http.addNewGet();
      final String href = m_env.getServiceUrl() + "?SERVICE=" + IODSConstants.ODS_SERVICE_SHORT + "&VERSION=" + IODSConstants.ODS_VERSION + "&REQUEST=" + op.getKey();
      get.setHref( href );

      // Parameter
      if( params != null )
        for( final OperationParameter param : params )
        {

          final DomainType p = operation.addNewParameter();

          p.setName( param.getName() );
          final MetadataType md = p.addNewMetadata();
          final ODSMetaDataDocument omd = ODSMetaDataDocument.Factory.newInstance();
          omd.setODSMetaData( param.getDescription() );
          md.set( omd );

          for( final String value : param.getValues() )
          {
            final XmlString val = p.addNewValue();
            val.setStringValue( value );
          }
        }
    }
  }

  public ODSCapabilitiesDocument getCapabilitiesDocument( final String sceneId )
  {
    final ODSCapabilitiesDocument copy = (ODSCapabilitiesDocument) m_ocd.copy();
    final ODSCapabilitiesType capabilities = copy.getODSCapabilities();
    final ChartsOfferingType charts = capabilities.addNewCharts();
    charts.setChartArray( m_sceneOfferings.get( sceneId ) );
    return copy;
  }

  private void createOfferings( )
  {
    final String[] sceneIds = m_env.getConfigLoader().getSceneIds();
    m_sceneOfferings = new HashMap<String, ChartOfferingType[]>();
    for( final String sceneId : sceneIds )
      m_sceneOfferings.put( sceneId, createSceneOfferings( sceneId ) );

  }

  public ChartOfferingType[] createSceneOfferings( final String sceneId )
  {
    final List<ChartOfferingType> chartList = new ArrayList<ChartOfferingType>();

    final List<IODSChart> list = m_env.getScenes().get( sceneId );

    if( list != null )
      for( final IODSChart chart : list )
      {
        final ChartConfigurationLoader ccl = chart.getDefinitionType();
        final String[] chartIds = ccl.getChartIds();
        for( final String string : chartIds )
        {
          final IChartModel model = new ChartModel();
          try
          {
            URL url = null;
            try
            {
              url = m_env.getConfigDir().toURI().toURL();
            }
            catch( final MalformedURLException e1 )
            {
              // TODO Auto-generated catch block
              // should not happen -> else, the ODSEnvironment would not be valid
              e1.printStackTrace();
            }

            ChartFactory.configureChartModel( model, ccl, string, ChartExtensionLoader.getInstance(), url );
          }
          catch( final ConfigurationException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          final ChartOfferingType cd = createChartOffering( model, sceneId );
          chartList.add( cd );
        }
      }
    return chartList.toArray( new ChartOfferingType[] {} );
  }

  public ChartOfferingType createChartOffering( final IChartModel model, final String scene )
  {

    final ChartOfferingType xmlChart = ChartOfferingType.Factory.newInstance();

    // Chart-Info
    xmlChart.setId( model.getId() );
    final TitleTypeBean[] titleBeans = model.getTitles();
    final String[] titleArray = new String[titleBeans.length];
    for( int i = 0; i < titleBeans.length; i++ )
    {
      titleArray[i] = titleBeans[i].getText();
    }
    xmlChart.setTitleArray( titleArray );
    xmlChart.setDescription( model.getDescription() );

    // Layer
    final LayersOfferingType xmlLayers = xmlChart.addNewLayers();
    for( final IChartLayer layer : model.getLayerManager().getLayers() )
    {
      final LayerOfferingType xmlLayer = xmlLayers.addNewLayer();
      xmlLayer.setId( layer.getId() );
      xmlLayer.setTitle( layer.getTitle() );
      xmlLayer.setDescription( layer.getDescription() );
      final DomainAxis domAxis = xmlLayer.addNewDomainAxis();
      domAxis.setRef( layer.getCoordinateMapper().getDomainAxis().getId() );
      final TargetAxis targetAxis = xmlLayer.addNewTargetAxis();
      targetAxis.setRef( layer.getCoordinateMapper().getTargetAxis().getId() );
      // Symbols
      final SymbolsOfferingType xmlSymbols = xmlLayer.addNewSymbols();
      final ILegendEntry[] les = layer.getLegendEntries();
      for( int i = 0; i < les.length; i++ )
      {
        final ILegendEntry le = les[i];
        final Point iconSize = new Point( 20, 20 );
        final Point size = le.computeSize( iconSize );
        final SymbolOfferingType xmlSymbol = xmlSymbols.addNewSymbol();
        String desc = le.getDescription();
        if( desc == null )
          desc = "";
        xmlSymbol.setTitle( desc );
        final String symbolId = "symbol" + i;
        xmlSymbol.setId( symbolId );
        xmlSymbol.setWidth( size.x );
        xmlSymbol.setHeight( size.y );

        // Symbol-Grafiken cachen
        final ImageData symbol = le.getSymbol( size );
        ODSUtils.writeSymbol( m_env.getTmpDir(), symbol, scene, model.getId(), layer.getId(), symbolId );
      }
    }

    // Achsen
    final AxesOfferingType xmlAxes = xmlChart.addNewAxes();
    for( final IAxis axis : model.getMapperRegistry().getAxes() )
    {
      final AxisOfferingType xmlAxis = xmlAxes.addNewAxis();
      xmlAxis.setId( axis.getId() );
      xmlAxis.setTitle( axis.getLabel() );
      // Achsen haben keine Beschreibung!!!
      xmlAxis.setDescription( "" );

      // Direction
      de.openali.odysseus.service.ods.x020.AxisDirectionType.Enum dir = AxisDirectionType.POSITIVE;
      if( axis.getDirection().equals( DIRECTION.NEGATIVE ) )
        dir = AxisDirectionType.NEGATIVE;
      xmlAxis.setDirection( dir );

      // Position
      de.openali.odysseus.service.ods.x020.AxisPositionType.Enum xmlPos = AxisPositionType.BOTTOM;
      final POSITION pos = axis.getPosition();
      if( pos.equals( POSITION.LEFT ) )
        xmlPos = AxisPositionType.LEFT;
      else if( pos.equals( POSITION.TOP ) )
        xmlPos = AxisPositionType.TOP;
      else if( pos.equals( POSITION.RIGHT ) )
        xmlPos = AxisPositionType.RIGHT;
      xmlAxis.setPosition( xmlPos );

      final IDataRange<Number> numericRange = axis.getNumericRange();
      final Class< ? > clazz = axis.getDataClass();

      if( Number.class.isAssignableFrom( clazz ) )
      {
        final NumberRange range = xmlAxis.addNewNumberRange();
        range.setMinValue( numericRange.getMin().doubleValue() );
        range.setMaxValue( numericRange.getMax().doubleValue() );
      }
      else if( Calendar.class.isAssignableFrom( clazz ) )
      {
        final IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
        final DateRange range = xmlAxis.addNewDateRange();
        range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
        range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
      }
      else if( String.class.isAssignableFrom( clazz ) )
      {
        final IDataOperator<String> dop = axis.getDataOperator( String.class );
        final StringRange range = xmlAxis.addNewStringRange();
        range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
        range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
        final IOrdinalDataOperator<String> odop = (IOrdinalDataOperator<String>) dop;
        // alle einzelnen Werte angeben
        final ValueSet valueSet = range.addNewValueSet();
        valueSet.setValueArray( odop.getValues() );
      }

    }
    return xmlChart;
  }
}
