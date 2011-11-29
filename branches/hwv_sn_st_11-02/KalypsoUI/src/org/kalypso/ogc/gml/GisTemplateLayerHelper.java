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
package org.kalypso.ogc.gml;

import java.util.List;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.core.jaxb.TemplateUtilities;
import org.kalypso.ogc.gml.map.themes.KalypsoImageTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoLegendTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoScaleTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoTextTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;
import org.kalypso.template.gismapview.CascadingLayer;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Property;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Holger Albert
 */
public class GisTemplateLayerHelper
{
  /**
   * The constructor.
   */
  private GisTemplateLayerHelper( )
  {
  }

  public static void updateProperties( final StyledLayerType layer, final IKalypsoTheme theme )
  {
    final List<Property> propertyList = layer.getProperty();
    for( final Property property : propertyList )
      theme.setProperty( property.getName(), property.getValue() );
  }

  public static JAXBElement< ? extends StyledLayerType> configureLayer( final IKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName, final IProgressMonitor monitor ) throws CoreException
  {
    if( theme instanceof CascadingLayerKalypsoTheme )
      return configureCascadingLayerKalypsoTheme( (CascadingLayerKalypsoTheme) theme, id, srsName, monitor );

    if( theme instanceof GisTemplateFeatureTheme )
      return configureGisTemplateFeatureTheme( (GisTemplateFeatureTheme) theme, id );

    if( theme instanceof KalypsoWMSTheme )
      return configureKalypsoWMSTheme( (KalypsoWMSTheme) theme, id );

    if( theme instanceof KalypsoPictureTheme )
      return configureKalypsoPictureTheme( (KalypsoPictureTheme) theme, id );

    if( theme instanceof CascadingKalypsoTheme )
      return configureCascadingKalypsoTheme( (CascadingKalypsoTheme) theme, id, bbox, srsName, monitor );

    if( theme instanceof KalypsoLegendTheme )
      return configureKalypsoLegendTheme( (KalypsoLegendTheme) theme, id );

    if( theme instanceof KalypsoScaleTheme )
      return configureKalypsoScaleTheme( (KalypsoScaleTheme) theme, id );

    if( theme instanceof KalypsoImageTheme )
      return configureKalypsoImageTheme( (KalypsoImageTheme) theme, id );

    if( theme instanceof KalypsoTextTheme )
      return configureKalypsoTextTheme( (KalypsoTextTheme) theme, id );

    return configureViaType( theme, id, bbox, srsName );
  }

