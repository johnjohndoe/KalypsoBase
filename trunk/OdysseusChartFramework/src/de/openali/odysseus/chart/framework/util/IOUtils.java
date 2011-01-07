package de.openali.odysseus.chart.framework.util;

import java.io.IOException;
import java.io.InputStream;

public class IOUtils
{

  public static void closeQuietly( final InputStream input )
  {
    try
    {
      if( input != null )
      {
        input.close();
      }
    }
    catch( final IOException ioe )
    {
      // do nothing
    }
  }
}
