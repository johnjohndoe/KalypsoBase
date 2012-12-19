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
package org.kalypso.zml.ui.chart.grafik;

import java.net.URI;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.template.obsdiagview.Obsdiagview;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class OpenDiagramInGrafikExeOperation extends WorkspaceModifyOperation
{
  private final IObservation[] m_observations;

  public OpenDiagramInGrafikExeOperation( final IObservation[] observations )
  {
    m_observations = observations;
  }

  @Override
  protected void execute( final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      IFolder tempFolder = null;

      final DiagView diagView = new DiagView( StringUtils.EMPTY, "no legend", false ); //$NON-NLS-1$ //$NON-NLS-1$
      for( final IObservation observation : m_observations )
      {
        diagView.addObservation( new PlainObsProvider( observation, null ), observation.getName(), ObsView.DEFAULT_ITEM_DATA );

        if( tempFolder == null )
          tempFolder = getTempFolder( observation.getHref() );
      }

      final Obsdiagview odt = DiagViewUtils.buildDiagramTemplateXML( diagView, null );
      final IStatus status = GrafikLauncher.startGrafikODT( Messages.OpenDiagramInGrafikExeOperation_0, odt, tempFolder, monitor );
      if( !status.isOK() )
        throw new CoreException( status );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();

      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, Messages.OpenDiagramInGrafikExeOperation_1, e );
      throw new CoreException( status );
    }
  }

  private IFolder getTempFolder( final String href ) throws CoreException
  {
    try
    {
      final IPath grafikPath = Path.fromOSString( "grafik" ); //$NON-NLS-1$

      final URI uri = URIUtil.fromString( href );
      final URL url = uri.toURL();

      final IFile eclipseFile = ResourceUtilities.findFileFromURL( url );
      if( eclipseFile != null )
        return eclipseFile.getParent().getFolder( grafikPath );

      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      final IFile[] files = root.findFilesForLocationURI( uri );
      for( final IFile file : files )
      {
        final IFolder folder = (IFolder) file.getParent();
        return folder.getFolder( grafikPath );
      }

      return null;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoZmlUI.PLUGIN_ID, Messages.OpenDiagramInGrafikExeOperation_2, e );
      throw new CoreException( status );
    }
  }

}