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
package org.kalypso.zml.ui.table.binding;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.contribs.eclipse.swt.graphics.RGBUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.ICatalog;
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

  private static final FontRegistry FONT_REGISTRY = new FontRegistry();

  private static final ImageRegistry IMAGE_REGISTRY = new ImageRegistry();

  private final CellStyleType m_style;

  private Color m_backgroundColor;

  private Font m_font;

  private Color m_foregroundColor;

  private Image m_image;

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

  private boolean hasProperty( final CellStyleType style, final StylePropertyType type )
  {
    final String name = TableTypeHelper.getPropertyName( type );
    final StylePropertyName property = StylePropertyName.fromValue( name );

    final String value = TableTypeHelper.findProperty( style, property );

    return value != null;
  }

  public Color getBackgroundColor( )
  {
    if( m_backgroundColor != null )
      return m_backgroundColor;

    final String htmlColor = TableTypeHelper.findProperty( m_style, StylePropertyName.BACKGROUND_COLOR );
    if( htmlColor == null )
      return null;

    final String id = "background" + m_style.getId();
    final RGB rgb = RGBUtilities.decodeHtmlColor( htmlColor );
    COLOR_REGISTRY.put( id, rgb );

    m_backgroundColor = COLOR_REGISTRY.get( id );

    return m_backgroundColor;
  }

  public Color getForegroundColor( )
  {
    if( m_foregroundColor != null )
      return m_foregroundColor;

    final String htmlColor = TableTypeHelper.findProperty( m_style, StylePropertyName.TEXT_COLOR );
    if( htmlColor == null )
      return null;

    final String id = "foreground" + m_style.getId();
    final RGB rgb = RGBUtilities.decodeHtmlColor( htmlColor );
    COLOR_REGISTRY.put( id, rgb );

    m_foregroundColor = COLOR_REGISTRY.get( id );
    return m_foregroundColor;
  }

  public Font getFont( )
  {
    if( m_font != null )
      return m_font;

    final String fontFamily = TableTypeHelper.findProperty( m_style, StylePropertyName.FONT_FAMILY );
    final String fontSize = TableTypeHelper.findProperty( m_style, StylePropertyName.FONT_SIZE );
    final int fontWeight = TableTypeHelper.toSWTFontWeight( TableTypeHelper.findProperty( m_style, StylePropertyName.FONT_WEIGHT ) );

    final FontData data = new FontData( fontFamily == null ? "Arial" : fontFamily, fontSize == null ? 10 : Integer.valueOf( fontSize ), fontWeight );
    FONT_REGISTRY.put( m_style.getId(), new FontData[] { data } );

    m_font = FONT_REGISTRY.get( m_style.getId() );

    return m_font;
  }

  public Image getImage( ) throws IOException
  {
    if( m_image != null )
      return m_image;

    final String urlString = TableTypeHelper.findProperty( m_style, StylePropertyName.ICON );
    if( urlString == null )
      return null;

    final ICatalog baseCatalog = KalypsoCorePlugin.getDefault().getCatalogManager().getBaseCatalog();
    final String uri = baseCatalog.resolve( urlString, urlString );

    final URL url = new URL( uri );

    final Image registered = IMAGE_REGISTRY.get( uri );
    if( registered == null )
    {
      m_image = new Image( null, url.openStream() );
      IMAGE_REGISTRY.put( uri, m_image );
    }
    else
      m_image = registered;

    return m_image;
  }
}
