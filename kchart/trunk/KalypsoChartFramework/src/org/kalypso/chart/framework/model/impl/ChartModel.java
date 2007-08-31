package org.kalypso.chart.framework.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.IChartModelEventListener;
import org.kalypso.chart.framework.model.data.IDataContainer;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.layer.ILayerManager;
import org.kalypso.chart.framework.model.layer.ILayerManagerEventListener;
import org.kalypso.chart.framework.model.layer.impl.LayerManager;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.component.IAxisComponent;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.model.mapper.registry.impl.MapperRegistry;


public class ChartModel implements ILayerManagerEventListener, IChartModel
{
	 
	private boolean m_hideUnusedAxes = false;
	
	private final IMapperRegistry m_mapperRegistry = new MapperRegistry();
	  
	 /** axis --> List of layers */
	  private final Map<IAxis< ? >, List<IChartLayer>> m_axis2Layers = new HashMap<IAxis< ? >, List<IChartLayer>>();
	

	private final ChartModelEventHandler m_handler=new ChartModelEventHandler();
	  
	private final ILayerManager m_manager  = new LayerManager();

	/**
	 * if set to true, all axes are sized automatically to fit all data into a layer
	 */
    private boolean m_autoscale=false;
	  
	
	public ChartModel()
	{
		m_manager.addLayerManagerEventListener(this);
	}
	  
	  
	 /**
	   * adds layers to or removes layers from the chart
	   * 
	   * @param bAdding
	   *          if true, the layer will be added; if false, the layer will be removed
	   */
	  private void updateAxisLayerMap( final IChartLayer layer, final boolean bAdding )
	  {
	    List<IChartLayer> domList = m_axis2Layers.get( layer.getDomainAxis() );
	    List<IChartLayer> valList = m_axis2Layers.get( layer.getTargetAxis() );

	    if( bAdding )
	    {
	      // mapping for domain axis
	      if( domList == null )
	      {
	        domList = new ArrayList<IChartLayer>();
	        m_axis2Layers.put( layer.getDomainAxis(), domList );
	      }
	      domList.add( layer );

	      // mapping for value axis
	      if( valList == null )
	      {
	        valList = new ArrayList<IChartLayer>();
	        m_axis2Layers.put( layer.getTargetAxis(), valList );
	      }
	      valList.add( layer );

	      // axis-components must be visible
	      
	     IAxisComponent domainComp=m_mapperRegistry.getComponent( layer.getDomainAxis() );
	     if (domainComp!=null)
	    	 domainComp.setVisible( true );
	     IAxisComponent valueComp=m_mapperRegistry.getComponent( layer.getTargetAxis() );
	     if (valueComp!=null)
	    	 valueComp.setVisible( true );
	     //m_mapperRegistry.getComponent( layer.getValueAxis() ).setVisible( true );

	    }
	    else
	    {
	      // remove domain mapping
	      if( domList != null )
	        domList.remove( layer );

	      // remove value mapping
	      if( valList != null )
	        valList.remove( layer );

	      // eventually hide axes
	      if( m_hideUnusedAxes )
	      {
	        if( domList == null || domList.size() == 0 )
	          m_mapperRegistry.getComponent( layer.getDomainAxis() ).setVisible( false );
	        if( valList == null || valList.size() == 0 )
	          m_mapperRegistry.getComponent( layer.getTargetAxis() ).setVisible( false );
	      }
	    }
	    
	    if( m_autoscale )
          autoscale( new IAxis[] { layer.getDomainAxis(), layer.getTargetAxis() } );
	     
	    
	  }

	/* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#onLayerAdded(org.kalypso.chart.framework.model.layer.IChartLayer)
	 */
	public void onLayerAdded(IChartLayer layer) {
		 updateAxisLayerMap( layer, true );
		 m_handler.fireModelChanged();
	}

	/* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#onLayerRemoved(org.kalypso.chart.framework.model.layer.IChartLayer)
	 */
	public void onLayerRemoved(IChartLayer layer) {
		 updateAxisLayerMap( layer, false );
		 m_handler.fireModelChanged();
	}
	
	  /* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#getAxisRegistry()
	 */
	public IMapperRegistry getMapperRegistry( )
	  {
	    return m_mapperRegistry;
	  }
	  
