package org.kalypso.chart.ext.test.layer;

import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.chart.ext.base.data.AbstractDomainValueData;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.ITabularDataContainer;
import org.kalypso.chart.framework.model.layer.AbstractEditableChartLayer;
import org.kalypso.chart.framework.model.layer.EditInfo;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu
 */
public class EditableLineLayer<T_domain, T_target> extends AbstractEditableChartLayer<T_domain, T_target>
{

  // private AbstractDomainValueData<T_domain, T_target> m_dataContainer;

  private T_target[] m_targetData;

  private T_domain[] m_domainData;

  public EditableLineLayer( AbstractDomainValueData<T_domain, T_target> dataContainer, IAxis<T_domain> domainAxis, IAxis<T_target> targetAxis )
  {
    super( domainAxis, targetAxis );
    setDataContainer( dataContainer );

    if( dataContainer != null )
    {
      dataContainer.open();
      m_domainData = dataContainer.getDomainValues();
      m_targetData = dataContainer.getTargetValues();
    }
    else
    {
      Logger.logWarning( Logger.TOPIC_LOG_GENERAL, "Layer " + getId() + " has not yet been opened" );
    }
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

    final ArrayList<Point> path = new ArrayList<Point>();
    final IStyledElement line = getStyle().getElement( SE_TYPE.LINE, 1 );
    for( int i = 0; i < m_domainData.length; i++ )
    {
      final T_domain domVal = m_domainData[i];
      final T_target targetVal = m_targetData[i];
      final int domScreen = getDomainAxis().logicalToScreen( domVal );
      final int valScreen = getTargetAxis().logicalToScreen( targetVal );
      path.add( new Point( domScreen, valScreen ) );
    }
    line.setPath( path );
    line.paint( gc );
  }

  @Override
  public ITabularDataContainer<T_domain, T_target> getDataContainer( )
  {
    return (ITabularDataContainer<T_domain, T_target>) super.getDataContainer();
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#getEditInfo(org.eclipse.swt.graphics.Point)
   */
  public EditInfo getEditInfo( Point p )
  {
    // Umrechnen von screen nach logisch

    // falls der Punkt in den Daten vorhanden ist, dann eine Info zurückgeben
    return null;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#alwaysAllowsEditing()
   */
  public boolean alwaysAllowsEditing( )
  {
    // TODO Auto-generated method stub
    return true;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#edit(org.eclipse.swt.graphics.Point,
   *      java.lang.Object)
   */
  public void edit( Point point, Object data )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IEditableChartLayer#setActivePoint(java.lang.Object)
   */
  public void setActivePoint( Object data )
  {
    // TODO Auto-generated method stub

  }

}
