package org.kalypso.metaproject.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.kalypso.metaproject.MetaProject;

class MetaProjectLabelProvider extends LabelProvider
{
  public String getText( final Object obj )
  {
    return ( (MetaProject)obj ).getProject().getName();
  }

  public Image getImage( Object obj )
  {
    return PlatformUI.getWorkbench().getSharedImages().getImage( org.eclipse.ui.ide.IDE.SharedImages.IMG_OBJ_PROJECT );
  }
}