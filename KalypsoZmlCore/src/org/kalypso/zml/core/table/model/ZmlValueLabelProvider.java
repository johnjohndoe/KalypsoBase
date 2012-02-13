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
package org.kalypso.zml.core.table.model;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.DataColumn;
import org.kalypso.zml.core.table.binding.rule.ZmlCellRule;
import org.kalypso.zml.core.table.model.references.IZmlLabelStrategy;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelCellLabelProvider;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.InstantaneousValueLabelingStrategy;
import org.kalypso.zml.core.table.model.references.SumValueLabelingStrategy;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;

/**
 * @author Dirk Kuch
 */
public class ZmlValueLabelProvider implements IZmlModelCellLabelProvider
{

  private final ZmlModelColumn m_column;

  private IZmlLabelStrategy m_labeling;

  public ZmlValueLabelProvider( final ZmlModelColumn column )
  {
    m_column = column;

    final DataColumn datacolumn = column.getDataColumn();
    final String type = datacolumn.getType().getValueAxis();

    if( ITimeseriesConstants.TYPE_RAINFALL.equals( type ) )
      m_labeling = new SumValueLabelingStrategy();
    else
      m_labeling = new InstantaneousValueLabelingStrategy();
  }

  @Override
  public String getText( final IZmlModelCell cell )
  {
    try
    {
      return m_labeling.getText( cell );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return "error";
  }

  @Override
  public Image[] getImages( final IZmlModelCell cell )
  {
    final ZmlCellRule[] rules = cell.findActiveRules();
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
  public Font getFont( )
  {
    try
    {
      return m_column.getDataColumn().getDefaultStyle().getFont();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Color getBackground( )
  {
    try
    {
      return m_column.getDataColumn().getDefaultStyle().getBackgroundColor();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;

  }

  @Override
  public Color getForeground( )
  {
    try
    {
      return m_column.getDataColumn().getDefaultStyle().getForegroundColor();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;

  }

}
