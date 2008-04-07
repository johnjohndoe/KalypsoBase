package de.openali.odysseus.chart.factory.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.parameters.impl.XmlbeansParameterContainer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chartconfig.x010.ParametersType;
import de.openali.odysseus.chartconfig.x010.ProviderType;

public class ChartFactoryUtilities
{

  public static IParameterContainer createXmlbeansParameterContainer( String ownerId, ProviderType pt )
  {
    ParametersType parameters = null;
    if( pt != null )
    {
      parameters = pt.getParameters();
    }
    final IParameterContainer pc = new XmlbeansParameterContainer( ownerId, pt.getEpid(), parameters );
    return pc;
  }

  /**
   * tries to create an reaturn an url; if the url can't be created, an error message is logged
   * 
   * @return created url or null if url cant be created
   */
  public static URL createURLQuietly( URL context, String path )
  {
    try
    {
      URL url = new URL( context, path );
      return url;
    }
    catch( MalformedURLException e )
    {
      Logger.logError( Logger.TOPIC_LOG_STYLE, "Could not create url: '" + context.toString() + "', '" + path + "'" );
    }
    return null;
  }

  /**
   * Helper class that tries to load an image from the given url and scales it to width and height if each are greater
   * than 0. If the image cannot be recieved, an error is logged and a black image is returned.
   * 
   * @param path
   *            path
   * @param context
   *            if path is relative
   * @param width
   *            preferred width of image data; will be ignored if < 1
   * @param height
   *            preferred height of image data; will be ignored if < 1
   */
  public static ImageData loadImageData( URL context, String path, int width, int height )
  {
    URL url = createURLQuietly( context, path );
    ImageData id = null;
    InputStream is = null;
    try
    {
      is = url.openStream();
      id = new ImageData( is );
      is.close();
    }
    catch( IOException e )
    {
      Logger.logError( Logger.TOPIC_LOG_STYLE, "Could not load image from: " + url.toString() + "; will use default image." );
    }
    finally
    {
      if( is != null )
        try
        {
          is.close();
        }
        catch( IOException e )
        {
          // nix machen
        }
    }

    if( id == null )
    {
      id = new ImageData( width > 0 ? width : 10, height > 0 ? height : 10, 1, new PaletteData( new RGB[] { new RGB( 0, 0, 0 ) } ) );
    }
    return id.scaledTo( width < 1 ? id.width : width, height < 1 ? id.height : height );
  }
}
