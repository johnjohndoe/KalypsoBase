package de.openali.odysseus.service.ods.operation;

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
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.util.img.ChartPainter;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.service.ods.util.ImageOutput;
import de.openali.odysseus.service.ods.util.ODSChartManipulation;

/**
 * @author burtscher IODS operation to display an image containing a chart
 */
public class GetChart extends AbstractODSDisplayOperation
{
  @Override
  public void run( )
  {
    try
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

        final URL context = new URL( ccl.getDocumentSource() );
        final IChartModel model = new ChartModel();
        final IExtensionLoader el = ChartExtensionLoader.getInstance();

        try
        {
          ChartFactory.configureChartModel( model, ccl, reqName, el, context );
        }
        catch( final ConfigChartNotFoundException e )
        {
          throw new OWSException( "No chart available by NAME '" + reqName + "'", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );
        }

        ODSChartManipulation.manipulateChart( model, req );

        final Rectangle bounds = new Rectangle( 0, 0, width, height );

        final ImageData id = ChartPainter.createChartImageData( model, bounds, new NullProgressMonitor() );
        if( id == null )
          throw new OWSException( "", OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

        ImageOutput.imageResponse( req, getResponse(), id );
      }
    }
    catch( final OWSException ex )
    {
      setException( ex );
    }
    catch( final Exception ex )
    {
      setException( new OWSException( ex.getLocalizedMessage(), OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null ) );
    }
  }
}
