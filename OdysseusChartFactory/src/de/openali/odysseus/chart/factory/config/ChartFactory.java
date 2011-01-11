package de.openali.odysseus.chart.factory.config;

import java.awt.Insets;
import java.net.URL;

import de.openali.odysseus.chart.factory.config.exception.ConfigChartNotFoundException;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.factory.config.resolver.ChartTypeResolver;
import de.openali.odysseus.chart.factory.config.resolver.ExtendedReferenceResolver;
import de.openali.odysseus.chart.factory.util.IReferenceResolver;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.ChartType;
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

  public static final String MAPPER_PROVIDER_KEY = "de.openali.odysseus.chart.factory.mapperprovider";

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

  public static void doConfiguration( final IChartModel model, final IReferenceResolver resolver, final ChartType chartType, final IExtensionLoader extLoader, final URL context )
  {
    model.setId( chartType.getId() );

    final ChartTypeResolver chartTypeResolver = ChartTypeResolver.getInstance();

    for( final TitleType type : chartType.getTitleArray() )
    {
      final TitleTypeBean title = new TitleTypeBean( type.getStringValue() );

      try
      {
        final AbstractStyleType styleType = chartTypeResolver.findStyleType( type.getStyleref(), context );
        final ITextStyle style = StyleFactory.createTextStyle( (TextStyleType) styleType );

        title.setTextStyle( style );
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
      }

      title.setAlignmentHorizontal( StyleHelper.getAlignment( type.getHorizontalAlignment() ) );
      title.setAlignmentVertical( StyleHelper.getAlignment( type.getVerticalAlignment() ) );
      title.setTextAnchorX( StyleHelper.getAlignment( type.getHorizontalTextAnchor() ) );
      title.setTextAnchorY( StyleHelper.getAlignment( type.getVerticalTextAnchor() ) );

      final Insets inset = new Insets( type.getInsetTop(), type.getInsetLeft(), type.getInsetBottom(), type.getInsetBottom() );
      title.setInsets( inset );

      model.addTitles( title );
    }

    model.setDescription( chartType.getDescription() );
    model.setHideLegend( !chartType.getLegend() );

    final ExtendedReferenceResolver extendedResolver = new ExtendedReferenceResolver( resolver );
    final ChartMapperFactory mapperFactory = new ChartMapperFactory( model, extendedResolver, extLoader, context );
    mapperFactory.build( chartType );

    final ChartLayerFactory layerFactory = new ChartLayerFactory( model, extendedResolver, extLoader, context, mapperFactory );
    layerFactory.build( chartType );
  }
}
