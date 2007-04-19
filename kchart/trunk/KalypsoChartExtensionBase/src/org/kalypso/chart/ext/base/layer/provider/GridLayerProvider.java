package org.kalypso.chart.ext.base.layer.provider;

import java.net.URL;

import org.kalypso.chart.ext.base.layer.GridLayer;
import org.kalypso.chart.ext.base.layer.GridLayer.GridOrientation;
import org.kalypso.chart.factory.provider.AbstractLayerProvider;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;



/**
 * @author burtscher
 *
 * Layer Provider for gauge data from O&M data;
 * it's looking for a feature named "wasserstandsmessung", then tries to
 * transform it into an IObservation, creates a WasserstandLayer
 *  and uses the result components which go by the name "Datum" (domain data) and
 *  "Wasserstand" (value data) as data input for the layer; the WasserstandLayer draws
 *  its data as line chart
 *
 *  The following configuration parameters are needed for the LayerProvider:
 *  dataSource:     URL or relative path leading to observation data
 *
 */
public class GridLayerProvider extends AbstractLayerProvider
{

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( URL context )
  {
      IChartLayer icl = null;

      try
      {

          String domainAxisId = m_lt.getMapper().getDomainAxisRef().getRef();
          String targetAxisId = m_lt.getMapper().getTargetAxisRef().getRef();

          IAxis domAxis = m_model.getAxisRegistry().getAxis( domainAxisId );
          IAxis valAxis = m_model.getAxisRegistry().getAxis( targetAxisId );

          GridOrientation go;
          String orientation=m_pc.getParameterValue( "orientation", "BOTH" );

          if (orientation.compareTo( "VERTICAL" )==0)
            go=GridOrientation.VERTICAL;
          else if (orientation.compareTo( "HORIZONTAL" )==0)
            go=GridOrientation.HORIZONTAL;
          else
            go=GridOrientation.BOTH;

          icl = new GridLayer(domAxis, valAxis, go );
      }

      catch( Exception e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return icl;
  }

}
