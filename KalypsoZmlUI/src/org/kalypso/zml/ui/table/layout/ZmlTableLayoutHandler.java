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

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.provider.strategy.IExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableLayoutHandler
{
  private static final MutexRule MUTEX_TABLE_UPDATE = new MutexRule( "updating of time series table layout" ); // $NON-NLS-1$

  private static final Color COLOR_TABLE_DISABLED = new Color( null, new RGB( 0xea, 0xea, 0xea ) );

  private static final Color COLOR_TABLE_ENABLED = new Color( null, new RGB( 0xff, 0xff, 0xff ) );

  protected final ZmlTableComposite m_table;

  private UIJob m_job;

  public ZmlTableLayoutHandler( final ZmlTableComposite table )
  {
    m_table = table;
  }

  /**
   * assert all columns will be updated an not disposed before update!
   */
  protected final Set<IExtendedZmlTableColumn> m_columnStack = Collections.synchronizedSet( new LinkedHashSet<IExtendedZmlTableColumn>() );

  public void tableChanged( final IExtendedZmlTableColumn[] update )
  {
    synchronized( this )
    {
      if( Objects.isNotNull( m_job ) )
        m_job.cancel();

      Collections.addAll( m_columnStack, update );

      m_job = new UIJob( "Aktualisiere Tabellen-Layout" )
      {
        @Override
        public IStatus runInUIThread( final IProgressMonitor monitor )
        {
          if( m_table.isDisposed() )
            return Status.CANCEL_STATUS;

          synchronized( this )
          {
            final IExtendedZmlTableColumn[] columns = m_columnStack.toArray( new IExtendedZmlTableColumn[] {} );
            m_columnStack.clear();
            doUpdateColumns( columns );
          }

          return Status.OK_STATUS;
        }
      };

      m_job.setRule( MUTEX_TABLE_UPDATE );
      m_job.schedule( 50 );
    }
  }

  protected void doUpdateColumns( final IExtendedZmlTableColumn[] columns )
  {
    final boolean visible = !ArrayUtils.isEmpty( m_table.getRows() );

    final PackTableColumnVisitor data = new PackTableColumnVisitor();
    final PackIndexColumnsVisitor index = new PackIndexColumnsVisitor( visible );

    for( final IExtendedZmlTableColumn column : columns )
    {
      data.visit( column );
      index.visit( column );
    }

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
}
