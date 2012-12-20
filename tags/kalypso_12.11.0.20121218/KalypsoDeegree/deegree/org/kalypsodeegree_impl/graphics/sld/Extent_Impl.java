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

import org.kalypsodeegree.graphics.sld.Extent;
import org.kalypsodeegree.xml.Marshallable;

/**
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class Extent_Impl implements Extent, Marshallable
{
  private String m_name = null;

  private String m_value = null;

  /**
   * constructor initializing the class with the <Extent>
   */
  Extent_Impl( final String value, final String name )
  {
    setName( name );
    setValue( value );
  }

  /**
   * returns the name of the extent
   *
   * @return the name of the extent
   */
  @Override
  public String getName( )
  {
    return m_name;
  }

  /**
   * sets the name of the extent
   *
   * @param name
   *          the name of the extent
   */
  @Override
  public void setName( final String name )
  {
    this.m_name = name;
  }

  /**
   * returns the value of the extent
   *
   * @return the value of the extent
   */
  @Override
  public String getValue( )
  {
    return m_value;
  }

  /**
   * sets the value of the extent
   *
   * @param value
   *          the value of the extent
   */
  @Override
  public void setValue( final String value )
  {
    this.m_value = value;
  }

  /**
   * exports the content of the FeatureTypeConstraint as XML formated String
   *
   * @return xml representation of the FeatureTypeConstraint
   */
  @Override
  public String exportAsXML( )
  {
    final StringBuffer sb = new StringBuffer( 1000 );

    sb.append( "<Extent>" );
    sb.append( "<Name>" ).append( m_name ).append( "</Name>" );
    sb.append( "<Value>" ).append( m_name ).append( "</Value>" );
    sb.append( "</Extent>" );

    return sb.toString();
  }

}