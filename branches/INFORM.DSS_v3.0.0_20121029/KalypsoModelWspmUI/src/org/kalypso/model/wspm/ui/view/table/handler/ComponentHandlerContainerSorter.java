package org.kalypso.model.wspm.ui.view.table.handler;

import java.util.Comparator;

public class ComponentHandlerContainerSorter implements Comparator<ComponentHandlerSortContainer>
{

  /**
   * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
   */
  @Override
  public int compare( final ComponentHandlerSortContainer c1, final ComponentHandlerSortContainer c2 )
  {
    return c1.getPriority().compareTo( c2.getPriority() );
  }

}