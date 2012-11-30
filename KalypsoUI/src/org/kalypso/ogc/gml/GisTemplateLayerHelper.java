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
import org.kalypso.template.gismapview.CascadingLayer;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Property;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * @author Holger Albert
 */
public final class GisTemplateLayerHelper
{
  private GisTemplateLayerHelper( )
  {
    throw new UnsupportedOperationException();
  }

  public static void updateProperties( final StyledLayerType layer, final IKalypsoTheme theme )
  {
    final List<Property> propertyList = layer.getProperty();
    for( final Property property : propertyList )
      theme.setProperty( property.getName(), property.getValue() );
  }

  public static JAXBElement< ? extends StyledLayerType> configureLayer( final IKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName, final IProgressMonitor monitor ) throws CoreException
  {
    /* Get the type. */
    final String linktype = theme.getType();

    /* Return null for unknown themes, else we produce invalid XML. */
    final IKalypsoThemeFactory themeFactory = ThemeFactoryExtension.getThemeFactory( linktype );
    if( themeFactory == null )
      return null;

    /* Basic stuff for all layers */
    final StyledLayerType layer = themeFactory.createLayerType( theme );
    layer.setName( theme.getName().getKey() );
    layer.setVisible( theme.isVisible() );
    layer.setId( id );

    layer.setFeaturePath( "" ); //$NON-NLS-1$
    layer.setHref( "" ); //$NON-NLS-1$
    layer.setActuate( "onRequest" ); //$NON-NLS-1$
    layer.setType( "simple" ); //$NON-NLS-1$

    layer.setLinktype( linktype );

    configureLegend( theme, layer );

    /* Configure the properties. */
    configureProperties( theme, layer );

    themeFactory.configureLayer( theme, id, bbox, srsName, layer, monitor );

    if( layer instanceof CascadingLayer )
      return TemplateUtilities.OF_GISMAPVIEW.createCascadingLayer( (CascadingLayer)layer );

    return TemplateUtilities.OF_GISMAPVIEW.createLayer( layer );
  }

  private static void configureLegend( final IKalypsoTheme theme, final StyledLayerType layer )
  {
    final org.kalypso.template.types.ObjectFactory extentFac = new org.kalypso.template.types.ObjectFactory();
    final String legendIcon = theme.getLegendIcon();
    if( legendIcon != null )
      layer.setLegendicon( extentFac.createStyledLayerTypeLegendicon( legendIcon ) );
    layer.setShowChildren( extentFac.createStyledLayerTypeShowChildren( theme.shouldShowLegendChildren() ) );
  }

  private static void configureProperties( final IKalypsoTheme theme, final StyledLayerType layer )
  {
    /* Get the property names. */
    final String[] propertyNames = theme.getPropertyNames();
    for( final String propertyName : propertyNames )
    {
      final Property property = TemplateUtilities.OF_TEMPLATE_TYPES.createStyledLayerTypeProperty();
      property.setName( propertyName );
      property.setValue( theme.getProperty( propertyName, null ) );
      layer.getProperty().add( property );
    }
  }
}