package com.bce.gis.operation.raster2vector.collector.ringtree;

/**
 * @author belger
 */
public interface RingTreeWalker
{
  void operate( final RingTreeElement element );

  Object getResult( );
}
