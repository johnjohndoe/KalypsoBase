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
package org.kalypso.ui.editor.sldEditor;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

/**
 * @author Gernot Belger
 */
public abstract class ColorMapLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider
{
  private static final String BRIGHT_FOREGROUND = "foregroundBright";//$NON-NLS-1$

  private static final String DARK_FOREGROUND = "foregroundDark";//$NON-NLS-1$

  private static final class DisposableColorRegistry extends ColorRegistry
  {
    public DisposableColorRegistry( final Display d )
    {
      super( d, false );
    }

    public void dispose( )
    {
      clearCaches();
      clearListeners();
    }
  }

  private final DisposableColorRegistry m_colorRegistry;

  public ColorMapLabelProvider( final Table table )
  {
    final Display display = table.getDisplay();

    m_colorRegistry = new DisposableColorRegistry( display );
    final RGB rgbDark = display.getSystemColor( SWT.COLOR_BLACK ).getRGB();
    final RGB rgbBright = display.getSystemColor( SWT.COLOR_WHITE ).getRGB();
    m_colorRegistry.put( BRIGHT_FOREGROUND, rgbBright );
    m_colorRegistry.put( DARK_FOREGROUND, rgbDark );

    table.addListener( SWT.EraseItem, new Listener()
    {
      @Override
      public void handleEvent( final Event event )
      {
        final Object element = event.item.getData();
        final java.awt.Color awtColor = getAwtColor( element, event.index );
        final String text = getColumnText( element, event.index );
        final Color foreground = getForeground( element, event.index );
        if( awtColor == null )
          return;

        if( (event.detail & SWT.SELECTED) != 0 )
          return;
        if( (event.detail & SWT.HOT) != 0 )
          return;
        if( (event.detail & SWT.BACKGROUND) == 0 )
          return;

        final GC gc = event.gc;
        final int oldAlpha = gc.getAlpha();

        final int alpha = awtColor.getAlpha();
        gc.setAlpha( alpha );
        gc.fillRectangle( event.x, event.y, event.width, event.height );
        gc.setAlpha( oldAlpha );

        final Color oldForeground = gc.getForeground();
        if( foreground != null )
          gc.setForeground( oldForeground );
        gc.drawText( text, event.x, event.y, true );
        gc.setForeground( oldForeground );

        event.doit = false;
        event.detail &= ~SWT.HOT;
        event.detail &= ~(SWT.BACKGROUND);
      }

    } );
  }

  /**
   * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    m_colorRegistry.dispose();
  }

  protected abstract java.awt.Color getAwtColor( Object element, int columnIndex );

  private RGB getRGB( final Object element, final int columnIndex )
  {
    final java.awt.Color color = getAwtColor( element, columnIndex );
    if( color == null )
      return null;

    return new RGB( color.getRed(), color.getGreen(), color.getBlue() );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang.Object, int)
   */
  @Override
  public Color getForeground( final Object element, final int columnIndex )
  {
    final RGB rgb = getRGB( element, columnIndex );
    if( rgb == null )
      return null;

    final float[] hsb = rgb.getHSB();
    if( hsb[2] < 0.7 )
      return m_colorRegistry.get( BRIGHT_FOREGROUND );
    else
      return m_colorRegistry.get( DARK_FOREGROUND );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang.Object, int)
   */
  @Override
  public Color getBackground( final Object element, final int columnIndex )
  {
    final RGB rgb = getRGB( element, columnIndex );
    if( rgb == null )
      return null;

    final String symbolicName = rgb.toString();
    m_colorRegistry.put( symbolicName, rgb );
    return m_colorRegistry.get( symbolicName );
  }

}
