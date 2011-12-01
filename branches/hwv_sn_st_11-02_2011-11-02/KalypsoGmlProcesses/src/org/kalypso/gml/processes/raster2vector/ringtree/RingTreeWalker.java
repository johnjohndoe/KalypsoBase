package org.kalypso.gml.processes.raster2vector.ringtree;

/**
 * @author belger
 */
public interface RingTreeWalker
{
  void operate( final RingTreeElement element );

  Object getResult( );
}
