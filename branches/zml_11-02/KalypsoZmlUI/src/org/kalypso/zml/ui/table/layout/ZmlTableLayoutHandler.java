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
package org.kalypso.zml.ui.table.layout;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.IZmlTableCompositeListener;
import org.kalypso.zml.ui.table.IZmlTableListener;
import org.kalypso.zml.ui.table.model.columns.IZmlTableColumn;
import org.kalypso.zml.ui.table.model.columns.ZmlTableColumns;
import org.kalypso.zml.ui.table.nat.ZmlTable;

/**
 * @author Dirk Kuch
 */
public class ZmlTableLayoutHandler implements IZmlTableListener
{
  protected final IZmlTable m_table;

  final Set<IZmlTableColumn> m_stack = Collections.synchronizedSet( new LinkedHashSet<IZmlTableColumn>() );

  private final MutexRule m_rule = new MutexRule( "updating column layout of zml table" );

  UIJob m_job;

  public ZmlTableLayoutHandler( final IZmlTable table )
  {
    m_table = table;
  }

  @Override
  public void eventTableChanged( final String type, final IZmlModelColumn... columns )
  {
    if( IZmlTableCompositeListener.TYPE_REFRESH.equals( type ) )
    {
      doRefreshColumns( ZmlTableColumns.toTableColumns( m_table, true, columns ) );
    }
    else if( IZmlTableCompositeListener.TYPE_ACTIVE_RULE_CHANGED.equals( type ) )
    {
      doRefreshColumns( ZmlTableColumns.toTableColumns( m_table, false, columns ) );
    }

  }

  private void doRefreshColumns( final IZmlTableColumn... columns )
  {
    synchronized( this )
    {
      Collections.addAll( m_stack, columns );
      if( Objects.isNotNull( m_job ) )
        m_job.cancel();

      m_job = new ZmlTableLayoutJob( (ZmlTable) m_table, m_stack );

      m_job.setRule( m_rule );
      m_job.schedule( 100 );
    }

  }
}
