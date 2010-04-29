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

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.xml.Marshallable;

/**
 * The FeatureTypeStyle defines the styling that is to be applied to a single feature type of a layer). This element may
 * also be externally re-used outside of the scope of WMSes and layers.
 * <p>
 * </p>
 * The FeatureTypeStyle element identifies that explicit separation in SLD between the handling of layers and the
 * handling of features of specific feature types. The layer concept is unique to WMS and SLD, but features are used
 * more generally, such as in WFS and GML, so this explicit separation is important.
 * <p>
 * ----------------------------------------------------------------------
 * </p>
 *
 * @author <a href="mailto:k.lupp@web.de">Katharina Lupp </a>
 * @version $Revision$ $Date$
 */
public class FeatureTypeStyle_Impl implements FeatureTypeStyle, Marshallable
{
  private final List<Rule> m_rules = new ArrayList<Rule>();

  private final List<String> m_semanticTypeIdentifier = new ArrayList<String>();

  private String m_abstract = null;

  private QName m_featureTypeName = null;

  private String m_name = null;

  private String m_title = null;

  /**
   * constructor initialising the class with the <FeatureTypeStyle>
   */
  FeatureTypeStyle_Impl( final String name, final String title, final String abstract_, final QName featureTypeQName, final String[] semanticTypeIdentifier, final Rule[] rules )
  {
    setName( name );
    setTitle( title );
    setAbstract( abstract_ );
    setFeatureTypeName( featureTypeQName );
    setSemanticTypeIdentifier( semanticTypeIdentifier );
    setRules( rules );
  }

  /**
   * The Name element does not have an explicit use at present, though it conceivably might be used to reference a
   * feature style in some feature-style library.
   *
   * @return name
   */
  public String getName( )
  {
    return m_name;
  }

  /**
   * The Name element does not have an explicit use at present, though it conceivably might be used to reference a
   * feature style in some feature-style library. Sets the <Name>o
   *
   * @param name
   *            the name
   */
  public void setName( final String name )
  {
    this.m_name = name;
  }

  /**
   * human-readable information about the style
   *
   * @return the title of the FeatureTypeStyle
   */
  public String getTitle( )
  {
    return m_title;
  }

  /**
   * sets the <Title>
   *
   * @param title
   *            the title of the FeatureTypeStyle
   */
  public void setTitle( final String title )
  {
    this.m_title = title;
  }

  /**
   * human-readable information about the style
   *
   * @return an abstract of the FeatureTypeStyle
   */
  public String getAbstract( )
  {
    return m_abstract;
  }

  /**
   * sets <Abstract>
   *
   * @param abstract_
   *            an abstract of the FeatureTypeStyle
   */
  public void setAbstract( final String abstract_ )
  {
    this.m_abstract = abstract_;
  }

  /**
   * returns the name of the affected feature type
   *
   * @return the name of the FeatureTypeStyle as String
   */
  public QName getFeatureTypeName( )
  {
    return m_featureTypeName;
  }

  /**
   * sets the name of the affected feature type
   *
   * @param featureTypeName
   *            the name of the FeatureTypeStyle
   */
  public void setFeatureTypeName( final QName featureTypeQName )
  {
    m_featureTypeName = featureTypeQName;
  }

  /**
   * The SemanticTypeIdentifier is experimental and is intended to be used to identify what the feature style is
   * suitable to be used for using community- controlled name(s). For example, a single style may be suitable to use
   * with many different feature types. The syntax of the SemanticTypeIdentifier string is undefined, but the strings
   * generic:line, generic:polygon, generic:point, generic:text, generic:raster, and generic:any are reserved to
   * indicate that a FeatureTypeStyle may be used with any feature type with the corresponding default geometry type
   * (i.e., no feature properties are referenced in the feature-type style).
   *
   * @return the SemanticTypeIdentifiers from the FeatureTypeStyle as String-Array
   */
  public String[] getSemanticTypeIdentifier( )
  {
    return m_semanticTypeIdentifier.toArray( new String[m_semanticTypeIdentifier.size()] );
  }