  private static JAXBElement< ? extends StyledLayerType> configureCascadingLayerKalypsoTheme( final CascadingLayerKalypsoTheme theme, final String id, final String srsName, final IProgressMonitor monitor ) throws CoreException
  {
    final CascadingLayer layer = GisTemplateHelper.OF_GISMAPVIEW.createCascadingLayer();
    layer.setId( id );
    layer.setLinktype( "gmt" ); //$NON-NLS-1$
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$
    layer.setName( theme.getName().getKey() );
    layer.setVisible( theme.isVisible() );
    layer.getDepends();

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the layer list. */
    theme.fillLayerList( layer.getLayer(), id, srsName, monitor );

    /* Configure the properties. */
    configureProperties( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createCascadingLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureGisTemplateFeatureTheme( final GisTemplateFeatureTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setId( id );
    layer.setHref( theme.getHref() );
    layer.setLinktype( theme.getLayerKey().getType() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$
    layer.setFeaturePath( theme.getFeaturePath() );
    layer.setName( theme.getName().getKey() );
    layer.setVisible( theme.isVisible() );

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the styles. */
    configureStyle( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoWMSTheme( final KalypsoWMSTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setVisible( theme.isVisible() );
    layer.setId( id );
    layer.setHref( theme.getSource() );
    layer.setLinktype( theme.getType() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the style. */
    configureStyle( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoPictureTheme( final KalypsoPictureTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setVisible( theme.isVisible() );
    layer.setId( id );
    layer.setHref( theme.getSource() );
    layer.setLinktype( theme.getType() );
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureCascadingKalypsoTheme( final CascadingKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName, final IProgressMonitor monitor ) throws CoreException
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setId( id );
    layer.setHref( theme.getMapViewRefUrl() );
    layer.setLinktype( "gmt" ); //$NON-NLS-1$
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$
    layer.setName( theme.getName().getKey() );
    layer.setVisible( theme.isVisible() );

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    theme.createGismapTemplate( bbox, srsName, monitor );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoLegendTheme( final KalypsoLegendTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setVisible( theme.isVisible() );
    layer.setId( id );
    layer.setHref( "" ); //$NON-NLS-1$
    layer.setLinktype( "legend" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoScaleTheme( final KalypsoScaleTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setVisible( theme.isVisible() );
    layer.setId( id ); //$NON-NLS-1$
    layer.setLinktype( "scale" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoImageTheme( final KalypsoImageTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setVisible( theme.isVisible() );
    layer.setId( id );
    layer.setHref( "" ); //$NON-NLS-1$
    layer.setLinktype( "image" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureKalypsoTextTheme( final KalypsoTextTheme theme, final String id )
  {
    final StyledLayerType layer = GisTemplateHelper.OF_TEMPLATE_TYPES.createStyledLayerType();
    layer.setName( theme.getName().getKey() );
    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setVisible( theme.isVisible() );
    layer.setId( id );
    layer.setHref( "" ); //$NON-NLS-1$
    layer.setLinktype( "text" ); //$NON-NLS-1$

    /* Configure the legend. */
    configureLegend( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static JAXBElement< ? extends StyledLayerType> configureViaType( final IKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName )
  {
    /* Get the type. */
    final String type = theme.getType();

    /* Return null for unknown themes, else we produce invalid XML. */
    final IKalypsoThemeFactory themeFactory = ThemeFactoryExtension.getThemeFactory( type );
    if( themeFactory == null )
      return null;

    return themeFactory.configureLayer( theme, id, bbox, srsName );
  }

  private static void configureLegend( final AbstractKalypsoTheme theme, final StyledLayerType layer )
  {
    final org.kalypso.template.types.ObjectFactory extentFac = new org.kalypso.template.types.ObjectFactory();
    final String legendIcon = theme.getLegendIcon();
    if( legendIcon != null )
      layer.setLegendicon( extentFac.createStyledLayerTypeLegendicon( legendIcon ) );
    layer.setShowChildren( extentFac.createStyledLayerTypeShowChildren( theme.shouldShowLegendChildren() ) );
  }

  private static void configureStyle( final GisTemplateFeatureTheme theme, final StyledLayerType layer )
  {
    final org.kalypso.template.types.ObjectFactory extentFac = new org.kalypso.template.types.ObjectFactory();
    final List<Style> stylesList = layer.getStyle();
    for( final IKalypsoStyle style : theme.getStyleList() )
    {
      final Style styleType = extentFac.createStyledLayerTypeStyle();
      style.fillStyleType( stylesList, styleType );
    }
  }

  private static void configureStyle( final KalypsoWMSTheme theme, final StyledLayerType layer )
  {
    final org.kalypso.template.types.ObjectFactory extentFac = new org.kalypso.template.types.ObjectFactory();
    final Style[] oldStyles = theme.getStyles();
    for( final Style oldStyle : oldStyles )
    {
      final Style newStyle = extentFac.createStyledLayerTypeStyle();
      newStyle.setActuate( oldStyle.getActuate() );
      newStyle.setArcrole( oldStyle.getArcrole() );
      newStyle.setHref( oldStyle.getHref() );
      newStyle.setLinktype( oldStyle.getLinktype() );
      newStyle.setRole( oldStyle.getRole() );
      newStyle.setShow( oldStyle.getShow() );
      newStyle.setStyle( oldStyle.getStyle() );
      newStyle.setTitle( oldStyle.getTitle() );
      newStyle.setType( oldStyle.getType() );

      layer.getStyle().add( newStyle );
    }
  }

  private static void configureProperties( final AbstractKalypsoTheme theme, final StyledLayerType layer )
  {
    /* Get the property names. */
    String[] propertyNames = theme.getPropertyNames();
    for( String propertyName : propertyNames )
    {
      Property property = TemplateUtilities.OF_TEMPLATE_TYPES.createStyledLayerTypeProperty();
      property.setName( propertyName );
      property.setValue( theme.getProperty( propertyName, null ) );
      layer.getProperty().add( property );
    }
  }
}