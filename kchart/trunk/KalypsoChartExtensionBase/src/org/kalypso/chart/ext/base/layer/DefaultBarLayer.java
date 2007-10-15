package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueData;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.data.impl.ComparableDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 */
public class DefaultBarLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{

  private final AbstractDomainIntervalValueData<T_domain, T_target> m_dataContainer;

  public DefaultBarLayer( AbstractDomainIntervalValueData<T_domain, T_target> dataContainer, IAxis<T_domain> domAxis, IAxis<T_target> valAxis )
  {
    super( domAxis, valAxis );
    m_dataContainer = dataContainer;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#drawIcon(org.eclipse.swt.graphics.Image, int, int)
   */
  public void drawIcon( Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );
    final IStyledElement element = getStyle().getElement( SE_TYPE.POLYGON, 1 );

    ArrayList<Point> path = new ArrayList<Point>();
    path.add( new Point( 0, height ) );
    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 2, height / 2 ) );
    path.add( new Point( width / 2, height ) );
    element.setPath( path );
    element.paint( gcw );

    path = new ArrayList<Point>();
    path.add( new Point( width / 2, height ) );
    path.add( new Point( width / 2, 0 ) );
    path.add( new Point( width, 0 ) );
    path.add( new Point( width, height ) );
    element.setPath( path );
    element.paint( gcw );

    gcw.dispose();
    gc.dispose();

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainRange()
   */
  public IDataRange<T_domain> getDomainRange( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetRange()
   */
  public IDataRange<T_target> getTargetRange( )
  {
    if( m_dataContainer != null )
    {
      m_dataContainer.open();
      return new ComparableDataRange<T_target>( m_dataContainer.getTargetValues() );
    }
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
  public void paint( GCWrapper gc )
  {
    if( m_dataContainer != null )
    {
      m_dataContainer.open();
      final T_domain[] domainStartComponent = m_dataContainer.getDomainDataIntervalStart();
      final T_domain[] domainEndComponent = m_dataContainer.getDomainDataIntervalEnd();
      final T_target[] targetComponent = m_dataContainer.getTargetValues();

      final ArrayList<Point> path = new ArrayList<Point>();
      final IStyledElement poly = getStyle().getElement( SE_TYPE.POLYGON, 1 );

      for( int i = 0; i < domainStartComponent.length; i++ )
      {

        final T_domain startValue = domainStartComponent[i];
        final T_domain endValue = domainEndComponent[i];
        final T_target targetValue = targetComponent[i];

        final IAxis<T_domain> domainAxis = getDomainAxis();
        final IAxis<T_target> targetAxis = getTargetAxis();

        if( domainAxis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
        {
          path.add( new Point( domainAxis.logicalToScreen( startValue ), targetAxis.zeroToScreen() ) );
          path.add( new Point( domainAxis.logicalToScreen( startValue ), targetAxis.logicalToScreen( targetValue ) ) );
          path.add( new Point( domainAxis.logicalToScreen( endValue ), targetAxis.logicalToScreen( targetValue ) ) );
          path.add( new Point( domainAxis.logicalToScreen( endValue ), targetAxis.zeroToScreen() ) );
        }
        else
        {
          path.add( new Point( targetAxis.zeroToScreen(), domainAxis.logicalToScreen( startValue ) ) );
          path.add( new Point( targetAxis.logicalToScreen( targetValue ), domainAxis.logicalToScreen( startValue ) ) );
          path.add( new Point( targetAxis.logicalToScreen( targetValue ), domainAxis.logicalToScreen( endValue ) ) );
          path.add( new Point( targetAxis.zeroToScreen(), domainAxis.logicalToScreen( endValue ) ) );
        }
        poly.setPath( path );
        poly.paint( gc );
      }
    }
  }
}
