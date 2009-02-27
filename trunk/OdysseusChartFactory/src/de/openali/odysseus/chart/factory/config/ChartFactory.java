package de.openali.odysseus.chart.factory.config;

import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlbeans.GDuration;

import de.openali.odysseus.chart.factory.config.exception.ConfigChartNotFoundException;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.parameters.impl.AxisDirectionParser;
import de.openali.odysseus.chart.factory.config.parameters.impl.AxisPositionParser;
import de.openali.odysseus.chart.factory.config.parameters.impl.XmlbeansParameterContainer;
import de.openali.odysseus.chart.factory.provider.IAxisProvider;
import de.openali.odysseus.chart.factory.provider.IAxisRendererProvider;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.factory.provider.IMapperProvider;
import de.openali.odysseus.chart.factory.util.DummyLayer;
import de.openali.odysseus.chart.factory.util.DummyLayerProvider;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.impl.AxisAdjustment;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chartconfig.x020.AreaStyleType;
import de.openali.odysseus.chartconfig.x020.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x020.AxisDurationRangeType;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x020.AxisRendererType;
import de.openali.odysseus.chartconfig.x020.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LineStyleType;
import de.openali.odysseus.chartconfig.x020.MapperType;
import de.openali.odysseus.chartconfig.x020.ParametersType;
import de.openali.odysseus.chartconfig.x020.PointStyleType;
import de.openali.odysseus.chartconfig.x020.ProviderType;
import de.openali.odysseus.chartconfig.x020.ReferencingType;
import de.openali.odysseus.chartconfig.x020.RoleReferencingType;
import de.openali.odysseus.chartconfig.x020.TextStyleType;
import de.openali.odysseus.chartconfig.x020.AxisType.PreferredAdjustment;
import de.openali.odysseus.chartconfig.x020.ChartType.Layers;
import de.openali.odysseus.chartconfig.x020.LayerType.MapperRefs;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;

/**
 * Creates a chart object from a configuration
 * 
 * @author alibu
 */
public class ChartFactory
{
  /**
   * Keys for saving providers in chart elements
   */
  public static String LAYER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.layerprovider";

  public static String AXIS_PROVIDER_KEY = "de.openali.odysseus.chart.factory.axisprovider";

  public static String AXISRENDERER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.axisrendererprovider";

  public static String MAPPER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.mapperprovider";

  public static String CONFIGURATION_TYPE_KEY = "de.openali.odysseus.chart.factory.configurationType";

  public static void configureChartModel( final IChartModel model, final ChartConfigurationLoader cl, final String configChartName, final IExtensionLoader extLoader, final URL context ) throws ConfigurationException
  {
    // ChartConfig auslesen
    // TODO: move the search for the chart into a separate search method
    ChartType dt = null;
    if( cl != null )
    {
      final ChartType[] charts = cl.getCharts();
      for( final ChartType chart : charts )
      {
        if( chart.getId().equals( configChartName ) )
        {
          dt = chart;
          break;
        }
      }
    }
    if( dt == null )
      throw new ConfigChartNotFoundException( configChartName );

    doConfiguration( model, cl, dt, extLoader, context );

  }

