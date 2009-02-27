package org.kalypso.chart.ext.observation.layer;

import org.eclipse.swt.graphics.Image;
import org.kalypso.chart.ext.base.layer.AbstractChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

public class TupleResultBarLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{

  public TupleResultBarLayer( IAxis<T_domain> domainAxis, IAxis<T_target> targetAxis )
  {
    super( domainAxis, targetAxis );
  }

  public void drawIcon( Image img )
  {
    // TODO Auto-generated method stub

  }

  public void paint( GCWrapper gc )
  {
    // TODO Auto-generated method stub

  }

}
