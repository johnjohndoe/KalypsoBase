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
package org.kalypso.ogc.gml.outline.handler;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.map.utilities.MapUtilities;
import org.kalypso.ogc.gml.mapmodel.IMapModell;

/**
 * This handler exports the legend of the selected layers in the map outline.
 *
 * @author Holger Albert
 */
public class LegendExportHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    /* Get the context. */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /* Get the active workbench part. */
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    if( part == null )
      throw new ExecutionException( Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.1" ) ); //$NON-NLS-1$

    /* Need a shell. */
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final String title = Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.2" ); //$NON-NLS-1$

    /* Get the selected elements. */
    final IStructuredSelection sel = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
    if( sel.isEmpty() )
    {
      MessageDialog.openWarning( shell, title, Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.3" ) ); //$NON-NLS-1$
      return Status.CANCEL_STATUS;
    }

    /* Collect all themes */
    final IKalypsoTheme[] themes = MapHandlerUtils.getSelectedThemes( sel );

    /* Ask user for file */
    final String fileName;
    if( themes.length == 1 )
      fileName = themes[0].getLabel();
    else
      fileName = ((IMapModell) themes[0].getParent( themes[0] )).getName().getValue();
    final String[] filterExtensions = new String[] { "*.png", "*.jpg", "*.gif" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final String[] filterNames = new String[] {
        Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.8" ), Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.9" ), Messages.get( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.10" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    final File legendFile = MapHandlerUtils.showSaveFileDialog( shell, title, fileName, getClass().getName(), filterExtensions, filterNames );
    if( legendFile == null )
      return null;

    /* Create the export job. */
    final Job job = new UIJob( "Export" )
    {
      /**
       * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
       */
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        /* Now save it to a file. */
        final String suffix = FileUtilities.getSuffix( legendFile );

        int format = SWT.IMAGE_PNG;
        if( "PNG".equals( suffix ) ) //$NON-NLS-1$
          format = SWT.IMAGE_PNG;
        else if( "JPG".equals( suffix ) ) //$NON-NLS-1$
          format = SWT.IMAGE_JPEG;
        else if( "GIF".equals( suffix ) ) //$NON-NLS-1$
          format = SWT.IMAGE_GIF;

        /* Export the legends. */
        return MapUtilities.exportLegends( themes, legendFile, format, getDisplay(), null, -1, -1, monitor );
      }
    };
    job.setUser( true );
    job.schedule();

    return Status.OK_STATUS;
  }
}