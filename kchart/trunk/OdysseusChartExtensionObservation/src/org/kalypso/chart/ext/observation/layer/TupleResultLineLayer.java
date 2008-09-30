package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

public class TupleResultLineLayer extends AbstractLineLayer
{

  private TupleResultDomainValueData m_data;

  public TupleResultLineLayer( TupleResultDomainValueData data, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;
  }

  @Override
  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );

    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 5, height / 2 ) );
    path.add( new Point( width / 5 * 2, height / 4 ) );
    path.add( new Point( width / 5 * 3, height / 4 * 3 ) );
    path.add( new Point( width / 5 * 4, height / 2 ) );
    path.add( new Point( width, height / 2 ) );

    drawLine( gc, path );
    gc.dispose();

  }

  @SuppressWarnings("unchecked")
  public void paint( final GC gc )
  {
    final List<Point> path = new ArrayList<Point>();

    m_data.open();

    final TupleResult result = m_data.getTupleResult();

    Object[] domainValues = m_data.getDomainValues();
    Object[] targetValues = m_data.getTargetValues();

    if( domainValues.length > 0 && targetValues.length > 0 )
    {
      IAxis domainAxis = getDomainAxis();
      IAxis targetAxis = getTargetAxis();
      IDataOperator dopDomain = domainAxis.getDataOperator( domainValues[0].getClass() );
      IDataOperator dopTarget = targetAxis.getDataOperator( targetValues[0].getClass() );

      if( dopDomain == null || dopTarget == null )
        return;

      for( int i = 0; i < result.size(); i++ )
      {
        final Object domainValue = domainValues[i];
        final Object targetValue = targetValues[i];

        // we have to check if all values are correct - an incorrect value means a null value - the axis would return 0
        // in that case
        if( domainValue != null && targetValue != null )
        {
          Point screen = getCoordinateMapper().numericToScreen( dopDomain.logicalToNumeric( domainValue ), dopTarget.logicalToNumeric( targetValue ) );
          path.add( screen );
        }
      }
    }

    drawLine( gc, path );
    drawPoints( gc, path );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  public IDataRange<Number> getDomainRange( )
  {
    IDataRange dataRange = m_data.getDomainRange();
    IDataOperator dop = getDomainAxis().getDataOperator( dataRange.getMin().getClass() );
    IDataRange<Number> numRange = new DataRange<Number>( dop.logicalToNumeric( dataRange.getMin() ), dop.logicalToNumeric( dataRange.getMax() ) );
    return numRange;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  public IDataRange<Number> getTargetRange( )
  {
    IDataRange dataRange = m_data.getTargetRange();
    IDataOperator dop = getTargetAxis().getDataOperator( dataRange.getMin().getClass() );
    IDataRange<Number> numRange = new DataRange<Number>( dop.logicalToNumeric( dataRange.getMin() ), dop.logicalToNumeric( dataRange.getMax() ) );
    return numRange;
  }

  protected void setData( TupleResultDomainValueData data )
  {
    m_data = data;
  }
}
