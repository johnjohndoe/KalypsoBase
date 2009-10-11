/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package de.openali.odysseus.chart.framework.test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * TODO: Auslagern in externes Test-Plugin
 * 
 * @author burtscher1
 */
public class ImageTest implements PaintListener
{
  public static void main( String[] args )
  {
    Display d = Display.getDefault();

    Shell s = new Shell( d );
    s.setLayout( new FillLayout() );

    Canvas c = new Canvas( s, SWT.FILL );

    c.addPaintListener( new ImageTest() );

    s.setSize( new Point( 200, 200 ) );

    s.open();

    while( !s.isDisposed() )
      d.readAndDispatch();

    c.dispose();
    s.dispose();
    d.dispose();
  }

  /**
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl( PaintEvent e )
  {
    GC gc = e.gc;
    Color blue = Display.getDefault().getSystemColor( SWT.COLOR_BLUE );

    ImageData id1 = new ImageData( 200, 200, 32, new PaletteData( 0, 0, 0 ) );
    id1.transparentPixel = 0;

    Image img1 = new Image( Display.getDefault(), id1 );
    GC gc1 = new GC( img1 );
    gc1.setForeground( blue );
    gc1.drawLine( 0, 0, 200, 200 );

    Image img2 = new Image( Display.getDefault(), 200, 200 );
    GC gc2 = new GC( img2 );
    gc2.setForeground( blue );
    gc2.drawLine( 0, 200, 200, 0 );

    gc.drawImage( img2, 0, 0 );
    gc.drawImage( img1, 0, 0 );

    gc1.dispose();
    img1.dispose();
    gc2.dispose();
    img2.dispose();

  }
}
