package de.openali.diagram.ext.base.layer.provider;

import java.net.URL;

import de.openali.diagram.ext.base.layer.GridLayer;
import de.openali.diagram.ext.base.layer.GridLayer.GridOrientation;
import de.openali.diagram.factory.configuration.parameters.IParameterContainer;
import de.openali.diagram.factory.configuration.xsd.LayerType;
import de.openali.diagram.factory.provider.ILayerProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.mapper.IAxis;


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
public class GridLayerProvider implements ILayerProvider
{
  private LayerType m_lt;

  private IDiagramModel m_model;

  public void init( final IDiagramModel model, final LayerType lt )
  {
    m_lt = lt;
    m_model = model;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.ILayerProvider#getLayers()
   */
  public IChartLayer getLayer( URL context )
  {
      IChartLayer icl = null;
      IParameterContainer pc=DiagramFactoryUtilities.createParameterContainer(m_lt.getProvider(), m_lt.getId());

      try
      {

          String domainAxisId = m_lt.getMapper().getDomainAxisRef().getRef();
          String valueAxisId = m_lt.getMapper().getValueAxisRef().getRef();

          IAxis domAxis = m_model.getAxisRegistry().getAxis( domainAxisId );
          IAxis valAxis = m_model.getAxisRegistry().getAxis( valueAxisId );

          GridOrientation go;
          String orientation=pc.getParameterValue( "orientation", "BOTH" );

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
