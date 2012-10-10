package de.openali.odysseus.chart.framework.model.layer.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.commons.exception.CancelVisitorException;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.ILayerContainer;
import de.openali.odysseus.chart.framework.model.event.ILayerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.LayerManagerEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindAxisLayerVisitor;
import de.openali.odysseus.chart.framework.model.layer.manager.visitors.FindLayerVisitor;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;

public class LayerManager implements ILayerManager
{
  final LayerManagerEventHandler m_handler = new LayerManagerEventHandler();

  private final ILayerEventListener m_layerListener = new AbstractLayerEventListener()
  {
    @Override
    public void onActiveLayerChanged( final IChartLayer layer )
    {
      m_handler.fireActiveLayerChanged( layer );

      final LayerManagerEventHandler parentHandler = findParentHandler();
      if( parentHandler != null )
        parentHandler.fireActiveLayerChanged( (IChartLayer)getContainer() );
    }

    private LayerManagerEventHandler findParentHandler( )
    {
      final ILayerContainer container = getContainer();
      if( container != null )
      {
        final ILayerContainer parent = container.getParent();
        if( parent != null )
        {
          final ILayerManager parentLayerManager = parent.getLayerManager();
          return parentLayerManager.getEventHandler();
        }
      }

      return null;
    }

    @Override
    public void onLayerContentChanged( final IChartLayer layer, final ContentChangeType type )
    {
      m_handler.fireLayerContentChanged( layer, type );

      final LayerManagerEventHandler parentHandler = findParentHandler();
      if( parentHandler != null )
        parentHandler.fireLayerContentChanged( (IChartLayer)getContainer(), type );
    }

    @Override
    public void onLayerVisibilityChanged( final IChartLayer layer )
    {
      m_handler.fireLayerVisibilityChanged( layer );

      final LayerManagerEventHandler parentHandler = findParentHandler();
      if( parentHandler != null )
        parentHandler.fireLayerVisibilityChanged( (IChartLayer)getContainer() );
    }
  };

  private final List<IChartLayer> m_layers = Collections.synchronizedList( new ArrayList<IChartLayer>() );

  private final ILayerContainer m_container;

  public LayerManager( final ILayerContainer container )
  {
    m_container = container;
  }

  @Override
  public ILayerContainer getContainer( )
  {
    return m_container;
  }

  @Override
  public void accept( final IChartLayerVisitor... visitors )
  {
    for( final IChartLayerVisitor visitor : visitors )
    {
      accept( visitor );
    }
  }

  @Override
  public void accept( final IChartLayerVisitor visitor )
  {
    try
    {
      final IChartLayer[] reverseLayers = getLayers();
      ArrayUtils.reverse( reverseLayers );
      for( final IChartLayer layer : reverseLayers )
      {
        try
        {
          visitor.visit( layer );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }
    finally
    {
      // only finalize chart model visitors (recursion!)
      if( m_container instanceof IChartModel )
        visitor.doFinialize();
    }
  }

  @Override
  public LayerManagerEventHandler getEventHandler( )
  {
    return m_handler;
  }

  @Override
  public void addLayer( final IChartLayer... layers )
  {
    if( ArrayUtils.isEmpty( layers ) )
      return;

    Collections.addAll( m_layers, layers );

    for( final IChartLayer layer : layers )
    {
      layer.setParent( m_container );
      layer.addListener( m_layerListener );

      m_handler.fireLayerAdded( layer );
    }
  }

  @Override
  public void addListener( final ILayerManagerEventListener l )
  {
    m_handler.addListener( l );
  }

  @Override
  public void clear( )
  {
    // REMARK: Bugfix, if the events were send directly after a remove, the remaining layers (that will removed next)
    // might try to repaint on a stale data model (happens for profile charts)
    final IChartLayer[] layersToRemove = m_layers.toArray( new IChartLayer[] {} );
    for( final IChartLayer layerToRemove : layersToRemove )
      removeLayerInternal( layerToRemove );

    // TODO Eventually send a composite event...
    for( final IChartLayer removedLayer : layersToRemove )
      m_handler.fireLayerRemoved( removedLayer );
  }

  @Override
  public IChartLayer findLayer( final String identifier )
  {
    final FindLayerVisitor visitor = new FindLayerVisitor( identifier );
    accept( visitor );

    return visitor.getLayer();
  }

  @Override
  public int getLayerPosition( final IChartLayer layer )
  {
    return m_layers.indexOf( layer );
  }

  @Override
  public IChartLayer[] getLayers( )
  {
    return m_layers.toArray( new IChartLayer[] {} );
  }

  @Override
  public void moveLayerToPosition( final IChartLayer layer, final int position )
  {
    m_layers.remove( layer );

    if( position < m_layers.size() )
      m_layers.add( position, layer );
    else
      m_layers.add( layer );

    m_handler.fireLayerMoved( layer );
  }

  @Override
  public void removeLayer( final IChartLayer layer )
  {
    removeLayerInternal( layer );

    m_handler.fireLayerRemoved( layer );
  }

  private void removeLayerInternal( final IChartLayer layer )
  {
    layer.setActive( false );

    m_layers.remove( layer );
    layer.removeListener( m_layerListener );
    layer.dispose();
  }

  @Override
  public void removeListener( final ILayerManagerEventListener l )
  {
    m_handler.removeListener( l );
  }

  @Override
  public int size( )
  {
    return m_layers.size();
  }

  @Override
  public IChartLayer[] getLayers( final IAxis axis, final boolean recursive )
  {
    final FindAxisLayerVisitor visitor = new FindAxisLayerVisitor( axis, recursive );
    accept( visitor );

    return visitor.getLayers();
  }
}