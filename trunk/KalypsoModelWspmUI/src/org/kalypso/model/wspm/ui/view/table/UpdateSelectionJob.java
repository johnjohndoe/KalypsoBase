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
package org.kalypso.model.wspm.ui.view.table;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.IProfilProvider;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class UpdateSelectionJob extends UIJob
{

  private final TableView m_tableView;

  public UpdateSelectionJob( final TableView tableView )
  {
    super( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.1" ) );
    m_tableView = tableView;

    setUser( false );
    setSystem( true );
  }

  @Override
  public IStatus runInUIThread( final IProgressMonitor monitor )
  {
    final TableViewer viewer = m_tableView.getTupleResultViewer();
    if( Objects.isNull( viewer ) || viewer.getTable().isDisposed() )
      return Status.CANCEL_STATUS;

    EditorFirstAdapterFinder.<IProfilProvider> instance();
    final IProfil profile = m_tableView.getProfil();
    final IRangeSelection selection = profile.getSelection();

    final IRecord[] records = toSelection( profile, selection );

    viewer.setSelection( new StructuredSelection( records ) );
    viewer.reveal( records );

    return Status.OK_STATUS;

  }

  private IRecord[] toSelection( final IProfil profile, final IRangeSelection selection )
  {
    if( selection.isEmpty() )
      return new IRecord[] {};

    final IProfileRecord[] points = selection.toPoints();
    if( ArrayUtils.isNotEmpty( points ) )
      return toRecords( points ); // table is based on original tuple result!

    final IProfileRecord point = profile.findPreviousPoint( selection.getRange().getMinimum() );
    if( Objects.isNotNull( point ) )
      return new IRecord[] { point.getRecord() };

    return new IRecord[] {};
  }

  private IRecord[] toRecords( final IProfileRecord[] selection )
  {
    if( ArrayUtils.isEmpty( selection ) )
      return new IRecord[] {};

    final Set<IRecord> records = new LinkedHashSet<>();
    for( final IProfileRecord record : selection )
    {
      records.add( record.getRecord() );
    }

    return records.toArray( new IRecord[] {} );
  }
}
