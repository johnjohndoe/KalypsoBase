package de.openali.odysseus.service.ods.operation;

import java.util.HashMap;
import java.util.Map;

import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.MapperRegistry;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.AxisType.Direction.Enum;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.PositionType;
import de.openali.odysseus.service.ods.environment.ODSConfigurationLoader;
import de.openali.odysseus.service.ods.util.ODSChartManipulation;
import de.openali.odysseus.service.ods.util.XMLOutput;
import de.openali.odysseus.service.ods.x020.AxesInfoDocument;
import de.openali.odysseus.service.ods.x020.AxesInfoType;
import de.openali.odysseus.service.ods.x020.AxisDirectionType;
import de.openali.odysseus.service.ods.x020.AxisOfferingType;
import de.openali.odysseus.service.ods.x020.AxisPositionType;
import de.openali.odysseus.service.ods.x020.NumberRangeDocument.NumberRange;

/**
 * The get axes info operation.
 * 
 * @author Holger Albert
 */
public class GetAxesInfo extends AbstractODSOperation implements IOGCOperation
{
  private ChartConfigurationLoader m_cl;

  private Map<String, AxisType> m_axes4Chart;

  private MapperRegistry m_mapperRegistry;

  /**
   * @see de.openali.odysseus.service.ods.operation.AbstractODSOperation#execute()
   */
  @Override
  public void execute( ) throws OWSException
  {
    final OGCRequest requestBean = getRequest();
    final String reqName = requestBean.getParameterValue( "NAME" );
    if( reqName == null )
      throw new OWSException( "Parameter NAME (of the chart) is missing...", OWSUtilities.OWS_VERSION, "en", ExceptionCode.MISSING_PARAMETER_VALUE, null );

    final String sceneId = requestBean.getParameterValue( "SCENE" );
    final ODSConfigurationLoader ocl = getEnv().getConfigLoader();
    final ChartConfigurationDocument scene = ocl.getSceneById( sceneId );
    m_cl = new ChartConfigurationLoader( scene.getChartConfiguration() );
    final ChartConfigurationDocument dcDocument = m_cl.getChartConfigurationDocument();
    final ChartConfigurationType chartConfiguration = dcDocument.getChartConfiguration();
    final ChartType[] chartArray = chartConfiguration.getChartArray();
    for( final ChartType chart : chartArray )
    {
      if( chart.getId().equals( reqName.trim() ) )
      {
        createAxesInfo( chart );
        return;
      }
    }

    throw new OWSException( "There's no chart by the name '" + reqName + "'...", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
  }

  private void createAxesInfo( final ChartType chart ) throws OWSException
  {
    final AxesInfoDocument aid = AxesInfoDocument.Factory.newInstance();
    final AxesInfoType ait = aid.addNewAxesInfo();

    m_axes4Chart = new HashMap<String, AxisType>();

    /* die Achsen k�nnen von mehreren Layern referenziert werden; */
    /* daher HashMap aufbauen und immer �berpr�fen, ob die Achse schon da ist */
    final LayerType[] layers = m_cl.getLayers( chart );
    for( final LayerType layer : layers )
    {
      final String domainAxisRef = layer.getMapperRefs().getDomainAxisRef().getRef();
      if( !m_axes4Chart.containsKey( domainAxisRef ) )
      {
        final AxisType domainAxisConfig = (AxisType) m_cl.resolveReference( domainAxisRef );
        m_axes4Chart.put( domainAxisRef, domainAxisConfig );
      }

      final String targetAxisRef = layer.getMapperRefs().getTargetAxisRef().getRef();
      if( !m_axes4Chart.containsKey( targetAxisRef ) )
      {
        final AxisType targetAxisConfig = (AxisType) m_cl.resolveReference( targetAxisRef );
        m_axes4Chart.put( targetAxisRef, targetAxisConfig );
      }
    }

    m_mapperRegistry = new MapperRegistry();

    for( final String axisId : m_axes4Chart.keySet() )
      toAxisInfo( ait, m_axes4Chart.get( axisId ) );

    XMLOutput.xmlResponse( getResponse(), aid );
  }

  private <T> void toAxisInfo( final AxesInfoType ait, final AxisType atConf ) throws OWSException
  {
    final AxisOfferingType atInfo = ait.addNewAxis();
    atInfo.setId( atConf.getId() );
    atInfo.setTitle( atConf.getLabel() );

    // Type
    Class< ? > type = null;
    IAxis axis = null;
    try
    {
      final String epId = atConf.getProvider().getEpid();
      final IAxisProvider provider = ChartExtensionLoader.createAxisProvider( epId );

      POSITION pos = POSITION.BOTTOM;
      if( atConf.getPosition() == PositionType.TOP )
        pos = POSITION.TOP;
      else if( atConf.getPosition() == PositionType.BOTTOM )
        pos = POSITION.BOTTOM;
      else if( atConf.getPosition() == PositionType.LEFT )
        pos = POSITION.LEFT;
      else if( atConf.getPosition() == PositionType.RIGHT )
        pos = POSITION.RIGHT;

      provider.init( null, atInfo.getId(), null, null, type, pos, null );
      axis = provider.getAxis();
      m_mapperRegistry.addMapper( axis );
      type = provider.getAxis().getDataClass();
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }

    if( type != null )
      atInfo.setType( type.getCanonicalName() );

    // request parameters can overwrite configuration parameters
    ODSChartManipulation.setAxesRanges( m_mapperRegistry, getRequest() );

    // Position
    final PositionType.Enum position = atConf.getPosition();
    if( position == PositionType.BOTTOM )
      atInfo.setPosition( AxisPositionType.BOTTOM );
    else if( position == PositionType.LEFT )
      atInfo.setPosition( AxisPositionType.LEFT );
    else if( position == PositionType.RIGHT )
      atInfo.setPosition( AxisPositionType.RIGHT );
    else if( position == PositionType.TOP )
      atInfo.setPosition( AxisPositionType.TOP );

    // Direction
    final Enum direction = atConf.getDirection();
    if( direction == AxisType.Direction.NEGATIVE )
      atInfo.setDirection( AxisDirectionType.NEGATIVE );
    else if( direction == AxisType.Direction.POSITIVE )
      atInfo.setDirection( AxisDirectionType.POSITIVE );

    IDataRange<Number> dataRange = null;

    if( axis != null )
    {
      dataRange = axis.getNumericRange();
    }
    else
    {
      // Should never be the case - otherwise the configuration file has errors
      final AxisNumberRangeType numberRange = atConf.getNumberRange();
      dataRange = new DataRange<Number>( numberRange.getMinValue(), numberRange.getMaxValue() );
    }

    final NumberRange numberRange = atInfo.addNewNumberRange();

    final Number min = dataRange.getMin();
    if( min != null )
      numberRange.setMinValue( min.doubleValue() );

    final Number max = dataRange.getMax();
    if( max != null )
      numberRange.setMaxValue( max.doubleValue() );
  }
}