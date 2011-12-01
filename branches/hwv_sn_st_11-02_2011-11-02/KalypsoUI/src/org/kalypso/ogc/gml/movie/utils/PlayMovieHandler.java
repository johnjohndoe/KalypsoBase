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
  public Object execute( ExecutionEvent event )
  {
    /* Get the evaluation context. */
    IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /* Get the shell. */
    Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );

    try
    {
      /* Get the workbench. */
      IWorkbench workbench = PlatformUI.getWorkbench();
      if( workbench == null )
        throw new Exception( "Es wurde keine Workbench gefunden..." );

      /* Get the progress service. */
      IProgressService service = workbench.getProgressService();
      if( service == null )
        throw new Exception( "Es wurde kein Progress-Service gefunden..." );

      /* Get the map panel. */
      IMapPanel mapPanel = MapHandlerUtils.getMapPanelChecked( context );

      /* Save it to a temporary file. */
      GisTemplateMapModell newMapModel = saveAndReloadGismapTemplate( mapPanel );

      /* Create the operation. */
      MovieImageProviderRunnable operation = new MovieImageProviderRunnable( newMapModel, mapPanel.getBoundingBox() );

      /* Execute the operation. */
      IStatus status = RunnableContextHelper.execute( service, true, true, operation );
      if( !status.isOK() )
      {
        /* Log the error message. */
        KalypsoGisPlugin.getDefault().getLog().log( status );

        /* Show an error, if the operation has failed. */
        ErrorDialog.openError( shell, "Film starten", "Initialisierung des Films ist fehlgeschlagen...", status );

        return null;
      }

      /* Get the image provider. */
      IMovieImageProvider imageProvider = operation.getImageProvider();

      /* Create the movie player. */
      MoviePlayer player = new MoviePlayer( imageProvider );

      /* Create the movie dialog. */
      MovieDialog movieDialog = new MovieDialog( shell, player );

      /* Open the movie dialog. */
      movieDialog.open();

      return null;
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
      MessageDialog.openError( shell, "Film starten", String.format( "Konnte den Film nicht abspielen: %s", ex.getLocalizedMessage() ) );

      return null;
    }
  }

  private GisTemplateMapModell saveAndReloadGismapTemplate( IMapPanel mapPanel ) throws IOException
  {
    /* Get the map model. */
    IMapModell mapModel = mapPanel.getMapModell();
    if( !(mapModel instanceof GisTemplateMapModell) )
      throw new IOException( "Ungültiges Karten-Modell..." );

    return MovieUtilities.cloneMapModel( (GisTemplateMapModell) mapModel, mapPanel.getBoundingBox() );
  }
}