	  /* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#getLayerManager()
	 */
	public ILayerManager getLayerManager()
	  {
		  return m_manager  ;
	  }
	  
	  /* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#clear()
	 */
	public void clear()
	  {
		  m_axis2Layers.clear();
		  m_manager.clear();
		  m_mapperRegistry.clear();
	  }
	  
	  /* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#getAxis2Layers()
	 */
	public Map<IAxis<?>, List<IChartLayer>> getAxis2Layers()
	  {
		  return m_axis2Layers;
	  }
	  
	  /* (non-Javadoc)
	 * @see org.kalypso.chart.framework.model.IChartModel#setHideUnusedAxes(boolean)
	 */
	  public void setHideUnusedAxes( boolean b )
	  {
	    m_hideUnusedAxes = b;

	    final IAxis[] axes = m_mapperRegistry.getAxes();
	    for( int i = 0; i < axes.length; i++ )
	    {
	      final IAxis axis = axes[i];
	      final List<IChartLayer> list = m_axis2Layers.get( axis );
	      if( list == null || list.size() == 0 )
	      {
	        final IAxisComponent comp = m_mapperRegistry.getComponent( axis );
	        comp.setVisible( !m_hideUnusedAxes );
	      }
	    }
	  }
	  
	  /**
	   * automatically scales all given axes; scaling means here: show all available values
	   */
	  @SuppressWarnings("unchecked")
	  public void autoscale( IAxis[] axes )
	  {
	    if( axes == null )
	      axes = getMapperRegistry().getAxes();

	    for( final IAxis axis : axes )
	    {
	      final List<IChartLayer> layers = getAxis2Layers().get( axis );
	      if( layers == null )
	        continue;

	      final List<IDataRange> ranges = new ArrayList<IDataRange>( layers.size() );

	      for( final IChartLayer layer : layers )
	      {
	          if (layer.isVisible())
	          {
	            final IDataRange range = getRangeFor( layer, axis );
	            if( range != null )
	              ranges.add( range );
	          }
	      }
	      axis.autorange( ranges.toArray( new IDataRange[ranges.size()] ) );
	    }
	    m_handler.fireModelChanged();
	  }
	  
	  /**
	   * @return DataRange of all domain or target data available in the given layer
	   */
	  @SuppressWarnings("unchecked")
    private IDataRange getRangeFor( final IChartLayer layer, final IAxis axis )
	  {
	    IDataContainer dc = layer.getDataContainer();
	    if (dc==null)
	      return null;
	    else
	    {
	      if( axis == layer.getDomainAxis() )
	        return dc.getDomainRange();
	      else if( axis == layer.getTargetAxis() )
	        return dc.getTargetRange();
	      //letzer Fall: Achse hat nix mit Layer zu tun
	      else
	        return null;
	    }
	    
	  }
	  
	  /**
	   * sets autoscaling
	   * 
	   * @param b
	   *          if true, axes are automatically scaled to show the layers full data range
	   */
	  public void setAutoscale( boolean b )
	  {
	    m_autoscale = b;

	    if( m_autoscale )
	      autoscale( null );
	  }


	  /**
	   * maximises the chart view - that means all the available data of all layers is shown
	   */
	  public void maximize( )
	  {
	    final IAxis[] axes = getMapperRegistry().getAxes();
	    autoscale( axes );
	    //ModelChangedEvent werfen, damit Composite das Model neu zeichnet
	  }


    /**
     * @see org.kalypso.chart.framework.model.IChartModelEventProvider#addChartModelEventListener(org.kalypso.chart.framework.model.IChartModelEventListener)
     */
    public void addChartModelEventListener( IChartModelEventListener l )
    {
      m_handler.addChartModelEventListener( l );
    }


    /**
     * @see org.kalypso.chart.framework.model.IChartModelEventProvider#removeChartModelEventListener(org.kalypso.chart.framework.model.IChartModelEventListener)
     */
    public void removeChartModelEventListener( IChartModelEventListener l )
    {
      m_handler.removeChartModelEventListener( l );
    }


    /**
     * @see org.kalypso.chart.framework.model.IChartModel#invalidate()
     */
    public void invalidate( )
    {
      m_handler.fireModelChanged();
    }
	  


}
