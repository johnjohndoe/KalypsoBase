package de.openali.odysseus.chart.ext.base.layer.provider;

import java.net.URL;

import de.openali.odysseus.chart.ext.base.layer.GridLayer;
import de.openali.odysseus.chart.ext.base.layer.GridLayer.GridOrientation;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * Provider for gauge data from O&M data; it's looking for a feature named "wasserstandsmessung", then tries to
 * transform it into an IObservation, creates a WasserstandLayer and uses the result components which go by the name
 * "Datum" (domain data) and "Wasserstand" (value data) as data input for the layer; the WasserstandLayer draws its data
 * as line chart The following configuration parameters are needed for the LayerProvider: dataSource: URL or relative
 * path leading to observation data
 * 
 * @author burtscher Layer
 */
public class GridLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( final URL context )
  {
    IChartLayer icl = null;

    final ILineStyle gridStyle = getStyleSet().getStyle( "line", ILineStyle.class );

    try
    {
      GridOrientation go;
      final String orientation = getParameterContainer().getParameterValue( "orientation", "BOTH" );

      if( orientation.compareTo( "VERTICAL" ) == 0 )
      {
        go = GridOrientation.VERTICAL;
      }
      else if( orientation.compareTo( "HORIZONTAL" ) == 0 )
      {
        go = GridOrientation.HORIZONTAL;
      }
      else
      {
        go = GridOrientation.BOTH;
      }

      icl = new GridLayer( go, gridStyle );
    }

    catch( final Exception e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return icl;
  }

}
