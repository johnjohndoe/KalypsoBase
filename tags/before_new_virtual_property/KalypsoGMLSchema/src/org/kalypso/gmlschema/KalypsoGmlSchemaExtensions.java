/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.gmlschema;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.adapter.IAnnotationProvider;

/**
 * @author Gernot Belger
 */
public class KalypsoGmlSchemaExtensions
{
  private static IAnnotationProvider[] THE_ANNOTATION_PROVIDERS = null;

  public static IAnnotationProvider[] getAnnotationProviders( )
  {
    if( THE_ANNOTATION_PROVIDERS != null )
      return THE_ANNOTATION_PROVIDERS;

    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.kalypso.gmlschema.annotationProvider" );

    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    final List<IAnnotationProvider> providers = new ArrayList<IAnnotationProvider>( configurationElements.length );

    for( int i = 0; i < configurationElements.length; i++ )
    {
      try
      {
        final IConfigurationElement element = configurationElements[i];
//        final String name = element.getName();
        final Object createExecutableExtension = element.createExecutableExtension( "class" );
        final IAnnotationProvider provider = (IAnnotationProvider) createExecutableExtension;
        providers.add( provider );
      }
      catch( final Throwable t )
      {
        // In order to prevent bad code from other plugins (see Eclipse-PDE-Rules)
        // catch exception here and just log it
        final IStatus status = StatusUtilities.statusFromThrowable( t );
        KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
      }
    }
    
    final IAnnotationProvider[] result = providers.toArray( new IAnnotationProvider[providers.size()] );
    THE_ANNOTATION_PROVIDERS = result;
    return result;
  }
}
