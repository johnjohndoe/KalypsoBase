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
package org.kalypso.zml.ui.table.layout;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.ColumnHeader;
import org.kalypso.zml.core.table.binding.rule.ZmlRule;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.schema.DataColumnType;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.provider.ZmlTableImage;
import org.kalypso.zml.ui.table.provider.ZmlTableImageMerger;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableLayoutHandler
{
  protected boolean m_firstChange = true;

  protected final ZmlTableComposite m_table;

  private final UIJob m_job = new UIJob( "" )
  {

    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      if( m_table.isDisposed() )
        return Status.CANCEL_STATUS;

      updateColumns();

      if( m_firstChange )
      {
        /* execute as separate ui job, otherwise it won't work */
        new UIJob( "" )
        {

          @Override
          public IStatus runInUIThread( final IProgressMonitor mon )
          {
            final RevealTableCommand command = new RevealTableCommand( m_table );
            command.execute( monitor );

            return Status.OK_STATUS;
          }
        }.schedule();

        m_firstChange = false;
      }

      return Status.OK_STATUS;
    }
  };

  public ZmlTableLayoutHandler( final ZmlTableComposite table )
  {
    m_table = table;
  }

  public void tableChanged( )
  {
    if( m_job.getState() == Job.SLEEPING )
      m_job.cancel();

    m_job.schedule( 250 );
  }

  protected void updateColumns( )
  {
    final ExtendedZmlTableColumn[] columns = m_table.getColumns();
    for( final ExtendedZmlTableColumn column : columns )
    {
      final BaseColumn columnType = column.getColumnType();
      final TableViewerColumn tableViewerColumn = column.getTableViewerColumn();
      final TableColumn tableColumn = tableViewerColumn.getColumn();

      updateHeader( column );

      /** only update headers of data column types */

      if( columnType.getType() instanceof DataColumnType )
      {
        final IZmlModelColumn modelColumn = column.getModelColumn();
        if( modelColumn == null )
        {
          final String label = columnType.getLabel();

          tableColumn.setWidth( 0 );
          tableColumn.setText( label );
          tableColumn.setResizable( false );
          tableColumn.setMoveable( false );
        }
        else
        {
          final String label = modelColumn.getLabel();

          tableColumn.setText( label );
          pack( tableColumn, columnType, label );
        }
      }
      else
      {
        final String label = columnType.getLabel();

        tableColumn.setText( label );
        pack( tableColumn, columnType, label );
      }

    }
  }

  private void updateHeader( final ExtendedZmlTableColumn column )
  {
    final TableColumn tableColumn = column.getTableViewerColumn().getColumn();
    final ZmlRule[] applied = column.getAppliedRules();

    final ZmlTableImageMerger provider = new ZmlTableImageMerger( 2 );

    final BaseColumn columnType = column.getColumnType();
    for( final ColumnHeader header : columnType.getHeaders() )
    {
      try
      {
        final Image icon = header.getIcon();
        if( icon != null )
          provider.addImage( new ZmlTableImage( header.getIdentifier(), icon ) );
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    for( final ZmlRule rule : applied )
    {
      try
      {
        if( rule.hasHeaderIcon() )
        {
          final CellStyle style = rule.getPlainStyle();
          final Image image = style.getImage();
          if( image != null )
            provider.addImage( new ZmlTableImage( style.getIdentifier(), image ) );
        }
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    tableColumn.setImage( provider.createImage( tableColumn.getDisplay() ) );
  }

  private void pack( final TableColumn column, final BaseColumn base, final String label )
  {
    if( base.isAutopack() )
    {
      column.pack();
    }
    else
    {
      final Integer width = base.getWidth();
      if( width == null )
      {
        final Integer calculated = calculateSize( column, base, label );
        if( calculated == null )
          column.pack();
        else
        {
          /* set biggest value - calculated header with or packed cell width */
          column.pack();
          final int packedWith = column.getWidth();

          if( packedWith < calculated )
            column.setWidth( calculated );
        }

      }
      else
        column.setWidth( width );
    }

    column.setMoveable( false );
    column.setResizable( true );
  }

  /**
   * @return minimal header size
   */
  private Integer calculateSize( final TableColumn table, final BaseColumn base, final String label )
  {
    final Device dev = PlatformUI.getWorkbench().getDisplay();
    final Image image = new Image( dev, 1, 1 );
    final GC gc = new GC( image );

    try
    {
      final int spacer = 10;

      final CellStyle style = base.getDefaultStyle();

      if( style.getFont() != null )
        gc.setFont( style.getFont() );

      final Point extend = gc.textExtent( label );

      final Image img = table.getImage();
      if( img != null )
      {
        return extend.x + spacer * 2 + img.getImageData().width;
      }

      return extend.x + spacer;
    }
    catch( final Throwable t )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );

      return null;
    }
    finally
    {
      gc.dispose();
      image.dispose();
    }
  }

}
