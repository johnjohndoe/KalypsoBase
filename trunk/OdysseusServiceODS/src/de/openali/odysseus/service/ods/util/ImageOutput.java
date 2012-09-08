package de.openali.odysseus.service.ods.util;

import java.io.BufferedOutputStream;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.service.OGCResponse;

import de.openali.odysseus.chart.framework.logging.impl.Logger;

/**
 * @author Alexander Burtscher, Holger Albert
 */
public class ImageOutput
{
  /**
   * This function creates the image response.
   *
   * @param request
   *          The OGC request.
   * @param response
   *          The OGC response.
   * @param id
   *          The image data.
   */
  public static void imageResponse( final OGCRequest request, final OGCResponse response, ImageData id )
  {
    /* The output stream. */
    BufferedOutputStream outputStream = null;

    try
    {
      /* Get the parameter values. */
      final String transparency = request.getParameterValue( "TRANSPARENT" );
      final String imgTypeStr = request.getParameterValue( "TYPE" );

      /* PNG is default. */
      String contentType = "image/png";
      int imgTypeSWT = SWT.IMAGE_PNG;

      /* Image type. */
      if( imgTypeStr != null && !imgTypeStr.toLowerCase().equals( "png" ) )
      {
        if( imgTypeStr.toLowerCase().equals( "jpg" ) )
        {
          contentType = "image/jpeg";
          imgTypeSWT = SWT.IMAGE_JPEG;
        }
        else if( imgTypeStr.toLowerCase().equals( "gif" ) )
        {
          contentType = "image/gif";
          imgTypeSWT = SWT.IMAGE_GIF;

          /* Convert the palette. */
          if( !id.palette.isDirect )
          {
            final RGB[] rgbs = id.getRGBs();
            if( rgbs != null )
              Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "RGBs hat die Größe " + rgbs.length );
            else
              Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "RGBs ist null " );
          }
          else
            Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Palette is direct" );

          final PaletteData pd = id.palette;

          final TreeSet<Integer> pixels = new TreeSet<>();
          final HashSet<RGB> rgbs = new HashSet<>();

          for( int y = 0; y < id.height; y++ )
            for( int x = 0; x < id.width; x++ )
            {
              final int pixel = id.getPixel( x, y );
              final RGB rgbPixel = pd.getRGB( pixel );
              rgbs.add( rgbPixel );
            }

          /* Now create a new image. */

          /* Convert integers to rgb. */
          final RGB[] rgbArray = rgbs.toArray( new RGB[] {} );

          /* Create the palette. */
          final PaletteData pd2 = new PaletteData( rgbArray );

          final ImageData id2 = new ImageData( id.width, id.height, 8, pd2 );
          for( int y = 0; y < id2.height; y++ )
          {
            for( int x = 0; x < id2.width; x++ )
              id2.setPixel( x, y, pd2.getPixel( pd.getRGB( id.getPixel( x, y ) ) ) );
          }

          id = id2;

          Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Farbenanzahl: " + pixels.size() );
        }
      }

      /* Transparency. */
      if( transparency != null )
      {
        if( transparency.toLowerCase().equals( "true" ) )
          id.transparentPixel = id.palette.getPixel( new RGB( 255, 255, 255 ) );
      }

      response.setContentType( contentType );

      final ImageLoader il = new ImageLoader();
      il.data = new ImageData[] { id };

      /* INFO: If you get an swt_error, you need to include swt-plugin 3.3 or greater. */
      outputStream = new BufferedOutputStream( response.getOutputStream() );
      il.save( outputStream, imgTypeSWT );
    }
    finally
    {
      /* Close the output stream. */
      IOUtils.closeQuietly( outputStream );
    }
  }
}