  public static void doConfiguration( final IChartModel model, final IReferenceResolver rr, final ChartType chartType, final IExtensionLoader extLoader, final URL context )
  {
    model.setId( chartType.getId() );
    model.setTitle( chartType.getTitle() );
    model.setDescription( chartType.getDescription() );

    final Layers layers = chartType.getLayers();
    final LayerType[] layerRefArray = layers.getLayerArray();
    for( final LayerType layerType : layerRefArray )
      if( layerType != null )
        addLayer( model, rr, context, layerType, extLoader );
      else
        Logger.logWarning( Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved " );

  }

  /**
   */
  @SuppressWarnings("unchecked")
  private static void addMapper( final IChartModel model, final MapperType mapperType, final IExtensionLoader extLoader, final URL context )
  {
    if( mapperType != null )
    {
      final String mpId = mapperType.getProvider().getEpid();
      if( (mpId != null) && (mpId.length() > 0) )
        try
        {
          IMapperRegistry mr = model.getMapperRegistry();
          final IMapperProvider mp = extLoader.getExtension( IMapperProvider.class, mpId );
          String mid = mapperType.getId();
          IParameterContainer pc = createParameterContainer( mid, mapperType.getProvider() );
          mp.init( model, mid, pc, context );
          final IMapper mapper = mp.getMapper();
          // save provider so it can be used for saving to chartfile
          mapper.setData( ChartFactory.AXIS_PROVIDER_KEY, mp );
          // save configuration type so it can be used for saving to chartfile
          mapper.setData( CONFIGURATION_TYPE_KEY, mapperType );
          mr.addMapper( mapper );
        }
        catch( final ConfigurationException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      else
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + mpId + " not known" );
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
  }

  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  public static void addAxis( final IChartModel model, final IReferenceResolver rr, final AxisType axisType, final IExtensionLoader extLoader, final URL context )
  {
    IMapperRegistry mr = model.getMapperRegistry();
    if( axisType != null )
    {
      // wenn die Achse schon da ist, dann muss man sie nicht mehr
      // erzeugen
      if( mr.getAxis( axisType.getId() ) != null )
        return;

      final String apId = axisType.getProvider().getEpid();
      if( (apId != null) && (apId.length() > 0) )
        try
        {
          final IAxisProvider ap = extLoader.getExtension( IAxisProvider.class, apId );
          if( ap != null )
          {
            String id = axisType.getId();
            POSITION axisPosition = getAxisPosition( axisType );
            IParameterContainer pc = createParameterContainer( id, axisType.getProvider() );
            Class< ? > dataClass = getAxisDataClass( axisType );
            String[] valueList = null;
            if( axisType.isSetStringRange() )
              valueList = axisType.getStringRange().getValueSet().getValueArray();
            ap.init( model, id, pc, context, dataClass, axisPosition, valueList );
            final IAxis axis = ap.getAxis();
            axis.setRegistry( mr );
            // Provider in Element setzen - fürs speichern benötigt
            axis.setData( ChartFactory.AXIS_PROVIDER_KEY, ap );
            // save configuration type so it can be used for saving to chartfile
            axis.setData( CONFIGURATION_TYPE_KEY, axisType );
            axis.setDirection( getAxisDirection( axisType ) );
            axis.setLabel( axisType.getLabel() );
            axis.setPreferredAdjustment( getAxisAdjustment( axisType ) );
            axis.setNumericRange( getAxisRange( axis, axisType ) );

            mr.addMapper( axis );

            // Renderer nur erzeugen, wenn es noch keinen für die
            // Achse gibt
            if( mr.getRenderer( axis ) == null )
            {
              IAxisRenderer axisRenderer = mr.getRenderer( axisType.getRendererRef().getRef() );
              // schon vorhanden => einfach zuweisen
              if( axisRenderer != null )
                mr.setRenderer( axis.getId(), axisRenderer );
              else
              {
                final ReferencingType rendererRef = axisType.getRendererRef();
                final AxisRendererType rendererType = (AxisRendererType) rr.resolveReference( rendererRef.getRef() );
                if( rendererType != null )
                {
                  final String arpId = rendererType.getProvider().getEpid();
                  final IAxisRendererProvider arp = extLoader.getExtension( IAxisRendererProvider.class, arpId );
                  String rid = rendererType.getId();
                  IStyleSet styleSet = createStyleSet( rendererType.getStyles(), context );
                  IParameterContainer rpc = createParameterContainer( rid, rendererType.getProvider() );
                  arp.init( model, rid, rpc, context, styleSet );
                  try
                  {
                    axisRenderer = arp.getAxisRenderer();
                    mr.setRenderer( axis.getId(), axisRenderer );
                    axisRenderer.setData( ChartFactory.AXISRENDERER_PROVIDER_KEY, arp );
                    // save configuration type so it can be used for saving to chartfile
                    axisRenderer.setData( CONFIGURATION_TYPE_KEY, rendererType );
                  }
                  catch( ConfigurationException e )
                  {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                  }
                }
              }
            }
          }
          else
            Logger.logError( Logger.TOPIC_LOG_CONFIG, "Axis could not be created. EPID was: " + apId );
        }
        catch( final ConfigurationException e )
        {
          e.printStackTrace();
        }
      else
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + apId + " not known" );
    }
    else
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
  }

  public static void addLayer( IChartModel model, IReferenceResolver rr, URL context, LayerType layerType, final IExtensionLoader extLoader )
  {
    // Achsen hinzufügen
    final MapperRefs mapper = layerType.getMapperRefs();
    final AxisType domainAxisType = (AxisType) rr.resolveReference( mapper.getDomainAxisRef().getRef() );
    addAxis( model, rr, domainAxisType, extLoader, context );
    final AxisType targetAxisType = (AxisType) rr.resolveReference( mapper.getTargetAxisRef().getRef() );
    addAxis( model, rr, targetAxisType, extLoader, context );

    // Restliche Mapper hinzufügen
    final RoleReferencingType[] mapperRefArray = mapper.getMapperRefArray();
    for( final RoleReferencingType mapperRef : mapperRefArray )
    {
      final MapperType mapperType = (MapperType) rr.resolveReference( mapperRef.getRef() );
      if( mapperType != null )
        // nur dann hinzufügen, wenn nicht schon vorhanden
        if( model.getMapperRegistry().getMapper( mapperType.getId() ) == null )
          addMapper( model, mapperType, extLoader, context );
    }

    // Layer erzeugen
    final ProviderType provider2 = layerType.getProvider();
    final String providerId = provider2.getEpid();
    ILayerProvider provider = null;
    try
    {
      provider = extLoader.getExtension( ILayerProvider.class, providerId );
      if( provider != null )
        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "LayerProvider loaded:" + provider.getClass().toString() );
      else
      {
        provider = new DummyLayerProvider();
        Logger.logError( Logger.TOPIC_LOG_GENERAL, "No LayerProvider for " + providerId );
      }

      // create styles
      IStyleSet styleSet = createStyleSet( layerType.getStyles(), context );
      // create map if mapper roles to ids
      Map<String, String> mapperMap = createMapperMap( layerType );
      // create parameter container
      IParameterContainer parameters = createParameterContainer( layerType.getId(), layerType.getProvider() );

      String domainAxisId = layerType.getMapperRefs().getDomainAxisRef().getRef();
      String targetAxisId = layerType.getMapperRefs().getTargetAxisRef().getRef();

      provider.init( model, layerType.getId(), parameters, context, domainAxisId, targetAxisId, mapperMap, styleSet );
      final IChartLayer icl = provider.getLayer( context );
      if( icl != null )
      {
        IAxis domainAxis = model.getMapperRegistry().getAxis( layerType.getMapperRefs().getDomainAxisRef().getRef() );
        IAxis targetAxis = model.getMapperRegistry().getAxis( layerType.getMapperRefs().getTargetAxisRef().getRef() );
        ICoordinateMapper cm = new CoordinateMapper( domainAxis, targetAxis );
        icl.setCoordinateMapper( cm );
        icl.setId( layerType.getId() );
        icl.setTitle( layerType.getTitle() );
        icl.setDescription( layerType.getDescription() );
        icl.setVisible( layerType.getVisible() );
        // saving provider so it can be reused for saving to chartfile
        icl.setData( LAYER_PROVIDER_KEY, provider );
        // save configuration type so it can be used for saving to chartfile
        icl.setData( CONFIGURATION_TYPE_KEY, layerType );
        // initialize layer
        icl.init();

        model.getLayerManager().addLayer( icl );
        Logger.logInfo( Logger.TOPIC_LOG_CONFIG, "Adding Layer: " + icl.getTitle() );
      }
    }
    catch( final ConfigurationException e )
    {
      e.printStackTrace();
    }
    catch( Exception e )
    {
      e.printStackTrace();
      IAxis domainAxis = model.getMapperRegistry().getAxis( layerType.getMapperRefs().getDomainAxisRef().getRef() );
      IAxis targetAxis = model.getMapperRegistry().getAxis( layerType.getMapperRefs().getTargetAxisRef().getRef() );
      ICoordinateMapper cm = new CoordinateMapper( domainAxis, targetAxis );
      IChartLayer icl = new DummyLayer();
      icl.setTitle( layerType.getTitle() );
      icl.setCoordinateMapper( cm );
      // saving the provider into the layer so it can be reused to save changed charts
      icl.setData( LAYER_PROVIDER_KEY, provider );
      // save configuration type so it can be used for saving to chartfile
      icl.setData( CONFIGURATION_TYPE_KEY, layerType );
      model.getLayerManager().addLayer( icl );
    }
  }

