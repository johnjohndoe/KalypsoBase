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

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.observation.result.IRecord;

/**
 * @author Dirk Kuch
 */
public class RefreshProfileMarkerJob extends UIJob
{

  private final TableView m_tableView;

  public RefreshProfileMarkerJob( final TableView tableView )
  {
    super( Messages.getString( "org.kalypso.model.wspm.ui.view.table.TableView.0" ) ); //$NON-NLS-1$
    m_tableView = tableView;

    setUser( false );
    setSystem( true );
  }

  @Override
  public IStatus runInUIThread( final IProgressMonitor monitor )
  {
    final IProfil profile = m_tableView.getProfil();
    if( Objects.isNull( profile ) )
      return Status.CANCEL_STATUS;
// TODO: nur die ge‰nderten Marker neu zeichnen
// MarkerIndex markers=profile.getProblemMarker();
// final IRecord[] points=markers.getRecords();
    final IRecord[] points = profile.getResult().toArray( new IRecord[] {} );

    if( ArrayUtils.isNotEmpty( points ) )
    {
      final TableViewer viewer = m_tableView.getTupleResultViewer();
      if( Objects.isNotNull( viewer ) && !viewer.getTable().isDisposed() )
        // viewer.update( points, new String[] { "" } ); //$NON-NLS-1$
        ViewerUtilities.update( viewer, points, new String[] { "" }, true );//$NON-NLS-1$
    }

    m_tableView.updateProblemView();

    return Status.OK_STATUS;
  }
}
