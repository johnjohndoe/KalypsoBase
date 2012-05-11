package de.openali.odysseus.service.ods.operation;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.service.OGCRequest;
import org.kalypso.ogc.core.utils.OWSUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.factory.config.exception.ConfigChartNotFoundException;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.util.img.ChartPainter;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.service.ods.util.ImageOutput;
import de.openali.odysseus.service.ods.util.ODSChartManipulation;

/**
 * @author burtscher IODS operation to display an image containing a chart
 */
public class GetChart extends AbstractODSDisplayOperation implements Runnable
{
  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run( )
  {
    Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Accessing servlet: GetChart" );

    int width = 500;
    int height = 400;
    final OGCRequest req = getRequest();
    final String reqWidth = req.getParameterValue( "WIDTH" );
    final String reqHeight = req.getParameterValue( "HEIGHT" );
    final String reqName = req.getParameterValue( "NAME" );

    if( reqWidth != null && !reqWidth.trim().equals( "" ) )
      width = Integer.parseInt( reqWidth );
    if( reqHeight != null && !reqWidth.trim().equals( "" ) )
      height = Integer.parseInt( reqHeight );

    // der Name muss da sein, sonst kann kein Chart ausgewählt werden
    if( reqName != null )
    {
      final String sceneId = req.getParameterValue( "SCENE" );
      final ChartConfigurationDocument scene = getEnv().getConfigLoader().getSceneById( sceneId );
      final ChartConfigurationLoader ccl = new ChartConfigurationLoader( scene );

      URL context = null;
      try
      {
        context = new URL( ccl.getDocumentSource() );
        // context = getEnv().getConfigDir().toURI().toURL();
      }
      catch( final MalformedURLException e1 )
      {
        // this should not happen, otherwise the Env would not be valid
        e1.printStackTrace();
      }

      final IChartModel model = new ChartModel();
      final IExtensionLoader el = ChartExtensionLoader.getInstance();

      try
      {
        ChartFactory.configureChartModel( model, ccl, reqName, el, context );
      }
      catch( final ConfigChartNotFoundException e )
      {
        setException( new OWSException( "No chart available by NAME '" + reqName + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null ) );
        return;
      }
      catch( final ConfigurationException e )
      {
        /*
         * This exception will not be handled - it should not occur as the ODSEnvironment already checked the
         * correctness of the chartfile
         */
        e.printStackTrace();
      }

      try
      {
        ODSChartManipulation.manipulateChart( model, req );
      }
      catch( final OWSException e )
      {
        setException( e );
        return;
      }

      final ChartPainter chartPainter = new ChartPainter( model, new Rectangle( 0, 0, width, height ) );
      final ImageData id = chartPainter.getImageData( new NullProgressMonitor() );
      // ChartImageFactory.createChartImage(
      // chart.getChartModel(),
      // new Point( width, height ) );
      if( id != null )
        ImageOutput.imageResponse( req, getResponse(), id );
      else
      {
        setException( new OWSException( "", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null ) );
        return;
      }
    }
  }
}
