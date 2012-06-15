/*--------------- Kalypso-Header --------------------------------------------------------------------

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
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.kalypso.contribs.java.net.IUrlResolver2;

/**
 * MementoUtils
 * 
 * @author schlienger
 */
@SuppressWarnings("restriction")
public class MementoUtils
{
  public final static String ISO88591 = "ISO-8859-1";

  private MementoUtils( )
  {
    // do not instanciate
  }

  /**
   * Saves the given properties into the memento. The properties are simply serialized using the load/save mechanims of
   * the Properties class. The string representation is then saved in the text data of the tag.
   */
  public static void saveProperties( final IMemento memento, final Properties props ) throws IOException
  {
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();

    try
    {
      props.store( stream, "" );

      memento.putTextData( stream.toString( ISO88591 ) );

      stream.close();
    }
    finally
    {
      stream.close();
    }
  }

  /**
   * The pendant to the saveProperties().
   */
  public static void loadProperties( final IMemento memento, final Properties props ) throws IOException
  {
    InputStream ins = null;
    try
    {
      ins = new ByteArrayInputStream( memento.getTextData().getBytes( ISO88591 ) );

      props.load( ins );

      ins.close();
    }
    catch( UnsupportedEncodingException e )
    {
      e.printStackTrace();
      // ignored
    }
    finally
    {
      ins.close();
    }
  }

  public static MementoWithUrlResolver createMementoWithUrlResolver( IMemento memento, Properties properties, IUrlResolver2 resolver )
  {
    return new MementoWithUrlResolver( memento, properties, resolver );
  }

  public static void restoreWorkbenchPage( IMemento stateMemento )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final IPerspectiveRegistry registry = workbench.getPerspectiveRegistry();
    final IWorkbenchWindow activeWindow = workbench.getActiveWorkbenchWindow();
    // get the actual size of the active Window
    final Rectangle bounds = activeWindow.getShell().getDisplay().getBounds();
    int heigth = bounds.height;
    int width = bounds.width;
    int x = bounds.x;
    int y = bounds.y;

    // do the memento business
    final IMemento windowMemento = stateMemento.getChild( IWorkbenchConstants.TAG_WINDOW );
    // HACK: to make sure that if the tag maximized is set true in the memento the size attributes must be
    // adjusted to actual size of the current shell of the active workbench window.
    if( windowMemento.getString( IWorkbenchConstants.TAG_MAXIMIZED ).equalsIgnoreCase( "true" ) )
    {
      windowMemento.putInteger( IWorkbenchConstants.TAG_HEIGHT, heigth );
      windowMemento.putInteger( IWorkbenchConstants.TAG_WIDTH, width );
      windowMemento.putInteger( IWorkbenchConstants.TAG_X, x );
      windowMemento.putInteger( IWorkbenchConstants.TAG_Y, y );
      System.out.println( "heigth: " + heigth + "\twidth: " + width + "\tx: " + x + "\ty: " + y );
    }
    final IMemento pageMemento = windowMemento.getChild( IWorkbenchConstants.TAG_PAGE );
    final IMemento perspspectiveMemento = pageMemento.getChild( IWorkbenchConstants.TAG_PERSPECTIVES );
    final IMemento singlePerspective = perspspectiveMemento.getChild( IWorkbenchConstants.TAG_PERSPECTIVE );
    final IMemento descMemento = singlePerspective.getChild( IWorkbenchConstants.TAG_DESCRIPTOR );
    final String perspectiveID = descMemento.getString( IWorkbenchConstants.TAG_ID );
    final PerspectiveDescriptor realDesc = (PerspectiveDescriptor) registry.findPerspectiveWithId( perspectiveID );

    WorkbenchWindow page = ((WorkbenchWindow) activeWindow);
    final WorkbenchPage activePage = (WorkbenchPage) activeWindow.getActivePage();
    if( activePage != null )
    {
      // DO NOT REMOVE: THIS IS A SWITCH
      // This part of code does not change the size of the window before restoring but opens two
      // activePage.closeAllPerspectives( false, false );
      // activePage.restoreState( pageMemento, realDesc );
      // activePage.resetPerspective();
      activePage.closeAllPerspectives( false, true );
      page.restoreState( windowMemento, realDesc );
      page.getActivePage().resetPerspective();
    }
  }
}