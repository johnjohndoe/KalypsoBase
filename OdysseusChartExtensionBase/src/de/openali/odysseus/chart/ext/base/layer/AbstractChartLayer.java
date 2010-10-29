package de.openali.odysseus.chart.ext.base.layer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.ImageData;

import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private boolean m_isVisible = true;

  private String m_title = null;

  private String m_id = "";

  private String m_description = "";

  private boolean m_isActive = false;

  private final LayerEventHandler m_handler = new LayerEventHandler();

  /**
   * hash map to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private ICoordinateMapper m_coordinateMapper;

  private ILegendEntry[] m_legendEntries;

  private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<String, IRetinalMapper>();

  private boolean m_legend = true;

  @Override
  public void addListener( final ILayerEventListener l )
  {
    m_handler.addListener( l );
  }

  public void addMapper( final String role, final IRetinalMapper mapper )
  {
    m_mapperMap.put( role, mapper );
  }

  protected abstract ILegendEntry[] createLegendEntries( );

  @Override
  public final ICoordinateMapper getCoordinateMapper( )
  {
    return m_coordinateMapper;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#getData()
   */
  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * convenience method; same as getCoordinateMapper().getDomainAxis()
   */
  protected IAxis getDomainAxis( )
  {
    return getCoordinateMapper().getDomainAxis();
  }

  public LayerEventHandler getEventHandler( )
  {
    return m_handler;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getId()
   */
  @Override
  public String getId( )
  {
    return m_id;
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( m_legendEntries == null )
      m_legendEntries = createLegendEntries();
    return m_legendEntries;
  }

  protected IRetinalMapper getMapper( final String role )
  {
    return m_mapperMap.get( role );
  }

  public Map<String, ImageData> getSymbolMap( )
  {
    return null;
  }

  /**
   * convenience method; same as getCoordinateMapper().getTargetAxis()
   */
  protected IAxis getTargetAxis( )
  {
    return getCoordinateMapper().getTargetAxis();
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTitle()
   */
  @Override
  public String getTitle( )
  {
    return m_title;
  }

  @Override
  public void init( )
  {

  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  @Override
  public boolean isActive( )
  {
    return m_isActive;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getVisibility()
   */
  @Override
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  @Override
  public void removeListener( final ILayerEventListener l )
  {
    m_handler.removeListener( l );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#isActive()
   */
  @Override
  public void setActive( final boolean isActive )
  {
    if( m_isActive != isActive )
    {
      m_isActive = isActive;
      getEventHandler().fireActiveLayerChanged( this );
    }
  }

  @Override
  public void setCoordinateMapper( final ICoordinateMapper coordinateMapper )
  {
    m_coordinateMapper = coordinateMapper;
  }

  /**
   * @see org.kalypso.chart.framework.model.layer.IChartLayer#setData()
   */
  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setDescription(java.lang.String)
   */
  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setID(java.lang.String)
   */
  @Override
  public void setId( final String id )
  {
    m_id = id;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setTitle(java.lang.String)
   */
  @Override
  public void setTitle( final String title )
  {
    m_title = title;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  @Override
  public void setVisible( final boolean isVisible )
  {
    if( m_isVisible != isVisible )
    {
      m_isVisible = isVisible;
      m_handler.fireLayerVisibilityChanged( this );
    }
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return String.format( "IChartLayer - id: %s", getId() );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#isLegend()
   */
  @Override
  public boolean isLegend( )
  {
    return m_legend;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#setLegend(boolean)
   */
  @Override
  public void setLegend( final boolean isVisible )
  {
    m_legend = isVisible;
  }

}