  /**
   * Sets the SemanticTypeIdentifiers.
   *
   * @param semanticTypeIdentifiers
   *            SemanticTypeIdentifiers for the FeatureTypeStyle
   */
  public void setSemanticTypeIdentifier( final String[] semanticTypeIdentifiers )
  {
    m_semanticTypeIdentifier.clear();

    if( semanticTypeIdentifiers != null )
    {
      for( final String semanticTypeIdentifier : semanticTypeIdentifiers )
      {
        m_semanticTypeIdentifier.add( semanticTypeIdentifier );
      }
    }
  }

  /**
   * adds the <SemanticTypeIdentifier>
   *
   * @param semanticTypeIdentifier
   *            SemanticTypeIdentifier to add
   */
  public void addSemanticTypeIdentifier( final String semanticTypeIdentifier )
  {
    this.m_semanticTypeIdentifier.add( semanticTypeIdentifier );
  }

  /**
   * Removes an <SemanticTypeIdentifier>.
   *
   * @param semanticTypeIdentifier
   *            SemanticTypeIdentifier to remove
   */
  public void removeSemanticTypeIdentifier( final String semanticTypeIdentifier )
  {
    this.m_semanticTypeIdentifier.remove( this.m_semanticTypeIdentifier.indexOf( semanticTypeIdentifier ) );
  }

  /**
   * Rules are used to group rendering instructions by feature-property conditions and map scales. Rule definitions are
   * placed immediately inside of feature-style definitions.
   *
   * @return the rules of the FeatureTypeStyle as Array
   */
  public Rule[] getRules( )
  {
    return m_rules.toArray( new Rule[m_rules.size()] );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#getRule(java.lang.String)
   */
  public Rule getRule( final String ruleName )
  {
    for( int i = 0; i < m_rules.size(); i++ )
    {
      if( m_rules.get( i ) != null && m_rules.get( i ).getName().equals( ruleName ) )
        return m_rules.get( i );
    }
    return null;
  }

  /**
   * sets the <Rules>
   *
   * @param rules
   *            the rules of the FeatureTypeStyle as Array
   */
  public void setRules( final Rule[] rules )
  {
    this.m_rules.clear();

    if( rules != null )
    {
      for( final Rule rule : rules )
      {
        this.m_rules.add( rule );
      }
    }
  }

  /**
   * adds the <Rules>
   *
   * @param rule
   *            a rule
   */
  public void addRule( final Rule rule )
  {
    m_rules.add( rule );
  }

  /**
   * removes a rule
   *
   * @param rule
   *            a rule
   */
  public void removeRule( final Rule rule )
  {
    m_rules.remove( m_rules.indexOf( rule ) );
  }

  /**
   * exports the content of the FeatureTypeStyle as XML formated String
   *
   * @return xml representation of the FeatureTypeStyle
   */
  public String exportAsXML( )
  {
    final StringBuffer sb = new StringBuffer( 1000 );
    sb.append( "<FeatureTypeStyle xmlns=\"" + NS.SLD + "\" xmlns:ogc=\"" + NS.OGC + "\">" );
    if( m_name != null && !m_name.equals( "" ) )
    {
      sb.append( "<Name>" ).append( m_name ).append( "</Name>" );
    }
    if( m_title != null && !m_title.equals( "" ) )
    {
      sb.append( "<Title>" ).append( m_title ).append( "</Title>" );
    }
    if( m_abstract != null && !m_abstract.equals( "" ) )
    {
      sb.append( "<Abstract>" ).append( m_abstract ).append( "</Abstract>" );
    }
    if( m_featureTypeName != null && !m_featureTypeName.equals( "" ) )
    {
      sb.append( "<FeatureTypeName>" ).append( m_featureTypeName ).append( "</FeatureTypeName>" );
    }
    for( int i = 0; i < m_semanticTypeIdentifier.size(); i++ )
    {
      sb.append( "<SemanticTypeIdentifier>" ).append( m_semanticTypeIdentifier.get( i ) ).append( "</SemanticTypeIdentifier>" );
    }
    for( int i = 0; i < m_rules.size(); i++ )
    {
      sb.append( ((Marshallable) m_rules.get( i )).exportAsXML() );
    }
    sb.append( "</FeatureTypeStyle>" );

    return sb.toString();
  }

}