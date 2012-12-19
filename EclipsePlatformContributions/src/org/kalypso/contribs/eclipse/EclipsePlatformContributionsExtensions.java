/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.osgi.framework.Bundle;

/**
 * Helper class handling the extension-points of this plug-in.
 *
 * @author Gernot Belger
 */
public class EclipsePlatformContributionsExtensions
{
  private static final String EXT_PROJECT_TEMPLATE = "org.kalypso.contribs.eclipseplatform.projectTemplate"; //$NON-NLS-1$

  /**
   * Returns all registered project templates.
   *
   * @param If
   *          non-<code>null</code>, only the project templates of the given category are returned, else, every
   *          registered template project is returned.
   */
  public static ProjectTemplate[] getProjectTemplates( final String categoryId )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IExtensionPoint extensionPoint = registry.getExtensionPoint( EXT_PROJECT_TEMPLATE );
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    final Collection<ProjectTemplate> demoProjects = new ArrayList<>( configurationElements.length );
    for( final IConfigurationElement configurationElement : configurationElements )
    {
      final String id = configurationElement.getAttribute( "id" ); //$NON-NLS-1$
      final String label = configurationElement.getAttribute( "label" ); //$NON-NLS-1$
      final String projectName = configurationElement.getAttribute( "projectName" ); //$NON-NLS-1$
      final String description = configurationElement.getAttribute( "description" ); //$NON-NLS-1$
      final String icon = configurationElement.getAttribute( "icon" ); //$NON-NLS-1$
      final String data = configurationElement.getAttribute( "data" ); //$NON-NLS-1$
      final String category = configurationElement.getAttribute( "category" ); //$NON-NLS-1$

      /* Ignore templates of the wrong category id, if set. */
      if( categoryId == null || categoryId.equals( category ) )
      {
        final URL dataLocation = findLocation( data, configurationElement );
        if( dataLocation == null )
        {
          final String msg = String.format( "Resource not found for project template '%s': %s", label, data ); //$NON-NLS-1$
          final IStatus status = new Status( IStatus.WARNING, EclipsePlatformContributionsPlugin.getID(), msg );
          EclipsePlatformContributionsPlugin.getDefault().getLog().log( status );
        }
        else
          demoProjects.add( new ProjectTemplate( id, label, projectName, description, icon, dataLocation ) );
      }
    }

    return demoProjects.toArray( new ProjectTemplate[demoProjects.size()] );
  }

  private static URL findLocation( final String data, final IConfigurationElement configurationElement )
  {
    final String[] parts = data.split( ":", 2 );
    final String definingBundleId = configurationElement.getContributor().getName();
    final String bundleID;
    final String path;
    if( parts.length == 2 )
    {
      bundleID = parts[0];
      path = parts[1];
    }
    else
    {
      bundleID = definingBundleId;
      path = parts[0];
    }

    final Bundle bundle = Platform.getBundle( bundleID );
    if( bundle == null )
      return null;

    return FileLocator.find( bundle, new Path( path ), null );
  }
}
