package de.openali.odysseus.chart.factory.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.runtime.FileLocator;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.parameters.impl.XmlbeansParameterContainer;
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

  public static File getRelativeFile( URL context, String path, XmlObject obj )
  {

    URI uri = null;
    try
    {

      URL resolved = FileLocator.resolve( context );
      String path2 = resolved.getPath();
      String file = resolved.getFile();

      File p = new File( path2 );
      File f = null;
      if( p.isDirectory() )
        f = new File( p, path );
      else if( p.isFile() )
      {
        f = new File( p.getParent(), path );
      }

      return f;

    }
    catch( MalformedURLException e1 )
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
