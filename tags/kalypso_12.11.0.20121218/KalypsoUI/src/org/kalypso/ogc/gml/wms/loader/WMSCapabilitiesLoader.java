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
package org.kalypso.ogc.gml.wms.loader;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.ogc.gml.wms.utils.KalypsoWMSUtilities;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.util.net.URLGetter;

/**
 * This loader loads the capabilities.
 * 
 * @author Holger Albert
 */
public class WMSCapabilitiesLoader extends AbstractWMSCapabilitiesLoader
{
  /**
   * @param timeout
   *          The timeout for the access.
   */
  public WMSCapabilitiesLoader( final int timeout )
  {
    super( timeout );
  }

  @Override
  public InputStream openCapabilitiesStream( final URL serviceURL, final IProgressMonitor monitor ) throws CoreException
  {
    if( serviceURL == null )
      return null;

    monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.wms.loader.WMSCapabilitiesLoader.0" ), 100 ); //$NON-NLS-1$

    try
    {
      monitor.subTask( Messages.getString( "org.kalypso.ogc.gml.wms.loader.WMSCapabilitiesLoader.1" ) ); //$NON-NLS-1$

      /* Create the capabilities URL. */
      final URL capabilitiesURL = KalypsoWMSUtilities.createCapabilitiesRequest( serviceURL );

      monitor.worked( 25 );
      monitor.subTask( Messages.getString( "org.kalypso.ogc.gml.wms.loader.WMSCapabilitiesLoader.2" ) ); //$NON-NLS-1$

      /* Create a getter for retrieving the URL. */
      final URLGetter getter = URLGetter.createURLGetter( capabilitiesURL, getTimeout(), 0 );

      monitor.worked( 25 );
      monitor.subTask( Messages.getString( "org.kalypso.ogc.gml.wms.loader.WMSCapabilitiesLoader.3" ) ); //$NON-NLS-1$

      /* Execute. */
      final IStatus status = getter.execute( new SubProgressMonitor( monitor, 50 ) );
      if( !status.isOK() )
        throw new CoreException( status );

      return getter.getResult();
    }
    catch( final CoreException ce )
    {
      throw ce;
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, "org.kalypso.ui", Messages.getString( "org.kalypso.ogc.gml.wms.loader.WMSCapabilitiesLoader.5" ) + serviceURL.toExternalForm(), ex ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    finally
    {
      monitor.done();
    }
  }
}