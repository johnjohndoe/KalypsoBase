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
package de.renew.workflow.connector.internal.cases;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsExtensions;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;

import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;
import de.renew.workflow.connector.internal.WorkflowConnectorPlugin;

/**
 * Helps to ensure backwards compatibility of kalypso projects.
 *
 * @author Gernot Belger
 */
public class ScenarioCompatibilityHelper
{
  // FIXME: probably (hopefully) not needed any more...; remove?
  public static boolean ensureBackwardsCompatibility( final ScenarioHandlingProjectNature nature )
  {
    // FIXME: this is dirty fix only for this release 2.3
    // should be implemented in other way, we just do not have any time now
    try
    {
      if( nature == null || !nature.getProject().hasNature( "org.kalypso.kalypso1d2d.pjt.Kalypso1D2DProjectNature" ) ) //$NON-NLS-1$
        return true;

      // FIXME: the whole code here does not belong to this place -> this is a hidden dependency to 1d2d: bad!
      // TODO: instead implement an extension point mechanism
      final ProjectTemplate[] lTemplate = EclipsePlatformContributionsExtensions.getProjectTemplates( "org.kalypso.kalypso1d2d.pjt.projectTemplate" ); //$NON-NLS-1$
      try
      {
        // FIXME: this very probably does not work correctly or any more at all!

        /* Unpack project from template */
        final File destinationDir = nature.getProject().getLocation().toFile();
        final URL data = lTemplate[0].getData();
        final String location = data.toString();
        final String extension = FilenameUtils.getExtension( location );
        if( "zip".equalsIgnoreCase( extension ) ) //$NON-NLS-1$
        {
          // TODO: this completely overwrite the old project content, is this intended?
          ZipUtilities.unzip( data.openStream(), destinationDir, false );
        }
        else
        {
          final URL fileURL = FileLocator.toFileURL( data );
          final File dataDir = FileUtils.toFile( fileURL );
          if( dataDir == null )
          {
            return false;
          }

          // FIXME: this only fixes the basic scenario, is this intended?

          final IOFileFilter lFileFilter = new WildcardFileFilter( new String[] { "wind.gml" } ); //$NON-NLS-1$
          final IOFileFilter lDirFilter = TrueFileFilter.INSTANCE;
          final Collection< ? > windFiles = FileUtils.listFiles( destinationDir, lFileFilter, lDirFilter );

          if( dataDir.isDirectory() && (windFiles == null || windFiles.size() == 0) )
          {
            final WildcardFileFilter lCopyFilter = new WildcardFileFilter( new String[] { "*asis", "models", "wind.gml" } ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            FileUtils.copyDirectory( dataDir, destinationDir, lCopyFilter );
          }
          else
          {
            return true;
          }
        }
      }
      catch( final Throwable t )
      {
        t.printStackTrace();
        return false;
      }

      nature.getProject().refreshLocal( IResource.DEPTH_INFINITE, null );
    }
    catch( final CoreException e )
    {
      // FIXME: this is no error handling; the users are not informed and will stumble over following errors caued by
      // this problem

      WorkflowConnectorPlugin.getDefault().getLog().log( e.getStatus() );
      e.printStackTrace();
      return false;
    }

    return true;
  }
}
