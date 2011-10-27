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
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
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

  public DefaultDoubleLineLayer( final ILayerProvider provider, final ITabularDataContainer<Double, Double> data, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( provider, lineStyle, pointStyle );
    m_data = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
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

    // Zeichnen
    paint( gc, path.toArray( new Point[] {} ) );

    gc.dispose();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @Override
  public void paint( final GC gc )
  {
    final ITabularDataContainer<Double, Double> dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final Double[] domainData = dataContainer.getDomainValues();
      final Double[] targetData = dataContainer.getTargetValues();

      final IDataRange<Number> dr = getDomainAxis().getNumericRange();

      final double max = dr.getMax().doubleValue();
      final double min = dr.getMin().doubleValue();

      final LinkedList<Point> path = new LinkedList<Point>();

      int first = -1;
      int last = -1;

      final ICoordinateMapper cm = new CoordinateMapper( getDomainAxis(), getTargetAxis() );

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
      final PolylineFigure lf = new PolylineFigure();
      final PointFigure pf = new PointFigure();

      paint( gc, path.toArray( new Point[] {} ) );

    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getIdentifier() + " has not yet been opened" );
    }
  }

  protected ITabularDataContainer<Double, Double> getDataContainer( )
  {
    return m_data;
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    return new ComparableDataRange<Number>( getDataContainer().getDomainValues() );
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {
    return new ComparableDataRange<Number>( getDataContainer().getTargetValues() );
  }

}
