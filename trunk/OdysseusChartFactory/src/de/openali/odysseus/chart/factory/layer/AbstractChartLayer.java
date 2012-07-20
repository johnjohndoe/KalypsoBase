package de.openali.odysseus.chart.factory.layer;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.kalypso.commons.java.lang.Objects;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;

import de.openali.odysseus.chart.framework.OdysseusChartExtensions;
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
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor2;
import de.openali.odysseus.chart.framework.model.layer.manager.LayerManager;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.IRetinalMapper;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private ICoordinateMapper m_coordinateMapper;

  private final Set<IChartLayerFilter> m_filters = new LinkedHashSet<IChartLayerFilter>();

  /**
   * hash map to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<String, Object>();

  private String m_description = "";

  private final LayerEventHandler m_eventHandler = new LayerEventHandler();

  private String m_identifier = "";

  private boolean m_isActive = false;

  private boolean m_isVisible = true;

  private boolean m_isAutoScale = true;

  private final ILayerManager m_layerManager = new LayerManager( this );

  private boolean m_legendIsVisible = true;

  private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<String, IRetinalMapper>();

  private final ILayerProvider m_provider;

  private String m_title = null;

  private IStyleSet m_styleSet;

  private ILayerContainer m_parent;

  final ILayerManagerEventListener m_layerManagerListener = new ILayerManagerEventListener()
  {
    @Override
    public void onActivLayerChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerAdded( final IChartLayer layer )
    {
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerContentChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerMoved( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
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
  };

  public AbstractChartLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    m_provider = provider;
    m_layerManager.addListener( m_layerManagerListener );

    setFilters();
    if( styleSet == null )
    {
      m_styleSet = new StyleSet();
    }
    else
    {
      m_styleSet = styleSet;
    }
  }

  @Override
  public void init( )
  {
    // TODO Auto-generated method stub
    // FIXME remove from here, this is an abstract class
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
  public void addListener( final ILayerEventListener listener )
  {
    m_eventHandler.addListener( listener );
  }

  public void addMapper( final String role, final IRetinalMapper mapper )
  {
    m_mapperMap.put( role, mapper );
  }

  @Override
  public void dispose( )
  {
    m_layerManager.clear();
  }

  @Override
  public final ICoordinateMapper getCoordinateMapper( )
  {
    return m_coordinateMapper;
  }

  @Override
  public Object getData( final String id )
  {
    return m_data.get( id );
  }

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
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
  @Override
  public IDataRange< ? > getDomainRange( )
  {
    return null;
  }

  public LayerEventHandler getEventHandler( )
  {
    return m_eventHandler;
  }

  @Override
  public IChartLayerFilter[] getFilters( )
  {
    return m_filters.toArray( new IChartLayerFilter[] {} );
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public ILayerManager getLayerManager( )
  {
    return m_layerManager;
  }

  @Override
  // TODO:FIXME remove from here. This is an abstract class
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    return new ILegendEntry[] {};
  }

  protected IRetinalMapper getMapper( final String role )
  {
    return m_mapperMap.get( role );
  }

  @Override
  public IChartModel getModel( )
  {
    return getParent().getModel();
  }

  @Override
  public ILayerContainer getParent( )
  {
    return m_parent;
  }

  @Override
  public ILayerProvider getProvider( )
  {
    return m_provider;
  }

  protected final <T extends IStyle> T getStyle( final Class<T> clazz )
  {
    return getStyle( clazz, 0 );
  }

  public <T extends IStyle> T getStyle( final Class<T> clazz, final int index )
  {
    final StyleSetVisitor visitor = new StyleSetVisitor( false );
    return visitor.visit( getStyleSet(), clazz, index );
  }

  protected IStyleSet getStyleSet( )
  {
    return m_styleSet;
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
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > intervall )
  {
    return null;
  }

  /**
   * @see org.kalypso.swtchart.chart.layer.IChartLayer#getTitle()
   */
  @Override
  public String getTitle( )
  {
    return m_title;
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
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#isAutoScale()
   */
  @Override
  public boolean isAutoScale( )
  {
    return m_isAutoScale;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#isLegend()
   */
  @Override
  public boolean isLegend( )
  {
    return m_legendIsVisible;
  }

  @Override
  public boolean isVisible( )
  {
    return m_isVisible;
  }

  /**
   * Default implementation does nothing.
   */
  @Override
  public void paint( final GC gc, final IProgressMonitor monitor )
  {
  }

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
  public void removeListener( final ILayerEventListener listener )
  {
    m_eventHandler.removeListener( listener );
  }

  @Override
  public void setActive( final boolean isActive )
  {
    if( m_isActive != isActive )
    {
      m_isActive = isActive;
      getEventHandler().fireActiveLayerChanged( this );
    }
  }

  protected void setAutoScale( final boolean isAutoScale )
  {
    m_isAutoScale = isAutoScale;
  }

  @Override
  public final void setCoordinateMapper( final ICoordinateMapper coordinateMapper )
  {
    m_coordinateMapper = coordinateMapper;
  }

  @Override
  public void setData( final String id, final Object data )
  {
    m_data.put( id, data );
  }

  @Override
  public void setDescription( final String description )
  {
    m_description = description;
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
      final IChartLayerFilter filter = OdysseusChartExtensions.createFilter( reference );
      if( Objects.isNotNull( filter ) )
        addFilter( filter );
    }
  }

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

  @Override
  public void setParent( final ILayerContainer parent )
  {
    m_parent = parent;
  }

  protected void setStyleSet( final IStyleSet styleSet )
  {
    m_styleSet = styleSet;
  }

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
  public void accept( final IChartLayerVisitor2 visitor )
  {
    final boolean doRecurse = visitor.visit( this );
    if( !doRecurse )
      return;

    final IChartLayer[] children = getLayerManager().getLayers();
    for( final IChartLayer child : children )
      child.accept( visitor );
  }
}