package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueData;

import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author alibu
 */
public class DefaultBarLayer extends AbstractBarLayer
{

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
  public void paint( GC gc )
  {
    AbstractDomainIntervalValueData dataContainer = getDataContainer();

    if( dataContainer != null )
    {

      PolygonFigure pf = getPolygonFigure();

      dataContainer.open();

      final Object[] domainStartComponent = dataContainer.getDomainDataIntervalStart();
      final Object[] domainEndComponent = dataContainer.getDomainDataIntervalEnd();
      final Object[] targetComponent = dataContainer.getTargetValues();

      final ArrayList<Point> path = new ArrayList<Point>();

      IDataOperator dopDomain = getCoordinateMapper().getDomainAxis().getDataOperator( domainStartComponent[0].getClass() );
      IDataOperator dopTarget = getCoordinateMapper().getTargetAxis().getDataOperator( targetComponent[0].getClass() );

      ICoordinateMapper cm = getCoordinateMapper();

      for( int i = 0; i < domainStartComponent.length; i++ )
      {
        path.clear();

        final Object startValue = domainStartComponent[i];
        final Object endValue = domainEndComponent[i];
        final Object targetValue = targetComponent[i];

        Point p1 = cm.numericToScreen( dopDomain.logicalToNumeric( startValue ), 0 );
        Point p2 = cm.numericToScreen( dopDomain.logicalToNumeric( startValue ), dopTarget.logicalToNumeric( targetValue ) );
        Point p3 = cm.numericToScreen( dopDomain.logicalToNumeric( endValue ), dopTarget.logicalToNumeric( targetValue ) );
        Point p4 = cm.numericToScreen( dopDomain.logicalToNumeric( endValue ), 0 );

        path.add( p1 );
        path.add( p2 );
        path.add( p3 );
        path.add( p4 );

        pf.setPoints( path.toArray( new Point[] {} ) );
        pf.paint( gc );
      }
    }
  }

  @Override
  public AbstractDomainIntervalValueData getDataContainer( )
  {
    return (AbstractDomainIntervalValueData) super.getDataContainer();
  }

  @Override
  public IDataRange<Number> getTargetRange( )
  {
    // muss als minimalen Wert 0 zurückgeben, weil die Bars bis dahin laufen
    IDataRange targetRange = getDataContainer().getTargetRange();
    IDataOperator dop = getTargetAxis().getDataOperator( getTargetAxis().getDataClass() );

    return new DataRange<Number>( 0, dop.logicalToNumeric( targetRange.getMax() ) );
  }

}
