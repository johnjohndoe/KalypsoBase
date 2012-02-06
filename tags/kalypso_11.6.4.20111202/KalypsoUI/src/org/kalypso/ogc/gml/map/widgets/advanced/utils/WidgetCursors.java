/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.map.widgets.advanced.utils;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ui.KalypsoGisPlugin;
import org.osgi.framework.Bundle;

/**
 * @author Gernot Belger
 */
public final class WidgetCursors
{

  private static final String CURSOR_ADD_DRAWING = "icons/cursor/cursor_add_drawing.png"; //$NON-NLS-1$"

  private static final String CURSOR_ADD_RECTANGLE = "icons/cursor/cursor_add_rectangle.png"; //$NON-NLS-1$"

  private static final String CURSOR_ADD = "icons/cursor/cursor_add.png"; //$NON-NLS-1$"

  private static final String CURSOR_REMOVE = "icons/cursor/cursor_remove.png"; //$NON-NLS-1$"

  private WidgetCursors( )
  {
    throw new UnsupportedOperationException();
  }

  public static Cursor createAddCursor( )
  {
    return createCursor( CURSOR_ADD, new Point( 2, 1 ), "Add" ); //$NON-NLS-1$
  }

  public static Cursor createAddDrawingCursor( )
  {
    return createCursor( CURSOR_ADD_DRAWING, new Point( 2, 1 ), "Add Drawing" ); //$NON-NLS-1$
  }

  public static Cursor createAddRectangleCursor( )
  {
    return createCursor( CURSOR_ADD_RECTANGLE, new Point( 2, 1 ), "Add Rectangle" ); //$NON-NLS-1$
  }

  public static Cursor createRemoveCursor( )
  {
    return createCursor( CURSOR_REMOVE, new Point( 2, 1 ), "Remove" ); //$NON-NLS-1$
  }

  private static Cursor createCursor( final String path, final Point anchor, final String name )
  {
    try
    {
      final Toolkit toolkit = Toolkit.getDefaultToolkit();
      final Bundle bundle = KalypsoGisPlugin.getDefault().getBundle();
      final URL location = bundle.getResource( path );
      final BufferedImage image = ImageIO.read( location );
      return toolkit.createCustomCursor( image, anchor, name );
    }
    catch( final Throwable e )
    {
      KalypsoCorePlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return Cursor.getDefaultCursor();
    }
  }
}
