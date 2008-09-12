package org.kalypso.service.ods.operation;

import java.io.BufferedOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.impl.ChartModel;
import org.kalypso.chart.framework.trash.LegendImageContainer;
import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.DisplayHelper;
import org.kalypso.service.ods.util.ODSConfigurationLoader;
import org.kalypso.service.ods.util.ODSChartManipulation;
import org.kalypso.service.ods.util.ODSScene;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.ksp.chart.factory.ChartConfigurationType;

/**
 * @author burtscher operation creating a legend image from a given name
 */
public class GetLegend implements IODSOperation, Runnable
{

  private RequestBean m_requestBean;

  private ResponseBean m_responseBean;

  public GetLegend( )
  {

  }

  /**
   * @see de.openali.ows.service.IOWSOperation#operate(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  public void operate( RequestBean requestBean, ResponseBean responseBean )
  {
    m_requestBean = requestBean;
    m_responseBean = responseBean;
    // Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet:
    // GetLegend", null ) );
    // TODO: Logger mit Plugin-Namen instanziieren oder so...
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetLegend" );
    DisplayHelper.getInstance().getDisplay().syncExec( this );
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    final String reqName = m_requestBean.getParameterValue( "NAME" );
    // der Name muss da sein, sonst kann kein Chart ausgewählt werden
    if( reqName != null )
    {
      Display display = null;
      BufferedOutputStream outputStream = null;
      LegendImageContainer lic = null;
      try
      {
        final String sceneId = m_requestBean.getParameterValue( "SCENE" );
        final ODSConfigurationLoader ocl = ODSConfigurationLoader.getInstance();
        final ODSScene scene = ocl.getSceneById( sceneId );
        final ChartConfigurationType chartConfiguration = scene.getChartConfiguration();
        final ChartConfigurationLoader cl = new ChartConfigurationLoader( chartConfiguration );
        Logger.trace( "GetLegend: Creating new chart model" );
        final IChartModel model = new ChartModel();
        ChartFactory.configureChartModel( model, cl, reqName, null );

        ODSChartManipulation.setLayerVisibility( model.getLayerManager(), m_requestBean );
        display = DisplayHelper.getInstance().getDisplay();
        lic = new LegendImageContainer( model.getLayerManager(), display );
        final Image legendImg = lic.getImage();
        m_responseBean.setContentType( "image/png" );
        final ImageLoader il = new ImageLoader();
        final ImageData id = legendImg.getImageData();
        il.data = new ImageData[] { id };
        outputStream = new BufferedOutputStream( m_responseBean.getOutputStream() );
        il.save( outputStream, SWT.IMAGE_PNG );
        outputStream.close();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
      finally
      {
        if( lic != null )
          lic.dispose();
        if( outputStream != null )
        {
          try
          {
            outputStream.close();
          }
          catch( final IOException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }

    }
    else
    {
      // ODSException.showException( m_request, m_response, ERROR_CODE.NO_NAME_SPECIFIED );
    }
  }
}
