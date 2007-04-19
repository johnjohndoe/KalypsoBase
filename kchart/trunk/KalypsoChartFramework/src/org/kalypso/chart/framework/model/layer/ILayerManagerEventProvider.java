package org.kalypso.chart.framework.model.layer;

/**
 * @author alibu
 */
public interface ILayerManagerEventProvider
{
  public void addLayerManagerEventListener( ILayerManagerEventListener l );

  public void removeLayerManagerEventListener( ILayerManagerEventListener l );
}
