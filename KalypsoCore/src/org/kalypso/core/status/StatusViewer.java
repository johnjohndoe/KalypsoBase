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
package org.kalypso.core.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.IStatusWithTime;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerUtil;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerColumnItem;
import org.kalypso.contribs.eclipse.jface.viewers.table.ColumnWidthInfo;
import org.kalypso.contribs.eclipse.jface.viewers.table.ColumnsResizeControlListener;
import org.kalypso.core.i18n.Messages;

/**
 * This viewer shows an array of {@link org.eclipse.core.runtime.IStatus} (or an
 * {@link org.eclipse.core.runtime.MultiStatus} as table or tree.
 *
 * @author Gernot Belger
 */
public abstract class StatusViewer
{
  ColumnsResizeControlListener m_resizeListener = new ColumnsResizeControlListener();

  public abstract ColumnViewer getViewer( );

  public Control getControl( )
  {
    return getViewer().getControl();
  }

  protected void hookListener( )
  {
    getControl().addControlListener( m_resizeListener );

    getViewer().addDoubleClickListener( new IDoubleClickListener()
    {
      @Override
      public void doubleClick( final DoubleClickEvent event )
      {
        final IStructuredSelection sel = (IStructuredSelection) event.getSelection();
        final IStatus selection = (IStatus) sel.getFirstElement();
        if( selection != null )
        {
          final Shell shell = getControl().getShell();
          final StatusDialog dialog = new StatusDialog( shell, selection, "Details" ); //$NON-NLS-1$
          dialog.open();
        }
      }
    } );
  }

  protected void updateColumnSizes( )
  {
    m_resizeListener.updateColumnSizes();
  }

  /**
   * Adds a column that shows the severity of a status.
   */
  public static ViewerColumnItem addSeverityColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn severityColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.CENTER );
    final ViewerColumnItem severityCol = new ViewerColumnItem( severityColumn );
    severityCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.3" ) ); //$NON-NLS-1$
    severityCol.setResizable( false );
    severityCol.setMoveable( false );
    severityColumn.setLabelProvider( new StatusLabelSeverityProvider() );

    ColumnsResizeControlListener.setWidthInfo( severityCol.getColumn(), ColumnWidthInfo.PACK, false );

    return severityCol;
  }

  /**
   * Adds a column that shows the message of a status.
   */
  public static void addMessageColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn messageColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.LEFT );
    final ViewerColumnItem messageCol = new ViewerColumnItem( messageColumn );
    messageCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.4" ) ); //$NON-NLS-1$
    messageCol.setResizable( false );
    messageCol.setMoveable( false );
    messageColumn.setLabelProvider( new StatusLabelMessageProvider() );

    // TODO: we should have a maximum width here
    ColumnsResizeControlListener.setMinimumPackWidth( messageCol.getColumn() );
  }

  /**
   * Adds a column that shows the time of a status.
   */
  public static void addTimeColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn timeColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.LEFT );
    final ViewerColumnItem timeCol = new ViewerColumnItem( timeColumn );
    timeCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.5" ) ); //$NON-NLS-1$
    timeCol.setResizable( false );
    timeCol.setMoveable( false );
    timeColumn.setLabelProvider( new StatusLabelTimeProvider() );

    ColumnsResizeControlListener.setMinimumPackWidth( timeCol.getColumn() );
  }

  public void addTimeColumn( )
  {
    addTimeColumn( getViewer() );
  }

  public final IStatus[] getInput( )
  {
    return (IStatus[]) getViewer().getInput();
  }

  public void setInput( final IStatus[] children )
  {
    getViewer().setInput( children );

    // FIXME: should handle time column automatically ?

// final boolean hasTime = hasTime(children);
// final ViewerColumn timeColumn = findTimeColumn();
//
// if( !hasTime && timeColumn != null )
// {
// final ViewerColumnItem timeItem = new ViewerColumnItem( timeColumn );
// timeItem.getColumn().dispose();
// }
// else if( hasTime && timeColumn == null )
// addTimeColumn();
  }

// private ViewerColumn findTimeColumn( )
// {
// getViewer();
// // TODO Auto-generated method stub
// return null;
// }

  public static boolean hasTime( final IStatus[] children )
  {
    for( final IStatus status : children )
    {
      if( status instanceof IStatusWithTime )
        return true;
    }

    return false;
  }
}
