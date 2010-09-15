/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.obstableeditor.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.i18n.Messages;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.tableview.TableView;
import org.kalypso.ogc.sensor.tableview.TableViewColumn;
import org.kalypso.ogc.sensor.tableview.TableViewUtils;
import org.kalypso.ogc.sensor.template.ObsViewItem;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.AbstractEditorActionDelegate;
import org.kalypso.ui.editor.obstableeditor.ObservationTableEditor;
import org.kalypso.util.swt.StatusDialog;

/**
 * Save data
 * 
 * @author schlienger
 */
public class SaveDataAction extends AbstractEditorActionDelegate
{
  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run( final IAction action )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final Collection<IStatus> problems = new ArrayList<IStatus>();
    final TableView tableView = (TableView) ((ObservationTableEditor) getEditor()).getView();

    final ObsViewItem[] itemsAsObsView = tableView.getItems();
    final TableViewColumn[] itemsAsTableViewColumns = Arrays.copyOf( itemsAsObsView, itemsAsObsView.length, TableViewColumn[].class );
    final IObservation[] observations = TableViewUtils.getObservations( itemsAsTableViewColumns );
    for( final IObservation obs : observations )
    {
      final KeyInfo info = pool.getInfo( obs );
      final boolean dirty = info != null && info.isDirty();
      if( dirty )
      {
        // TODO: instead: show first a least of all dirty observations to user and let decide which ones to save
        // then save all in one go.

        final String msg = Messages.getString( "org.kalypso.ui.editor.obstableeditor.actions.SaveDataAction.1", obs.getName() ); //$NON-NLS-1$
        final boolean bConfirm = MessageDialog.openQuestion( getShell(), Messages.getString( "org.kalypso.ui.editor.obstableeditor.actions.SaveDataAction.4" ), msg ); //$NON-NLS-1$
        if( !bConfirm )
          break;

        final Job job = new Job( Messages.getString( "org.kalypso.ui.editor.obstableeditor.actions.SaveDataAction.5" ) + obs.getName() ) //$NON-NLS-1$
        {
          @Override
          public IStatus run( final IProgressMonitor monitor )
          {
            try
            {
              pool.saveObject( obs, monitor );
            }
            catch( final LoaderException e )
            {
              final IStatus status = e.getStatus();
              KalypsoGisPlugin.getDefault().getLog().log( status );
              problems.add( status );
              return status;
            }

            return Status.OK_STATUS;
          }
        };

        job.schedule();
      }
    }

    final String multiMessage = Messages.getString( "org.kalypso.ui.editor.obstableeditor.actions.SaveDataAction.0" ); //$NON-NLS-1$
    final MultiStatus multiStatus = new MultiStatus( KalypsoGisPlugin.getId(), 0, problems.toArray( new IStatus[problems.size()] ), multiMessage, null );
    if( !multiStatus.isOK() )
      new StatusDialog( getShell(), multiStatus, Messages.getString( "org.kalypso.ui.editor.obstableeditor.actions.SaveDataAction.9" ) ).open(); //$NON-NLS-1$ 
  }
}