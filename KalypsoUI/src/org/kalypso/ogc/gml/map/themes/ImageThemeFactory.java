/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeFactory;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.types.StyledLayerType;
import org.kalypsodeegree.model.geometry.GM_Envelope;

/**
 * Theme factory for {@link KalypsoImageTheme}s.
 * 
 * @author Holger Albert
 */
public class ImageThemeFactory implements IKalypsoThemeFactory
{
  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeFactory#createTheme(org.kalypso.commons.i18n.I10nString,
   *      org.kalypso.template.types.StyledLayerType, java.net.URL, org.kalypso.ogc.gml.mapmodel.IMapModell,
   *      org.kalypso.ogc.gml.selection.IFeatureSelectionManager)
   */
  @Override
  public IKalypsoTheme createTheme( I10nString layerName, StyledLayerType layerType, URL context, IMapModell mapModell, IFeatureSelectionManager selectionManager )
  {
    return new KalypsoImageTheme( layerName, mapModell );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeFactory#configureLayer(org.kalypso.ogc.gml.IKalypsoTheme, java.lang.String,
   *      org.kalypsodeegree.model.geometry.GM_Envelope, java.lang.String)
   */
  @Override
  public JAXBElement< ? extends StyledLayerType> configureLayer( IKalypsoTheme theme, String id, GM_Envelope bbox, String srsName )
  {
    throw new NotImplementedException();
  }
}