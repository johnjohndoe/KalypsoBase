/*
 * --------------- Kalypso-Header --------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.view.actions;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.view.ObservationChooser;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.ui.repository.RepositoryDumper;

/**
 * This action starts the dump of the structure of a repository. It will ask the user for a directory name and puts the
 * data there.
 * 
 * @author Holger Albert
 */
public class DumpExtendedHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    final ObservationChooser chooser = (ObservationChooser) part.getAdapter( ObservationChooser.class );

    /* Get the repository. */
    final IRepositoryItem item = chooser.isRepositoryItem();
    if( item == null )
      return null;

    /* Ask the user for a directory. */
    final DirectoryDialog dialog = new DirectoryDialog( shell );
    final String directoryPath = dialog.open();
    if( directoryPath == null )
      return null;

    final File directory = new File( directoryPath );
    if( !directory.exists() )
    {
      /* Should not happen, but secure is secure. */
      return null;
    }

    /* If the 'structure.txt' exists already, ask the user, if he want to delete it. */
    final File structureFile = new File( directory, "structure.txt" ); //$NON-NLS-1$
    if( structureFile.exists() )
    {
      if( !MessageDialog.openConfirm( shell, Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.0" ), Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.2" ) ) )
        return null;

      try
      {
        FileUtils.deleteDirectory( directory );
        FileUtils.forceMkdir( directory );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
        final String title = Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.3" ); //$NON-NLS-1$
        final String message = Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.4" ); //$NON-NLS-1$
        MessageDialog.openError( shell, title, message );
        return null;
      }
    }

    /* Dump the structure. */
    final ICoreRunnableWithProgress runnable = new RepositoryDumper( directory, item );

    final IStatus status = ProgressUtilities.busyCursorWhile( runnable );
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.ogc.sensor.view.DumpStructureHandler.1" ), Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.6" ), status ); //$NON-NLS-1$ //$NON-NLS-2$

    return null;
  }
}