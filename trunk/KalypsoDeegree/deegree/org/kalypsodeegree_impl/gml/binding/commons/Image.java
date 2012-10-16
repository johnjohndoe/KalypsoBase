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
package org.kalypsodeegree_impl.gml.binding.commons;

import java.net.URI;
import java.net.URISyntaxException;

import javax.activation.MimeType;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.URIUtil;
import org.kalypso.commons.java.activation.MimeTypeUtils;
import org.kalypso.commons.xml.NS;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree_impl.model.feature.Feature_Impl;

/**
 * Feature-Binding for common:Image type.
 *
 * @author Gernot Belger
 */
public class Image extends Feature_Impl
{
  public static final QName FEATURE_IMAGE = new QName( NS.COMMON, "Image" ); //$NON-NLS-1$

  private static final QName PROPERTY_MIME_TYPE = new QName( NS.COMMON, "mimeType" ); //$NON-NLS-1$

  public static final QName PROPERTY_URI = new QName( NS.COMMON, "uri" ); //$NON-NLS-1$

  public Image( final Object parent, final IRelationType parentRelation, final IFeatureType ft, final String id, final Object[] propValues )
  {
    super( parent, parentRelation, ft, id, propValues );
  }

  public void setUri( final URI uri )
  {
    if( uri == null )
      setProperty( PROPERTY_URI, null );
    else
    {
      final String unencoded = URIUtil.toUnencodedString( uri );
      setProperty( PROPERTY_URI, unencoded );
    }
  }

  public URI getUri( )
  {
    try
    {
      final String uriString = getProperty( PROPERTY_URI, String.class );
      if( StringUtils.isBlank( uriString ) )
        return null;

      return URIUtil.fromString( uriString );
    }
    catch( final URISyntaxException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  public void setMimeType( final MimeType mimeType )
  {
    if( mimeType == null )
      setProperty( PROPERTY_MIME_TYPE, null );
    else
      setProperty( PROPERTY_MIME_TYPE, mimeType.toString() );
  }

  public MimeType getMimeType( )
  {
    final String property = getProperty( PROPERTY_MIME_TYPE, String.class );
    if( StringUtils.isBlank( property ) )
      return null;

    return MimeTypeUtils.createQuietly( property );
  }
}