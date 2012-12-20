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
package org.kalypso.ogc.gml.featureview.control;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.URIUtil;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public abstract class AbstractImageFeatureControl extends AbstractFeatureControl
{
  private static final QName QNAME_STRING = new QName( XMLConstants.W3C_XPATH_DATATYPE_NS_URI, "string" ); //$NON-NLS-1$

  public AbstractImageFeatureControl( final IPropertyType ftp )
  {
    super( ftp );
  }

  public AbstractImageFeatureControl( final Feature feature, final IPropertyType ftp )
  {
    super( feature, ftp );
  }

  protected String getImagePath( )
  {
    final Feature feature = getFeature();
    final IPropertyType pt = getFeatureTypeProperty();

    if( feature == null || pt == null || GMLSchemaUtilities.substitutes( feature.getFeatureType(), QNAME_STRING ) )
      return null;

    final String uriString = (String)feature.getProperty( pt );
    if( uriString == null )
      return ""; //$NON-NLS-1$

    return uriString;
  }

  protected URL resolveImagePath( final String imgPath ) throws MalformedURLException, URISyntaxException
  {
    // REMARK: prevent empty string to resolve to gml file location
    if( StringUtils.isBlank( imgPath ) )
      return null;

    final Feature feature = getFeature();
    final GMLWorkspace workspace = feature.getWorkspace();
    final URL context = workspace.getContext();

    final URL location = new URL( context, imgPath );
    final URI uriEncoded = URIUtil.toURI( location );
    final URL urlEncoded = URIUtil.toURL( uriEncoded );

    return translateLocation( urlEncoded );
  }

  /** Translates platform url's to file url's */
  private URL translateLocation( final URL url )
  {
    if( url == null )
      return null;

    final IFile file = ResourceUtilities.findFileFromURL( url );
    if( file == null )
      return url;

    final URI locationURI = file.getLocationURI();
    if( locationURI == null )
      return url;

    try
    {
      return locationURI.toURL();
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      return url;
    }
  }
}