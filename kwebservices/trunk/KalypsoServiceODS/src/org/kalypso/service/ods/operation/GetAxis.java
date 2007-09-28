package org.kalypso.service.ods.operation;

import java.io.BufferedOutputStream;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.view.AxisImageFactory;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.DisplayHelper;
import org.kalypso.service.ods.util.HeadlessAxis;
import org.kalypso.service.ods.util.HeadlessChart;
import org.kalypso.service.ods.util.ImageOutput;
import org.kalypso.service.ods.util.ODSChartManipulation;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author burtscher IODS operation to display an image containing a chart
 */
public class GetAxis implements IODSOperation, Runnable
{

  private RequestBean m_requestBean;

  private OWSException m_exception = null;

  private ResponseBean m_responseBean;

  public GetAxis( )
  {

  }

  public void operate( RequestBean requestBean, final ResponseBean responseBean ) throws OWSException
  {
    m_requestBean = requestBean;
    m_responseBean = responseBean;
    final DisplayHelper dh = DisplayHelper.getInstance();
    final Display d = dh.getDisplay();
    d.syncExec( this );
    if( m_exception != null )
    {
      throw m_exception;
    }
  }

  /**
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {

    // Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet:
    // GetChart", null ) );
    // TODO: Logger mit Plugin-Namen instanziieren oder so...
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetPlot" );

    int width = 500;
    int height = 400;
    final String reqWidth = m_requestBean.getParameterValue( "WIDTH" );
    final String reqHeight = m_requestBean.getParameterValue( "HEIGHT" );
    final String reqName = m_requestBean.getParameterValue( "NAME" );

    if( reqWidth != null && !reqWidth.trim().equals( "" ) )
      width = Integer.parseInt( reqWidth );
    if( reqHeight != null && !reqHeight.trim().equals( "" ) )
      height = Integer.parseInt( reqHeight );
    // der Name muss da sein, sonst kann kein Chart ausgewählt werden
    if( reqName != null )
    {
      final Display display = DisplayHelper.getInstance().getDisplay();
      final ChartComposite chart = null;

      final String sceneId = m_requestBean.getParameterValue( "SCENE" );
      final String axisId = m_requestBean.getParameterValue( "AXISID" );

      final HeadlessAxis hc = new HeadlessAxis( sceneId, axisId );
      final Shell shell = hc.getAxisShell();

      // Gecachetes Chart verwenden
      if( shell != null )
      {
        // ODSChartManipulation.setLayerVisibility( chart.getModel().getLayerManager(), m_requestBean );
        final ImageData id = AxisImageFactory.createAxisImageFromShell( shell, hc.getMapperRegistry(), axisId, display, width, height );

        if( id != null )
        {
          ImageOutput.imageResponse( m_requestBean, m_responseBean, id );
        }
        else
        {
          m_exception = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" );
          return;
        }
      }
      shell.dispose();

    }
    else
    {
      m_exception = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" );
      return;
    }
  }

  public void runold( )
  {

    // Activator.getDefault().getLog().log( new Status( IStatus.INFO, Activator.PLUGIN_ID, 0, "Accessing servlet:
    // GetChart", null ) );
    // TODO: Logger mit Plugin-Namen instanziieren oder so...
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetPlot" );

    int width = 500;
    int height = 400;
    final String reqWidth = m_requestBean.getParameterValue( "WIDTH" );
    final String reqHeight = m_requestBean.getParameterValue( "HEIGHT" );
    final String reqName = m_requestBean.getParameterValue( "NAME" );
    final String transparency = m_requestBean.getParameterValue( "TRANSPARENT" );
    final String imgTypeStr = m_requestBean.getParameterValue( "TYPE" );

    if( reqWidth != null )
      width = Integer.parseInt( reqWidth );
    if( reqHeight != null )
      height = Integer.parseInt( reqHeight );
    // der Name muss da sein, sonst kann kein Chart ausgewählt werden
    if( reqName != null )
    {
      final Display display = DisplayHelper.getInstance().getDisplay();
      final BufferedOutputStream outputStream = null;
      ChartComposite chart = null;

      final String sceneId = m_requestBean.getParameterValue( "SCENE" );
      final String axisId = m_requestBean.getParameterValue( "AXISID" );

      final HeadlessChart cc = new HeadlessChart( sceneId, reqName, new RGB( 255, 255, 255 ) );
      Logger.trace( "GetChart: Creating new chart" );
      chart = cc.getChart();

      // Gecachetes Chart verwenden
      if( chart != null )
      {
        ODSChartManipulation.setLayerVisibility( chart.getModel().getLayerManager(), m_requestBean );
        final ImageData id = AxisImageFactory.createAxisImageFromChart( chart, axisId, display, width, height );

        if( id != null )
        {
          ImageOutput.imageResponse( m_requestBean, m_responseBean, id );
        }
        else
        {
          m_exception = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" );
          return;
        }
      }
      cc.dispose();

    }
    else
    {
      m_exception = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" );
      return;
    }
  }

}
