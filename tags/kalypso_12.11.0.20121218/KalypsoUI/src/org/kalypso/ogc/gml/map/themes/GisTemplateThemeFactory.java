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
package org.kalypso.ogc.gml.map.themes;

import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.GisTemplateFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.types.StyledLayerType;
import org.kalypso.template.types.StyledLayerType.Style;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Theme factory for {@link org.kalypso.ogc.gml.GisTemplateFeatureTheme}s.
 * 
 * @author Gernot Belger
 */
public class GisTemplateThemeFactory extends AbstractThemeFactory
{
  @Override
  public IKalypsoTheme createTheme( final I10nString layerName, final StyledLayerType layerType, final URL context, final IMapModell mapModell, final IFeatureSelectionManager selectionManager )
  {
    return new GisTemplateFeatureTheme( layerName, layerType, context, selectionManager, mapModell );
  }

  @Override
  public void configureLayer( final IKalypsoTheme theme, final String id, final GM_Envelope bbox, final String srsName, final StyledLayerType layer, final IProgressMonitor monitor )
  {
    final GisTemplateFeatureTheme featureTheme = (GisTemplateFeatureTheme)theme;

    layer.setHref( featureTheme.getHref() );
    layer.setFeaturePath( featureTheme.getFeaturePath() );

    /* Configure the styles. */
    configureStyle( featureTheme, layer );
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
}