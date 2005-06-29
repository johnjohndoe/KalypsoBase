package org.kalypso.metaproject.views;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.metaproject.MetaProject;

/**
 * @author schlienger
 */
public class MetaProjectTreeContentProvider implements ITreeContentProvider
{
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( Object parentElement )
  {
    if( parentElement instanceof MetaProject )
      return ((MetaProject)parentElement).getDependencies();
    
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( Object element )
  {
    if( element instanceof MetaProject )
      return ((MetaProject)element).getDependencies().length > 0;
    
    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( Object inputElement )
  {
    if( inputElement instanceof MetaProject[] )
      return (MetaProject[])inputElement;
    
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose()
  {
    // empty
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
    // empty
  }
}
