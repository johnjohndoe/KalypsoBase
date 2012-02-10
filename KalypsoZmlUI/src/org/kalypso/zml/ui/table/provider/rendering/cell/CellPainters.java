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
package org.kalypso.zml.ui.table.provider.rendering.cell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.table.model.cells.IZmlTableIndexCell;
import org.kalypso.zml.ui.table.model.cells.IZmlTableValueCell;
import org.kalypso.zml.ui.table.model.columns.IZmlTableIndexColumn;
import org.kalypso.zml.ui.table.model.columns.IZmlTableValueColumn;
import org.kalypso.zml.ui.table.provider.ZmlLabelProvider;

/**
 * @author Dirk Kuch
 */
public final class CellPainters
{
  private CellPainters( )
  {
  }

  public static ZmlCellRule[] getActiveRules( final IZmlTableValueCell cell )
  {
    final IZmlTableValueColumn column = cell.getColumn();
    final IZmlModelRow modelRow = cell.getRow().getModelRow();

    return column.findActiveRules( modelRow );
  }

  public static ZmlCellRule[] getActiveRules( final IZmlTableIndexCell cell )
  {
    final IZmlTableIndexColumn column = cell.getColumn();
    final IZmlModelRow modelRow = cell.getRow().getModelRow();

    return column.findActiveRules( modelRow );
  }

  public static ZmlLabelProvider getLabelProivder( final IZmlTableValueCell cell, final ZmlCellRule[] activeRules )
  {
    return new ZmlLabelProvider( cell.getRow().getModelRow(), cell.getColumn(), activeRules );
  }

  public static ZmlLabelProvider getLabelProivder( final IZmlTableIndexCell cell, final ZmlCellRule[] activeRules )
  {
    return new ZmlLabelProvider( cell.getRow().getModelRow(), cell.getColumn(), activeRules );
  }

  public static Image[] findImages( final IZmlTableValueCell cell, final ZmlLabelProvider provider, final ZmlCellRule[] rules )
  {
    final IZmlModelRow row = cell.getRow().getModelRow();
    final IZmlModelValueCell reference = row.get( cell.getColumn().getModelColumn() );
    if( Objects.isNull( reference ) )
      return new Image[] {};

    return findImages( reference, provider, rules );
  }

  public static Image[] findImages( final IZmlTableIndexCell cell, final ZmlLabelProvider provider, final ZmlCellRule[] activeRules )
  {
    final IZmlModelRow row = cell.getRow().getModelRow();
    final IZmlModelValueCell reference = row.get( cell.getColumn().getModelColumn() );
    if( Objects.isNull( reference ) )
      return new Image[] {};

    return findImages( reference, provider, activeRules );
  }

  public static Image[] findImages( final IZmlModelValueCell reference, final ZmlLabelProvider provider, final ZmlCellRule[] rules )
  {
    final List<Image> images = new ArrayList<Image>();
    for( final ZmlCellRule rule : rules )
    {
      try
      {
        final CellStyle style = provider.resolveRuleStyle( rule, reference );
        if( Objects.isNull( style ) )
          continue;

        final Image image = style.getImage();
        if( Objects.isNull( image ) )
          continue;

        images.add( image );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
    }

    return images.toArray( new Image[] {} );
  }

}
