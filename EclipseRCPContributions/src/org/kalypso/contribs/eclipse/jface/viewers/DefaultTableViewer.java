/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * DefaultTableViewer handles common functionality that you wish you had when working with a TableViewer.
 * 
 * @author Marc Schlienger
 */
public class DefaultTableViewer extends TableViewer
{
  public static final String COLUMN_PROP_NAME = "columnName";

  public static final String COLUMN_PROP_EDITABLE = "columnEditable";

  public static final String COLUMN_PROP_WIDTH = "columnWidth";

  public static final String COLUMN_PROP_WIDTH_PERCENT = "columnWidthPercent";

  /**
   * True when this viewer is being disposed. This information is held so that removeAllColumns does not remove the
   * columns if this viewer is being disposed, it else leads to conflicts with SWT
   */
  private boolean m_disposing = false;

  public DefaultTableViewer( final Composite parent )
  {
    super( parent );
  }

  public DefaultTableViewer( final Composite parent, final int style )
  {
    super( parent, style );
  }

  public DefaultTableViewer( final Table table )
  {
    super( table );
  }

  /**
   * @see org.eclipse.jface.viewers.AbstractTableViewer#hookControl(org.eclipse.swt.widgets.Control)
   */
  @Override
  protected void hookControl( final Control control )
  {
    super.hookControl( control );

    // After resize, adjust the columns according to the percent settings.
    control.addControlListener( new ControlAdapter()
    {
      @Override
      public void controlResized( final ControlEvent e )
      {
        final Table table = getTable();
        final int totalWidth = table.getSize().x;

        final TableColumn[] columns = table.getColumns();
        for( final TableColumn tableColumn : columns )
        {
          if( tableColumn.getText() == null )
            continue;

          final Integer minWidth = (Integer) tableColumn.getData( COLUMN_PROP_WIDTH );
          final Integer widthPercent = (Integer) tableColumn.getData( COLUMN_PROP_WIDTH_PERCENT );

          if( minWidth == -1 )
            tableColumn.pack();
          else if( widthPercent == -1 )
            tableColumn.setWidth( minWidth );
          else
          {
            final int width = totalWidth * widthPercent / 100;
            final int widthToSet = Math.max( width - 2, minWidth ); // 2 pixels less, else we always get a scrollbar
            tableColumn.setWidth( widthToSet );
          }
        }
      }
    } );
  }

  /**
   * @see org.eclipse.jface.viewers.ContentViewer#handleDispose(org.eclipse.swt.events.DisposeEvent)
   */
  @Override
  protected void handleDispose( final DisposeEvent event )
  {
    m_disposing = true;

    super.handleDispose( event );
  }

  /**
   * Same as {@link #addColumn(String, String, null, int, int, boolean, SWT.CENTER)}.
   */
  public TableColumn addColumn( final String name, final String title, final int width, final int widthPercent, final boolean isEditable )
  {
    return addColumn( name, title, null, width, widthPercent, isEditable, SWT.CENTER, true );
  }

  /**
   * Adds a column to the underlying table control.
   */
  public TableColumn addColumn( final String name, final String title, final String tooltip, final int width, final int widthPercent, final boolean isEditable, final int style, final boolean isResizeable )
  {
    if( m_disposing )
      throw new IllegalStateException();

    final TableColumn tc = new TableColumn( getTable(), style );
    tc.setData( COLUMN_PROP_NAME, name );
    tc.setData( COLUMN_PROP_EDITABLE, Boolean.valueOf( isEditable ) );
    tc.setData( COLUMN_PROP_WIDTH, new Integer( width ) );
    tc.setData( COLUMN_PROP_WIDTH_PERCENT, new Integer( widthPercent ) );
    tc.setWidth( width );
    tc.setResizable( isResizeable );

    tc.setText( title );
    tc.setToolTipText( tooltip );

    return tc;
  }

  public void removeAllColumns( )
  {
    final Table table = getTable();
    if( m_disposing || table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    for( final TableColumn element : columns )
      element.dispose();

    refreshColumnProperties();
  }

  /**
   * Refreshes the column properties according to the current list of columns
   * 
   * @see org.eclipse.jface.viewers.TableViewer#getColumnProperties()
   */
  public void refreshColumnProperties( )
  {
    final Table table = getTable();
    if( m_disposing || table.isDisposed() )
      return;

    final TableColumn[] columns = table.getColumns();
    final String[] properties = new String[columns.length];

    for( int i = 0; i < properties.length; i++ )
      properties[i] = columns[i].getData( COLUMN_PROP_NAME ).toString();

    setColumnProperties( properties );
  }
}
