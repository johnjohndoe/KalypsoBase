package org.bce.eclipse.jface.viewers;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Utitlitiy methods for {@link org.eclipse.jface.viewers.Viewer}'s.
 * 
 * @author belger
 */
public final class ViewerUtilities
{
  /** Never gets instatiated */
  private ViewerUtilities( )
  {
  }

  /**
   * Refresh a {@link Viewer}in the display thread of its underlying control.
   * 
   * Does nothing if the viewer is null, or its control is disposed.
   * 
   * @param async
   *          if true, refresh is done asynchron, else refresh is
   *          done synchronisely.
   * 
   * @see Viewer#refresh()
   * @see Display#asyncExec(java.lang.Runnable)
   * @see Display#syncExec(java.lang.Runnable)
   */
  public static void refresh( final Viewer viewer, final boolean async )
  {
    if( viewer != null )
    {
      final Control control = viewer.getControl();
      if( control != null && !control.isDisposed() )
      {
        final Runnable runner = new Runnable()
        {
          public void run( )
          {
            viewer.refresh();
          }
        };
        final Display display = control.getDisplay();

        if( async )
          display.asyncExec( runner );
        else
          display.syncExec( runner );
      }
    }
  }
}
