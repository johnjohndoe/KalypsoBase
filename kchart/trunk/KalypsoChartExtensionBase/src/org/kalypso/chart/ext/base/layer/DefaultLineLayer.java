package org.kalypso.chart.ext.base.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.data.AbstractDomainValueData;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.ITabularDataContainer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 */
public class DefaultLineLayer<T_domain, T_target> extends AbstractChartLayer<T_domain, T_target>
{

  // private AbstractDomainValueData<T_domain, T_target> m_dataContainer;

  public DefaultLineLayer( AbstractDomainValueData<T_domain, T_target> dataContainer, IAxis<T_domain> domainAxis, IAxis<T_target> targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( dataContainer );
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

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#paint(org.kalypso.contribs.eclipse.swt.graphics.GCWrapper,
   *      org.eclipse.swt.graphics.Device)
   */
  public void paint( GCWrapper gc )
  {
    final ITabularDataContainer<T_domain, T_target> dataContainer = getDataContainer();
    if( dataContainer != null )
    {
      dataContainer.open();
      final T_domain[] domainData = dataContainer.getDomainValues();
      final T_target[] targetData = dataContainer.getTargetValues();
      final ArrayList<Point> path = new ArrayList<Point>();
      final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
      for( int i = 0; i < domainData.length; i++ )
      {
        final T_domain domVal = domainData[i];
        final T_target targetVal = targetData[i];
        final int domScreen = getDomainAxis().logicalToScreen( domVal );
        final int valScreen = getTargetAxis().logicalToScreen( targetVal );
        path.add( new Point( domScreen, valScreen ) );
      }
      line.setPath( path );
      line.paint( gc );
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
