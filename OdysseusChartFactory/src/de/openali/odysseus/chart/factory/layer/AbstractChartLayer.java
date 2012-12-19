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
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
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
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author alibu
 */
public abstract class AbstractChartLayer implements IChartLayer
{
  private ICoordinateMapper< ? , ? > m_coordinateMapper;

  private final Set<IChartLayerFilter> m_filters = new LinkedHashSet<>();

  /**
   * hash map to store arbitrary key value pairs
   */
  private final Map<String, Object> m_data = new HashMap<>();

  private String m_description = ""; //$NON-NLS-1$

  private final LayerEventHandler m_eventHandler = new LayerEventHandler();

  private String m_identifier = ""; //$NON-NLS-1$

  private boolean m_isActive = false;

  private boolean m_hideIfNoData = false;

  private boolean m_isVisible = true;

  private boolean m_isAutoScale = true;

  private final ILayerManager m_layerManager = new LayerManager( this );

  private boolean m_legendIsVisible = true;

  private final ILayerProvider m_provider;

  private String m_title = null;

  private IStyleSet m_styleSet;

  // private final Map<String, IRetinalMapper> m_mapperMap = new HashMap<>();

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
      // TODO: check: strange: why is the content of this layer changed if a sub-layer is added
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this, ContentChangeType.all );
    }

    @Override
    public void onLayerContentChanged( final IChartLayer layer, final ContentChangeType type )
    {
      // TODO: check: strange: why is the content of this layer changed if a sub-layer is changed?

      // TODO: shouldn't we give layer as argument instead of this?
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this, ContentChangeType.all );
    }

    @Override
    public void onLayerMoved( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }

    @Override
    public void onLayerRemoved( final IChartLayer layer )
    {
      // TODO: check: strange: why is trhe content of this layer changed if a sub-layer is removed
      getEventHandler().fireLayerContentChanged( AbstractChartLayer.this, ContentChangeType.all );
    }

    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer )
    {
      getEventHandler().fireLayerVisibilityChanged( AbstractChartLayer.this );
    }

    @Override
    public void redrawRequested( )
    {
      // Do nothing
      // ChartImageComposite SWT.redraw
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
  public void accept( final IChartLayerVisitor2 visitor )
  {
    final boolean doRecurse = visitor.visit( this );
    if( !doRecurse )
      return;

    final boolean direction = visitor.getVisitDirection();

    final IChartLayer[] children = getLayerManager().getLayers();
    if( !direction )
      ArrayUtils.reverse( children );

    for( final IChartLayer child : children )
      child.accept( visitor );
  }

  @Override
  public final void addFilter( final IChartLayerFilter... filters )
  {
    if( ArrayUtils.isEmpty( filters ) )
      return;

    Collections.addAll( m_filters, filters );
    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  @Override
  public void addListener( final ILayerEventListener listener )
  {
    m_eventHandler.addListener( listener );
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

//  public void addMapper( final String role, final IRetinalMapper mapper )
//  {
//    m_mapperMap.put( role, mapper );
//  }

  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * convenience method; same as getCoordinateMapper().getDomainAxis()
   */
  protected IAxis< ? > getDomainAxis( )
  {
    return getCoordinateMapper() == null ? null : getCoordinateMapper().getDomainAxis();
  }

  /**
   * Always returns null: Important: do not recurse into children, they may have different axes, so merging those ranges
   * is just wrong.<br/>
   * The caller of getDomainRange is responsible for recursion.
   */
//  @Override
//  public IDataRange<Number> getDomainRange( )
//  {
//    return null;
//  }

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

  @Override
  public IChartModel getModel( )
  {
    return getParent().getModel();
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  protected IDataRange<Double> getNumericRange( final IAxis axis, final IDataRange logicalRange )
  {
    if( logicalRange == null || axis == null || logicalRange.getMin() == null )
      return new DataRange<>( null, null );
    final Class< ? > clazz = axis.getDataClass();
    final Class< ? > dataClass = logicalRange.getMin().getClass();
    try
    {
      final Double min = axis.logicalToNumeric( logicalRange.getMin() );
      final Double max = axis.logicalToNumeric( logicalRange.getMax() );
      return new DataRange<>( min, max );
    }
    catch( final ClassCastException e )
    {
      System.out.println( "axis " + axis.getIdentifier() + " expect " + clazz.getSimpleName() + ", " + dataClass.getSimpleName() + " given." ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      return new DataRange<>( null, null );
    }

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

//  protected IRetinalMapper getMapper( final String role )
//  {
//    return m_mapperMap.get( role );
//  }

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
  @SuppressWarnings( "rawtypes" )
  protected IAxis getTargetAxis( )
  {
    if( getCoordinateMapper() == null )
      return null;

    final ICoordinateMapper coordinateMapper = getCoordinateMapper();

    return coordinateMapper.getTargetAxis();
  }

  @Override
  public String getTitle( )
  {
    return m_title;
  }

  @Override
  public boolean hasData( )
  {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public void init( )
  {
    // TODO Auto-generated method stub
    // FIXME remove from here, this is an abstract class
  }

  @Override
  public boolean isActive( )
  {
    return m_isActive;
  }

  @Override
  public boolean isAutoScale( )
  {
    return m_isAutoScale;
  }

  @Override
  public boolean isHideIfNoData( )
  {
    return m_hideIfNoData;
  }

  @Override
  public boolean isLegend( )
  {
    return m_legendIsVisible;
  }

  @Override
  public boolean isVisible( )
  {
    if( m_hideIfNoData )
      return hasData() && m_isVisible;

    return m_isVisible;
  }

  /**
   * Default implementation does nothing.
   */
  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
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

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  @Override
  public void removeListener( final ILayerEventListener listener )
  {
    m_eventHandler.removeListener( listener );
  }

  @Override
  public boolean requireVisibleAxis( )
  {
    return true;
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
  public void setCoordinateMapper( final ICoordinateMapper coordinateMapper )
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

    getEventHandler().fireLayerContentChanged( this, ContentChangeType.value );
  }

  private void setFilters( )
  {
    if( Objects.isNull( m_provider ) )
      return;

    final IParameterContainer container = m_provider.getParameterContainer();
    if( Objects.isNull( container ) )
      return;

    final String property = container.getParameterValue( "filter", "" ); //$NON-NLS-1$ //$NON-NLS-2$
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
  public void setHideIfNoData( final boolean hideIfNoData )
  {
    m_hideIfNoData = hideIfNoData;
  }

  @Override
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
  }

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
    return String.format( "IChartLayer - id: %s, visible: %s", getIdentifier(), Boolean.valueOf( isVisible() ).toString() ); //$NON-NLS-1$
  }
}