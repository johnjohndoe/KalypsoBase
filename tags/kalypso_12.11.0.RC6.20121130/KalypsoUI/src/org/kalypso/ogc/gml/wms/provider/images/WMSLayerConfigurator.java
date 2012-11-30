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
package org.kalypso.ogc.gml.wms.provider.images;

import java.util.LinkedHashMap;
import java.util.Map;

import org.deegree.graphics.sld.UserStyle;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.capabilities.Style;
import org.deegree.ogcwebservices.wms.capabilities.WMSCapabilities;

/**
 * @author Gernot Belger
 */
public class WMSLayerConfigurator
{
  private final Map<String, String> m_configuration = new LinkedHashMap<>();

  private final Map<String, String> m_whishConfiguration;

  public WMSLayerConfigurator( final WMSCapabilities capabilities, final Map<String, String> layersAndStyles )
  {
    m_whishConfiguration = layersAndStyles;

    configure( capabilities );
  }

  private void configure( final WMSCapabilities capabilities )
  {
    /* recurse through capabilities */
    final Layer layer = capabilities.getLayer();
    configureLayer( layer );
  }

  private void configureLayer( final Layer layer )
  {
    /* configure this layer */
    final String name = layer.getName();
    if( m_whishConfiguration.containsKey( name ) )
    {
      final String whishStyle = m_whishConfiguration.get( name );
      final String style = findStyle( layer, whishStyle );
      m_configuration.put( name, style );
    }

    // TODO: like this, the output is always ordered as it is in the capabilities. Does it makes sense to let the user
    // change the order?

    /* recurse */
    final Layer[] layers = layer.getLayer();
    for( final Layer child : layers )
      configureLayer( child );
  }

  private String findStyle( final Layer layer, final String whishStyle )
  {
    if( whishStyle != null )
    {
      /* Try to use style that was previously configured */
      final UserStyle style = layer.getStyle( whishStyle );
      if( style != null )
        return style.getName();
    }

    /* Else use first style or 'default' */
    final Style[] styles = layer.getStyles();
    if( styles.length > 0 )
      return styles[0].getName();

    /* Still try to use 'default' */
    return "default"; //$NON-NLS-1$
  }

  public String[] getLayers( )
  {
    return m_configuration.keySet().toArray( new String[m_configuration.size()] );
  }

  public String[] getStyles( )
  {
    return m_configuration.values().toArray( new String[m_configuration.size()] );
  }
}