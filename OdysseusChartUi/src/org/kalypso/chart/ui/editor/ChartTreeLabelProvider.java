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
package org.kalypso.chart.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.util.ChartUtilities;

/**
 * @author alibu
 */
public class ChartTreeLabelProvider extends LabelProvider implements ITableLabelProvider
{
  private final Map<Object, Image> m_images = new HashMap<>();

  /** Default size for legend icons: use 16, this is default for all eclipse icons */
  private final Point m_defaultIconSize = new Point( 16, 16 );

  @Override
  public void dispose( )
  {
    super.dispose();

    clearImages();
  }

  public void clearImages( )
  {
    final Image[] layerImages = m_images.values().toArray( new Image[m_images.size()] );
    m_images.clear();
    for( final Image img : layerImages )
      img.dispose();
  }

  @Override
  public String getText( final Object element )
  {
    // Falls das Layer nicht erzeugt werden konnte sollte das in der Legende ersichtlich sein
    if( element instanceof IChartLayer )
    {
      return ((IChartLayer) element).getTitle();
    }

    else if( element instanceof ILegendEntry )
    {
      return ((ILegendEntry) element).getDescription();
    }

    return super.getText( element );
  }

  @Override
  public Image getImage( final Object element )
  {
    final Display display = ChartUtilities.getDisplay();

    if( element instanceof IChartLayer )
    {
      // Wenn nur ein Kind-Icon vorhanden ist, dann wird das verwendet
      final IChartLayer layer = (IChartLayer) element;
      final ILegendEntry[] entries = layer.getLegendEntries();
      if( entries == null || entries.length == 0 )
      {
        return null;
      }

      if( entries.length == 1 )
      {
        final ImageData data = entries[0].getSymbol( m_defaultIconSize );
        if( data == null )
          return null;

        final Image img = new Image( display, data );
        addImage( layer, img );
        return img;
      }
      else
      {
        // TODO: create image file instead of painting per source code
        final Image img = new Image( display, m_defaultIconSize.x, m_defaultIconSize.y );
        final GC gc = new GC( img );
        gc.setAntialias( SWT.ON );
        gc.setForeground( display.getSystemColor( SWT.COLOR_GRAY ) );
        gc.setBackground( display.getSystemColor( SWT.COLOR_BLUE ) );
        final int width = m_defaultIconSize.x;
        final int height = m_defaultIconSize.y;

        final int[] path1 = new int[8];
        final int[] path2 = new int[8];

        path1[0] = (int) ((float) width / (float) 10 * 2);
        path1[1] = (int) ((float) height / (float) 10 * 2);
        path1[2] = (int) ((float) width / (float) 10 * 7);
        path1[3] = path1[1];
        path1[4] = (int) ((float) width / (float) 10 * 6);
        path1[5] = (int) ((float) height / (float) 10 * 7);
        path1[6] = (int) ((float) width / (float) 10 * 1);
        path1[7] = path1[5];

        final int offset = 3;
        for( int i = 0; i < path1.length; i += 2 )
        {
          path2[i] = path1[i] + 2;
          path2[i + 1] = path1[i + 1] + offset;
        }

        gc.drawPolygon( path2 );
        gc.setAlpha( 40 );
        gc.fillPolygon( path2 );
        gc.setAlpha( 255 );

        gc.drawPolygon( path1 );
        gc.setAlpha( 40 );
        gc.fillPolygon( path1 );
        gc.setAlpha( 255 );

        gc.setForeground( display.getSystemColor( SWT.COLOR_BLACK ) );
        gc.setLineWidth( 3 );
        gc.drawLine( width - 9, 5, width - 1, 5 );
        gc.drawLine( width - 5, 1, width - 5, 9 );

        gc.dispose();
        addImage( layer, img );
        return img;
      }

    }
    if( element instanceof ILegendEntry )
    {
      final ILegendEntry le = (ILegendEntry) element;

      if( m_images.containsKey( le ) )
        return m_images.get( le );

      final Image img = new Image( display, le.getSymbol( m_defaultIconSize ) );
      addImage( le, img );
      return img;
    }

    return super.getImage( element );
  }

  private void addImage( final Object key, final Image img )
  {
    final Image oldImage = m_images.put( key, img );
    if( oldImage != null )
      oldImage.dispose();
  }

  @Override
  public Image getColumnImage( final Object element, final int columnIndex )
  {
    return getImage( element );
  }

  @Override
  public String getColumnText( final Object element, final int columnIndex )
  {
    return getText( element );
  }
}