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
package org.kalypso.zml.ui.table.provider;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.IZmlTableComposite;
import org.kalypso.zml.ui.table.binding.BaseColumn;
import org.kalypso.zml.ui.table.binding.CellStyle;
import org.kalypso.zml.ui.table.binding.ZmlRule;
import org.kalypso.zml.ui.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.model.ZmlModelRow;
import org.kalypso.zml.ui.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.table.provider.strategy.IZmlLabelStrategy;
import org.kalypso.zml.ui.table.provider.strategy.IndexValueLabelingStrategy;
import org.kalypso.zml.ui.table.provider.strategy.InstantaneousValueLabelingStrategy;
import org.kalypso.zml.ui.table.schema.CellStyleType;
import org.kalypso.zml.ui.table.schema.IndexColumnType;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider extends ColumnLabelProvider
{
  private final BaseColumn m_column;

  private Object m_lastRow = null;

  private CellStyle m_lastCellStyle = null;

  private final ZmlTooltipSupport m_tooltip;

  private final IZmlTableComposite m_table;

  private final RuleMapper m_mapper = new RuleMapper();

  private IZmlLabelStrategy m_strategy;

  public ZmlLabelProvider( final IZmlTableComposite table, final BaseColumn column )
  {
    m_table = table;
    m_column = column;
    m_tooltip = new ZmlTooltipSupport( column );
  }

  public CellStyle findStyle( final IZmlModelRow row ) throws CoreException
  {
    if( m_lastRow == row )
      return m_lastCellStyle;

    final ZmlRule[] rules = m_mapper.findActiveRules( row, m_column );
    if( ArrayUtils.isNotEmpty( rules ) )
    {
      CellStyleType baseType = m_column.getDefaultStyle().getType();
      for( final ZmlRule rule : rules )
      {
        baseType = CellStyle.merge( baseType, rule.getStyle( row, m_column ).getType() );
      }

      m_lastCellStyle = new CellStyle( baseType );
    }
    else
    {
      m_lastCellStyle = m_column.getDefaultStyle();
    }

    m_lastRow = row;

    return m_lastCellStyle;
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getBackgroundColor();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getBackground( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getFont();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getFont( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getForegroundColor();
      }
      catch( final CoreException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getForeground( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = findStyle( (IZmlModelRow) element );

        return style.getImage();
      }
      catch( final Exception e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }

    return super.getImage( element );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element instanceof IZmlModelRow )
    {
      try
      {
        final IZmlModelRow row = (IZmlModelRow) element;
        final IZmlLabelStrategy strategy = getStrategy( row );
        if( strategy == null )
          return "";

        return strategy.getText();
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return super.getText( element );
  }

  private IZmlLabelStrategy getStrategy( final IZmlModelRow row )
  {
    if( m_strategy != null )
      return m_strategy;

    // index column type?
    if( m_column.getType() instanceof IndexColumnType )
      m_strategy = new IndexValueLabelingStrategy( this, row );
    else
    {
      // empty and hidden value column?
      final IZmlValueReference reference = row.get( m_column.getType() );
      if( reference == null )
        return null;

      // value column type - differ between momentan and summenwerten
      final IAxis valueAxis = reference.getValueAxis();

      if( valueAxis.getType() == "N" )
        throw new NotImplementedException();
      else
        m_strategy = new InstantaneousValueLabelingStrategy( this, row, reference );
    }

    return m_strategy;
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipStyle(java.lang.Object)
   */
  @Override
  public int getToolTipStyle( final Object object )
  {
    return SWT.TOP | SWT.BEGINNING | SWT.LEFT | SWT.SHADOW_NONE;
  }

  private Object getValue( final IZmlModelRow row ) throws SensorException
  {
    if( m_column.getType() instanceof IndexColumnType )
    {
      return row.getIndexValue();
    }

    final IZmlValueReference reference = row.get( m_column.getType() );
    if( reference != null )
      return reference.getValue();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipImage(java.lang.Object)
   */
  @Override
  public Image getToolTipImage( final Object object )
  {
    if( object instanceof ZmlModelRow )
    {
      return m_tooltip.getToolTipImage( (ZmlModelRow) object );
    }

    return super.getToolTipImage( object );
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
   */
  @Override
  public String getToolTipText( final Object element )
  {
    if( element instanceof ZmlModelRow )
    {
      return m_tooltip.getToolTipText( (ZmlModelRow) element );
    }

    return super.getToolTipText( element );
  }

  public RuleMapper getMapper( )
  {
    return m_mapper;
  }

  public BaseColumn getColumn( )
  {
    return m_column;
  }

}
