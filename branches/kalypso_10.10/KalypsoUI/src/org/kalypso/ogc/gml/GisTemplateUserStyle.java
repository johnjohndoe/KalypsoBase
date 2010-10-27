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

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Andreas von Dömming
 */
public class GisTemplateUserStyle extends AbstractTemplateStyle implements IKalypsoUserStyle
{
  protected final String m_styleName;

  protected UserStyle m_userStyle;

  public GisTemplateUserStyle( final PoolableObjectType poolableStyleKey, final String styleName, final boolean usedForSelection )
  {
    super( poolableStyleKey, usedForSelection );

    m_userStyle = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.0" ), String.format( "Loading style", poolableStyleKey.getLocation() ) ); //$NON-NLS-1$ //$NON-NLS-2$
    m_styleName = styleName;

    startLoad();
  }

  /**
   * @return a empty style
   */
  private static UserStyle createDummyStyle( final String title, final String abstr )
  {
    return StyleFactory.createUserStyle( "dummyStyle", title, abstr, false, new FeatureTypeStyle[0] ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypsodeegree.xml.Marshallable#exportAsXML()
   */
  @Override
  public String exportAsXML( )
  {
    return ((Marshallable) m_userStyle).exportAsXML();
  }

  @Override
  public void addFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    m_userStyle.addFeatureTypeStyle( featureTypeStyle );
  }

  @Override
  public String getAbstract( )
  {
    return m_userStyle.getAbstract();
  }

  @Override
  public FeatureTypeStyle[] getFeatureTypeStyles( )
  {
    return m_userStyle.getFeatureTypeStyles();
  }

  @Override
  public String getName( )
  {
    return m_userStyle.getName();
  }

  @Override
  public String getTitle( )
  {
    return m_userStyle.getTitle();
  }

  @Override
  public boolean isDefault( )
  {
    return m_userStyle.isDefault();
  }

  @Override
  public void removeFeatureTypeStyle( final FeatureTypeStyle featureTypeStyle )
  {
    m_userStyle.removeFeatureTypeStyle( featureTypeStyle );
  }

  @Override
  public void setAbstract( final String abstract_ )
  {
    m_userStyle.setAbstract( abstract_ );
  }

  @Override
  public void setDefault( final boolean default_ )
  {
    m_userStyle.setDefault( default_ );
  }

  @Override
  public void setFeatureTypeStyles( final FeatureTypeStyle[] featureTypeStyles )
  {
    m_userStyle.setFeatureTypeStyles( featureTypeStyles );
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.UserStyle#getFeatureTypeStyle(java.lang.String)
   */
  @Override
  public FeatureTypeStyle getFeatureTypeStyle( final String featureTypeStyleName )
  {
    return m_userStyle.getFeatureTypeStyle( featureTypeStyleName );
  }

  @Override
  public void setName( final String name )
  {
    m_userStyle.setName( name );
  }

  @Override
  public void setTitle( final String title )
  {
    m_userStyle.setTitle( title );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractTemplateStyle#handleObjectLoaded(java.lang.Object)
   */
  @Override
  protected void handleObjectLoaded( final Object newValue )
  {
    if( newValue == null )
    {
      m_userStyle = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.3" ), Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.9" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    if( !(newValue instanceof StyledLayerDescriptor) )
    {
      m_userStyle = createDummyStyle( Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.3" ), "Top-Level element of .sld file must be 'StyledLayerDescriptor'" ); //$NON-NLS-1$
      return;
    }

    final StyledLayerDescriptor sld = (StyledLayerDescriptor) newValue;
    m_userStyle = findUserStyle( sld );
  }

  private UserStyle findUserStyle( final StyledLayerDescriptor sld )
  {
    final UserStyle userStyle = sld.findUserStyle( m_styleName );

    if( userStyle != null )
      return userStyle;

    final UserStyle defaultUserStyle = sld.getDefaultUserStyle();
    if( defaultUserStyle != null )
      return defaultUserStyle;

    final String message = Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.1", m_styleName ) + m_styleName; //$NON-NLS-1$
    final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, message, null );
    KalypsoGisPlugin.getDefault().getLog().log( status );

    final String title = Messages.getString( "org.kalypso.ogc.gml.GisTemplateUserStyle.2" ); //$NON-NLS-1$
    final String abstr = message;
    return createDummyStyle( title, abstr );
  }

  /**
   * @see org.kalypso.ogc.gml.AbstractTemplateStyle#getStyleName()
   */
  @Override
  protected String getStyleName( )
  {
    return m_styleName;
  }

}
