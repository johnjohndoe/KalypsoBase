package de.openali.odysseus.service.ods.operation;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.factory.config.exception.ConfigChartNotFoundException;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.util.img.ChartImageFactory;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.service.ods.util.DisplayHelper;
import de.openali.odysseus.service.ods.util.HeadlessChart;
import de.openali.odysseus.service.ods.util.ImageOutput;
import de.openali.odysseus.service.ods.util.ODSChartManipulation;
import de.openali.odysseus.service.ows.exception.OWSException;
import de.openali.odysseus.service.ows.request.RequestBean;

/**
 * @author burtscher IODS operation to display an image containing a chart
 */
public class GetChart extends AbstractODSDisplayOperation implements Runnable
{

  /**
   * @throws OWSException
   * @see java.lang.Runnable#run()
   */
  public void run( )
  {
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetChart" );

    int width = 500;
    int height = 400;
    RequestBean req = getRequest();
    final String reqWidth = req.getParameterValue( "WIDTH" );
    final String reqHeight = req.getParameterValue( "HEIGHT" );
    final String reqName = req.getParameterValue( "NAME" );

    if( (reqWidth != null) && !reqWidth.trim().equals( "" ) )
      width = Integer.parseInt( reqWidth );
    if( (reqHeight != null) && !reqWidth.trim().equals( "" ) )
      height = Integer.parseInt( reqHeight );
    // der Name muss da sein, sonst kann kein Chart ausgew�hlt werden
    if( reqName != null )
    {
      final Display display = DisplayHelper.getInstance().getDisplay();
      final String sceneId = req.getParameterValue( "SCENE" );

      ChartConfigurationDocument scene = getEnv().getConfigLoader().getSceneById( sceneId );
      ChartConfigurationLoader ccl = new ChartConfigurationLoader( scene );
      URL context = null;
      try
      {
        context = getEnv().getConfigDir().toURI().toURL();
      }
      catch( MalformedURLException e1 )
      {
        // this should not happen, otherwise the Env would not be valid
        e1.printStackTrace();
      }
      IChartModel model = new ChartModel();
      IExtensionLoader el = ChartExtensionLoader.getInstance();
      try
      {
        ChartFactory.configureChartModel( model, ccl, reqName, el, context );
      }
      catch( ConfigChartNotFoundException e )
      {
        setException( new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, e.getMessage(), "" ) );
        return;
      }
      catch( ConfigurationException e )
      {
        /*
         * This exception will not be handled - it should not occur as the ODSEnvironment already checked the
         * correctness of the chartfile
         */
        e.printStackTrace();
      }

      final HeadlessChart hc = new HeadlessChart( model, new RGB( 255, 255, 255 ) );
      ChartComposite chart = hc.getChart();

      if( chart != null )
      {
        try
        {
          ODSChartManipulation.manipulateChart( chart.getChartModel(), req );
        }
        catch( OWSException e )
        {
          setException( e );
          return;
        }
        final ImageData id = ChartImageFactory.createChartImage( chart, display, width, height );
        if( id != null )
          ImageOutput.imageResponse( req, getResponse(), id );
        else
        {
          setException( new OWSException( OWSException.ExceptionCode.INVALID_PARAMETER_VALUE, "", "" ) );
          return;
        }
      }
      hc.dispose();
    }
    else
    {
      setException( new OWSException( OWSException.ExceptionCode.MISSING_PARAMETER_VALUE, "Missing mandatory parameter 'NAME'", getRequest().getUrl() ) );
      return;
    }
  }
}
