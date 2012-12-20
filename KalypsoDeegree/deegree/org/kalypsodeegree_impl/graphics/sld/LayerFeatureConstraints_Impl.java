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
package org.kalypsodeegree_impl.graphics.sld;

import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.graphics.sld.FeatureTypeConstraint;
import org.kalypsodeegree.graphics.sld.LayerFeatureConstraints;
import org.kalypsodeegree.xml.Marshallable;

/**
 * The LayerFeatureConstraints element is optional in a NamedLayer and allows the user to specify constraints on what
 * features of what feature types are to be selected by the named-layer reference. It is essentially a filter that
 * allows the selection of fewer features than are present in the named layer.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class LayerFeatureConstraints_Impl implements LayerFeatureConstraints, Marshallable
{
  private final List<FeatureTypeConstraint> m_featureTypeConstraint = new ArrayList<>();

  /**
   * constructor initializing the class with the <LayerFeatureConstraints>
   */
  LayerFeatureConstraints_Impl( final FeatureTypeConstraint[] featureTypeConstraint )
  {
    setFeatureTypeConstraint( featureTypeConstraint );
  }

  /**
   * A FeatureTypeConstraint element is used to identify a feature type by a well-known name, using the FeatureTypeName
   * element.
   *
   * @return the FeatureTypeConstraints as Array
   */
  @Override
  public FeatureTypeConstraint[] getFeatureTypeConstraint( )
  {
    return m_featureTypeConstraint.toArray( new FeatureTypeConstraint[m_featureTypeConstraint.size()] );
  }

  /**
   * sets the <FeatureTypeConstraint>
   *
   * @param featureTypeConstraint
   *          the <FeatureTypeConstraint>
   */
  @Override
  public void setFeatureTypeConstraint( final FeatureTypeConstraint[] featureTypeConstraint )
  {
    m_featureTypeConstraint.clear();

    if( featureTypeConstraint != null )
    {
      for( final FeatureTypeConstraint element : featureTypeConstraint )
      {
        m_featureTypeConstraint.add( element );
      }
    }
  }

  /**
   * adds the <FeatureTypeConstraint>
   *
   * @param featureTypeConstraint
   *          the <FeatureTypeConstraint>
   */
  @Override
  public void addFeatureTypeConstraint( final FeatureTypeConstraint featureTypeConstraint )
  {
    m_featureTypeConstraint.add( featureTypeConstraint );
  }

  /**
   * Removes a FeatureTypeConstraint.
   *
   * @param featureTypeConstraint
   *          the <FeatureTypeConstraint>
   */
  @Override
  public void removeFeatureTypeConstraint( final FeatureTypeConstraint featureTypeConstraint )
  {
    m_featureTypeConstraint.remove( m_featureTypeConstraint.indexOf( featureTypeConstraint ) );
  }

  /**
   * returns the LayerFeatureConstraints as String.
   *
   * @return the LayerFeatureConstraints as String
   */
  @Override
  public String toString( )
  {
    String ret = getClass().getName() + "\n";
    ret = "featureTypeConstraint = " + m_featureTypeConstraint + "\n";

    return ret;
  }

  /**
   * exports the content of the Font as XML formated String
   *
   * @return xml representation of the Font
   */
  @Override
  public String exportAsXML( )
  {
    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<LayerFeatureConstraints>" );
    for( int i = 0; i < m_featureTypeConstraint.size(); i++ )
    {
      sb.append( ((Marshallable) m_featureTypeConstraint.get( i )).exportAsXML() );
    }
    sb.append( "</LayerFeatureConstraints>" );

    return sb.toString();
  }

}