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
package org.kalypso.ogc.gml.table;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * Describes a column of the gis table.
 * 
 * @author Gernot Belger
 */
public class ColumnDescriptor implements IColumnDescriptor
{
  private final Map<String, String> m_params = new HashMap<>();

  private final GMLXPath m_propertyPath;

  private LayerTableStyle m_style = new LayerTableStyle( null );

  private String m_label = null;

  private String m_tooltip = null;

  private boolean m_editable = true;

  private int m_alignment = SWT.LEAD;

  private String m_format = null;

  private String m_modifierID = null;

  private boolean m_sortEnabled = true;

  private int m_width = 100;

  private IFeatureModifier m_modifier;

  private boolean m_resizeable = true;

  public ColumnDescriptor( final GMLXPath propertyPath )
  {
    m_propertyPath = propertyPath;
  }

  @Override
  public LayerTableStyle getStyle( )
  {
    return m_style;
  }

  public void setStyle( final LayerTableStyle style )
  {
    Assert.isNotNull( style );

    m_style = style;
  }

  @Override
  public String getLabel( )
  {
    return m_label;
  }

  public void setLabel( final String label )
  {
    m_label = label;
  }

  @Override
  public String getTooltip( )
  {
    return m_tooltip;
  }

  public void setTooltip( final String tooltip )
  {
    m_tooltip = tooltip;
  }

  @Override
  public boolean isEditable( )
  {
    return m_editable;
  }

  public void setEditable( final boolean editable )
  {
    m_editable = editable;
  }

// @Override
// public int getWidth( )
// {
// return m_width;
// }

// public void setWidth( final int width )
// {
// m_width = width;
// }

  @Override
  public int getAlignment( )
  {
    return m_alignment;
  }

  public void setAlignment( final int alignment )
  {
    m_alignment = alignment;
  }

  @Override
  public String getFormat( )
  {
    return m_format;
  }

  public void setFormat( final String format )
  {
    m_format = format;
  }

  @Override
  public String getModifierID( )
  {
    return m_modifierID;
  }

  public void setModifier( final String modifier )
  {
    m_modifierID = modifier;
  }

  @Override
  public boolean isSortEnabled( )
  {
    return m_sortEnabled;
  }

  public void setSortEnabled( final boolean sortEnabled )
  {
    m_sortEnabled = sortEnabled;
  }

  public void setParam( final String key, final String value )
  {
    m_params.put( key, value );
  }

  @Override
  public GMLXPath getPropertyPath( )
  {
    return m_propertyPath;
  }

  @Override
  public String getColumnProperty( )
  {
    if( m_propertyPath == null )
      return StringUtils.EMPTY;

    return m_propertyPath.toString();
  }

  @Override
  public int getWidth( )
  {
    return m_width;
  }

  @Override
  public void setWidth( final int width )
  {
    m_width = width;
  }

  @Override
  public String getParam( final String key )
  {
    return m_params.get( key );
  }

  @Override
  public Map<String, String> getParameters( )
  {
    return Collections.unmodifiableMap( m_params );
  }

  @Override
  public void setModifier( final IFeatureModifier modifier )
  {
    m_modifier = modifier;
  }

  @Override
  public IFeatureModifier getModifier( )
  {
    return m_modifier;
  }

  public void setResizeable( final boolean resizeable )
  {
    m_resizeable = resizeable;
  }

  @Override
  public boolean isResizeable( )
  {
    return m_resizeable;
  }
}