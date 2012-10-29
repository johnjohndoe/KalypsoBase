/**
 *
 */
package org.kalypso.afgui.internal.ui.workflow;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.renew.workflow.base.ITaskGroup;

/**
 * @author Stefan Kurzbach
 */
public class WorkflowContentProvider implements ITreeContentProvider
{
  @Override
  public Object[] getChildren( final Object element )
  {
    if( element instanceof ITaskGroup )
    {
      final ITaskGroup taskGroup = (ITaskGroup) element;
      return taskGroup.getTasks().toArray();
    }
    else
    {
      return new Object[] {};
    }
  }

  @Override
  public Object getParent( final Object element )
  {
    return null;
  }

  @Override
  public boolean hasChildren( final Object element )
  {
    if( element instanceof ITaskGroup )
    {
      final ITaskGroup taskGroup = (ITaskGroup) element;
      return !taskGroup.getTasks().isEmpty();
    }
    else
    {
      return false;
    }
  }

  @Override
  public Object[] getElements( final Object element )
  {
    return getChildren( element );
  }

  @Override
  public void dispose( )
  {
  }

  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {

  }
}