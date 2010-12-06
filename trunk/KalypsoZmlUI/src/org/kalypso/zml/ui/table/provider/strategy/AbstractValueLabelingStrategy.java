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
package org.kalypso.zml.ui.table.provider.strategy;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.binding.CellStyle;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.provider.IZmlLabelProvider;
import org.kalypso.zml.ui.table.provider.RuleMapper;
import org.kalypso.zml.ui.table.viewmodel.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractValueLabelingStrategy implements IZmlLabelStrategy
{
  private final IZmlLabelProvider m_provider;

  public AbstractValueLabelingStrategy( final IZmlLabelProvider provider )
  {
    m_provider = provider;
  }

  protected String format( final IZmlModelRow row, final Object value ) throws CoreException
  {
    final CellStyle style = m_provider.findStyle( row );
    final String format = style.getTextFormat();
    if( value instanceof Date )
    {
      final SimpleDateFormat sdf = new SimpleDateFormat( format == null ? "dd.MM.yyyy HH:mm" : format );
      return sdf.format( value );
    }

    return String.format( format == null ? "%s" : format, value );
  }

  private RuleMapper getMapper( )
  {
    return m_provider.getMapper();
  }

  protected ZmlRule[] findActiveRules( final IZmlModelRow row )
  {
    return getMapper().findActiveRules( row, m_provider.getColumn() );
  }

  protected IZmlValueReference getReference( final IZmlModelRow row )
  {
    return row.get( m_provider.getColumn().getType() );
  }

  protected IZmlTable getTable( )
  {
    return m_provider.getTable();
  }

  protected IZmlTableColumn getTableColumn( )
  {
    final IZmlTable table = getTable();
    final IZmlTableColumn column = table.findColumn( m_provider.getColumn() );

    return column;
  }
}
