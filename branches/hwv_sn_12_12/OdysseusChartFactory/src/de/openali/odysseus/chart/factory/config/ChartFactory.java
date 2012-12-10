package de.openali.odysseus.chart.factory.config;

import java.awt.Insets;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;

import de.openali.odysseus.chart.factory.config.exception.ConfigChartNotFoundException;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.config.resolver.ExtendedReferenceResolver;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.impl.settings.CHART_DATA_LOADER_STRATEGY;
import de.openali.odysseus.chart.framework.model.impl.settings.IBasicChartSettings;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.InsetType;
import de.openali.odysseus.chartconfig.x020.LineStyleType;
import de.openali.odysseus.chartconfig.x020.PlotFrameStyle;
import de.openali.odysseus.chartconfig.x020.PlotFrameStyle.Edge;
import de.openali.odysseus.chartconfig.x020.TextStyleType;
import de.openali.odysseus.chartconfig.x020.TitleType;

/**
 * Creates a chart object from a configuration
 * 
 * @author alibu
 */
public final class ChartFactory
{
  private ChartFactory( )
  {
  }

  public static final String AXIS_PROVIDER_KEY = "de.openali.odysseus.chart.factory.axisprovider";

  public static final String AXISRENDERER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.axisrendererprovider";

// public static final String MAPPER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.mapperprovider";

  public static void configureChartModel( final IChartModel model, final ChartConfigurationLoader configurationLoader, final String configChartName, final IExtensionLoader extLoader, final URL context ) throws ConfigurationException
  {
    // ChartConfig auslesen
    // TODO: move the search for the chart into a separate search method
    ChartType dt = null;
    if( configurationLoader != null )
    {
      final ChartType[] charts = configurationLoader.getCharts();
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

    doConfiguration( model, configurationLoader, dt, extLoader, context );
  }

  private static void addInsets( final IBasicChartSettings settings, final InsetType insetType )
  {
    settings.addInsets( insetType.getId(), new Insets( insetType.getTop(), insetType.getLeft(), insetType.getBottom(), insetType.getRight() ) );
  }

  private static void setPlotFrame( final IBasicChartSettings settings, final URL context, final Edge[] plotFrameEdges )
  {
    final ChartTypeResolver chartTypeResolver = ChartTypeResolver.getInstance();
    for( final Edge edge : plotFrameEdges )
    {
      AbstractStyleType styleType = edge.getLineStyle();
      if( edge.isSetStyleReference() )
      {
        try
        {
          styleType = chartTypeResolver.findStyleType( edge.getStyleReference(), context );
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
        }
      }
      final ILineStyle style = StyleFactory.createLineStyle( (LineStyleType) styleType );
      settings.addPlotFrameStyle( POSITION.valueOf( edge.getPosition().toString() ), style );
    }
  }

  public static void doConfiguration( final IChartModel model, final IReferenceResolver resolver, final ChartType chartType, final IExtensionLoader extLoader, final URL context )
  {
    model.setIdentifier( chartType.getId() );

    final ChartTypeResolver chartTypeResolver = ChartTypeResolver.getInstance();

    for( final TitleType type : chartType.getTitleArray() )
    {
      try
      {
        final AbstractStyleType styleType = chartTypeResolver.findStyleType( type.getStyleref(), context );
        final ITextStyle style = StyleFactory.createTextStyle( (TextStyleType) styleType );
        final TitleTypeBean title = StyleHelper.getTitleTypeBean( type, style );
        model.getSettings().addTitles( title );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }
    }
    if( chartType.isSetChartInsets() )
    {
      addInsets( model.getSettings(), chartType.getChartInsets() );
    }
    if( chartType.isSetPlotInsets() )
    {
      addInsets( model.getSettings(), chartType.getPlotInsets() );
    }
    if( chartType.isSetPlotFrame() )
    {
      final PlotFrameStyle plotFrameStyle = chartType.getPlotFrame();
      final Edge[] plotFrameEdges = plotFrameStyle.getEdgeArray();
      if( plotFrameEdges.length > 0 )
      {
        setPlotFrame( model.getSettings(), context, plotFrameEdges );
      }
    }
    model.getSettings().setDescription( chartType.getDescription() );

    model.getBehaviour().setHideLegend( !chartType.getLegend() );
    model.getSettings().setLegendRenderer( chartType.getLegendRenderer() );

    model.getSettings().setDataLoaderStrategy( CHART_DATA_LOADER_STRATEGY.convert( chartType.getLoader().toString() ) );

    final ExtendedReferenceResolver extendedResolver = new ExtendedReferenceResolver( resolver );
    final ChartMapperFactory mapperFactory = new ChartMapperFactory( model, extendedResolver, extLoader, context );
    mapperFactory.build( chartType );

    final ChartLayerFactory layerFactory = new ChartLayerFactory( model, extendedResolver, extLoader, context, mapperFactory );
    layerFactory.build( chartType );

    chartTypeResolver.clear();
  }
}
