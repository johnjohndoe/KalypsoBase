package de.openali.odysseus.chart.factory.layer;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private ICoordinateMapper m_coordinateMapper;

  /**
   * hash map to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private String m_description = "";

  private final LayerEventHandler m_handler = new LayerEventHandler();

  private String m_id = "";

  private boolean m_isActive = false;

  private boolean m_isVisible = true;

  private final ILayerManager m_layerManager = new LayerManager( this );

  private ILegendEntry[] m_legendEntries = new ILegendEntry[] {};

  private boolean m_legendIsVisible = true;

  private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<String, IRetinalMapper>();

  private final ILayerProvider m_provider;

  private String m_title = null;

  private ILayerContainer m_parent;

  public AbstractChartLayer( final ILayerProvider provider )
  {
    m_provider = provider;
  }

  @Override
  public void addListener( final ILayerEventListener l )
  {
    m_handler.addListener( l );
  }

  public void addMapper( final String role, final IRetinalMapper mapper )
  {
    m_mapperMap.put( role, mapper );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#createLegendEntries()
   */
  protected ILegendEntry[] createLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    m_layerManager.clear();
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer#addLayer(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  protected final void drawClippingRect( final GC gc )
  {
    final Color col = new Color( gc.getDevice(), new RGB( 0, 0, 0 ) );
    try
    {
      gc.setForeground( col );
      final Rectangle clipping = gc.getClipping();
      gc.setLineWidth( 1 );
      gc.drawRectangle( clipping.x, clipping.y, clipping.width - 1, clipping.height - 1 );
      gc.setClipping( clipping.x + 1, clipping.y + 1, clipping.width - 2, clipping.height - 2 );
    }
    finally
    {
      col.dispose();
    }
  }

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
    return getCoordinateMapper() == null ? null : getCoordinateMapper().getDomainAxis();
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    Double min = null;
    Double max = null;
    for( final IChartLayer layer : getLayerManager().getLayers() )
    {

      final IDataRange<Number> dr = layer.getDomainRange();
      if( dr != null )
      {
        if( max == null )
          max = dr.getMax().doubleValue();
        else
          max = Math.max( max, dr.getMax().doubleValue() );
        if( min == null )
          min = dr.getMin().doubleValue();
        else
          min = Math.min( min, dr.getMin().doubleValue() );
      }
    }
    if( (min == null) || (max == null) )
      return null;
    return new DataRange<Number>( min, max );
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
    if( ArrayUtils.isEmpty( m_legendEntries ) )
      m_legendEntries = createLegendEntries();

    return m_legendEntries;
  }

  protected IRetinalMapper getMapper( final String role )
  {
    return m_mapperMap.get( role );
  }

  @Override
  public ILayerProvider getProvider( )
  {
    return m_provider;
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
    if( getCoordinateMapper() == null )
      return null;
    return getCoordinateMapper().getTargetAxis();
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.AbstractProfilLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    Double min = null;
    Double max = null;
    for( final IChartLayer layer : getLayerManager().getLayers() )
    {
      final IDataRange<Number> dr = layer.getTargetRange( null );
      if( dr != null )
      {
        if( max == null )
          max = dr.getMax().doubleValue();
        else
          max = Math.max( max, dr.getMax().doubleValue() );
        if( min == null )
          min = dr.getMin().doubleValue();
        else
          min = Math.min( min, dr.getMin().doubleValue() );
      }
    }
    if( (min == null) || (max == null) )
      return null;
    return new DataRange<Number>( min, max );
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
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#isLegend()
   */
  @Override
  public boolean isLegend( )
  {
    return m_legendIsVisible;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#isVisible()
   */
  @Override
  public boolean isVisible( )
  {
    final ILayerManager layerManager = getLayerManager();
    final IChartLayer[] layers = layerManager.getLayers();
    for( final IChartLayer layer : layers )
    {
      if( layer.isVisible() )
        return true;
    }

    return m_isVisible;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final IChartLayer[] layers = getLayerManager().getLayers();
    ArrayUtils.reverse( layers );

    for( final IChartLayer layer : layers )
    {
      if( layer.isVisible() )
        layer.paint( gc );
    }
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

    // FIXME sure? update coordinate mapper of child layers, too?
    // Test:kim
// for( final IChartLayer layer : getLayerManager().getLayers() )
// {
// layer.setCoordinateMapper( coordinateMapper );
// }
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
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#setLegend(boolean)
   */
  @Override
  public void setLegend( final boolean isVisible )
  {
    m_legendIsVisible = isVisible;
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

  @Override
  public ILayerManager getLayerManager( )
  {
    return m_layerManager;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#setParent(java.lang.Object)
   */
  @Override
  public void setParent( final ILayerContainer parent )
  {
    m_parent = parent;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getParent()
   */
  @Override
  public ILayerContainer getParent( )
  {
    return m_parent;
  }
}
