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

import org.eclipse.swt.graphics.RGB;
import org.kalypso.template.obsdiagview.Alignment;
import org.kalypso.template.obsdiagview.FontWeight;
import org.kalypso.template.obsdiagview.Obsdiagview;
import org.kalypso.template.obsdiagview.Obsdiagview.TitleFormat;

import de.openali.odysseus.chart.framework.model.figure.impl.IDefaultStyles;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;

/**
 * @author Dirk Kuch
 */
public class ObsdiagviewTextStyleProvider extends TextStyle implements ITextStyle
{
  public ObsdiagviewTextStyleProvider( final Obsdiagview xml )
  {
    super( getHeight( xml ), getStyleFamily( xml ), getRgbText( xml ), getRgbFill( xml ), getFontStyle( xml ), getFontWeight( xml ), getAlignment( xml ), getAlpha( xml ), isVisible( xml ) );
  }

  private static ALIGNMENT getAlignment( final Obsdiagview xml )
  {
    final Alignment alignment = xml.getTitleFormat().getAlignment();
    if( Alignment.LEFT.name().equals( alignment.name() ) )
      return ALIGNMENT.LEFT;
    else if( Alignment.CENTER.name().equals( alignment.name() ) )
      return ALIGNMENT.CENTER;
    else if( Alignment.RIGHT.name().equals( alignment.name() ) )
      return ALIGNMENT.RIGHT;

    return ALIGNMENT.LEFT;

  }

  private static boolean isVisible( final Obsdiagview xml )
  {
    return true;
  }

  private static int getAlpha( final Obsdiagview xml )
  {
    return IDefaultStyles.DEFAULT_ALPHA;
  }

  private static FONTWEIGHT getFontWeight( final Obsdiagview xml )
  {
    final TitleFormat format = xml.getTitleFormat();
    final FontWeight weight = format.getFontWeight();

    if( FontWeight.BOLD.equals( weight ) )
      return FONTWEIGHT.BOLD;

    return FONTWEIGHT.NORMAL;
  }

  private static FONTSTYLE getFontStyle( final Obsdiagview xml )
  {
    final TitleFormat format = xml.getTitleFormat();
    final FontWeight weight = format.getFontWeight();

    if( FontWeight.ITALIC.equals( weight ) )
      return FONTSTYLE.ITALIC;

    return FONTSTYLE.NORMAL;
  }

  private static RGB getRgbFill( final Obsdiagview xml )
  {
    return IDefaultStyles.DEFAULT_RGB_TEXT;
  }

  private static RGB getRgbText( final Obsdiagview xml )
  {
    return IDefaultStyles.DEFAULT_RGB_TEXT;
  }

  private static String getStyleFamily( final Obsdiagview xml )
  {
    final TitleFormat format = xml.getTitleFormat();

    return format.getFontFamily();
  }

  private static int getHeight( final Obsdiagview xml )
  {
    final TitleFormat format = xml.getTitleFormat();

    return format.getFontSize();
  }

}
