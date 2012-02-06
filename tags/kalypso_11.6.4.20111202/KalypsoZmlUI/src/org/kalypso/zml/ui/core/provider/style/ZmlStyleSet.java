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
package org.kalypso.zml.ui.core.provider.style;

import java.awt.BasicStroke;
import java.awt.Color;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.ogc.sensor.template.ObsView.ItemData;

import de.openali.odysseus.chart.framework.model.figure.impl.IDefaultStyles;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;

/**
 * @author Dirk Kuch
 */
public class ZmlStyleSet extends AbstractStyleSetProvider implements IStyleSetProvider
{

  public ZmlStyleSet( final ItemData data )
  {
    buildLineStyles( data );
    buildPointStyles( data );
// buildTextStyles( data );
    buildAreaStyles( data );
  }

  private void buildLineStyles( final ItemData data )
  {
    final RGB rgb;
    final int alpha;

    final Color color = data.color;
    if( color == null )
      return;

    rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
    alpha = color.getAlpha();

    final BasicStroke stroke = (BasicStroke) data.stroke;
    final int width = Float.valueOf( stroke.getLineWidth() ).intValue();

    final LineStyle lineStyle = new LineStyle( width, rgb, alpha, IDefaultStyles.DEFAULT_DASHOFFSET, IDefaultStyles.DEFAULT_DASHARRAY, IDefaultStyles.DEFAULT_LINEJOIN, IDefaultStyles.DEFAULT_LINECAP, IDefaultStyles.DEFAULT_MITERLIMIT, IDefaultStyles.DEFAULT_VISIBILITY );
    getStyleSet().addStyle( LINE_PREFIX, lineStyle );
  }

  private void buildAreaStyles( final ItemData data )
  {

    final RGB rgb;
    final int alpha;

    final Color color = data.color;
    if( color == null )
      return;

    rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
    alpha = color.getAlpha();

    final ILineStyle lineStyle = getDefaultLineStyle();

    final AreaStyle areaStyle = new AreaStyle( new ColorFill( rgb ), alpha, lineStyle.clone(), true );
    getStyleSet().addStyle( AREA_PREFIX, areaStyle );
  }

  private void buildPointStyles( final ItemData data )
  {
    final RGB rgb;
    final int alpha;

    final Color color = data.color;
    if( color == null )
      return;

    rgb = new RGB( color.getRed(), color.getGreen(), color.getBlue() );
    alpha = color.getAlpha();

    final BasicStroke stroke = (BasicStroke) data.stroke;
    final int width = Float.valueOf( stroke.getLineWidth() ).intValue();

    final ILineStyle lineStyle = getDefaultLineStyle();
    final ILineStyle clone = lineStyle.clone();
    clone.setVisible( false );

    final PointStyle pointStyle = new PointStyle( clone, width, width, alpha, rgb, IDefaultStyles.DEFAULT_FILL_VISIBILITY, IDefaultStyles.DEFAULT_MARKER, IDefaultStyles.DEFAULT_VISIBILITY );
    getStyleSet().addStyle( POINT_PREFIX, pointStyle );
  }
}
