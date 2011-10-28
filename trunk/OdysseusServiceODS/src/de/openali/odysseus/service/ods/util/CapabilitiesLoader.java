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

import org.apache.xmlbeans.XmlString;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.IOrdinalDataOperator;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
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

/**
 * Loads (marshalls) the capabilities configuration file and supplements the information with infos from the
 * configuration file in order to create a service metadata document.
 * 
 * @author Alexander Burtscher, Holger Albert
 */
public class CapabilitiesLoader
{
  /**
   * The scene offerings.
   */
  private Map<String, ChartOfferingType[]> m_sceneOfferings;

  /**
   * The ODS capabilities document.
   */
  private ODSCapabilitiesDocument m_odsCapabilities;

  /**
   * The ODS enverionment.
   */
  private final IODSEnvironment m_odsEnvironment;

  /**
   * The constructor.
   * 
   * @param odsEnvironment
   *          The ODS environment.
   */
  public CapabilitiesLoader( IODSEnvironment odsEnvironment ) throws OWSException
  {
    m_odsEnvironment = odsEnvironment;
    createCapabilitiesDocument();
  }

  /**
   * This function creates the capabilities document.
   */
  private void createCapabilitiesDocument( ) throws OWSException
  {
    m_odsCapabilities = ODSCapabilitiesDocument.Factory.newInstance();

    ODSCapabilitiesType caps = m_odsCapabilities.addNewODSCapabilities();
    caps.setServiceProvider( m_odsEnvironment.getConfigLoader().getServiceProvider() );
    caps.setServiceIdentification( m_odsEnvironment.getConfigLoader().getServiceIdentification() );

    createOperationsMetadata( caps.addNewOperationsMetadata() );
    createOfferings();
  }

  /**
   * This function creates the operations metadata.
   * 
   * @param metadata
   *          The operation metadata.
   */
  private void createOperationsMetadata( OperationsMetadata metadata )
  {
    final Map<String, IConfigurationElement> installedOperations = OWSOperationExtensionLoader.getOperations();
    for( final Entry<String, IConfigurationElement> op : installedOperations.entrySet() )
    {
      List<OperationParameter> parameter = OWSOperationExtensionLoader.getParametersForOperation( op.getKey() );

      Operation operation = metadata.addNewOperation();
      operation.setName( op.getKey() );

      DCP dcp = operation.addNewDCP();
      HTTP http = dcp.addNewHTTP();
      RequestMethodType get = http.addNewGet();

      String href = m_odsEnvironment.getServiceUrl() + "?SERVICE=" + IODSConstants.ODS_SERVICE_SHORT + "&VERSION=" + IODSConstants.ODS_VERSION + "&REQUEST=" + op.getKey();
      get.setHref( href );

      if( parameter != null )
      {
        for( OperationParameter param : parameter )
        {
          DomainType p = operation.addNewParameter();
          p.setName( param.getName() );

          ODSMetaDataDocument omd = ODSMetaDataDocument.Factory.newInstance();
          omd.setODSMetaData( param.getDescription() );

          MetadataType md = p.addNewMetadata();
          md.set( omd );

          for( String value : param.getValues() )
          {
            XmlString val = p.addNewValue();
            val.setStringValue( value );
          }
        }
      }
    }
  }

  /**
   * This function returns the capabilities document.
   * 
   * @param sceneId
   *          The scene id.
   */
  public ODSCapabilitiesDocument getCapabilitiesDocument( String sceneId )
  {
    ODSCapabilitiesDocument copy = (ODSCapabilitiesDocument) m_odsCapabilities.copy();
    ODSCapabilitiesType capabilities = copy.getODSCapabilities();
    ChartsOfferingType charts = capabilities.addNewCharts();
    charts.setChartArray( m_sceneOfferings.get( sceneId ) );

    return copy;
  }

  /**
   * This function creates the offerings.
   */
  private void createOfferings( ) throws OWSException
  {
    String[] sceneIds = m_odsEnvironment.getConfigLoader().getSceneIds();
    m_sceneOfferings = new HashMap<String, ChartOfferingType[]>();
    for( final String sceneId : sceneIds )
      m_sceneOfferings.put( sceneId, createSceneOfferings( sceneId ) );

  }

  public ChartOfferingType[] createSceneOfferings( String sceneId ) throws OWSException
  {
    List<IODSChart> charts = m_odsEnvironment.getScenes().get( sceneId );
    if( charts == null )
      return new ChartOfferingType[] {};

    List<ChartOfferingType> chartList = new ArrayList<ChartOfferingType>();

    for( IODSChart chart : charts )
    {
      ChartConfigurationLoader ccl = chart.getDefinitionType();
      String[] chartIds = ccl.getChartIds();
      for( String chartId : chartIds )
      {
        try
        {
          IChartModel model = new ChartModel();
          URL url = m_odsEnvironment.getConfigDir().toURI().toURL();
          ChartFactory.configureChartModel( model, ccl, chartId, ChartExtensionLoader.getInstance(), url );
          chartList.add( createChartOffering( model, sceneId ) );
        }
        catch( MalformedURLException ex )
        {
          /* Should not happen -> else, the ODSEnvironment would not be valid. */
          ex.printStackTrace();
        }
        catch( ConfigurationException ex )
        {
          ex.printStackTrace();
        }
      }
    }

    return chartList.toArray( new ChartOfferingType[] {} );
  }

