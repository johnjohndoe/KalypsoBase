package org.kalypso.service.ods.operation;

import java.io.IOException;
import java.io.OutputStream;

import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.ODSConfigurationLoader;
import org.kalypso.service.ods.util.ODSScene;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;
import org.kalypso.service.ogc.exception.OWSException.ExceptionCode;

public class DoTest implements IODSOperation
{

  public void operate( RequestBean requestBean, ResponseBean responseBean ) throws OWSException
  {
    // TODO Auto-generated method stub

    final ODSConfigurationLoader loader = ODSConfigurationLoader.getInstance();
    final String reset = requestBean.getParameterValue( "RESET" );
    if( "TRUE".equals( reset ) )
      loader.reload();

    final OutputStream outputStream = responseBean.getOutputStream();

    final String sceneId = requestBean.getParameterValue( "SCENE" );
    final ODSScene sceneById = loader.getSceneById( sceneId );

    if( sceneById != null )
    {
      try
      {
        sceneById.getODSSceneDocument().save( outputStream );
      }
      catch( final IOException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    else
    {
      throw new OWSException( ExceptionCode.INVALID_PARAMETER_VALUE, "Scene '" + sceneId + "' not defined", requestBean.getUrl() );
    }
  }

}
