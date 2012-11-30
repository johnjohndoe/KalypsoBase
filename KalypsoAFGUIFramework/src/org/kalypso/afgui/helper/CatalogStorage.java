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
package org.kalypso.afgui.helper;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IStorageEditorInput;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.resources.UrlStorage;
import org.kalypso.contribs.eclipse.ui.editorinput.StorageEditorInput;
import org.kalypso.core.KalypsoCorePlugin;

/**
 * A {@link org.kalypso.contribs.eclipse.core.resources.IStorageWithContext} implementation that is based on the
 * KalypsoCore catalog service.
 * 
 * @author Gernot Belger
 */
public class CatalogStorage extends UrlStorage
{
  public static IStorageEditorInput createEditorInput( final String resource, final IContainer scenarioFolder ) throws CoreException
  {
    Assert.isTrue( resource != null && resource.startsWith( "urn:" ) ); //$NON-NLS-1$

    final String resolvedUrl = KalypsoCorePlugin.getDefault().getCatalogManager().resolve( resource, resource );

    try
    {
      final URL content = new URL( resolvedUrl );

      final URL scenarioContext = ResourceUtilities.createURL( scenarioFolder );

      final CatalogStorage storage = new CatalogStorage( content, scenarioContext );
      return new StorageEditorInput( storage );
    }
    catch( final MalformedURLException | URIException e )
    {
      final String message = String.format( "Failed to resolve urn: %s", resource ); //$NON-NLS-1$

      final IStatus status = new Status( IStatus.ERROR, KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, message, e );
      throw new CoreException( status );
    }
  }

  CatalogStorage( final URL content, final URL context )
  {
    super( content, context );
  }
}
