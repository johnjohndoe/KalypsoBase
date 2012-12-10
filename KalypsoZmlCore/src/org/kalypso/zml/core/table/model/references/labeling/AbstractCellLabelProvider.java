/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.core.table.model.references.labeling;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import net.sourceforge.nattable.style.CellStyleAttributes;
import net.sourceforge.nattable.style.HorizontalAlignmentEnum;
import net.sourceforge.nattable.style.Style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.core.table.schema.AlignmentType;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractCellLabelProvider implements IZmlCellLabelProvider
{
  private final BaseColumn m_column;

  public AbstractCellLabelProvider( final BaseColumn column )
  {
    m_column = column;
  }

  @Override
  public Image[] getImages( final ZmlModelViewport viewport, final IZmlModelCell cell ) throws SensorException
  {
    final ZmlCellRule[] rules = cell.findActiveRules( viewport );
    final Set<Image> images = new LinkedHashSet<Image>();

    for( final ZmlCellRule rule : rules )
    {
      try
      {
        final CellStyle style = resolveRuleStyle( rule, (IZmlModelValueCell) cell );
        if( Objects.isNull( style ) )
          continue;

        final Image image = style.getImage();
        if( Objects.isNull( image ) )
          continue;

        images.add( image );
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }
    }

    return images.toArray( new Image[] {} );
  }

  public CellStyle resolveRuleStyle( final ZmlCellRule rule, final IZmlModelValueCell reference )
  {
    if( Objects.isNull( reference ) )
      return null;

    try
    {
      final IZmlCellRuleImplementation implementation = rule.getImplementation();
      final CellStyle style = implementation.getCellStyle( rule, reference );
      if( Objects.isNotNull( style ) )
        return style;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Style getStyle( final ZmlModelViewport viewport, final IZmlModelCell cell )
  {
    final Style style = new Style();

    try
    {
      final CellStyle defaultStyle = cell.getStyle( viewport );

      final Color background = defaultStyle.getBackgroundColor();
      if( Objects.isNotNull( background ) )
        style.setAttributeValue( CellStyleAttributes.BACKGROUND_COLOR, background );

      final Color foreground = defaultStyle.getForegroundColor();
      if( Objects.isNotNull( foreground ) )
        style.setAttributeValue( CellStyleAttributes.FOREGROUND_COLOR, foreground );

      final Font font = defaultStyle.getFont();
      if( Objects.isNotNull( font ) )
        style.setAttributeValue( CellStyleAttributes.FONT, font );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    final AlignmentType alignment = m_column.getAlignment();
    if( Objects.isNotNull( alignment ) )
    {
      if( AlignmentType.LEFT.equals( alignment ) )
        style.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT );
      else if( AlignmentType.CENTER.equals( alignment ) )
        style.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER );
      else if( AlignmentType.RIGHT.equals( alignment ) )
        style.setAttributeValue( CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT );
    }

    return style;
  }
}
