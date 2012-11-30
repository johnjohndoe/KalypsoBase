/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ui.editor.styleeditor.style;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTypeStyle;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypso.ui.editor.styleeditor.IStyleContext;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;

/**
 * @author Gernot Belger
 */
public class StyleContext implements IStyleContext
{
  private final IKalypsoStyle m_style;

  private final IFeatureType m_featureType;

  private final FeatureTypeStyle m_featureTypeStyle;

  public StyleContext( final IKalypsoStyle style, final IKalypsoFeatureTheme theme )
  {
    m_style = style;
    m_featureType = theme == null ? null : theme.getFeatureType();
    m_featureTypeStyle = findFeatureTypeStyle();
  }

  private FeatureTypeStyle findFeatureTypeStyle( )
  {
    if( m_style instanceof IKalypsoFeatureTypeStyle )
      return (IKalypsoFeatureTypeStyle)m_style;

    if( m_style instanceof IKalypsoUserStyle )
    {
      final FeatureTypeStyle[] featureTypeStyles = ((IKalypsoUserStyle)m_style).getFeatureTypeStyles();
      if( featureTypeStyles.length > 0 )
        return featureTypeStyles[0];
    }

    return null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IStyleContext#fireStyleChanged()
   */
  @Override
  public void fireStyleChanged( )
  {
    m_style.fireStyleChanged();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IStyleContext#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    return m_featureType;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IStyleContext#getStyle()
   */
  @Override
  public FeatureTypeStyle getStyle( )
  {
    return m_featureTypeStyle;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IStyleContext#getKalypsoStyle()
   */
  @Override
  public IKalypsoStyle getKalypsoStyle( )
  {
    return m_style;
  }
}
