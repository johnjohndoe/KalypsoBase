package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;
import java.util.LinkedList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.ITabularDataContainer;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.impl.CoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author alibu
 */
public class DefaultDoubleLineLayer extends AbstractLineLayer
{

  private final ITabularDataContainer<Double, Double> m_data;

  public DefaultDoubleLineLayer( ITabularDataContainer<Double, Double> data, ILineStyle lineStyle, IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_data = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  @Override
  public void drawIcon( Image img )
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

    // Zeichnen
    drawLine( gc, path );
    drawPoints( gc, path );

    gc.dispose();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
public void paint( GC gc )
  {
    final ITabularDataContainer<Double, Double> dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final Double[] domainData = dataContainer.getDomainValues();
      final Double[] targetData = dataContainer.getTargetValues();

      IDataRange<Number> dr = getDomainAxis().getNumericRange();

      double max = dr.getMax().doubleValue();
      double min = dr.getMin().doubleValue();

      final LinkedList<Point> path = new LinkedList<Point>();

      int first = -1;
      int last = -1;

      ICoordinateMapper cm = new CoordinateMapper( getDomainAxis(), getTargetAxis() );

      for( int i = 0; i < domainData.length; i++ )
      {
        // nur zeichnen, wenn linie innerhalb des Sichtbarkeitsintervalls liegt
        boolean setPoint = false;
        final double domVal = domainData[i].doubleValue();

        if( domVal > min && domVal < max )
        {
          setPoint = true;
          if( first == -1 )
          {
            first = i;
          }
          else
          {
            last = i;
          }
        }
        if( setPoint )
        {
          final double targetVal = targetData[i].doubleValue();
          path.add( cm.numericToScreen( domVal, targetVal ) );
        }
      }

      // ersten und letzten Punkt setzen
      if( first > 0 )
      {
        final double domVal = domainData[first - 1].doubleValue();
        final double targetVal = targetData[first - 1].doubleValue();
        path.add( 0, cm.numericToScreen( domVal, targetVal ) );
      }
      if( last < domainData.length - 1 )
      {
        final double domVal = domainData[last + 1].doubleValue();
        final double targetVal = targetData[last + 1].doubleValue();
        path.add( cm.numericToScreen( domVal, targetVal ) );
      }

      // Zeichnen
      PolylineFigure lf = getPolylineFigure();
      PointFigure pf = getPointFigure();

      Point[] points = path.toArray( new Point[] {} );

      lf.setPoints( points );
      lf.paint( gc );

      pf.setPoints( points );
      pf.paint( gc );

    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

  protected ITabularDataContainer<Double, Double> getDataContainer( )
  {
    return m_data;
  }

  @Override
public IDataRange<Number> getDomainRange( )
  {
    return new ComparableDataRange<Number>( getDataContainer().getDomainValues() );
  }

  @Override
public IDataRange<Number> getTargetRange(IDataRange<Number> domainIntervall )
  {
    return new ComparableDataRange<Number>( getDataContainer().getTargetValues() );
  }

}
