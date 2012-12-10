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
package org.kalypso.gml.ui.jface;

import java.nio.charset.Charset;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.jface.viewers.CharsetViewer;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;

/**
 * Helper class to show a {@link java.nio.charset.Charset} selector for shapes to the user.
 * 
 * @author Gernot Belger
 */
public final class ShapeCharsetUI
{
  private static final String SETTINGS_CHARSET = "shapeCharset";

  private ShapeCharsetUI( )
  {
    throw new UnsupportedOperationException( "Helper class, do not instantiate" );
  }

  /**
   * Creates and configures a charset viewer suitable for shapes.
   */
  public static CharsetViewer createCharsetViewer( final Composite parent, final IDialogSettings dialogSettings )
  {
    final CharsetViewer charsetViewer = new CharsetViewer( parent );

    final Charset shapeDefaultCharset = ShapeSerializer.getShapeDefaultCharset();
    final String shapeLabel = String.format( "%s (default for ESRI Shape)", shapeDefaultCharset.displayName() );
    charsetViewer.addLabelMapping( shapeDefaultCharset, shapeLabel );

    final Charset charset = getInitialCharset( shapeDefaultCharset, dialogSettings );
    charsetViewer.setCharset( charset );

    charsetViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        if( dialogSettings == null )
          return;

        final Charset newCharset = charsetViewer.getCharset();
        if( newCharset == null )
          dialogSettings.put( SETTINGS_CHARSET, (String) null );
        else
        {
          final String charsetName = newCharset.name();
          dialogSettings.put( SETTINGS_CHARSET, charsetName );
        }
      }
    } );

    return charsetViewer;
  }

  private static Charset getInitialCharset( final Charset defaultCharset, final IDialogSettings dialogSettings )
  {
    if( dialogSettings == null )
      return defaultCharset;

    final String charsetName = dialogSettings.get( SETTINGS_CHARSET );
    if( charsetName == null )
      return defaultCharset;

    return Charset.forName( charsetName );
  }

}
