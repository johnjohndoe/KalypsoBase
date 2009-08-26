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
package org.kalypso.ui.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.GisTemplateMapModell;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Property;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypso.ui.i18n.Messages;

public class AddThemeCommand implements IThemeCommand
{
  private final Collection<Style> m_styles = new ArrayList<Style>();

  private final Map<String, String> m_properties = new HashMap<String, String>();

  private IKalypsoLayerModell m_mapModell;

  private IKalypsoTheme m_theme;

  private final String m_name;

  private final String m_type;

  private final String m_featurePath;

  private final String m_source;

  private StyledLayerType m_layer;

  /**
   * This command adds a new theme to a map.
   * 
   * @param model
   *          active GisTemplateMapModell from the active Map
   * @param name
   *          name of the layer
   * @param type
   *          type of source (must be a valid loader) ex.: wms, wfs, shape, etc.
   * @param featurePath
   *          the feature path in the gml workspace
   * @param source
   *          a String having keywords and (paired values) depending on the Loader context
   * @param style
   *          name of the style
   * @param styleLocation
   *          a valid resource path (of the used plug-in or a valid URL )
   */
  public AddThemeCommand( final IKalypsoLayerModell model, final String name, final String type, final String featurePath, final String source )
  {
    m_mapModell = model;
    m_name = name;
    m_type = type;
    m_featurePath = featurePath;
    m_source = source;
  }

  /**
   * Adds properties to this command which will be added to the theme after creation.<br>
   * Must be called before {@link #process()}
   */
  public void addProperties( final Map<String, String> properties )
  {
    m_properties.putAll( properties );
  }

  /**
   * Adds a property to this command which will be added to the theme after creation.<br>
   * Must be called before {@link #process()}
   */
  public void addProperty( final String key, final String value )
  {
    m_properties.put( key, value );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  public String getDescription( )
  {
    return Messages.getString("org.kalypso.ui.action.AddThemeCommand.0"); //$NON-NLS-1$
  }

  private StyledLayerType init( )
  {
    final int id = m_mapModell.getThemeSize() + 1;

    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setHref( m_source );
    layer.setFeaturePath( m_featurePath );
    layer.setName( m_name );
    layer.setLinktype( m_type );
    layer.setId( "ID_" + id ); //$NON-NLS-1$
    layer.setVisible( true );

    final List<Style> styleList = layer.getStyle();
    for( final Style style : m_styles )
      styleList.add( style );

    for( final Entry<String, String> entry : m_properties.entrySet() )
    {
      final Property property = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerTypeProperty();
      property.setName( entry.getKey() );
      property.setValue( entry.getValue() );

      layer.getProperty().add( property );
    }

    return layer;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#isUndoable()
   */
  public boolean isUndoable( )
  {
    return true;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#process()
   */
  public void process( ) throws Exception
  {
    m_layer = init();
    m_theme = m_mapModell.insertLayer( m_layer, 0 );
    m_mapModell.activateTheme( m_theme );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  public void redo( ) throws Exception
  {
    m_mapModell.addLayer( m_layer );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  public void undo( ) throws Exception
  {
    m_mapModell.removeTheme( m_theme );
  }

  public StyledLayerType updateMapModel( final GisTemplateMapModell model )
  {
    m_mapModell = model;
    return init();
  }

  /**
   * @see org.kalypso.ui.action.IThemeCommand#toStyledLayerType()
   */
  public StyledLayerType toStyledLayerType( )
  {
    return init();
  }

  /**
   * Adds a style to this command which will be added to the theme after creation.<br>
   * Must be called before {@link #process()}
   */
  public void addStyle( final String style, final String location )
  {
    final StyledLayerType.Style layertype = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerTypeStyle();
    layertype.setLinktype( "sld" ); //$NON-NLS-1$
    layertype.setStyle( style );
    layertype.setHref( location );
    layertype.setActuate( "onRequest" ); //$NON-NLS-1$
    layertype.setType( "simple" ); //$NON-NLS-1$

    m_styles.add( layertype );
  }
}