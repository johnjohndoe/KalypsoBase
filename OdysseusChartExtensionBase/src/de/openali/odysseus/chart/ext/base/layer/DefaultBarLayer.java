package de.openali.odysseus.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author alibu
 */
public class DefaultBarLayer extends AbstractBarLayer
{
  private final AbstractDomainIntervalValueData m_data;

  public DefaultBarLayer( final ILayerProvider provider, final AbstractDomainIntervalValueData data, final IAreaStyle areaStyle )
  {
    super( provider, areaStyle );
    m_data = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void paint( final GC gc )
  {
    final AbstractDomainIntervalValueData dataContainer = getDataContainer();

    if( dataContainer != null )
    {
      final PolygonFigure pf = getPolygonFigure();

      dataContainer.open();

      final Object[] domainStartComponent = dataContainer.getDomainDataIntervalStart();
      final Object[] domainEndComponent = dataContainer.getDomainDataIntervalEnd();
      final Object[] targetComponent = dataContainer.getTargetValues();

      final ArrayList<Point> path = new ArrayList<Point>();

      final IDataOperator dopDomain = getCoordinateMapper().getDomainAxis().getDataOperator( domainStartComponent[0].getClass() );
      final IDataOperator dopTarget = getCoordinateMapper().getTargetAxis().getDataOperator( targetComponent[0].getClass() );
      if( dopDomain == null || dopTarget == null )
        return;

      final ICoordinateMapper cm = getCoordinateMapper();

      for( int i = 0; i < domainStartComponent.length; i++ )
      {
        path.clear();

        final Object startValue = domainStartComponent[i];
        final Object endValue = domainEndComponent[i];
        final Object targetValue = targetComponent[i];

        final Point p1 = cm.numericToScreen( dopDomain.logicalToNumeric( startValue ), 0 );
        final Point p2 = cm.numericToScreen( dopDomain.logicalToNumeric( startValue ), dopTarget.logicalToNumeric( targetValue ) );
        final Point p3 = cm.numericToScreen( dopDomain.logicalToNumeric( endValue ), dopTarget.logicalToNumeric( targetValue ) );
        final Point p4 = cm.numericToScreen( dopDomain.logicalToNumeric( endValue ), 0 );

        path.add( p1 );
        path.add( p2 );
        path.add( p3 );
        path.add( p4 );

        pf.setPoints( path.toArray( new Point[] {} ) );
        pf.paint( gc );
      }
    }
  }

  protected AbstractDomainIntervalValueData getDataContainer( )
  {
    return m_data;
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    // muss als minimalen Wert 0 zurückgeben, weil die Bars bis dahin laufen
    final IDataRange targetRange = getDataContainer().getTargetRange();
    final IDataOperator dop = getTargetAxis().getDataOperator( getTargetAxis().getDataClass() );
    if( dop == null )
      return null;

    return new DataRange<Number>( 0, dop.logicalToNumeric( targetRange.getMax() ) );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  @SuppressWarnings("unchecked")
  public IDataRange<Number> getDomainRange( )
  {
    final IDataRange domainRange = getDataContainer().getDomainRange();
    final Object max = domainRange.getMax();
    final IDataOperator dop = getDomainAxis().getDataOperator( max.getClass() );
    return new DataRange<Number>( dop.logicalToNumeric( domainRange.getMin() ), dop.logicalToNumeric( domainRange.getMax() ) );
  }

}
