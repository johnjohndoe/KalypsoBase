package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.chart.framework.model.layer.AbstractChartLayer;
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

  public void drawIcon( final Image img, final int width, final int height )
  {
    // TODO Auto-generated method stub
  }

  public void paint( final GCWrapper gc )
  {
    final Rectangle c = gc.getClipping();
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
        final int x = xAxis.logicalToScreen( xValues[i] );
        final int y = yAxis.logicalToScreen( yValues[i] );
        path.add( new Point( x, y ) );
      }
    }

    sl.setPath( path );
    sl.paint( gc );
    sp.setPath( path );
    sp.paint( gc );

  }

}
