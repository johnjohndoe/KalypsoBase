/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * Utility methods for {@link org.eclipse.jface.viewers.Viewer}'s.
 * 
 * @author Gernot Belger
 */
public final class ViewerUtilities
{
  /** Never gets instantiated */
  private ViewerUtilities( )
  {
    // never instantiate it
  }

  /**
   * Sets the selection of this viewer to its first input-element.
   * <p>
   * The content provider must be a {@link IStructuredContentProvider}.
   * </p>
   */
  public static void selectFirstElement( final ContentViewer viewer )
  {
    final IContentProvider contentProvider = viewer.getContentProvider();
    if( contentProvider instanceof IStructuredContentProvider )
    {
      final IStructuredContentProvider provider = (IStructuredContentProvider) contentProvider;
      final Object[] elements = provider.getElements( viewer.getInput() );
      if( elements != null && elements.length > 0 )
        viewer.setSelection( new StructuredSelection( elements[0] ), true );
    }
  }

  /**
   * Sets the input of a {@link Viewer}in the display thread of its underlying control. Does nothing if the viewer is
   * null, or its control is disposed.
   * 
   * @param async
   *          if true, refresh is done asynchronously, else refresh is done synchronously.
   * @see Viewer#setInput(java.lang.Object )
   * @see Display#asyncExec(java.lang.Runnable)
   * @see Display#syncExec(java.lang.Runnable)
   */
  public static void setInput( final Viewer viewer, final Object input, final boolean async )
  {
    if( viewer == null )
      return;

    final Control control = viewer.getControl();
    if( control == null || control.isDisposed() )
      return;

    final Runnable runner = new Runnable()
    {
      @Override
      public void run( )
      {
        if( !viewer.getControl().isDisposed() )
          viewer.setInput( input );
      }
    };

    final Display display = control.getDisplay();
    if( async )
      display.asyncExec( runner );
    else
      display.syncExec( runner );
  }

  /**
   * Refresh a {@link Viewer}in the display thread of its underlying control. Does nothing if the viewer is null, or its
   * control is disposed.
   * 
   * @param async
   *          if true, refresh is done asynchronously, else refresh is done synchronously.
   * @see Viewer#refresh()
   * @see Display#asyncExec(java.lang.Runnable)
   * @see Display#syncExec(java.lang.Runnable)
   */
  public static void refresh( final Viewer viewer, final boolean async )
  {
    if( viewer == null )
      return;

    final Control control = viewer.getControl();
    if( control == null || control.isDisposed() )
      return;

    final Runnable runner = new Runnable()
    {
      @Override
      public void run( )
      {
        if( !viewer.getControl().isDisposed() )
          viewer.refresh();
      }
    };
    final Display display = control.getDisplay();

    if( async )
      display.asyncExec( runner );
    else
      display.syncExec( runner );
  }

  public static void refresh( final StructuredViewer viewer, final Object element, final boolean async )
  {
    if( viewer != null )
    {
      final Control control = viewer.getControl();
      if( control != null && !control.isDisposed() )
      {
        final Runnable runner = new Runnable()
        {
          @Override
          public void run( )
          {
            if( !viewer.getControl().isDisposed() )
              viewer.refresh( element );
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

  public static void update( final StructuredViewer viewer, final Object[] elements, final String[] properties, final boolean async )
  {
    if( viewer != null )
    {
      final Control control = viewer.getControl();
      if( control != null && !control.isDisposed() )
      {
        final Runnable runner = new Runnable()
        {
          @Override
          public void run( )
          {
            if( !viewer.getControl().isDisposed() )
              viewer.update( elements, properties );
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
