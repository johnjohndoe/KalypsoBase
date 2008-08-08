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

  private String m_title = "";

  private String m_id = "";

  private String m_description = "";

  private boolean m_isActive = false;

  private final LayerEventHandler m_handler = new LayerEventHandler();

  /**
   * Hashmap to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private ICoordinateMapper m_coordinateMapper;

  private ILegendEntry[] m_legendEntries;

  private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<String, IRetinalMapper>();

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getDescription()
   */
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
   * convenience method; same as getCoordinateMapper().getTargetAxis()
   */
  protected IAxis getTargetAxis( )
  {
    return getCoordinateMapper().getTargetAxis();
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
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#setVisibility(boolean)
   */
  public void setVisible( final boolean isVisible )
  {
    m_isVisible = isVisible;
    m_handler.fireLayerVisibilityChanged( this );
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

  // /**
  // * @see org.kalypso.chart.framework.model.layer.IChartLayer#getDomainRange()
  // */
  // public IDataRange<Number> getDomainRange( )
  // {
  //
  // IDataRange logRange = getDataContainer().getDomainRange();
  // Object min = logRange.getMin();
  // if( min != null )
  // {
  // IDataOperator dop = getDomainAxis().getDataOperator( min.getClass() );
  // return new ComparableDataRange<Number>( new Number[] { dop.logicalToNumeric( min ), dop.logicalToNumeric(
  // logRange.getMax() ) } );
  // }
  // return null;
  // }

  // /**
  // * @see org.kalypso.chart.framework.model.layer.IChartLayer#getTargetRange()
  // */
  // public IDataRange<Number> getTargetRange( )
  // {
  //
  // IDataRange logRange = getDataContainer().getTargetRange();
  // Object min = logRange.getMin();
  // if( min != null )
  // {
  // IDataOperator dop = getDomainAxis().getDataOperator( min.getClass() );
  // return new ComparableDataRange<Number>( new Number[] { dop.logicalToNumeric( min ), dop.logicalToNumeric(
  // logRange.getMax() ) } );
  // }
  // return null;
  // }

  public final void setCoordinateMapper( ICoordinateMapper coordinateMapper )
  {
    m_coordinateMapper = coordinateMapper;
  }

  public final ICoordinateMapper getCoordinateMapper( )
  {
    return m_coordinateMapper;
  }

  public Map<String, ImageData> getSymbolMap( )
  {
    return null;
  }

  public void init( )
  {

  }

  protected abstract ILegendEntry[] createLegendEntries( );

  public synchronized ILegendEntry[] getLegendEntries( )
  {
    if( m_legendEntries == null )
      m_legendEntries = createLegendEntries();
    return m_legendEntries;
  }

  public void addMapper( String role, IRetinalMapper mapper )
  {
    m_mapperMap.put( role, mapper );
  }

  protected IRetinalMapper getMapper( String role )
  {
    return m_mapperMap.get( role );
  }

}
