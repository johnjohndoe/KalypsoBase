/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.nat.painter;

import net.sourceforge.nattable.config.IConfigRegistry;
import net.sourceforge.nattable.layer.cell.LayerCell;
import net.sourceforge.nattable.painter.cell.AbstractCellPainter;
import net.sourceforge.nattable.painter.cell.ICellPainter;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.zml.core.table.model.references.IZmlModelIndexCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.table.provider.rendering.cell.ZmlTableValueCellPainter;

/**
 * @author Dirk Kuch
 */
public class ZmlModelCellPainter extends AbstractCellPainter implements ICellPainter
{

  @Override
  public void paintCell( final LayerCell cell, final GC gc, final Rectangle bounds, final IConfigRegistry configRegistry )
  {
    final Object object = cell.getDataValue();
    if( object instanceof IZmlModelIndexCell )
    {
      final IZmlModelIndexCell index = (IZmlModelIndexCell) object;
      final ZmlIndexLabelProvider provider = new ZmlIndexLabelProvider( index.getStyleProvider() );

      final Image[] images = provider.getImages( index );
      final String text = provider.getText( index );

    }
    else if( object instanceof IZmlModelValueCell )
    {
      final IZmlModelValueCell value = (IZmlModelValueCell) object;

      final ZmlTableValueCellPainter painter = new ZmlTableValueCellPainter( value );
      painter.initGc( gc );
      painter.drawBackground( gc, bounds );
      apply( bounds, painter.drawImage( gc, bounds ) );
      apply( bounds, painter.drawText( gc, bounds ) );

      painter.resetGc( gc );
    }

  }

  private void apply( final Rectangle bounds, final Point extend )
  {
    bounds.width -= extend.x;
    bounds.x += extend.x;
    bounds.height = Math.max( bounds.height, extend.y );
  }

  @Override
  public int getPreferredWidth( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public int getPreferredHeight( final LayerCell cell, final GC gc, final IConfigRegistry configRegistry )
  {
    // TODO Auto-generated method stub
    return 0;
  }

}
