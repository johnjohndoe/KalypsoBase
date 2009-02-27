package org.kalypso.chart.ext.base.layer;

import java.util.HashMap;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.event.ILayerEventListener;
import org.kalypso.chart.framework.model.event.impl.LayerEventHandler;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.legend.ILegendItem;
import org.kalypso.chart.framework.model.legend.LegendItem;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.styles.ILayerStyle;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer<T_domain, T_target> implements IChartLayer<T_domain, T_target>
{
  private boolean m_isVisible = true;

  private ILayerStyle m_style = null;

  private String m_title = "";

  private String m_id = "";

  private String m_description = "";

  private boolean m_isActive = false;

  private final IAxis<T_target> m_targetAxis;

  private final IAxis<T_domain> m_domainAxis;

  private IDataContainer<T_domain, T_target> m_dataContainer = null;

  private final LayerEventHandler m_handler = new LayerEventHandler();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  public AbstractChartLayer( final IAxis<T_domain> domainAxis, final IAxis<T_target> targetAxis )
  {
    m_targetAxis = targetAxis;
    m_domainAxis = domainAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDomainAxis()
   */
  public IAxis<T_domain> getDomainAxis( )
  {
    return m_domainAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTitle()
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getId()
   */
  public String getId( )
  {
    return m_id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getStyle()
   */
  public ILayerStyle getStyle( )
  {
    return m_style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTargetAxis()
   */
  public IAxis<T_target> getTargetAxis( )
  {
    return m_targetAxis;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getVisibility()
   */
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public boolean isActive( )
  {
    return m_isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  public void setActive( final boolean isActive )
  {
    m_isActive = isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setDescription(java.lang.String)
   */
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setTitle(java.lang.String)
   */
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setID(java.lang.String)
   */
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setStyle(org.kalypso.swtchart.chart.styles.ILayerStyle)
   */
  public void setStyle( final ILayerStyle style )
  {
    m_style = style;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  public void setVisible( final boolean isVisible )
  {
    m_isVisible = isVisible;
    m_handler.fireLayerVisibilityChanged( this );
  }

  public IDataContainer<T_domain, T_target> getDataContainer( )
  {
    return m_dataContainer;
  }

  protected void setDataContainer( final IDataContainer<T_domain, T_target> data )
  {
    m_dataContainer = data;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getLegendItem() returns an empty Image
   */
  public ILegendItem getLegendItem( )
  {
    ILegendItem l = null;
    final Image img = new Image( Display.getCurrent(), 20, 20 );
    drawIcon( img );
    final ImageData id = img.getImageData();
    img.dispose();
    l = new LegendItem( null, getTitle(), id );
    return l;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  public void setData( String id, Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  public Object getData( String id )
  {
    return m_data.get( id );
  }

  public void addListener( ILayerEventListener l )
  {
    m_handler.addListener( l );
  }

  public void removeListener( ILayerEventListener l )
  {
    m_handler.removeListener( l );
  }

  public LayerEventHandler getEventHandler( )
  {
    return m_handler;
  }
}
