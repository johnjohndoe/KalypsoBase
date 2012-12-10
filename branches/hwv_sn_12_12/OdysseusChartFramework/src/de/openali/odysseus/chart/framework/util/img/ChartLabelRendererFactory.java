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
package de.openali.odysseus.chart.framework.util.img;

import java.awt.Insets;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author kimwerner
 */
public class ChartLabelRendererFactory
{
  static public TitleTypeBean getTickLabelType( final POSITION axisPosition, final Insets tickInsets, final ITextStyle tickLabelStyle )
  {
    final TitleTypeBean titleType = new TitleTypeBean( null );
    titleType.setPositionVertical( ALIGNMENT.CENTER );
    titleType.setPositionHorizontal( ALIGNMENT.CENTER );
    titleType.setInsets( tickInsets );
    titleType.setTextStyle( tickLabelStyle );

    if( axisPosition.getOrientation() == ORIENTATION.VERTICAL )
    {
      titleType.setMirrorVertical( true );
      titleType.setTextAnchorX( ALIGNMENT.LEFT );
      titleType.setTextAnchorY( ALIGNMENT.CENTER );
      titleType.setRotation( 90 );
      if( axisPosition == POSITION.LEFT )
        titleType.setMirrorHorizontal( true );
    }
    else
    {
      titleType.setTextAnchorX( ALIGNMENT.CENTER );
      titleType.setTextAnchorY( ALIGNMENT.TOP );
      if( axisPosition == POSITION.TOP )
        titleType.setMirrorVertical( true );
    }
    return titleType;
  }

  static public IChartLabelRenderer getTickLabelRenderer( final POSITION axisPosition, final Insets tickInsets, final ITextStyle tickLabelStyle, final IAreaStyle tickFrameStyle )
  {
    final IChartLabelRenderer labelRenderer = new GenericChartLabelRenderer( getTickLabelType( axisPosition, tickInsets, tickLabelStyle ), tickFrameStyle );
    labelRenderer.setBorderStyle( tickFrameStyle );
    return labelRenderer;
  }

  static public IChartLabelRenderer getAxisLabelRenderer( final POSITION axisPosition, final Insets axisLabelInsets, final ITextStyle axisLabelStyle, final IAreaStyle labelFrameStyle )
  {
    final IChartLabelRenderer labelRenderer = new GenericChartLabelRenderer( getAxisLabelType( axisPosition, null, axisLabelInsets, axisLabelStyle ), labelFrameStyle );
    labelRenderer.setBorderStyle( labelFrameStyle );
    return labelRenderer;
  }

  public static TitleTypeBean getAxisLabelType( final POSITION axisPosition, final String label, final Insets axisLabelInsets, final ITextStyle axisLabelStyle )
  {
    final TitleTypeBean titleType = new TitleTypeBean( label, ALIGNMENT.CENTER, ALIGNMENT.CENTER, axisLabelStyle, axisLabelInsets );
    titleType.setTextAnchorX( ALIGNMENT.CENTER );
    titleType.setTextAnchorY( ALIGNMENT.CENTER );
    if( axisPosition == POSITION.LEFT )
    {
      titleType.setRotation( 180 );
    }
    else if( axisPosition == POSITION.RIGHT )
    {
      titleType.setMirrorVertical( true );
    }
    return titleType;
  }
}
