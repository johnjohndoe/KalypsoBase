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
import org.kalypso.ogc.gml.wms.provider.images.IKalypsoImageProvider;

/**
 * @author Gernot Belger
 */
public class WMSThemeNode extends KalypsoThemeNode<KalypsoWMSTheme>
{
  private Image m_legend;

  WMSThemeNode( final IThemeNode parent, final KalypsoWMSTheme theme )
  {
    super( parent, theme );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.KalypsoThemeNode#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_legend != null )
    {
      m_legend.dispose();
      m_legend = null;
    }

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.wms.provider.legends.IKalypsoLegendProvider#getLegendGraphic(org.eclipse.swt.graphics.Font)
   */
  @Override
  public Image getLegendGraphic( final Font font ) throws CoreException
  {
    final KalypsoWMSTheme element = getElement();

    final IKalypsoImageProvider imageProvider = element.getImageProvider();

    if( imageProvider == null || !(imageProvider instanceof ILegendProvider) )
      return super.getLegendGraphic( font );

    if( m_legend == null )
    {
      final ILegendProvider legendProvider = (ILegendProvider) imageProvider;
      m_legend = legendProvider.getLegendGraphic( font );
    }

    if( m_legend != null )
    {
      // Clone this image, the returned image will be disposed outside!
      return new Image( font.getDevice(), m_legend, SWT.IMAGE_COPY );
    }

    return super.getLegendGraphic( font );
  }
}