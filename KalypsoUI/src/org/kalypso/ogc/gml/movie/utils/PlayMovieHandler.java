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
package org.kalypso.ogc.gml.movie.utils;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.movie.IMovieImageProvider;
import org.kalypso.ogc.gml.movie.MovieDialog;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * This handler starts the movie.
 * 
 * @author Holger Albert
 */
public class PlayMovieHandler extends AbstractHandler
{
  /**
   * The constructor.
   */
  public PlayMovieHandler( )
  {
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event )
  {
    /* Get the evaluation context. */
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();

    /* Get the shell. */
    final Shell shell = (Shell)context.getVariable( ISources.ACTIVE_SHELL_NAME );

    try
    {
      /* Get the workbench. */
      final IWorkbench workbench = PlatformUI.getWorkbench();
      if( workbench == null )
        throw new Exception( Messages.getString( "PlayMovieHandler_0" ) ); //$NON-NLS-1$

      /* Get the progress service. */
      final IProgressService service = workbench.getProgressService();
      if( service == null )
        throw new Exception( Messages.getString( "PlayMovieHandler_1" ) ); //$NON-NLS-1$

      /* Get the map panel. */
      final IMapPanel mapPanel = MapHandlerUtils.getMapPanelChecked( context );

      /* Save it to a temporary file. */
      final GisTemplateMapModell newMapModel = saveAndReloadGismapTemplate( mapPanel );

      /* Create the operation. */
      final MovieImageProviderRunnable operation = new MovieImageProviderRunnable( newMapModel, mapPanel.getBoundingBox() );

      /* Execute the operation. */
      final IStatus status = RunnableContextHelper.execute( service, true, true, operation );
      if( !status.isOK() )
      {
        /* Log the error message. */
        KalypsoGisPlugin.getDefault().getLog().log( status );

        /* Show an error, if the operation has failed. */
        ErrorDialog.openError( shell, Messages.getString( "PlayMovieHandler_2" ), Messages.getString( "PlayMovieHandler_3" ), status ); //$NON-NLS-1$ //$NON-NLS-2$

        return null;
      }

      /* Get the image provider. */
      final IMovieImageProvider imageProvider = operation.getImageProvider();

      /* Create the movie player. */
      final MoviePlayer player = new MoviePlayer( imageProvider );

      /* Create the movie dialog. */
      final MovieDialog movieDialog = new MovieDialog( shell, player );

      /* Open the movie dialog. */
      movieDialog.open();

      return null;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
      MessageDialog.openError( shell, Messages.getString( "PlayMovieHandler_4" ), String.format( Messages.getString( "PlayMovieHandler_5" ), ex.getLocalizedMessage() ) ); //$NON-NLS-1$ //$NON-NLS-2$

      return null;
    }
  }

  private GisTemplateMapModell saveAndReloadGismapTemplate( final IMapPanel mapPanel ) throws IOException
  {
    /* Get the map model. */
    final IMapModell mapModel = mapPanel.getMapModell();
    if( !(mapModel instanceof GisTemplateMapModell) )
      throw new IOException( Messages.getString( "PlayMovieHandler_6" ) ); //$NON-NLS-1$

    return MovieUtilities.cloneMapModel( (GisTemplateMapModell)mapModel, mapPanel.getBoundingBox() );
  }
}