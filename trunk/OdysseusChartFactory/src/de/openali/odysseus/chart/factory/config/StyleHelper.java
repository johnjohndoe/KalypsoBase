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
package de.openali.odysseus.chart.factory.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chartconfig.x020.AbstractStyleType;
import de.openali.odysseus.chartconfig.x020.AreaStyleType;
import de.openali.odysseus.chartconfig.x020.ChartType;
import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.LayersType;
import de.openali.odysseus.chartconfig.x020.LineStyleType;
import de.openali.odysseus.chartconfig.x020.PointStyleType;
import de.openali.odysseus.chartconfig.x020.ReferencableType;
import de.openali.odysseus.chartconfig.x020.StylesDocument.Styles;
import de.openali.odysseus.chartconfig.x020.TextStyleType;

/**
 * @author Dirk Kuch
 */
public final class StyleHelper
{
  private StyleHelper( )
  {
  }

  /**
   * @param b
   *          a byte value
   */
  public static int byteToInt( final byte b )
  {
    return b & 0xff;
  }

  /**
   * @param color
   *          3 byte array
   */
  public static RGB colorByteToRGB( final byte[] color )
  {
    final int red = byteToInt( color[0] );
    final int green = byteToInt( color[1] );
    final int blue = byteToInt( color[2] );

    return new RGB( red, green, blue );
  }

  public static ALIGNMENT getAlignment( final de.openali.odysseus.chartconfig.x020.AlignmentType.Enum alignment )
  {
    if( alignment == null )
      return ALIGNMENT.LEFT;
    if( "LEFT".equals( alignment.toString() ) )
    {
      return ALIGNMENT.LEFT;
    }
    else if( "CENTER".equals( alignment.toString() ) )
    {
      return ALIGNMENT.CENTER;
    }
    else if( "RIGHT".equals( alignment.toString() ) )
    {
      return ALIGNMENT.RIGHT;
    }

    return ALIGNMENT.LEFT;
  }

  public static AbstractStyleType findStyle( final ReferencableType[] baseReferences, final String identifier )
  {
    final Set<Styles> styles = new LinkedHashSet<Styles>();

    for( final ReferencableType reference : baseReferences )
    {
      if( reference instanceof LayerType )
      {
        Collections.addAll( styles, findStyles( (LayerType) reference ) );
      }
      else if( reference instanceof ChartType )
      {
        Collections.addAll( styles, findStyles( (ChartType) reference ) );
      }
      else
        throw new NotImplementedException();
    }

    for( final Styles style : styles )
    {
      final AbstractStyleType found = findStyle( style, identifier );
      if( found != null )
        return found;
    }

    return null;
  }

  private static Styles[] findStyles( final ChartType chartType )
  {
    final Set<Styles> styles = new LinkedHashSet<Styles>();
    styles.add( chartType.getStyles() );

    final LayersType layersType = chartType.getLayers();
    final LayerType[] layers = layersType.getLayerArray();
    for( final LayerType layerType : layers )
    {
      final Styles layerStyles = layerType.getStyles();
      if( layerStyles != null )
        styles.add( layerStyles );
    }

    return styles.toArray( new Styles[] {} );
  }

  private static Styles[] findStyles( final LayerType layerType )
  {
    final Set<Styles> styles = new LinkedHashSet<Styles>();
    styles.add( layerType.getStyles() );

    return styles.toArray( new Styles[] {} );
  }

  public static AbstractStyleType findStyle( final Styles styles, final String identifier )
  {
    if( styles == null )
      return null;

    final AreaStyleType[] areaStyleArray = styles.getAreaStyleArray();
    for( final AreaStyleType areaStyleType : areaStyleArray )
    {
      if( areaStyleType.getRole().equals( identifier ) )
        return areaStyleType;
    }

    final LineStyleType[] lineStyleArray = styles.getLineStyleArray();
    for( final LineStyleType lineStyleType : lineStyleArray )
    {
      if( lineStyleType.getRole().equals( identifier ) )
        return lineStyleType;
    }

    final PointStyleType[] pointStyleArray = styles.getPointStyleArray();
    for( final PointStyleType pointStyleType : pointStyleArray )
    {
      if( pointStyleType.getRole().equals( identifier ) )
        return pointStyleType;
    }

    final TextStyleType[] textStyleArray = styles.getTextStyleArray();
    for( final TextStyleType textStyleType : textStyleArray )
    {
      if( textStyleType.getRole().equals( identifier ) )
        return textStyleType;
    }

    return null;
  }
}
