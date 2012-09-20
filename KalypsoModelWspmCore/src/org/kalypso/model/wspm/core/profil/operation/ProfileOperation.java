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
package org.kalypso.model.wspm.core.profil.operation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.IllegalProfileOperationException;
import org.kalypso.model.wspm.core.profil.changes.IllegalChange;

public final class ProfileOperation extends AbstractOperation
{
  private final List<IProfileChange> m_undoChanges = new ArrayList<>();

  private final IProfile m_profile;

  private final List<IProfileChange> m_changes = new ArrayList<>();

  private final boolean m_rollbackAll;

  private boolean m_canUndo = true;

  public ProfileOperation( final String label, final IProfile profile, final boolean rollbackAll )
  {
    this( label, profile, new IProfileChange[] {}, rollbackAll );
  }

  public ProfileOperation( final String label, final IProfile profile, final IProfileChange change, final boolean rollbackAll )
  {
    this( label, profile, new IProfileChange[] { change }, rollbackAll );
  }

  public ProfileOperation( final String label, final IProfile profile, final IProfileChange[] changes, final boolean rollbackAll )
  {
    super( label );

    addContext( new ProfileUndoContext( profile ) );

    m_changes.addAll( Arrays.asList( changes ) );
    m_profile = profile;
    m_rollbackAll = rollbackAll;
  }

  public void addChange( final IProfileChange... changes )
  {
    if( ArrayUtils.isEmpty( changes ) )
      return;

    Collections.addAll( m_changes, changes );
  }

  protected IProfile getProfil( )
  {
    return m_profile;
  }

  @Override
  public IStatus redo( final IProgressMonitor monitor, final IAdaptable info )
  {
    return doit( monitor, info, new ArrayList<IProfileChange>(), m_changes );
  }

  @Override
  public IStatus undo( final IProgressMonitor monitor, final IAdaptable info )
  {
    return doit( monitor, info, new ArrayList<IProfileChange>(), m_undoChanges );
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor, final IAdaptable info )
  {
    return doit( monitor, info, m_undoChanges, m_changes );
  }

  private IStatus doit( final IProgressMonitor monitor, @SuppressWarnings("unused")//$NON-NLS-1$
  final IAdaptable info, final List<IProfileChange> undoChanges, final List<IProfileChange> changes )
  {
    m_profile.startTransaction( this );

    monitor.beginTask( org.kalypso.model.wspm.core.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.operation.ProfilOperation.0" ), changes.size() ); //$NON-NLS-1$
    final List<IProfileChange> doneChanges = new ArrayList<>();
    try
    {
      for( final IProfileChange change : changes )
      {
        // FIXME: suspect, why can any change be null here=?
        final IProfileChange undoChange;
        if( change == null )
          undoChange = null;
        else
        {
          undoChange = change.doChange();
        }

        if( undoChange instanceof IllegalChange )
          throw new IllegalProfileOperationException( undoChange.toString(), change );

        doneChanges.add( change );
        undoChanges.add( 0, undoChange );
        monitor.worked( 1 );
      }
    }
    catch( final IllegalProfileOperationException e )
    {
      if( m_rollbackAll )
      {
        rollback( undoChanges );
        doneChanges.clear();
      }
      final Display d = PlatformUI.getWorkbench().getDisplay();
      d.asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          if( !d.isDisposed() )
          {
            final IProfileChange change = e.getProfilChange();
            if( change == null )
            {
              MessageDialog.openWarning( d.getActiveShell(), org.kalypso.model.wspm.core.i18n.Messages.getString( "org.kalypso.model.wspm.ui.profil.operation.ProfilOperation.1" ), e.getMessage() ); //$NON-NLS-1$
            }
            else
            {
              MessageDialog.openWarning( d.getActiveShell(), e.getMessage(), change.toString() );
            }
          }
        }
      } );
    }
    finally
    {
      // auf jeden Fall monitor beenden und
      // einen fire auf allen changes absetzen (zuviel ist nicht schlimm)
      // TODO: doch! fast jede aktion auf einem Profil feuert zwei events...!
      m_canUndo = undoChanges.size() > 0;
      monitor.done();

      m_profile.stopTransaction( this );

    }
    // auf jeden Fall OK zurückgeben da sonst die UNDO-Liste nicht gefüllt wird
    return Status.OK_STATUS;
  }

  private void rollback( final List<IProfileChange> changes )
  {
    for( final IProfileChange undo : changes )
    {
      try
      {
        if( undo != null )
        {
          undo.doChange();
        }
      }
      catch( final IllegalProfileOperationException e )
      {
        // should never happen
        e.printStackTrace();
      }
    }
  }

  @Override
  public boolean canUndo( )
  {
    return m_canUndo;
  }

  public boolean isEmpty( )
  {
    return m_changes.isEmpty();
  }

}
