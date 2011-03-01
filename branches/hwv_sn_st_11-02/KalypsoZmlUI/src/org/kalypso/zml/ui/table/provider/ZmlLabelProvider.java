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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.ZmlModelRow;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.labeling.IZmlLabelStrategy;

/**
 * @author Dirk Kuch
 */
public class ZmlLabelProvider extends ColumnLabelProvider
{
  private final ZmlTooltipSupport m_tooltip;

  private final ExtendedZmlTableColumn m_column;

  public ZmlLabelProvider( final ExtendedZmlTableColumn column )
  {
    m_column = column;
    m_tooltip = new ZmlTooltipSupport( column );
  }

  /**
   * @see org.eclipse.jface.viewers.ColumnLabelProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground( final Object element )
  {
    if( !m_column.isVisible() )
      return null;

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = m_column.findStyle( (IZmlModelRow) element );

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
    if( !m_column.isVisible() )
      return null;

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = m_column.findStyle( (IZmlModelRow) element );

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
    if( !m_column.isVisible() )
      return null;

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final CellStyle style = m_column.findStyle( (IZmlModelRow) element );

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
    if( !m_column.isVisible() )
      return null;

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final ZmlTableImageMerger iconMerger = new ZmlTableImageMerger( 2 );

        final IZmlModelRow row = (IZmlModelRow) element;
        final ZmlRule[] rules = m_column.findActiveRules( row );
        for( final ZmlRule rule : rules )
        {
          final CellStyle style = rule.getPlainStyle();
          final Image image = style.getImage();
          if( image != null )
            iconMerger.addImage( new ZmlTableImage( style.getIdentifier(), image ) );
        }

        return iconMerger.createImage( PlatformUI.getWorkbench().getDisplay() );
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
    if( !m_column.isVisible() )
      return "";

    if( element instanceof IZmlModelRow )
    {
      try
      {
        final IZmlModelRow row = (IZmlModelRow) element;
        final IZmlLabelStrategy strategy = m_column.getLabelingStrategy();
        if( strategy == null )
          return "";

        return strategy.getText( row );
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return super.getText( element );
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipStyle(java.lang.Object)
   */
  @Override
  public int getToolTipStyle( final Object object )
  {
    return SWT.TOP | SWT.BEGINNING | SWT.LEFT | SWT.SHADOW_NONE;
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipImage(java.lang.Object)
   */
  @Override
  public Image getToolTipImage( final Object object )
  {
    if( !ZmlTooltipSupport.isShowTooltips() )
      return null;

    if( !m_column.isVisible() )
      return null;
    else if( object instanceof IZmlModelRow )
      return m_tooltip.getToolTipImage();

    return super.getToolTipImage( object );
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
   */
  @Override
  public String getToolTipText( final Object element )
  {
    if( !ZmlTooltipSupport.isShowTooltips() )
      return null;

    if( !m_column.isVisible() )
      return null;

    if( element instanceof ZmlModelRow )
    {
      return m_tooltip.getToolTipText( (ZmlModelRow) element );
    }

    return super.getToolTipText( element );
  }

  /**
   * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipShift(java.lang.Object)
   */
  @Override
  public Point getToolTipShift( final Object object )
  {
    return new Point( 10, 20 );
  }

  public Object getPlainValue( final IZmlModelRow row ) throws SensorException
  {
    final IZmlValueReference reference = row.get( m_column.getModelColumn() );

    return reference.getValue();
  }
}
