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
package org.kalypso.gml.ui.coverage;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.command.EmptyCommand;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * @author Holger Albert
 */
public class ImportCoverageUtilities
{
  private ImportCoverageUtilities( )
  {
  }

  public static void zoomToCoverages( final ICoverage[] coverages, final CoverageManagementWidget widget )
  {
    final GM_Envelope bbox = FeatureHelper.getEnvelope( coverages );
    if( bbox != null )
    {
      final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( bbox, 1.05 );
      widget.getMapPanel().setBoundingBox( scaledBox );
    }
  }

  public static IStatus saveCoverages( final CoverageManagementWidget widget )
  {
    /* Get the selected theme. */
    final IKalypsoFeatureTheme theme = widget.getSelectedTheme();

    /* TODO: Move into finish method? Very slow, because all the tins are converted as well... */
    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          /* Save the model. */
          /* We cannot allow the model not to be saved, as the underlying files will then not be deleted. */
          monitor.beginTask( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.17" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$
          theme.postCommand( new EmptyCommand( "", false ), widget.getRefreshRunnable() ); //$NON-NLS-1$
          final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
          final CommandableWorkspace workspace = theme.getWorkspace();
          pool.saveObject( workspace, monitor );

          return Status.OK_STATUS;
        }
        catch( final LoaderException e )
        {
          e.printStackTrace();

          throw new InvocationTargetException( e );
        }
      }
    };

    return ProgressUtilities.busyCursorWhile( operation );
  }
}