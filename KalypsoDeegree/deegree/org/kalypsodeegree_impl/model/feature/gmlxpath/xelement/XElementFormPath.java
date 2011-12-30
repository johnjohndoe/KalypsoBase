/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.model.feature.gmlxpath.xelement;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.kalypso.contribs.javax.xml.namespace.QNameUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * xelement that represents a path-xpath element s *
 *
 * @author doemming
 */
public class XElementFormPath extends AbstractXElement
{
  private static final QName QNAME_DOT_DOT = new QName( ".." );

  /* QName constant indicating that '*' was given as path. */
  private static final QName QNAME_ALL = new QName( "*", "*", "*" );

  private final QName m_propName;

  public XElementFormPath( final String condition, final NamespaceContext namespaceContext )
  {
    final QName regularQname = QNameUtilities.createQName( condition, namespaceContext );
    if( "*".equals( condition ) )
      m_propName = QNAME_ALL;
    else
      m_propName = regularQname;

    if( m_propName == null )
      throw new IllegalArgumentException( "Could not parse qname: " + condition );
  }

  /**
   * @see org.kalypsodeegree_impl.model.feature.xpath.AbstractXElement#evaluateFeature(org.kalypsodeegree.model.feature.Feature,
   *      boolean)
   */
  @Override
  public Object evaluateFeature( final Feature contextFeature, final boolean featureTypeLevel )
  {
    final IFeatureType featureType = contextFeature.getFeatureType();

    if( featureTypeLevel )
    {
      // check featureType
      // TODO: still tests for local part first, remove, if qname is always correctly used.
      if( m_propName == QNAME_ALL || m_propName.getLocalPart().equals( featureType.getQName().getLocalPart() ) || m_propName.equals( featureType.getQName() ) )
        return contextFeature;
      else
        return null;
    }

    if( QNAME_DOT_DOT.equals( m_propName ) )
      return evaluateDotDotFromFeature( contextFeature );

    final IPropertyType pt = getPropertyType( featureType, m_propName );
    if( pt == null )
    {
      // REMARK: the featureTypeLevel flag does not work in all cases, due to the missing
      // Property-Level in our data model.
      // IN order to let it work properly
      if( m_propName == QNAME_ALL || m_propName.getLocalPart().equals( featureType.getQName().getLocalPart() ) || m_propName.equals( featureType.getQName() ) )
        return contextFeature;

      return null;
    }

    final Object value = contextFeature.getProperty( pt );
    if( pt instanceof IRelationType )
    {
      /* Resolve xlinks */
      if( value instanceof String )
        return contextFeature.getWorkspace().getFeature( (String) value );

      return value;
    }
    return value;
  }

  private Object evaluateDotDotFromFeature( final Feature contextFeature )
  {
    final Feature parent = contextFeature.getOwner();
    if( parent == null )
      return null;

    final IRelationType parentRelation = contextFeature.getParentRelation();
    final Object property = parent.getProperty( parentRelation );
    if( property instanceof FeatureList )
      return property;

    /* Throw exception instead? */
    return null;
  }

  @SuppressWarnings("deprecation")
  private IPropertyType getPropertyType( final IFeatureType featureType, final QName qname )
  {
    final String nsuri = qname.getNamespaceURI();
    final String localPart = qname.getLocalPart();

    if( XMLConstants.NULL_NS_URI.equals( nsuri ) )
      return featureType.getProperty( localPart );

    return featureType.getProperty( qname );
  }

  @Override
  public Object evaluateOther( final Object context, final boolean featureTypeLevel )
  {
    if( context instanceof GMLWorkspace )
    {
      final GMLWorkspace gmlWorkspace = (GMLWorkspace) context;
      return evaluateFeature( gmlWorkspace.getRootFeature(), featureTypeLevel );
    }

    if( context instanceof IFeatureType )
      return evaluateFeatureType( (IFeatureType) context );

    if( context instanceof IRelationType )
    {
      final IRelationType relationType = (IRelationType) context;
      return evaluateFeatureType( relationType.getTargetFeatureType() );
    }

    if( context instanceof FeatureList )
    {
      if( QNAME_DOT_DOT.equals( m_propName ) )
        return ((FeatureList) context).getParentFeature();
    }

    return null;
  }

  private Object evaluateFeatureType( final IFeatureType featureType )
  {
    if( m_propName == QNAME_ALL )
      return featureType.getProperties();

    return getPropertyType( featureType, m_propName );
  }

  public QName getQName( )
  {
    return m_propName;
  }
}
