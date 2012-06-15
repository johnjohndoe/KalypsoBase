package de.openali.odysseus.chart.factory.layer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import de.openali.odysseus.chart.framework.OdysseusChartFramework;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;
import de.openali.odysseus.chart.framework.model.mapper.IScreenAxis;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private ICoordinateMapper m_coordinateMapper;

  Set<IChartLayerFilter> m_filters = new LinkedHashSet<IChartLayerFilter>();

  /**
   * hash map to store arbitrary key value pairs
   */
  private final HashMap<String, Object> m_data = new HashMap<String, Object>();

  private String m_description = "";

  private final LayerEventHandler m_eventHandler = new LayerEventHandler();

  private String m_identifier = "";

  private boolean m_isActive = false;

  private boolean m_isVisible = true;

  private final ILayerManager m_layerManager = new LayerManager( this );

  private ILegendEntry[] m_legendEntries = new ILegendEntry[] {};

  private boolean m_legendIsVisible = true;

  private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<String, IRetinalMapper>();

  private final ILayerProvider m_provider;

  private String m_title = null;

  private ILayerContainer m_parent;

  final ILayerManagerEventListener m_layerManagerListener = new ILayerManagerEventListener()
  {
    @Override
    public void onLayerMoved( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerAdded( final IChartLayer layer )
    {
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerRemoved( final IChartLayer layer )
    {
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerContentChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this );
    }

    @Override
    public void onActivLayerChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }
  };

  public AbstractChartLayer( final ILayerProvider provider )
  {
    m_provider = provider;
    m_layerManager.addListener( m_layerManagerListener );

    setFilters();
  }

  private void setFilters( )
  {
    if( Objects.isNull( m_provider ) )
      return;

    final IParameterContainer container = m_provider.getParameterContainer();
    if( Objects.isNull( container ) )
      return;

    final String property = container.getParameterValue( "filter", "" ); //$NON-NLS-1$
    if( Strings.isNullOrEmpty( property ) )
      return;

    final Iterable<String> filters = Splitter.on( ";" ).split( property ); //$NON-NLS-1$
    for( final String reference : filters )
    {
      final IChartLayerFilter filter = OdysseusChartFramework.getDefault().findFilter( reference );
      if( Objects.isNotNull( filter ) )
        addFilter( filter );
    }
  }

  @Override
  public void addListener( final ILayerEventListener listener )
  {
    m_eventHandler.addListener( listener );
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
    final LayerDomainRangeVisitor rangeVisitor = new LayerDomainRangeVisitor();
    getLayerManager().accept( rangeVisitor );
    return rangeVisitor.getRange();
  }

  public LayerEventHandler getEventHandler( )
  {
    return m_eventHandler;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getId()
   */
  @Override
  public String getIdentifier( )
  {
    return m_identifier;
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
  public IDataRange<Number> getTargetRange( final IDataRange<Number> intervall )
  {
    final LayerTargetRangeVisitor rangeVisitor = new LayerTargetRangeVisitor();
    getLayerManager().accept( rangeVisitor );
    return rangeVisitor.getRange();
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
    if( !m_isVisible )
      return false;

    final ICoordinateMapper mapper = getCoordinateMapper();
    if( mapper == null )
      return true;

    if( isNotVisible( mapper.getDomainAxis() ) )
      return false;

    if( isNotVisible( mapper.getTargetAxis() ) )
      return false;

    return true;
  }

  private boolean isNotVisible( final IAxis axis )
  {
    if( Objects.isNull( axis ) )
      return true;
    else if( axis instanceof IScreenAxis )
      return false;

    return !axis.isVisible();
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
  public void removeListener( final ILayerEventListener listener )
  {
    m_eventHandler.removeListener( listener );
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
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
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

  @Override
  public void setVisible( final boolean isVisible )
  {
    if( m_isVisible != isVisible )
    {
      m_isVisible = isVisible;

      m_eventHandler.fireLayerVisibilityChanged( this );
    }
  }

  @Override
  public String toString( )
  {
    return String.format( "IChartLayer - id: %s, visible: %s", getIdentifier(), Boolean.valueOf( isVisible() ).toString() );
  }

  @Override
  public ILayerManager getLayerManager( )
  {
    return m_layerManager;
  }

  @Override
  public void setParent( final ILayerContainer parent )
  {
    m_parent = parent;
  }

  @Override
  public ILayerContainer getParent( )
  {
    return m_parent;
  }

  @Override
  public final void addFilter( final IChartLayerFilter... filters )
  {
    if( ArrayUtils.isEmpty( filters ) )
      return;

    Collections.addAll( m_filters, filters );
    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public final void setFilter( final IChartLayerFilter... filters )
  {
    if( ArrayUtils.isEmpty( filters ) )
      return;

    m_filters.clear();
    Collections.addAll( m_filters, filters );

    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public IChartLayerFilter[] getFilters( )
  {
    return m_filters.toArray( new IChartLayerFilter[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#removeFilter(de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter[])
   */
  @Override
  public void removeFilter( final IChartLayerFilter... filters )
  {
    if( ArrayUtils.isEmpty( filters ) )
      return;

    for( final IChartLayerFilter filter : filters )
    {
      m_filters.remove( filter );
    }

    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public IChartModel getModel( )
  {
    return getParent().getModel();
  }

}
