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
package org.kalypso.zml.ui.table.style;

import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.RGBUtilities;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.StylePropertyName;
import org.kalypso.zml.ui.table.schema.StylePropertyType;
import org.kalypso.zml.ui.table.schema.StyleSetType;
import org.kalypso.zml.ui.table.utils.TableTypeHelper;

/**
 * @author Dirk Kuch
 */
public class CellStyle
{
  private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

  private final CellStyleType m_style;

  public CellStyle( final StyleSetType styleSet, final CellStyleType style )
  {
    m_style = style;

    init( styleSet, style );
  }

  /**
   * initialize style (cascading style set!)
   */
  private void init( final StyleSetType styleSet, final CellStyleType style )
  {
    final Object styleRef = style.getStyleRef();
    if( styleRef instanceof CellStyleType )
    {
      final CellStyleType base = (CellStyleType) styleRef;
      init( styleSet, base );

      final List<StylePropertyType> baseProperties = base.getProperty();
      for( final StylePropertyType property : baseProperties )
      {
        if( !hasProperty( style, property ) )
          style.getProperty().add( property );
      }
    }
  }

  private boolean hasProperty( final CellStyleType style, final StylePropertyType property )
  {
    final String name = TableTypeHelper.getPropertyName( property );
    final String value = TableTypeHelper.findProperty( style, name );

    return value != null;
  }

  public Color getBackgroundColor( )
  {
    final String htmlColor = TableTypeHelper.findProperty( m_style, StylePropertyName.BACKGROUND_COLOR.value() );
    if( htmlColor == null )
      return null;

    final RGB rgb = RGBUtilities.decodeHtmlColor( htmlColor );
    COLOR_REGISTRY.put( htmlColor, rgb );

    return COLOR_REGISTRY.get( htmlColor );
  }

}
