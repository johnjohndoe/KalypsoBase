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

import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.tools.Debug;

/**
 * A user-defined allows map styling to be defined externally from a system and to be passed around in an interoperable
 * format.
 * <p>
 * </p>
 * A UserStyle is at the same semantic level as a NamedStyle used in the context of a WMS. In a sense, a named style can
 * be thought of as a reference to a hidden UserStyle that is stored inside of a map server.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class UserStyle_Impl extends Style_Impl implements UserStyle, Marshallable
{
  private final List<FeatureTypeStyle> m_featureTypeStyles = new ArrayList<FeatureTypeStyle>();

  private String m_abstract_ = null;

  private String m_title = null;

  private boolean m_default_ = false;

  /* default */UserStyle_Impl( final String name, final String title, final String abstract_, final boolean default_, final FeatureTypeStyle[] featureTypeStyles )
  {
    super( name );

    setTitle( title );
    setAbstract( abstract_ );
    setDefault( default_ );
    setFeatureTypeStyles( featureTypeStyles );
  }

  /**
   * The Title is a human-readable short description for the style that might be displayed in a GUI pick list.
   *
   * @return the title of the User-Style
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * sets the <Title>
   *
   * @param title
   *            the title of the User-Style
   */
  public void setTitle( final String title )
  {
    this.m_title = title;
  }

  /**
   * the Abstract is a more exact description that may be a few paragraphs long.
   *
   * @return the abstract of the User-Style
   */
  public String getAbstract( )
  {
    return m_abstract_;
  }

  /**
   * sets the <Abstract>
   *
   * @param abstract_
   *            the abstract of the User-Style
   */
  public void setAbstract( final String abstract_ )
  {
    this.m_abstract_ = abstract_;
  }

  /**
   * The IsDefault element identifies whether a style is the default style of a layer, for use in SLD library mode when
   * rendering or for storing inside of a map server. The default value is <tt>false</tt>.
   *
   * @return true if the style ist the default style
   */
  public boolean isDefault( )
  {
    return m_default_;
  }

  /**
   * sets the <Default>
   *
   * @param default_
   */
  public void setDefault( final boolean default_ )
  {
    this.m_default_ = default_;
  }

  /**
   * A UserStyle can contain one or more FeatureTypeStyles which allow the rendering of features of specific types.
   * <p>
   * </p>
   * The FeatureTypeStyle defines the styling that is to be applied to a single feature type of a layer.
   * <p>
   * </p>
   * The FeatureTypeStyle element identifies that explicit separation in SLD between the handling of layers and the
   * handling of features of specific feature types. The layer concept is unique to WMS and SLD, but features are used
   * more generally, such as in WFS and GML, so this explicit separation is important.
   *
   * @return the FeatureTypeStyles of a User-Style
   */
  public FeatureTypeStyle[] getFeatureTypeStyles( )
  {
    final FeatureTypeStyle[] ft = new FeatureTypeStyle[m_featureTypeStyles.size()];

    return m_featureTypeStyles.toArray( ft );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.UserStyle#getFeatureTypeStyle(java.lang.String)
   */
  public FeatureTypeStyle getFeatureTypeStyle( final String featureTypeStyleName )
  {
    for( int i = 0; i < m_featureTypeStyles.size(); i++ )
    {
      if( m_featureTypeStyles.get( i ) != null && (m_featureTypeStyles.get( i )).getName().equals( featureTypeStyleName ) )
        return m_featureTypeStyles.get( i );
    }
    return null;
  }

  /**
   * sets the <FeatureTypeStyle>
   *
   * @param featureTypeStyles
   *            the FeatureTypeStyles of a User-Style
   */
  public void setFeatureTypeStyles( final FeatureTypeStyle[] featureTypeStyles )
  {
    this.m_featureTypeStyles.clear();

    if( featureTypeStyles != null )
    {
      for( final FeatureTypeStyle featureTypeStyle : featureTypeStyles )
      {
        addFeatureTypeStyle( featureTypeStyle );
      }
    }
  }

  /**
   * Adds a <FeatureTypeStyle>
   *
   * @param featureTypeStyle
   *            a FeatureTypeStyle to add
   */
  public void addFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    m_featureTypeStyles.add( featureTypeStyle );
  }

  /**
   * Removes a <FeatureTypeStyle>
   */
  public void removeFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    if( m_featureTypeStyles.indexOf( featureTypeStyle ) != -1 )
    {
      m_featureTypeStyles.remove( m_featureTypeStyles.indexOf( featureTypeStyle ) );
    }
  }

  /**
   * exports the content of the UserStyle as XML formated String
   *
   * @return xml representation of the UserStyle
   */
  public String exportAsXML( )
  {
    Debug.debugMethodBegin();

    final StringBuffer sb = new StringBuffer( 100 );
    sb.append( "<UserStyle>" );
    final String name = getName();
    if( name != null && !name.equals( "" ) )
    {
      sb.append( "<Name>" ).append( name ).append( "</Name>" );
    }
    if( m_title != null && !m_title.equals( "" ) )
    {
      sb.append( "<Title>" ).append( m_title ).append( "</Title>" );
    }
    if( m_abstract_ != null && !m_abstract_.equals( "" ) )
    {
      sb.append( "<Abstract>" ).append( m_abstract_ ).append( "</Abstract>" );
    }
    if( m_default_ )
    {
      sb.append( "<IsDefault>" ).append( 1 ).append( "</IsDefault>" );
    }
    for( int i = 0; i < m_featureTypeStyles.size(); i++ )
    {
      sb.append( ((Marshallable) m_featureTypeStyles.get( i )).exportAsXML() );
    }
    sb.append( "</UserStyle>" );

    Debug.debugMethodEnd();
    return sb.toString();
  }

}