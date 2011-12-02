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
package org.kalypso.ogc.gml.outline.nodes;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.ogc.gml.map.themes.KalypsoWMSTheme;

/**
 * This node provides images for the ouline and the legend.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class WMSThemeNode extends KalypsoThemeNode<KalypsoWMSTheme>
{
  /**
   * The constructor.
   * 
   * @param parent
   *          The parent node.
   * @param theme
   *          The wms theme.
   */
  public WMSThemeNode( IThemeNode parent, KalypsoWMSTheme theme )
  {
    super( parent, theme );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getLegendGraphic(java.lang.String[],
   *      org.eclipse.swt.graphics.Font)
   */
  @Override
  public Image getLegendGraphic( String[] whiteList, Font font ) throws CoreException
  {
    /* Check, if this theme is allowed. */
    if( !checkWhiteList( whiteList ) )
      return null;

    /* Get the wms theme. */
    KalypsoWMSTheme element = getElement();

    /* Ask the theme for a legend. */
    Image legendGraphic = element.getLegendGraphic( font );

    /* Clone this image, the returned image will be disposed outside! */
    if( legendGraphic != null )
      return new Image( font.getDevice(), legendGraphic, SWT.IMAGE_COPY );

    return super.getLegendGraphic( whiteList, font );
  }
}