package de.openali.diagram.framework.model.layer;

/**
 * @author alibu
 */
public interface ILayerManagerEventProvider
{
  public void addLayerManagerEventListener( ILayerManagerEventListener l );

  public void removeLayerManagerEventListener( ILayerManagerEventListener l );
}
