package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.layer.AbstractChartLayer;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;
import org.kalypso.observation.result.TupleResult;

// TODO why do we still have several tuple result layer?
// @Alex: please combine them to ONE implementation!
public class TupleResultLineLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{
  public TupleResultLineLayer( final TupleResultDomainValueData data, final IAxis<T_domain> domainAxis, final IAxis<T_target> targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( data );
  }

  public void drawIcon( final Image img )
  {
    final Rectangle bounds = img.getBounds();
    final int height = bounds.height;
    final int width = bounds.width;
    final GC gc = new GC( img );
    final GCWrapper gcw = new GCWrapper( gc );

    final ArrayList<Point> path = new ArrayList<Point>();

    path.add( new Point( 0, height / 2 ) );
    path.add( new Point( width / 5, height / 2 ) );
    path.add( new Point( width / 5 * 2, height / 4 ) );
    path.add( new Point( width / 5 * 3, height / 4 * 3 ) );
    path.add( new Point( width / 5 * 4, height / 2 ) );
    path.add( new Point( width, height / 2 ) );

    final IStyledElement element = getStyle().getElement( SE_TYPE.LINE, 1 );

    element.setPath( path );
    element.paint( gcw );

    gcw.dispose();
    gc.dispose();
  }

  public void paint( final GCWrapper gc )
  {
    final IStyledElement sl = getStyle().getElement( SE_TYPE.LINE, 0 );
    final IStyledElement sp = getStyle().getElement( SE_TYPE.POINT, 0 );
    final List<Point> path = new ArrayList<Point>();
    final TupleResultDomainValueData data = (TupleResultDomainValueData) getDataContainer();

    data.open();

    final TupleResult result = data.getTupleResult();

    Object[] xValues = null;
    Object[] yValues = null;
    IAxis xAxis = null;
    IAxis yAxis = null;

    if( getDomainAxis().getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      xValues = data.getDomainValues();
      yValues = data.getTargetValues();
      xAxis = getDomainAxis();
      yAxis = getTargetAxis();
    }
    else
    {
      yValues = data.getDomainValues();
      xValues = data.getTargetValues();
      yAxis = getDomainAxis();
      xAxis = getTargetAxis();
    }

    if( xValues.length > 0 && yValues.length > 0 )
    {
      for( int i = 0; i < result.size(); i++ )
      {
        final Object xObj = xValues[i];
        final Object yObj = yValues[i];
        // we have to check if all values are correct - an incorrect value means a null value - the axis would return 0
        // in that case
        if( xObj != null && yObj != null )
        {
          final int x = xAxis.logicalToScreen( xObj );
          final int y = yAxis.logicalToScreen( yObj );
          path.add( new Point( x, y ) );
        }
      }
    }

    sl.setPath( path );
    sl.paint( gc );
    sp.setPath( path );
    sp.paint( gc );
  }

}