  private static Map<String, String> createMapperMap( LayerType lt )
  {
    Map<String, String> mapperMap = new HashMap<String, String>();
    RoleReferencingType[] mapperRefArray = lt.getMapperRefs().getMapperRefArray();
    for( RoleReferencingType rrt : mapperRefArray )
      mapperMap.put( rrt.getRole(), rrt.getRef() );
    return mapperMap;
  }

  private static IStyleSet createStyleSet( Styles styles, URL context )
  {
    // Styles erzeugen
    IStyleSet styleSet = new StyleSet();
    if( styles != null )
    {
      AreaStyleType[] asArray = styles.getAreaStyleArray();
      for( AreaStyleType ast : asArray )
      {
        IAreaStyle as = StyleFactory.createAreaStyle( ast, context );
        // save configuration type so it can be used for saving to chartfile
        as.setData( CONFIGURATION_TYPE_KEY, ast );
        styleSet.addStyle( ast.getRole(), as );
      }
      PointStyleType[] psArray = styles.getPointStyleArray();
      for( PointStyleType pst : psArray )
      {
        IPointStyle ps = StyleFactory.createPointStyle( pst, context );
        // save configuration type so it can be used for saving to chartfile
        ps.setData( CONFIGURATION_TYPE_KEY, pst );
        styleSet.addStyle( pst.getRole(), ps );
      }
      LineStyleType[] lsArray = styles.getLineStyleArray();
      for( LineStyleType lst : lsArray )
      {
        ILineStyle ls = StyleFactory.createLineStyle( lst );
        // save configuration type so it can be used for saving to chartfile
        ls.setData( CONFIGURATION_TYPE_KEY, lst );
        styleSet.addStyle( lst.getRole(), ls );
      }
      TextStyleType[] tsArray = styles.getTextStyleArray();
      for( TextStyleType tst : tsArray )
      {
        ITextStyle ts = StyleFactory.createTextStyle( tst );
        // save configuration type so it can be used for saving to chartfile
        ts.setData( CONFIGURATION_TYPE_KEY, tst );
        styleSet.addStyle( tst.getRole(), ts );
      }
    }
    return styleSet;
  }

