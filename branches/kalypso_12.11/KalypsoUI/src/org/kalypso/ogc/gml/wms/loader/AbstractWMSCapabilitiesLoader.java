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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.deegree.framework.xml.XMLException;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.ogcwebservices.getcapabilities.InvalidCapabilitiesException;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.net.Proxy;
import org.kalypso.ogc.gml.wms.deegree.document.KalypsoWMSCapabilitiesDocument;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.xml.sax.SAXException;

/**
 * This loader loads the capabilities.
 * 
 * @author Holger Albert
 */
public abstract class AbstractWMSCapabilitiesLoader implements ICapabilitiesLoader
{
  private static final String STR_FAILED_TO_LOAD_CAPABILITIES_DOCUMENT = Messages.getString( "AbstractWMSCapabilitiesLoader.0" ); //$NON-NLS-1$

  /**
   * The timeout for the access.
   */
  private final int m_timeout;

  /**
   * The constructor.
   * 
   * @param timeout
   *          The timeout for the access.
   */
  public AbstractWMSCapabilitiesLoader( final int timeout )
  {
    m_timeout = timeout;
  }

  protected final int getTimeout( )
  {
    return m_timeout;
  }

  protected abstract InputStream openCapabilitiesStream( final URL serviceURL, final IProgressMonitor monitor ) throws CoreException;

  /**
   * Loads the capabilities for the given service.
   * 
   * @param monitor
   *          A progress monitor.
   */
  @Override
  public final WMSCapabilities load( final URL serviceURL, final IProgressMonitor monitor ) throws CoreException
  {
    /* HACK: Initialize once, to init the java-proxy settings, in case they are not set. */
    Proxy.getProxyService( KalypsoGisPlugin.getDefault() );

    // FIXME: if the server returns a bad document or an error document, this gets lost here.
    // Instead, we should read the response as a string and then try to parse it.

    try( InputStream inputStream = openCapabilitiesStream( serviceURL, monitor ) )
    {
      /* This is a capabilities document from deegree, which was overwritten by Kalypso. */
      final KalypsoWMSCapabilitiesDocument doc = new KalypsoWMSCapabilitiesDocument();

      /* Load the capabilities xml. */
      doc.load( inputStream, XMLFragment.DEFAULT_URL );

      /* Create the capabilities. */
      final WMSCapabilities capabilities = (WMSCapabilities)doc.parseCapabilities();
      if( capabilities == null )
      {
        final String messsage = Messages.getString( "org.kalypso.ogc.gml.wms.deegree.DeegreeWMSUtilities.0" ); //$NON-NLS-1$
        throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), messsage ) );
      }

      return capabilities;
    }
    // TODO: better/more specific error messages
    catch( final IOException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), STR_FAILED_TO_LOAD_CAPABILITIES_DOCUMENT, e ) );
    }
    catch( final XMLException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), STR_FAILED_TO_LOAD_CAPABILITIES_DOCUMENT, e ) );
    }
    catch( final SAXException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), STR_FAILED_TO_LOAD_CAPABILITIES_DOCUMENT, e ) );
    }
    catch( final InvalidCapabilitiesException e )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), STR_FAILED_TO_LOAD_CAPABILITIES_DOCUMENT, e ) );
    }
  }
}