  public ChartOfferingType createChartOffering( final IChartModel model, final String scene ) throws OWSException
  {
    ChartOfferingType xmlChart = ChartOfferingType.Factory.newInstance();
    xmlChart.setId( model.getIdentifier() );
    xmlChart.setDescription( model.getSettings().getDescription() );

    TitleTypeBean[] titleBeans = model.getSettings().getTitles();
    String[] titleArray = new String[titleBeans.length];
    for( int i = 0; i < titleBeans.length; i++ )
      titleArray[i] = titleBeans[i].getText();

    xmlChart.setTitleArray( titleArray );

    LayersOfferingType xmlLayers = xmlChart.addNewLayers();
    for( IChartLayer layer : model.getLayerManager().getLayers() )
    {
      LayerOfferingType xmlLayer = xmlLayers.addNewLayer();
      xmlLayer.setId( layer.getIdentifier() );
      xmlLayer.setTitle( layer.getTitle() );
      xmlLayer.setDescription( layer.getDescription() );

      DomainAxis domAxis = xmlLayer.addNewDomainAxis();
      TargetAxis targetAxis = xmlLayer.addNewTargetAxis();

      ICoordinateMapper coordinateMapper = layer.getCoordinateMapper();
      if( coordinateMapper == null )
        throw new OWSException( "The coordinate mapper is missing. Is your configuration correct?", OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );

      domAxis.setRef( coordinateMapper.getDomainAxis().getIdentifier() );
      targetAxis.setRef( coordinateMapper.getTargetAxis().getIdentifier() );

      SymbolsOfferingType xmlSymbols = xmlLayer.addNewSymbols();
      ILegendEntry[] les = layer.getLegendEntries();
      for( int i = 0; i < les.length; i++ )
      {
        String symbolId = "symbol" + i;
        String desc = les[i].getDescription();
        if( desc == null )
          desc = "";
        Point size = les[i].computeSize( new Point( 20, 20 ) );

        SymbolOfferingType xmlSymbol = xmlSymbols.addNewSymbol();
        xmlSymbol.setId( symbolId );
        xmlSymbol.setTitle( desc );
        xmlSymbol.setWidth( size.x );
        xmlSymbol.setHeight( size.y );

        /* Cache symbols. */
        ImageData symbol = les[i].getSymbol( size );
        ODSUtils.writeSymbol( m_odsEnvironment.getTmpDir(), symbol, scene, model.getIdentifier(), layer.getIdentifier(), symbolId );
      }
    }

    AxesOfferingType xmlAxes = xmlChart.addNewAxes();
    for( IAxis axis : model.getMapperRegistry().getAxes() )
    {
      AxisOfferingType xmlAxis = xmlAxes.addNewAxis();
      xmlAxis.setId( axis.getIdentifier() );
      xmlAxis.setTitle( axis.getLabel() );

      /* Axis do not have a description. */
      xmlAxis.setDescription( "" );

      /* Direction. */
      de.openali.odysseus.service.ods.x020.AxisDirectionType.Enum dir = AxisDirectionType.POSITIVE;
      if( axis.getDirection().equals( DIRECTION.NEGATIVE ) )
        dir = AxisDirectionType.NEGATIVE;
      xmlAxis.setDirection( dir );

      /* Position. */
      de.openali.odysseus.service.ods.x020.AxisPositionType.Enum xmlPos = AxisPositionType.BOTTOM;

      POSITION pos = axis.getPosition();
      if( pos.equals( POSITION.LEFT ) )
        xmlPos = AxisPositionType.LEFT;
      else if( pos.equals( POSITION.TOP ) )
        xmlPos = AxisPositionType.TOP;
      else if( pos.equals( POSITION.RIGHT ) )
        xmlPos = AxisPositionType.RIGHT;
      xmlAxis.setPosition( xmlPos );

      IDataRange<Number> numericRange = axis.getNumericRange();
      Class< ? > clazz = axis.getDataClass();

      if(numericRange==null||numericRange.getMin()==null||numericRange.getMax()==null)
      {
        //do nothing
      }
      else if( Number.class.isAssignableFrom( clazz ) )
      {
        NumberRange range = xmlAxis.addNewNumberRange();
        range.setMinValue( numericRange.getMin().doubleValue() );
        range.setMaxValue( numericRange.getMax().doubleValue() );
      }
      else if( Calendar.class.isAssignableFrom( clazz ) )
      {
        IDataOperator<Calendar> dop = axis.getDataOperator( Calendar.class );
        DateRange range = xmlAxis.addNewDateRange();
        range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
        range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );
      }
      else if( String.class.isAssignableFrom( clazz ) )
      {
        IDataOperator<String> dop = axis.getDataOperator( String.class );

        StringRange range = xmlAxis.addNewStringRange();
        range.setMinValue( dop.numericToLogical( numericRange.getMin() ) );
        range.setMaxValue( dop.numericToLogical( numericRange.getMax() ) );

        /* Alle einzelnen Werte angeben. */
        IOrdinalDataOperator<String> odop = (IOrdinalDataOperator<String>) dop;
        ValueSet valueSet = range.addNewValueSet();
        valueSet.setValueArray( odop.getValues() );
      }
    }

    return xmlChart;
  }
}