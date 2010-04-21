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
package org.kalypso.project.database.client.extension.pages.module;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.i18n.I18nUtils;
import org.kalypso.project.database.client.extension.IKalypsoModule;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractKalypsoModulePage implements IKalypsoModulePage
{
  private final IKalypsoModule m_module;

  public AbstractKalypsoModulePage( final IKalypsoModule module )
  {
    m_module = module;
  }

  public IKalypsoModule getModule( )
  {
    return m_module;
  }

  protected URL getInfoURL( final Class< ? > clazz, final Plugin plugin )
  {
    final IPath stateLocation = plugin.getStateLocation();
    final File targetDir = new File( stateLocation.toFile(), "infoPage" ); //$NON-NLS-1$

    try
    {
      /* info page of plugin */
      final URL zipURL = I18nUtils.getLocaleResource( clazz, "infoPage", ".zip" ); //$NON-NLS-1$ //$NON-NLS-2$
      ZipUtilities.unzip( zipURL, targetDir );

      final File targetFile = I18nUtils.getLocaleFile( targetDir, "index", ".html" ); //$NON-NLS-1$ //$NON-NLS-2$
      if( targetFile == null )
        return null;

      return targetFile.toURI().toURL();
    }
    catch( final Exception e )
    {
      plugin.getLog().log( StatusUtilities.statusFromThrowable( e ) );
      return null;
    }

  }
}
