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
package de.openali.odysseus.chart.framework.util.img;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.LABEL_POSITION;

/**
 * @author Dirk Kuch
 */
public class TitleImageCreator
{

  private final IChartModel m_model;

  public TitleImageCreator( final IChartModel model )
  {
    m_model = model;
  }

  public Point getSize( )
  {
    if( m_model.isHideTitle() )
      return new Point( 0, 0 );

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Font font = getFont( dev );
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image );

    try
    {
      gc.setFont( font );

      int x = 0;
      int y = 0;

      final String[] title = m_model.getTitle();
      for( final String row : title )
      {
        final Point extend = gc.textExtent( row, SWT.DRAW_DELIMITER | SWT.DRAW_TAB );
        x = Math.max( extend.x, x );
        y += extend.y;
      }

      return new Point( x, y );
    }
    finally
    {
      image.dispose();
      gc.dispose();
      font.dispose();
    }
  }

  public Image createImage( final LABEL_POSITION position, final Point clientSize )
  {
    if( m_model.isHideTitle() || clientSize.x < 1 || clientSize.y < 1 )
      return null;

    final String[] title = m_model.getTitle();

    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, clientSize.x, clientSize.y );
    final GC gc = new GC( image );

    final Font font = getFont( dev );

    try
    {
      gc.setFont( font );
      for( int i = 0; i < title.length; i++ )
      {
        final Point lineSize = gc.textExtent( title[i] );
        gc.drawText( title[i], (clientSize.x - lineSize.x) / 2, i * lineSize.y, SWT.DRAW_TAB );

      }
      return image;
    }
    finally
    {
      font.dispose();
      gc.dispose();
    }
  }

  private Font getFont( final Device dev )
  {
    final FontData fontData = m_model.getTextStyle().toFontData();
    if( fontData == null )
      return new Font( dev, dev.getFontList( null, true )[0] );

    return new Font( dev, fontData );
  }
}
