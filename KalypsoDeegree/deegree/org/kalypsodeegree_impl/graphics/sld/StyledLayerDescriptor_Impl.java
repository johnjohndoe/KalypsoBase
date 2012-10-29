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
import org.kalypsodeegree.graphics.sld.NamedLayer;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserLayer;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.Marshallable;

/**
 * StyledLayerDescriptor: This is a sequence of styled layers, represented at the first level by Layer and UserLayer
 * elements. A "version" attribute has been added to allow the formatting of static-file
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class StyledLayerDescriptor_Impl implements StyledLayerDescriptor
{
  private final String m_version = "1.0.0";

  private ArrayList<Layer> m_layers = null;

  private String m_abstract = null;

  private String m_name = null;

  private String m_title = null;

  /**
   * @param name
   * @param title
   * @param version
   * @param abstract_
   * @param layers
   */
  public StyledLayerDescriptor_Impl( final String name, final String title, final String abstract_, final Layer[] layers )
  {
    m_layers = new ArrayList<>( layers.length );
    setLayers( layers );
    setAbstract( abstract_ );
    setName( name );
    setTitle( title );
  }

  /**
   * constructor initializing the class with the <StyledLayerDescriptor>
   */
  public StyledLayerDescriptor_Impl( final Layer[] layers )
  {
    m_layers = new ArrayList<>( layers.length );
    setLayers( layers );
  }

  /**
   * @return the Layers as Array
   */
  @Override
  public Layer[] getLayers( )
  {
    return m_layers.toArray( new Layer[m_layers.size()] );
  }

  /**
   * Sets Layers
   *
   * @param layers
   *          the Layers as Array
   */
  @Override
  public void setLayers( final Layer[] layers )
  {
    m_layers.clear();

    if( layers != null )
    {
      for( final Layer layer : layers )
      {
        m_layers.add( layer );
      }
    }
  }

  /**
   * adds the <Layer>
   *
   * @param layer
   *          a Layer to add
   */
  @Override
  public void addLayer( final Layer layer )
  {
    m_layers.add( layer );
  }

  /**
   * removes the <Layer>
   *
   * @param layer
   *          a Layer to remove
   */
  @Override
  public void removeLayer( final Layer layer )
  {
    if( m_layers.indexOf( layer ) != -1 )
    {
      m_layers.remove( m_layers.indexOf( layer ) );
    }
  }

  /**
   * A UserLayer can contain one or more UserStyles. A UserLayer may direct the WMS to a specified WFS source of feature
   * data. Multiple feature types can be included in a UserLayer, since this is semantically equivalent to a Layer. All
   * feature types of a UserLayer come from the same WFS. The WFS can be named explicitly with the "wfs" attribute or it
   * can be implied by context.
   *
   * @return the UserLayers as Array
   */
  @Override
  public UserLayer[] getUserLayers( )
  {
    final List<UserLayer> list = new ArrayList<>( m_layers.size() );
    for( int i = 0; i < m_layers.size(); i++ )
    {
      if( m_layers.get( i ) instanceof UserLayer )
      {
        list.add( (UserLayer) m_layers.get( i ) );
      }
    }
    return list.toArray( new UserLayer[list.size()] );
  }

  /**
   * A NamedLayer uses the "name" attribute to identify a layer known to the WMS and can contain zero or more styles,
   * either NamedStyles or UserStyles. In the absence of any styles the default style for the layer is used.
   *
   * @return the NamedLayers as Array
   */
  @Override
  public NamedLayer[] getNamedLayers( )
  {
    final List<NamedLayer> list = new ArrayList<>();
    for( int i = 0; i < m_layers.size(); i++ )
    {
      if( m_layers.get( i ) instanceof NamedLayer )
        list.add( (NamedLayer) m_layers.get( i ) );
    }
    return list.toArray( new NamedLayer[list.size()] );
  }

  /**
   * A NamedLayer uses the "name" attribute to identify a layer known to the WMS and can contain zero or more styles,
   * either NamedStyles or UserStyles. In the absence of any styles the default style for the layer is used.
   *
   * @return the NamedLayers as Array
   */
  @Override
  public NamedLayer getNamedLayer( final String layerName )
  {
    for( int i = 0; i < m_layers.size(); i++ )
    {
      if( m_layers.get( i ) instanceof NamedLayer && ((NamedLayer) m_layers.get( i )).getName().equals( layerName ) )
        return (NamedLayer) m_layers.get( i );
    }
    return null;
  }

  /**
   * The version attribute gives the SLD version of an SLD document, to facilitate backward compatibility with static
   * documents stored in various different versions of the SLD spec. The string has the format x.y.z, the same as in
   * other OpenGIS Web Server specs. For example, an SLD document stored according to this spec would have the version
   * string 0.7.2.
   *
   * @return the version of the SLD as String
   */
  @Override
  public String getVersion( )
  {
    return m_version;
  }

  /**
   * sets the <Version>
   *
   * @param version
   *          the version of the SLD
   */
  @Override
  public void setVersion( final String version )
  {
    throw new UnsupportedOperationException( "Cannot change version, must always be 1.0.0" );
  }

  /**
   * @return Returns the abstract_.
   */
  public String getAbstract( )
  {
    return m_abstract;
  }

  /**
   * @param abstract_
   *          The abstract_ to set.
   */
  public void setAbstract( final String abstract_ )
  {
    m_abstract = abstract_;
  }

  /**
   * @return Returns the name.
   */
  public String getName( )
  {
    return m_name;
  }

  /**
   * @param name
   *          The name to set.
   */
  public void setName( final String name )
  {
    m_name = name;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * @param title
   *          The title to set.
   */
  public void setTitle( final String title )
  {
    m_title = title;
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
    sb.append( "<StyledLayerDescriptor version='" ).append( m_version ).append( "' " );
    sb.append( "xmlns='http://www.opengis.net/sld' " );
    sb.append( "xmlns:sld='http://www.opengis.net/sld' " );
    sb.append( "xmlns:sldExt='http://www.opengis.net/sldExt' " );
    sb.append( "xmlns:gml='http://www.opengis.net/gml' " );
    sb.append( "xmlns:ogc='http://www.opengis.net/ogc' " );
    sb.append( "xmlns:xlink='http://www.w3.org/1999/xlink' " );
    sb.append( "xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>" );

    for( int i = 0; i < m_layers.size(); i++ )
    {
      sb.append( ((Marshallable) m_layers.get( i )).exportAsXML() );
    }

    sb.append( "</StyledLayerDescriptor>" );

    return sb.toString();
  }

  @Override
  public UserStyle findUserStyle( final String name )
  {
    final NamedLayer[] namedLayers = getNamedLayers();
    for( final NamedLayer namedLayer : namedLayers )
    {
      final Style[] styles = namedLayer.getStyles();
      for( final Style style : styles )
      {
        if( style instanceof UserStyle && name.equals( style.getName() ) )
          return (UserStyle) style;
      }
    }

    final UserLayer[] userLayers = getUserLayers();
    for( final UserLayer userLayer : userLayers )
    {
      final Style[] styles = userLayer.getStyles();
      for( final Style style : styles )
      {
        if( style instanceof UserStyle && name.equals( style.getName() ) )
          return (UserStyle) style;
      }
    }

    return null;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.StyledLayerDescriptor#getDefaultUserStyle()
   */
  @Override
  public UserStyle getDefaultUserStyle( )
  {
    final NamedLayer[] namedLayers = getNamedLayers();
    for( final NamedLayer namedLayer : namedLayers )
    {
      final Style[] styles = namedLayer.getStyles();
      for( final Style style : styles )
      {
        if( style instanceof UserStyle && ((UserStyle) style).isDefault() )
          return (UserStyle) style;
      }
    }

    final UserLayer[] userLayers = getUserLayers();
    for( final UserLayer userLayer : userLayers )
    {
      final Style[] styles = userLayer.getStyles();
      for( final Style style : styles )
      {
        if( style instanceof UserStyle && ((UserStyle) style).isDefault() )
          return (UserStyle) style;
      }
    }

    return null;
  }

}