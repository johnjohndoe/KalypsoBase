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
package org.kalypso.ogc.gml.wms.deegree;

import java.net.URL;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.CharsetUtils;
import org.deegree.framework.util.NetWorker;
import org.deegree.framework.util.StringTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;

/**
 * @author Holger Albert
 */
public class RemoteWMService extends org.deegree.ogcwebservices.wms.RemoteWMService
{
  private static ILogger LOG = LoggerFactory.getLogger( org.deegree.ogcwebservices.wms.RemoteWMService.class );

  private static final String GETFEATUREINFO_NAME = "GetFeatureInfo"; //$NON-NLS-1$

  private static final String FEATUREINFO_NAME = "FeatureInfo"; //$NON-NLS-1$

  public RemoteWMService( final WMSCapabilities wmsCapabilities )
  {
    super( wmsCapabilities );
  }

  /**
   * Copied and modified for handling more then only the gml response.
   * 
   * @see org.deegree.ogcwebservices.wms.RemoteWMService#handleFeatureInfo(org.deegree.ogcwebservices.wms.operation.GetFeatureInfo)
   */
  @Override
  protected Object handleFeatureInfo( final GetFeatureInfo request ) throws OGCWebServiceException
  {
    URL url = null;

    if( request.getVersion().equals( "1.0.0" ) ) //$NON-NLS-1$
    {
      url = addresses.get( FEATUREINFO_NAME );
    }
    else
    {
      url = addresses.get( GETFEATUREINFO_NAME );
    }

    if( url == null )
    {
      final String msg = Messages.getMessage( org.kalypso.ogc.gml.wms.deegree.Messages.RemoteWMService_0, capabilities.getServiceIdentification().getTitle() );
      throw new OGCWebServiceException( msg );
    }

    final String us = constructRequestURL( request.getRequestParameter(), OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );

    String result = null;
    try
    {
      LOG.logDebug( "GetFeatureInfo: ", us ); //$NON-NLS-1$
      final URL ur = new URL( us );
      // get map from the remote service
      final NetWorker nw = new NetWorker( ur );
      final byte[] b = nw.getDataAsByteArr( 20000 );
      final String contentType = nw.getContentType();

      // extract content charset if available; otherwise use configured system charset
      String charset = null;
      LOG.logDebug( "content type: ", contentType ); //$NON-NLS-1$
      if( contentType != null )
      {
        final String[] tmp = StringTools.toArray( contentType, ";", false ); //$NON-NLS-1$
        if( tmp.length == 2 )
        {
          charset = tmp[1].substring( tmp[1].indexOf( '=' ) + 1, tmp[1].length() );
        }
        else
        {
          charset = CharsetUtils.getSystemCharset();
        }
      }
      else
      {
        charset = CharsetUtils.getSystemCharset();
      }

      result = new String( b, charset );
    }
    catch( final Exception e )
    {
      LOG.logError( e.getMessage(), e );
      final String msg = Messages.getMessage( "REMOTEWMS_GFI_GENERAL_ERROR", capabilities.getServiceIdentification().getTitle(), us ); //$NON-NLS-1$
      throw new OGCWebServiceException( "RemoteWMS:handleFeatureInfo", msg ); //$NON-NLS-1$
    }

    return result;
  }

  // checks for excessive &
  private static String constructRequestURL( final String params, final String url )
  {
    if( url.endsWith( "?" ) && params.startsWith( "&" ) ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      return url + params.substring( 1 );
    }

    return url + params;
  }
}