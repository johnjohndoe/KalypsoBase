package org.kalypso.chart.ext.observation.layer;

import java.util.ArrayList;
import java.util.List;

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

public class TupleResultPointLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{

  public TupleResultPointLayer( TupleResultDomainValueData data, IAxis<T_domain> domainAxis, IAxis<T_target> targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( data );
  }

  public void drawIcon( Image img )
  {
    // TODO Auto-generated method stub

  }

  public void paint( GCWrapper gc )
  {
    final Rectangle c = gc.getClipping();
    final IStyledElement se = getStyle().getElement( SE_TYPE.POINT, 0 );
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
      for( int i = 0; i < result.size(); i++ )
      {
        final int x = xAxis.logicalToScreen( (T_domain) xValues[i] );
        final int y = yAxis.logicalToScreen( (T_target) yValues[i] );
        path.add( new Point( x, y ) );
      }
    se.setPath( path );
    se.paint( gc );

  }

}
