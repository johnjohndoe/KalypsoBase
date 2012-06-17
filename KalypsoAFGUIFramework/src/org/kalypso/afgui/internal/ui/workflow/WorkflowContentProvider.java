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
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
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

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( final Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
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

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object element )
  {
    return getChildren( element );
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {

  }
}