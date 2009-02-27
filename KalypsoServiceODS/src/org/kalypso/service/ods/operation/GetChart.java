package org.kalypso.service.ods.operation;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.framework.view.ChartImageFactory;
import org.kalypso.service.ods.IODSOperation;
import org.kalypso.service.ods.util.DisplayHelper;
import org.kalypso.service.ods.util.HeadlessChart;
import org.kalypso.service.ods.util.ImageOutput;
import org.kalypso.service.ods.util.ODSChartManipulation;
import org.kalypso.service.ogc.RequestBean;
import org.kalypso.service.ogc.ResponseBean;
import org.kalypso.service.ogc.exception.OWSException;

/**
 * @author burtscher IODS operation to display an image containing a chart
 */
public class GetChart implements IODSOperation, Runnable
{

  private RequestBean m_requestBean;

  private OWSException m_exception = null;

  private ResponseBean m_responseBean;

  public GetChart( )
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
   * @throws OWSException
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetChart" );

    int width = 500;
    int height = 400;
    final String reqWidth = m_requestBean.getParameterValue( "WIDTH" );
    final String reqHeight = m_requestBean.getParameterValue( "HEIGHT" );
    final String reqName = m_requestBean.getParameterValue( "NAME" );

    if( reqWidth != null && !reqWidth.trim().equals( "" ) )
      width = Integer.parseInt( reqWidth );
    if( reqHeight != null && !reqWidth.trim().equals( "" ) )
      height = Integer.parseInt( reqHeight );
    // der Name muss da sein, sonst kann kein Chart ausgewählt werden
    if( reqName != null )
    {
      final Display display = DisplayHelper.getInstance().getDisplay();

      ChartComposite chart = null;
      final String sceneId = m_requestBean.getParameterValue( "SCENE" );

      final HeadlessChart hc = new HeadlessChart( sceneId, reqName, new RGB( 255, 255, 255 ) );
      Logger.trace( "GetChart: Creating new chart" );
      chart = hc.getChart();

      if( chart != null )
      {
        ODSChartManipulation.manipulateChart( chart.getModel(), m_requestBean );
        final ImageData id = ChartImageFactory.createChartImage( chart, display, width, height );
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
      hc.dispose();
    }
    else
    {
      m_exception = new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" );
      return;
    }
  }

}
