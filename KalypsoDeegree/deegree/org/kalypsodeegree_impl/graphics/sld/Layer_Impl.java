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

import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.LayerFeatureConstraints;
import org.kalypsodeegree.graphics.sld.Style;

/**
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class Layer_Impl implements Layer
{
  private final List<Style> m_styles = new ArrayList<>();

  private LayerFeatureConstraints m_layerFeatureConstraints = null;

  private String m_name = null;

  /**
   * constructor initializing the class with the <NamedLayer>
   */
  Layer_Impl( final String name, final LayerFeatureConstraints layerFeatureConstraints, final Style[] styles )
  {
    setName( name );
    setLayerFeatureConstraints( layerFeatureConstraints );
    setStyles( styles );
  }

  /**
   * The Name element identifies the well-known name of the layer being referenced, and is required. All possible
   * well-known names are usually identified in the capabilities document for a server.
   *
   * @return the name of the layer
   */
  @Override
  public String getName( )
  {
    return m_name;
  }

  /**
   * sets the <Name>
   *
   * @param name
   *          the name of the layer
   */
  @Override
  public void setName( final String name )
  {
    this.m_name = name;
  }

  /**
   * The LayerFeatureConstraints element is optional in a NamedLayer and allows the user to specify constraints on what
   * features of what feature types are to be selected by the named-layer reference. It is essentially a filter that
   * allows the selection of fewer features than are present in the named layer.
   *
   * @return the LayerFeatureConstraints
   */
  @Override
  public LayerFeatureConstraints getLayerFeatureConstraints( )
  {
    return m_layerFeatureConstraints;
  }

  /**
   * sets the <LayerFeatureConstraints>
   *
   * @param layerFeatureConstraints
   *          the LayerFeatureConstraints
   */
  @Override
  public void setLayerFeatureConstraints( final LayerFeatureConstraints layerFeatureConstraints )
  {
    this.m_layerFeatureConstraints = layerFeatureConstraints;
  }

  /**
   * Returns the styles associated to the Layer. This may be UserStyles or NamedStyles
   * <p>
   * </p>
   * A UserStyle is at the same semantic level as a NamedStyle used in the context of a WMS. In a sense, a named style
   * can be thought of as a reference to a hidden UserStyle that is stored inside of a map server.
   *
   * @return the Styles of the Layer as ArrayList
   */
  @Override
  public Style[] getStyles( )
  {
    return m_styles.toArray( new Style[m_styles.size()] );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Layer#getStyle()
   */
  @Override
  public Style getStyle( final String styleName )
  {
    for( int i = 0; i < m_styles.size(); i++ )
    {
      if( m_styles.get( i ) != null && m_styles.get( i ).getName().equals( styleName ) )
        return m_styles.get( i );
    }
    return null;
  }

  /**
   * Adds styles to the Layer.
   *
   * @param styles
   *          the styles for the layer as Array
   */
  @Override
  public void setStyles( final Style[] styles )
  {
    this.m_styles.clear();

    if( styles != null )
    {
      for( final Style style : styles )
      {
        this.m_styles.add( style );
      }
    }
  }

  /**
   * @see org.kalypsodeegree_impl.graphics.sld.Layer_Impl#getStyles()
   * @param style
   *          a style to add
   */
  @Override
  public void addStyle( final Style style )
  {
    m_styles.add( style );
  }

  /**
   * @see org.kalypsodeegree_impl.graphics.sld.Layer_Impl#getStyles()
   * @param style
   *          a style to remove
   */
  @Override
  public void removeStyle( final Style style )
  {
    m_styles.remove( m_styles.indexOf( style ) );
  }

  /**
   * returns a STring-Representation of the layer
   *
   * @return the layer as String
   */
  @Override
  public String toString( )
  {
    String ret = getClass().getName() + "\n";
    ret = "name = " + m_name + "\n";
    ret += "layerFeatureConstraints = " + m_layerFeatureConstraints + "\n";
    ret += "styles = " + m_styles + "\n";

    return ret;
  }

}