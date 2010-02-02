/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml;

import javax.xml.namespace.QName;

import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.i18n.Messages;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class GisTemplateFeatureTypeStyle extends AbstractTemplateStyle implements IKalypsoFeatureTypeStyle
{
  protected FeatureTypeStyle m_style;

  public GisTemplateFeatureTypeStyle( final PoolableObjectType poolableStyleKey, final boolean usedForSelection )
  {
    super( poolableStyleKey, usedForSelection );

    m_style = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.0" ), String.format( "Loading style", poolableStyleKey.getLocation() ) ); //$NON-NLS-1$ //$NON-NLS-2$

    startLoad();
  }

  /**
   * @return a empty style
   */
  private static FeatureTypeStyle createDummyStyle( final String title, final String abstr )
  {
    return StyleFactory.createFeatureTypeStyle( "dummyStyle", title, abstr, null, new Rule[] {} );//$NON-NLS-1$
  }

  /**
   * @see org.kalypsodeegree.xml.Marshallable#exportAsXML()
   */
  public String exportAsXML( )
  {
    return ((Marshallable) m_style).exportAsXML();
  }

  @Override
  public String getName( )
  {
    return m_style.getName();
  }

  @Override
  public void setName( final String name )
  {
    m_style.setName( name );
  }

  @Override
  public String getTitle( )
  {
    return m_style.getTitle();
  }

  @Override
  public void setTitle( final String title )
  {
    m_style.setTitle( title );
  }

  @Override
  public String getAbstract( )
  {
    return m_style.getAbstract();
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractTemplateStyle#setAbstract(java.lang.String)
   */
  @Override
  public void setAbstract( final String abstract_ )
  {
    m_style.setAbstract( abstract_ );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#addRule(org.kalypsodeegree.graphics.sld.Rule)
   */
  @Override
  public void addRule( final Rule rule )
  {
    m_style.addRule( rule );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#addSemanticTypeIdentifier(java.lang.String)
   */
  @Override
  public void addSemanticTypeIdentifier( final String semanticTypeIdentifier )
  {
    m_style.addSemanticTypeIdentifier( semanticTypeIdentifier );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#getFeatureTypeName()
   */
  @Override
  public QName getFeatureTypeName( )
  {
    return m_style.getFeatureTypeName();
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#getRule(java.lang.String)
   */
  @Override
  public Rule getRule( final String ruleName )
  {
    return m_style.getRule( ruleName );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#getRules()
   */
  @Override
  public Rule[] getRules( )
  {
    return m_style.getRules();
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#getSemanticTypeIdentifier()
   */
  @Override
  public String[] getSemanticTypeIdentifier( )
  {
    return m_style.getSemanticTypeIdentifier();
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#removeRule(org.kalypsodeegree.graphics.sld.Rule)
   */
  @Override
  public void removeRule( final Rule rule )
  {
    m_style.removeRule( rule );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#removeSemanticTypeIdentifier(java.lang.String)
   */
  @Override
  public void removeSemanticTypeIdentifier( final String semanticTypeIdentifier )
  {
    m_style.removeSemanticTypeIdentifier( semanticTypeIdentifier );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#setFeatureTypeName(javax.xml.namespace.QName)
   */
  @Override
  public void setFeatureTypeName( final QName featureTypeQName )
  {
    m_style.setFeatureTypeName( featureTypeQName );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#setRules(org.kalypsodeegree.graphics.sld.Rule[])
   */
  @Override
  public void setRules( final Rule[] rules )
  {
    m_style.setRules( rules );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.FeatureTypeStyle#setSemanticTypeIdentifier(java.lang.String[])
   */
  @Override
  public void setSemanticTypeIdentifier( final String[] semanticTypeIdentifiers )
  {
    m_style.setSemanticTypeIdentifier( semanticTypeIdentifiers );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractTemplateStyle#handleObjectLoaded(java.lang.Object)
   */
  @Override
  protected void handleObjectLoaded( final Object newValue )
  {
    if( newValue == null )
    {
      m_style = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.3" ), Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.9" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    if( !(newValue instanceof FeatureTypeStyle) )
    {
      m_style = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.3" ), "Root element of .sld file must be of type 'FeatureTypeStyle'" ); //$NON-NLS-1$
      return;
    }

    m_style = (FeatureTypeStyle) newValue;
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractTemplateStyle#getStyleName()
   */
  @Override
  protected String getStyleName( )
  {
    return null;
  }


}
