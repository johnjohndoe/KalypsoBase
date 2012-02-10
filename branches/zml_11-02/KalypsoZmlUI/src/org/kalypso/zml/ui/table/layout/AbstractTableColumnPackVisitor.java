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
package org.kalypso.zml.ui.table.layout;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTableColumnVisitor;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractTableColumnPackVisitor implements IZmlTableColumnVisitor
{
  protected void pack( final TableColumn column, final BaseColumn base, final String label, final boolean visible )
  {
    if( !visible )
    {
      hide( column );
    }
    else
    {
      column.setMoveable( false );
      column.setResizable( true );

      if( base.isAutopack() )
      {
        column.pack();
      }
      else
      {
        final Integer width = base.getWidth();
        if( width == null )
        {
          final Integer calculated = calculateSize( column, base, label );
          if( calculated == null )
            column.pack();
          else
          {
            /* set biggest value - calculated header with or packed cell width */
            column.pack();
            final int packedWith = column.getWidth();

            if( packedWith < calculated )
              column.setWidth( calculated );
          }

        }
        else
          column.setWidth( width );
      }
    }
  }

  protected void hide( final TableColumn column )
  {
    column.setWidth( 0 );
    column.setResizable( false );
    column.setMoveable( false );
  }

  /**
   * @return minimal header size
   */
  private Integer calculateSize( final TableColumn table, final BaseColumn base, final String label )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image );

    try
    {
      final int spacer = 10;

      final CellStyle style = base.getDefaultStyle();

      if( style.getFont() != null )
        gc.setFont( style.getFont() );

      final Point extend = gc.textExtent( label );

      final Image img = table.getImage();
      if( img != null )
      {
        return extend.x + spacer * 2 + img.getImageData().width;
      }

      return extend.x + spacer;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );

      return null;
    }
    finally
    {
      gc.dispose();
      image.dispose();
    }
  }
}
