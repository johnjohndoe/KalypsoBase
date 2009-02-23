/*----------------    FILE HEADER Kimport java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
rmany
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
package org.apache.commons.vfs;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public class Activator extends Plugin
{
  private static final String EXTENSION_POINT_ID = "org.apache.commons.vfs.provider";

  private static HashMap<String, IConfigurationElement> THE_PROVIDER_LOCATIONS;

  @Override
  public void start( final BundleContext context ) throws Exception
  {
    super.start( context );
    readExtensions();
    final DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS.getManager();
    for( final Map.Entry<String, IConfigurationElement> entry : THE_PROVIDER_LOCATIONS.entrySet() )
    {
      final IConfigurationElement element = entry.getValue();

      final String scheme = element.getAttribute( "scheme" );
      final VFSProviderExtension provider = (VFSProviderExtension) element.createExecutableExtension( "class" );
      fsManager.addProvider( scheme, provider.getProvider() );
      provider.init( fsManager );
    }
  }

  private static void readExtensions( )
  {
    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    if( THE_PROVIDER_LOCATIONS == null )
    {
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( EXTENSION_POINT_ID );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      THE_PROVIDER_LOCATIONS = new HashMap<String, IConfigurationElement>( configurationElements.length );
      for( final IConfigurationElement element : configurationElements )
      {
        final String bundleName = element.getContributor().getName();
        THE_PROVIDER_LOCATIONS.put( bundleName, element );
      }
    }
  }
}
