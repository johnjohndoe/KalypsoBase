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

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;

/**
 * @author kimwerner
 */
public interface IChartLabelRenderer
{

  void eatBean( final TitleTypeBean titleTypeBean );

  boolean isDrawBorder( );

  Insets getInsets( );

  String getLabel( );

  ALIGNMENT getAlignmentX( );

  ALIGNMENT getAlignmentY( );

  int getRotation( );

  Point getSize( );

  ALIGNMENT getTextAnchorX( );

  ALIGNMENT getTextAnchorY( );

  ITextStyle getTextStyle( );

  void paint( final GC gc, final Point textAnchor );
  
  void paint( final GC gc, final Rectangle fixedWidth );

  void setDrawBorder( final boolean drawBorder );

  void setInsets( final Insets insets );

  void setLabel( final String label );

  void setAlignment( final ALIGNMENT alignmentX, final ALIGNMENT alignmentY );

  void setRotation( final int degree );

  void setTextAnchor( final ALIGNMENT positionX, final ALIGNMENT positionY );

  void setTextStyle( final ITextStyle textStyle );

}
