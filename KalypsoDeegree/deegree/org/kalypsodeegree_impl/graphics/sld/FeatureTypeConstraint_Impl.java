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

import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.graphics.sld.Extent;
import org.kalypsodeegree.graphics.sld.FeatureTypeConstraint;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.tools.Debug;

/**
 * A FeatureTypeConstraint element is used to identify a feature type by well-known name, using the FeatureTypeName
 * element.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
class FeatureTypeConstraint_Impl implements FeatureTypeConstraint, Marshallable
{
  private ArrayList extents = null;

  private Filter filter = null;

  private String featureTypeName = null;

  /**
   * constructor initializing the class with the <FeatureTypeConstraint>
   */
  FeatureTypeConstraint_Impl( final String featureTypeName, final Filter filter, final Extent[] extents )
  {
    this.extents = new ArrayList();
    setFeatureTypeName( featureTypeName );
    setFilter( filter );
    setExtents( extents );
  }

  /**
   * returns the name of the feature type
   * 
   * @return the name of the feature type
   */
  @Override
  public String getFeatureTypeName( )
  {
    return featureTypeName;
  }

  /**
   * sets the name of the feature type
   * 
   * @param featureTypeName
   *          the name of the feature type
   */
  @Override
  public void setFeatureTypeName( final String featureTypeName )
  {
    this.featureTypeName = featureTypeName;
  }

  /**
   * returns a feature-filter as defined in WFS specifications.
   * 
   * @return the filter of the FeatureTypeConstraints
   */
  @Override
  public Filter getFilter( )
  {
    return filter;
  }

  /**
   * sets a feature-filter as defined in WFS specifications.
   * 
   * @param filter
   *          the filter of the FeatureTypeConstraints
   */
  @Override
  public void setFilter( final Filter filter )
  {
    this.filter = filter;
  }

  /**
   * returns the extent for filtering the feature type
   * 
   * @return the extent for filtering the feature type
   */
  @Override
  public Extent[] getExtents( )
  {
    return (Extent[]) extents.toArray( new Extent[extents.size()] );
  }

  /**
   * sets the extent for filtering the feature type
   * 
   * @param extents
   *          extents for filtering the feature type
   */
  @Override
  public void setExtents( final Extent[] extents )
  {
    this.extents.clear();

    if( extents != null )
    {
      for( final Extent extent : extents )
      {
        addExtent( extent );
      }
    }
  }

  /**
   * Adds an Extent to the Extent-List of a FeatureTypeConstraint
   * 
   * @param extent
   *          an extent to add
   */
  @Override
  public void addExtent( final Extent extent )
  {
    extents.add( extent );
  }

  /**
   * Removes an Extent from the Extent-List of a FeatureTypeConstraint
   * 
   * @param extent
   *          an extent to remove
   */
  @Override
  public void removeExtent( final Extent extent )
  {
    extents.remove( extents.indexOf( extent ) );
  }

  /**
   * @return the FeatureTypeConstraint as String
   */
  @Override
  public String toString( )
  {
    String ret = getClass().getName() + "\n";
    ret = "featureTypeName = " + featureTypeName + "\n";
    ret += "filter = " + filter + "\n";
    ret += "extents = " + extents + "\n";

    return ret;
  }

  /**
   * exports the content of the FeatureTypeConstraint as XML formated String
   * 
   * @return xml representation of the FeatureTypeConstraint
   */
  @Override
  public String exportAsXML( )
  {
    Debug.debugMethodBegin();

    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<FeatureTypeConstraint>" );
    sb.append( "<FeatureTypeName>" ).append( featureTypeName );
    sb.append( "</FeatureTypeName>" );
    if( filter != null )
    {
      sb.append( filter.toXML() );
    }
    if( extents != null )
    {
      for( int i = 0; i < extents.size(); i++ )
      {
        sb.append( ((Marshallable) extents.get( i )).exportAsXML() );
      }
    }
    sb.append( "</FeatureTypeConstraint>" );

    Debug.debugMethodEnd();
    return sb.toString();
  }

}