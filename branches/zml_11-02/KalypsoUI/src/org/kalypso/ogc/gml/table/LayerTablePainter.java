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
package org.kalypso.ogc.gml.table;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class LayerTablePainter implements Listener
{
  private final LayerTableViewer m_viewer;

  public LayerTablePainter( final LayerTableViewer viewer )
  {
    m_viewer = viewer;
  }

  @Override
  public void handleEvent( final Event event )
  {
    switch( event.type )
    {
      case SWT.EraseItem:
        eraseItem( event );
        return;
    }
  }

  // TODO: draw hover and selected: mix style with hover/selection colors?
  private void eraseItem( final Event event )
  {
    if( (event.detail & SWT.SELECTED) != 0 )
      return; /* item selected */
    if( (event.detail & SWT.HOT) != 0 )
      return; /* item selected */

    final GC gc = event.gc;

    final Feature feature = (Feature) event.item.getData();

    final int columnIndex = event.index;

    final LayerTableStyle style = m_viewer.getStyle( columnIndex );
    if( style == null )
      return;

    final java.awt.Color awtColor = style.getBackground( feature );
    if( awtColor == null )
      return;

    // TODO: we should also support fill patterns one day...
    final int alpha = awtColor.getAlpha();
    final Color swtColor = getColor( awtColor );

    final Color oldBackground = gc.getBackground();
    final int oldAlpha = gc.getAlpha();

    gc.setBackground( swtColor );
    gc.setAlpha( alpha );

    gc.fillRectangle( event.x, event.y, event.width, event.height );

    gc.setBackground( oldBackground );
    gc.setAlpha( oldAlpha );

    event.detail &= ~SWT.BACKGROUND; // default cell background should not be drawn
  }

  private Color getColor( final java.awt.Color fillColor )
  {
    final RGB fillRGB = ColorUtilities.toRGB( fillColor );

    final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
    final String symbolicName = fillRGB.toString();
    colorRegistry.put( symbolicName, fillRGB );

    return colorRegistry.get( symbolicName );
  }

}