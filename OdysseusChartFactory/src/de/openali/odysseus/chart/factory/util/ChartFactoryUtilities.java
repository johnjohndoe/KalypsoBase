package de.openali.odysseus.chart.factory.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IAxisRegistry;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartType;

public final class ChartFactoryUtilities
{
  private ChartFactoryUtilities( )
  {
  }

  /**
   * tries to create an return an URL; if the URL can't be created, an error message is logged
   * 
   * @return created URL or null if URL can't be created
   */
  public static URL createURLQuietly( final URL context, final String path )
  {
    try
    {
      final URL url = new URL( context, path );
      return url;
    }
    catch( final MalformedURLException e )
    {
      String contextString = ""; //$NON-NLS-1$
      if( context != null )
        contextString = contextString.toString();
      Logger.logError( Logger.TOPIC_LOG_STYLE, "Could not create url: '" + contextString + "', '" + path + "'" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    return null;
  }

  /**
   * Helper class that tries to load an image from the given url and scales it to width and height if each are greater
   * than 0. If the image cannot be recieved, an error is logged and a black image is returned.
   * 
   * @param path
   *          path
   * @param context
   *          if path is relative
   * @param width
   *          preferred width of image data; will be ignored if < 1
   * @param height
   *          preferred height of image data; will be ignored if < 1
   */
  public static ImageData loadImageData( final URL context, final String path, final int width, final int height )
  {
    final URL url = createURLQuietly( context, path );
    ImageData id = null;
    InputStream is = null;
    try
    {
      is = url.openStream();
      id = new ImageData( is );
      is.close();
    }
    catch( final IOException e )
    {
      Logger.logError( Logger.TOPIC_LOG_STYLE, "Could not load image from: " + url.toString() + "; will use default image." ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    finally
    {
      if( is != null )
        try
        {
          is.close();
        }
        catch( final IOException e )
        {
          // do nothing
        }
    }

    if( id == null )
    {
      id = new ImageData( width > 0 ? width : 10, height > 0 ? height : 10, 1, new PaletteData( new RGB[] { new RGB( 0, 0, 0 ) } ) );
    }
    return id.scaledTo( width < 1 ? id.width : width, height < 1 ? id.height : height );
  }

  /**
   * Does an autoscale of all axes that are configures as such in the given chart.<br/>
   * FIXME: should probably not called by any client. Probably has to be moved into the chart loading mechanism.
   */
  public static void doAutoscale( final IChartModel chartModel, final ChartType chart )
  {
    final IAxisRegistry mapperRegistry = chartModel.getMapperRegistry();
    final AxisType[] axes = chart.getMappers().getAxisArray();
    final IAxis[] autoscaledAxes = AxisUtils.findAutoscaleAxes( axes, mapperRegistry );
    chartModel.autoscale( autoscaledAxes );
  }
}
