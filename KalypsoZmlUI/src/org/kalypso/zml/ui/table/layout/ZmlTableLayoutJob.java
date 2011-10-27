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
package org.kalypso.zml.ui.table.layout;

import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableLayoutJob extends UIJob
{
  protected static final Color COLOR_TABLE_DISABLED = new Color( null, new RGB( 0xea, 0xea, 0xea ) );

  protected static final Color COLOR_TABLE_ENABLED = new Color( null, new RGB( 0xff, 0xff, 0xff ) );

  private final ZmlTableComposite m_table;

  private final Set<IZmlTableColumn> m_stack;

  public ZmlTableLayoutJob( final ZmlTableComposite table, final Set<IZmlTableColumn> stack )
  {
    super( "Tabellen-Layout wird aktualisiert" );
    m_table = table;
    m_stack = stack;
  }

  /**
   * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus runInUIThread( final IProgressMonitor monitor )
  {
    if( m_table.isDisposed() )
      return Status.CANCEL_STATUS;

    synchronized( this )
    {
      final IZmlTableColumn[] stack = m_stack.toArray( new IZmlTableColumn[] {} );
      m_stack.clear();

      doVisitIndex( stack );
      doVisitHide( stack );
      doVisitPack( stack );

      final TableViewer viewer = m_table.getViewer();
      if( m_table.isEmpty() )
      {
        viewer.getControl().setBackground( COLOR_TABLE_DISABLED );
      }
      else
      {
        viewer.getControl().setBackground( COLOR_TABLE_ENABLED );
      }
    }

    return Status.OK_STATUS;
  }

  private void doVisitIndex( final IZmlTableColumn[] columns )
  {
    final PackIndexColumnsVisitor visitor = new PackIndexColumnsVisitor( !ArrayUtils.isEmpty( m_table.getRows() ) );

    for( final IZmlTableColumn column : columns )
    {
      if( column.isIndexColumn() )
        visitor.visit( column );
    }
  }

  private void doVisitHide( final IZmlTableColumn[] columns )
  {
    final HideInactiveColumnsVisitor visitor = new HideInactiveColumnsVisitor();

    for( final IZmlTableColumn column : columns )
    {
      if( !column.isVisible() )
        visitor.visit( column );
    }

  }

  private void doVisitPack( final IZmlTableColumn[] columns )
  {
    final PackTableColumnVisitor visitor = new PackTableColumnVisitor();

    for( final IZmlTableColumn column : columns )
    {
      if( column.isIndexColumn() )
        continue;
      else if( !column.isVisible() )
        continue;

      visitor.visit( column );
    }
  }

}