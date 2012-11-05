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
package org.kalypsodeegree_impl.model.feature;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.URIUtil;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IXLinkedFeature;

/**
 * Some helper methods for easy handling of linked features.
 * 
 * @author Gernot Belger
 */
public final class FeatureLinkUtils
{
  private FeatureLinkUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Inserts or sets a link to a feature inside the same workspace.
   * 
   * @param pos
   *          Insert position. If <code>-1</code> the new element is inserted at the end of the list.
   */
  public static void insertLink( final Feature feature, final IRelationType linkRelation, final int pos, final String href ) throws Exception
  {
    if( linkRelation.isList() )
    {
      final FeatureList list = (FeatureList)feature.getProperty( linkRelation );
      list.insertLink( pos, href );
    }
    else
    {
      feature.setLink( linkRelation, href );
    }
  }

  /**
   * @return position of link or -1 if relation does not exists
   */
  public static int indexOfLink( final Feature srcFE, final IRelationType relation, final IXLinkedFeature destLink )
  {
    if( !relation.isList() )
      return -1;

    if( destLink == null )
      return -1;

    final FeatureList list = (FeatureList)srcFE.getProperty( relation );
    for( int i = 0; i < list.size(); i++ )
    {
      final Object object = list.get( i );
      final IXLinkedFeature asXLink = asXLink( destLink, relation, object );

      if( ObjectUtils.equals( destLink, asXLink ) )
        return i;
    }

    return -1;
  }

  /**
   * Resolved a property valeu as an xlink.<br>
   * If the property is already a link, just return it.<br/>
   * If the property is a String, returns an internal xlink.<br/>
   * 
   * @throws IllegalStateException
   *           If the property is not a link.
   */
  public static IXLinkedFeature asXLink( final Feature feature, final IRelationType relationType, final Object property )
  {
    if( property instanceof IXLinkedFeature )
      return (IXLinkedFeature)property;

    if( property instanceof String )
    {
      final IFeatureType targetType = relationType.getTargetFeatureType();
      final String href = String.format( "#%s", property );
      return FeatureFactory.createXLink( feature, relationType, targetType, href );
    }

    if( property == null )
      return null;

    throw new IllegalStateException();
  }

  public static Object findMember( final Feature sourceFeature, final IRelationType relation, final Feature targetFeature )
  {
    if( relation.isList() )
    {
      final FeatureList list = (FeatureList)sourceFeature.getProperty( relation );
      return findMember( list, targetFeature );
    }
    else
    {
      final Object property = sourceFeature.getProperty( relation );
      if( isSameOrLinkTo( targetFeature, property ) )
        return property;
    }

    return null;
  }

  public static Object findMember( final FeatureList list, final Feature targetFeature )
  {
    for( final Object property : list )
    {
      if( isSameOrLinkTo( targetFeature, property ) )
        return property;
    }

    return null;
  }

  /**
   * Checks if a property is a link to or the same thing as a given feature.
   */
  public static boolean isSameOrLinkTo( final Feature feature, final Object property )
  {
    if( feature == property )
      return true;

    if( property instanceof String )
      return feature.getId().equals( property );

    if( property instanceof IXLinkedFeature )
      return ((IXLinkedFeature)property).getFeature() == feature;

    return false;
  }

  public static String findLinkPath( final Feature toLink, final GMLWorkspace sourceWorkspace )
  {
    final String id = toLink.getId();

    final GMLWorkspace linkedWorkspace = toLink.getWorkspace();

    /* Internal link, no uri */
    if( linkedWorkspace == sourceWorkspace )
      return id;

    final URL targetContext = linkedWorkspace.getContext();
    final URL sourceContext = sourceWorkspace.getContext();

    String path = null;
    try
    {
      final URI targetURI = targetContext.toURI();
      final URI sourceURI = sourceContext.toURI();
      final URI relativeURI = URIUtil.makeRelative( targetURI, sourceURI );
      path = relativeURI.toString();
    }
    catch( final URISyntaxException e )
    {
      // TODO: do we need this fallback?
      e.printStackTrace();
      path = targetContext.toString();
    }

    final StringBuilder s = new StringBuilder( path.length() + id.length() + 1 );
    s.append( path );
    s.append( '#' );
    s.append( id );
    return s.toString();
  }

  public static String findLinkPath( final Feature toLink, final Feature parent )
  {
    final GMLWorkspace sourceWorkspace = parent.getWorkspace();
    return findLinkPath( toLink, sourceWorkspace );
  }
}