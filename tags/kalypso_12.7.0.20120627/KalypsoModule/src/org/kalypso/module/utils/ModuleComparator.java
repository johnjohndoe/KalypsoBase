package org.kalypso.module.utils;

import java.util.Comparator;

import org.kalypso.module.IKalypsoModule;

/**
 * Compares {@link IKalypsoModule}s by their priority.
 * 
 * @author Gernot Belger
 */
public final class ModuleComparator implements Comparator<IKalypsoModule>
{
  @Override
  public int compare( final IKalypsoModule o1, final IKalypsoModule o2 )
  {
    final int compare = o1.getPriority().compareTo( o2.getPriority() );
    if( compare == 0 )
      return o1.getHeader().compareTo( o2.getHeader() );

    return compare;
  }
}