package org.kalypso.ui.editor.abstractobseditor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.ogc.sensor.template.ObsViewItem;

/**
 * ObsTemplateContentProvider
 * 
 * @author schlienger
 */
public class ObsTemplateContentProvider implements ITreeContentProvider
{
  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  @Override
  public Object[] getChildren( final Object parentElement )
  {
    if( parentElement instanceof ObsView )
      return ((ObsView)parentElement).getItems();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  @Override
  public Object getParent( final Object element )
  {
    if( element instanceof ObsViewItem )
      return ((ObsViewItem)element).getView();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  @Override
  public boolean hasChildren( final Object element )
  {
    return element instanceof ObsView && ((ObsView)element).getItems().length > 0;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement instanceof ObsView )
      return ((ObsView)inputElement).getItems();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    // empty
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // empty
  }
}
