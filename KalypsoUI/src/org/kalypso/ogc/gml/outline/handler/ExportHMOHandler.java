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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.serialize.Gml2HmoConverter;
import org.kalypso.ogc.gml.serialize.GmlTriSurface2HmoConverter;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

/**
 * This handler exports the selected layer in the map outline as a .hmo file
 * 
 * @author felipe maximino
 */
public class ExportHMOHandler extends AbstractHandler
{

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( ExecutionEvent event ) throws ExecutionException
  {
    /* gets the context */
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    /* gets active part */
    final IWorkbenchPart part = (IWorkbenchPart) context.getVariable( ISources.ACTIVE_PART_NAME );
    if( part == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportHMOHandler.1" ) ); //$NON-NLS-1$

    /* gets the shell */
    final Shell shell = part.getSite().getShell();
    final String title = Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportHMOHandler.2" ); //$NON-NLS-1$

    /* gets the selected elements. */
    final IStructuredSelection sel = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
   
    final IKalypsoFeatureTheme theme = (IKalypsoFeatureTheme) sel.getFirstElement();
    final FeatureList featureList = theme == null ? null : theme.getFeatureList();
    if( featureList == null || featureList.size() == 0 )
    {
      MessageDialog.openWarning( shell, title, Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportHMOHandler.3" ) ); //$NON-NLS-1$
      return Status.CANCEL_STATUS;
    }

    /* asks user for file */
    final String fileName = theme.getLabel();
    final String[] filterExtensions = new String[] { "*.hmo" }; //$NON-NLS-1$ //$NON-NLS-2$
    final String[] filterNames = new String[] { Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportHMOHandler.9" ) }; //$NON-NLS-1$ //$NON-NLS-2$

    final File file = MapHandlerUtils.showSaveFileDialog( shell, title, fileName, "hmoExport", filterExtensions, filterNames ); //$NON-NLS-1$
    if( file == null )
      return null;

    /* verifies file extension */
    final String result = file.getAbsolutePath();
    final String hmoFileBase;
    if( !result.toLowerCase().endsWith( ".hmo" ) ) //$NON-NLS-1$ //$NON-NLS-2$
      hmoFileBase = FileUtilities.setSuffix( result, "hmo" );
    else
      hmoFileBase = result;
    
    Feature feature = (Feature) featureList.get( 0 );
    final GM_TriangulatedSurface geometryProperty = (GM_TriangulatedSurface)feature.getDefaultGeometryProperty();
    final Gml2HmoConverter converter = new GmlTriSurface2HmoConverter(geometryProperty);

    /* Create the export job. */
    final Job job = new Job( title + " - " + result ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {        
        try
        {          
          converter.writeHmo(new File(hmoFileBase), monitor);
        }
        catch( Exception e )
        {
          return new Status(IStatus.ERROR,KalypsoGisPlugin.getId(), "Failed to write HMO", e);
        }
        
        return Status.OK_STATUS;
      }
    };
    job.setUser( true );
    job.schedule();  

    return Status.OK_STATUS;
  }
}
