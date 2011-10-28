package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class DefaultLineLayer extends AbstractLineLayer
{

  private final ITabularDataContainer< ? , ? > m_dataContainer;

  public DefaultLineLayer( final ILayerProvider provider, final ITabularDataContainer< ? , ? > data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );
    m_dataContainer = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
  public void paint( final GC gc )
  {
    final ITabularDataContainer< ? , ? > dataContainer = getDataContainer();
    if( dataContainer == null )
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getIdentifier() + "is unable to open data." );
      return;
    }
    dataContainer.open();

    final Object[] domainData = dataContainer.getDomainValues();
    final Object[] targetData = dataContainer.getTargetValues();

    if( domainData.length > 0 )
    {
      final IDataRange<Number> dr = getDomainAxis().getNumericRange();
      final Number max = dr.getMax();
      final Number min = dr.getMin();
      final ICoordinateMapper coordinateMapper = getCoordinateMapper();
      final int screenMin = coordinateMapper.numericToScreen( min, 0 ).x;
      final int screenMax = coordinateMapper.numericToScreen( max, 0 ).x;
      final ArrayList<Point> path = new ArrayList<Point>();
      for( int i = 0; i < domainData.length; i++ )
      {
        final Object domVal = domainData[i];
        final Object targetVal = targetData[i];
        final Point screen = getCoordinateMapper().logicalToScreen( domVal, targetVal );
        if( screen.x > screenMin && screen.x < screenMax )
          path.add( screen );
      }
      paint( gc, path.toArray( new Point[] {} ) );
    }
  }

  protected ITabularDataContainer< ? , ? > getDataContainer( )
  {
    return m_dataContainer;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange< ? > getDomainRange( )
  {

    final IDataRange domainRange = m_dataContainer.getDomainRange();
    final Object max = domainRange.getMax();
    final IDataOperator dop = getDomainAxis().getDataOperator( max.getClass() );
    return new DataRange<Number>( dop.logicalToNumeric( domainRange.getMin() ), dop.logicalToNumeric( domainRange.getMax() ) );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {

    final IDataRange targetRange = m_dataContainer.getTargetRange();
    final Object max = targetRange.getMax();
    final IDataOperator top = getTargetAxis().getDataOperator( max.getClass() );
    return new DataRange<Number>( top.logicalToNumeric( targetRange.getMin() ), top.logicalToNumeric( targetRange.getMax() ) );
  }

}
