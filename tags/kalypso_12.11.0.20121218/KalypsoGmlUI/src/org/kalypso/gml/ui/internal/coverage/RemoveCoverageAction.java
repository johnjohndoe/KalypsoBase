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
package org.kalypso.gml.ui.internal.coverage;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.coverage.CoverageManagementHelper;
import org.kalypso.gml.ui.coverage.CoverageManagementWidget;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;

/**
 * @author Gernot Belger
 */
public class RemoveCoverageAction extends Action implements IUpdateable
{
  private final CoverageManagementWidget m_widget;

  public RemoveCoverageAction( final CoverageManagementWidget widget )
  {
    m_widget = widget;

    setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.12" ) ); //$NON-NLS-1$
    setImageDescriptor( KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_REMOVE ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();

    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();
    final IKalypsoFeatureTheme selectedTheme = m_widget.getSelectedTheme();
    final Runnable refreshRunnable = m_widget.getRefreshRunnable();

    if( ArrayUtils.isEmpty( selectedCoverages ) || selectedTheme == null )
      return;

    if( !MessageDialog.openConfirm( shell, getText(), Messages.getString("RemoveCoverageAction.1") ) ) //$NON-NLS-1$
      return;

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          final CommandableWorkspace workspace = selectedTheme.getWorkspace();

          /* Delete coverage from collection */
          final DeleteFeatureCommand command = new DeleteFeatureCommand( selectedCoverages );
          selectedTheme.postCommand( command, refreshRunnable );

          /* save the model */
          final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
          pool.saveObject( workspace, monitor );

          /*
           * Delete underlying grid file: we do it in a job, later, in order to let the map give-up the handle to the
           * file
           */
          final Job job = new Job( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.20" ) ) //$NON-NLS-1$
          {
            @Override
            protected IStatus run( final IProgressMonitor progress )
            {
              final IStatusCollector log = new StatusCollector( KalypsoGmlUIPlugin.id() );

              for( final ICoverage coverage : selectedCoverages )
              {
                final IStatus status = CoverageManagementHelper.deleteRangeSetFile( coverage );
                log.add( status );
              }

              return log.asMultiStatusOrOK( Messages.getString( "RemoveCoverageAction.0" ) ); //$NON-NLS-1$
            }
          };
          job.setUser( false );
          job.setSystem( true );
          job.schedule( 5000 );

          return Status.OK_STATUS;
        }
        catch( final LoaderException e )
        {
          e.printStackTrace();

          throw new InvocationTargetException( e );
        }
      }
    };

    final IStatus status = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.12" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.22" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void update( )
  {
    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();
    setEnabled( selectedCoverages.length > 0 );
  }
}