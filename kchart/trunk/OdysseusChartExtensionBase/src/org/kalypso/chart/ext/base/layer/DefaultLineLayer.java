package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.framework.impl.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.ITabularDataContainer;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
 * @author alibu
 */
public class DefaultLineLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
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

    final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
    final IStyledElement point = getStyle().getElement( SE_TYPE.POINT, 1 );

    line.setPath( path );
    line.paint( gc );
    point.setPath( path );
    point.paint( gc );

    gc.dispose();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GC gc )
  {
    final ITabularDataContainer<T_domain, T_target> dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();

      final T_domain[] domainData = dataContainer.getDomainValues();
      final T_target[] targetData = dataContainer.getTargetValues();

      IDataOperator dopDomain = getDomainAxis().getDataOperator( domainData[0].getClass() );
      IDataRange<Number> dr = getDomainAxis().getNumericRange();
      IDataOperator dopTarget = getTargetAxis().getDataOperator( targetData[0].getClass() );

      Number max = dr.getMax();
      Number min = dr.getMin();

      final ArrayList<Point> path = new ArrayList<Point>();
      final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
      final IStyledElement point = getStyle().getElement( SE_TYPE.POINT, 1 );
      ORIENTATION ori = getDomainAxis().getPosition().getOrientation();
      for( int i = 0; i < domainData.length; i++ )
      {
        // nur zeichnen, wenn linie innerhalb des Sichtbarkeitsintervalls liegt
        boolean setPoint = false;
        final T_domain domVal = domainData[i];

        if( dopDomain.logicalToNumerical( domVal ).doubleValue() > min.doubleValue() && dopDomain.logicalToNumerical( domVal ).doubleValue() < max.doubleValue() )
          setPoint = true;
        else if( dopDomain.logicalToNumerical( domVal ).doubleValue() < min.doubleValue() && i < domainData.length - 1 )
        {
          T_domain next = domainData[i + 1];
          if( dopDomain.logicalToNumerical( next ).doubleValue() > min.doubleValue() )
            setPoint = true;
        }
        else if( dopDomain.logicalToNumerical( domVal ).doubleValue() > max.doubleValue() && i > 0 )
        {
          T_domain prev = domainData[i - 1];
          if( dopDomain.logicalToNumerical( prev ).doubleValue() < max.doubleValue() )
            setPoint = true;

        }

        if( setPoint )
        {
          final T_target targetVal = targetData[i];
          final int domScreen = getDomainAxis().numericToScreen( dopDomain.logicalToNumerical( domVal ) );
          final int valScreen = getTargetAxis().numericToScreen( dopTarget.logicalToNumerical( targetVal ) );
          // Koordinaten switchen
          Point unswitched = new Point( domScreen, valScreen );
          path.add( new Point( ori.getX( unswitched ), ori.getY( unswitched ) ) );
        }
      }

      line.setPath( path );
      line.paint( gc );
      point.setPath( path );
      point.paint( gc );
    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
  }

  @Override
  public ITabularDataContainer<T_domain, T_target> getDataContainer( )
  {
    return (ITabularDataContainer<T_domain, T_target>) super.getDataContainer();
  }

}
