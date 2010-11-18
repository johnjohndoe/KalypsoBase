package de.openali.odysseus.service.ods.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.service.ows.request.RequestBean;
import de.openali.odysseus.service.ows.request.ResponseBean;

public class ImageOutput
{

  public static void imageResponse( final RequestBean request, final ResponseBean response, ImageData id )
  {
    final String transparency = request.getParameterValue( "TRANSPARENT" );
    final String imgTypeStr = request.getParameterValue( "TYPE" );

    BufferedOutputStream outputStream = null;

    // PNG is default
    String contentType = "image/png";
    int imgTypeSWT = SWT.IMAGE_PNG;

    // Image-Type
    if( (imgTypeStr != null) && !imgTypeStr.toLowerCase().equals( "png" ) )
      if( imgTypeStr.toLowerCase().equals( "jpg" ) )
      {
        contentType = "image/jpeg";
        imgTypeSWT = SWT.IMAGE_JPEG;
      }
      else if( imgTypeStr.toLowerCase().equals( "gif" ) )
      {
        contentType = "image/gif";
        imgTypeSWT = SWT.IMAGE_GIF;

        // Palette umwandeln
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
        // HashMap<RGB, Integer> rgbs=new HashMap<RGB, Integer>();
        final TreeSet<Integer> pixels = new TreeSet<Integer>();

        // In einem Durchlauf Pixel auslesen
        // PaletteData pd2=new PaletteData();

        // Versucht, das mit einem TreeSet zu machen, aber da kommt eine
        // ClassCastException, weil RGB nicht Comparable
        // implementiert
        final HashSet<RGB> rgbs = new HashSet<RGB>();

        for( int y = 0; y < id.height; y++ )
          for( int x = 0; x < id.width; x++ )
          {
            final int pixel = id.getPixel( x, y );
            final RGB rgbPixel = pd.getRGB( pixel );
            rgbs.add( rgbPixel );
          }

        // Jetzt neues Bild aufbauen

        // Integers in RGBs umwandeln

        /**
         * Wenn man den Array über rgbs.toArray() erzeugt, dasnn gibt es eine trotz cast nach (RGB[]) eine
         * ClassCastException
         */
        final RGB[] rgbArray = new RGB[rgbs.size()];
        final Iterator<RGB> iter = rgbs.iterator();
        int count = 0;
        while( iter.hasNext() )
        {
          rgbArray[count] = iter.next();
          count++;
        }

        // Palette erzeugen
        final PaletteData pd2 = new PaletteData( rgbArray );

        final ImageData id2 = new ImageData( id.width, id.height, 8, pd2 );
        // Nochmal durchlaufen und zuweisen
        for( int y = 0; y < id2.height; y++ )
          for( int x = 0; x < id2.width; x++ )
            id2.setPixel( x, y, pd2.getPixel( pd.getRGB( id.getPixel( x, y ) ) ) );

        id = id2;

        Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Farbenanzahl: " + pixels.size() );
        // Integer[] rgbsArray = (Integer[]) rgbs.toArray();

      }

    // Transparenz
    if( transparency != null )
    {
      if( transparency.toLowerCase().equals( "true" ) )
        ;
      {
        id.transparentPixel = id.palette.getPixel( new RGB( 255, 255, 255 ) );
      }
    }

    response.setContentType( contentType );
    final ImageLoader il = new ImageLoader();
    il.data = new ImageData[] { id };
    try
    {
      outputStream = new BufferedOutputStream( response.getOutputStream() );
      // INFO: if you get an swt_error, you need to include swt-plugin 3.3
      // or
      // greater
      il.save( outputStream, imgTypeSWT );
    }
    finally
    {
      if( outputStream != null )
        try
        {
          outputStream.close();
        }
        catch( final IOException e )
        {
          e.printStackTrace();
        }

    }

  }
}
