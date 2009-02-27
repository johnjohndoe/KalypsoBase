package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;
import java.util.Calendar;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.data.AbstractDomainIntervalValueData;
import org.kalypso.chart.framework.exception.ZeroSizeDataRangeException;
import org.kalypso.chart.framework.impl.model.data.DataRange;
import org.kalypso.chart.framework.model.data.IDataOperator;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.ICoordinateMapper;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DATATYPE;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
 * @author alibu
 */
public class DefaultBarLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
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
    final IStyledElement element = getStyle().getElement( SE_TYPE.POLYGON, 1 );

    ArrayList<Point> path = new ArrayList<Point>();
    path.add( new Point( 0, height ) );
    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 2, height / 2 ) );
    path.add( new Point( width / 2, height ) );
    element.setPath( path );
    element.paint( gc );

    path = new ArrayList<Point>();
    path.add( new Point( width / 2, height ) );
    path.add( new Point( width / 2, 0 ) );
    path.add( new Point( width, 0 ) );
    path.add( new Point( width, height ) );
    element.setPath( path );
    element.paint( gc );

    gc.dispose();

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC,
   *      org.eclipse.swt.graphics.Device)
   */
  @SuppressWarnings("unchecked")
  public void paint( GC gc )
  {
    AbstractDomainIntervalValueData<T_domain, T_target> dataContainer = getDataContainer();

    if( dataContainer != null )
    {
      dataContainer.open();
      final T_domain[] domainStartComponent = dataContainer.getDomainDataIntervalStart();
      final T_domain[] domainEndComponent = dataContainer.getDomainDataIntervalEnd();
      final T_target[] targetComponent = dataContainer.getTargetValues();

      final ArrayList<Point> path = new ArrayList<Point>();
      final IStyledElement poly = getStyle().getElement( SE_TYPE.POLYGON, 1 );

      IDataOperator dopDomain = getCoordinateMapper().getDomainAxis().getDataOperator( domainStartComponent[0].getClass() );
      IDataOperator dopTarget = getCoordinateMapper().getTargetAxis().getDataOperator( targetComponent[0].getClass() );

      ICoordinateMapper cm = getCoordinateMapper();

      for( int i = 0; i < domainStartComponent.length; i++ )
      {

        final T_domain startValue = domainStartComponent[i];
        final T_domain endValue = domainEndComponent[i];
        final T_target targetValue = targetComponent[i];

        Point p1 = cm.numericToScreen( dopDomain.logicalToNumerical( startValue ), 0 );
        Point p2 = cm.numericToScreen( dopDomain.logicalToNumerical( startValue ), dopTarget.logicalToNumerical( targetValue ) );
        Point p3 = cm.numericToScreen( dopDomain.logicalToNumerical( endValue ), dopTarget.logicalToNumerical( targetValue ) );
        Point p4 = cm.numericToScreen( dopDomain.logicalToNumerical( endValue ), 0 );

        path.add( p1 );
        path.add( p2 );
        path.add( p3 );
        path.add( p4 );

        poly.setPath( path );
        poly.paint( gc );
      }
    }
  }

  @Override
  public AbstractDomainIntervalValueData<T_domain, T_target> getDataContainer( )
  {
    return (AbstractDomainIntervalValueData<T_domain, T_target>) super.getDataContainer();
  }

  @Override
  public IDataRange<Number> getTargetRange( )
  {
    // muss als minimalen Wert 0 zurückgeben, weil die Bars bis dahin laufen

    IDataRange<T_target> targetRange = getDataContainer().getTargetRange();
    DATATYPE dataType = getTargetAxis().getDataType();
    IDataOperator dop = null;
    if( dataType.equals( "STRING" ) )
      dop = getTargetAxis().getDataOperator( String.class );
    else if( dataType.equals( "DATE" ) )
      dop = getTargetAxis().getDataOperator( Calendar.class );
    else
      dop = getTargetAxis().getDataOperator( Number.class );

    try
    {
      return new DataRange<Number>( 0, dop.logicalToNumerical( targetRange.getMax() ) );
    }
    catch( ZeroSizeDataRangeException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }
}