  public static IParameterContainer createParameterContainer( String ownerId, ProviderType pt )
  {
    ParametersType parameters = null;
    if( pt != null )
      parameters = pt.getParameters();
    final IParameterContainer pc = new XmlbeansParameterContainer( ownerId, pt.getEpid(), parameters );
    return pc;
  }

  private static DIRECTION getAxisDirection( AxisType at )
  {
    final AxisDirectionParser app = new AxisDirectionParser();
    final String position = at.getPosition().toString();
    final DIRECTION dir = app.stringToLogical( position );
    return dir;
  }

  private static POSITION getAxisPosition( AxisType at )
  {
    final AxisPositionParser app = new AxisPositionParser();
    final String position = at.getPosition().toString();
    final POSITION pos = app.stringToLogical( position );
    return pos;
  }

  private static AxisAdjustment getAxisAdjustment( AxisType at )
  {
    AxisAdjustment aa = null;
    if( at.isSetPreferredAdjustment() )
    {
      PreferredAdjustment pa = at.getPreferredAdjustment();
      aa = new AxisAdjustment( pa.getBefore(), pa.getRange(), pa.getAfter() );
    }
    else
      aa = new AxisAdjustment( 0, 1, 0 );
    return aa;
  }

  /**
   * creates the axis range from the xml element
   */
  private static IDataRange<Number> getAxisRange( IAxis axis, AxisType at )
  {
    Number min = 0;
    Number max = 1;

    if( at.isSetDateRange() )
    {
      AxisDateRangeType range = at.getDateRange();
      IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
      Calendar minValue = range.getMinValue();
      min = dataOperator.logicalToNumeric( minValue );
      Calendar maxValue = range.getMaxValue();
      max = dataOperator.logicalToNumeric( maxValue );
    }
    else if( at.isSetNumberRange() )
    {
      AxisNumberRangeType range = at.getNumberRange();
      min = range.getMinValue();
      max = range.getMaxValue();
    }
    else if( at.isSetStringRange() )
    {
      AxisStringRangeType range = at.getStringRange();
      min = range.getMinValue();
      max = range.getMaxValue();
    }
    else if( at.isSetDurationRange() )
    {
      AxisDurationRangeType range = at.getDurationRange();
      IDataOperator<Calendar> dataOperator = axis.getDataOperator( Calendar.class );
      GDuration minDur = range.getMinValue();
      Calendar now = Calendar.getInstance();
      Calendar minValue = addDurationToCal( now, minDur );
      min = dataOperator.logicalToNumeric( minValue );
      GDuration maxDur = range.getMaxValue();
      Calendar maxValue = addDurationToCal( now, maxDur );
      max = dataOperator.logicalToNumeric( maxValue );
    }
    IDataRange<Number> range = new ComparableDataRange<Number>( new Number[] { min, max } );
    return range;
  }

  private static Calendar addDurationToCal( Calendar cal, GDuration dur )
  {
    int sign = dur.getSign();
    cal.add( Calendar.YEAR, sign * dur.getYear() );
    cal.add( Calendar.MONTH, sign * dur.getMonth() );
    cal.add( Calendar.DAY_OF_MONTH, sign * dur.getDay() );
    cal.add( Calendar.HOUR_OF_DAY, sign * dur.getHour() );
    cal.add( Calendar.MINUTE, sign * dur.getMinute() );
    cal.add( Calendar.SECOND, sign * dur.getSecond() );
    cal.add( Calendar.MILLISECOND, (int) (sign * dur.getFraction().doubleValue()) );
    return cal;
  }

  public static Class< ? > getAxisDataClass( AxisType at )
  {
    if( at.isSetDateRange() || at.isSetDurationRange() )
      return Calendar.class;
    else if( at.isSetStringRange() )
      return String.class;
    else
      return Number.class;
  }
}
