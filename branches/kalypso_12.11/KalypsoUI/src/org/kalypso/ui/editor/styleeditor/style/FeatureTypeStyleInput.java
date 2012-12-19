/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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

import java.net.URL;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ui.editor.styleeditor.IStyleEditorConfig;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;

/**
 * @author Gernot Belger
 */
public class FeatureTypeStyleInput implements IFeatureTypeStyleInput
{
  private final int m_styleToSelect;

  private final FeatureTypeStyle m_fts;

  private final IKalypsoStyle m_style;

  private final IFeatureType m_featureType;

  private final IStyleEditorConfig m_config;

  public FeatureTypeStyleInput( final FeatureTypeStyle fts, final IKalypsoStyle style, final int styleToSelect, final IFeatureType featureType, final IStyleEditorConfig config )
  {
    m_fts = fts;
    m_style = style;
    m_styleToSelect = styleToSelect;
    m_featureType = featureType;
    m_config = config;
  }

  @Override
  public int getStyleToSelect( )
  {
    return m_styleToSelect;
  }

  @Override
  public int hashCode( )
  {
    return new HashCodeBuilder().append( m_fts ).append( m_styleToSelect ).toHashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;
    if( obj == this )
      return true;
    if( !(obj instanceof IFeatureTypeStyleInput) )
      return false;

    final IFeatureTypeStyleInput other = (IFeatureTypeStyleInput)obj;
    return new EqualsBuilder().append( m_fts, other.getData() ).append( m_styleToSelect, other.getStyleToSelect() ).isEquals();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.binding.IStyleInput#getData()
   */
  @Override
  public FeatureTypeStyle getData( )
  {
    return m_fts;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.binding.IStyleInput#fireStyleChanged()
   */
  @Override
  public void fireStyleChanged( )
  {
    if( m_style != null )
      m_style.fireStyleChanged();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.binding.IStyleInput#getFeatureType()
   */
  @Override
  public IFeatureType getFeatureType( )
  {
    return m_featureType;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.binding.IStyleInput#getContext()
   */
  @Override
  public URL getContext( )
  {
    if( m_style != null )
      return m_style.getContext();

    return null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.binding.IStyleInput#getConfig()
   */
  @Override
  public IStyleEditorConfig getConfig( )
  {
    return m_config;
  }

  public IKalypsoStyle getStyle( )
  {
    return m_style;
  }
}