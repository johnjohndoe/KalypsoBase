package org.kalypso.simulation.ui.navigator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.kalypso.ui.editor.gmleditor.ui.GMLEditorLabelProvider2;

public class ModellLabelProvider extends LabelProvider implements ILabelProvider
{
  private final GMLEditorLabelProvider2 m_gmlLabelProvider = new GMLEditorLabelProvider2();

  private final WorkbenchLabelProvider m_workbenchLabelProvider = new WorkbenchLabelProvider();

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element instanceof IResource )
      return m_workbenchLabelProvider.getText( element );

    if( element instanceof IStatus )
      return ((IStatus)element).getMessage();
    
    return m_gmlLabelProvider.getText( element );
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IResource )
      return m_workbenchLabelProvider.getImage( element );

    if( element instanceof IStatus )
      return null; //((IStatus)element).getMessage();
    
    return m_gmlLabelProvider.getImage( element );
  }
}
