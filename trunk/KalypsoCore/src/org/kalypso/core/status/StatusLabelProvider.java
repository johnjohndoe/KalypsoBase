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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerUtil;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerColumnItem;
import org.kalypso.core.i18n.Messages;

/**
 * A label provider showing stati.
 * 
 * @author Gernot Belger
 */
public abstract class StatusLabelProvider extends ColumnLabelProvider
{
  /**
   * Adds a column that shows the severity of a status.
   */
  public static void addSeverityColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn severityColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.CENTER );
    final ViewerColumnItem severityCol = new ViewerColumnItem( severityColumn );
    severityCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.3" ) );
    severityCol.setWidth( 30 );
    severityCol.setResizable( true );
    severityCol.setMoveable( false );
    severityColumn.setLabelProvider( new StatusLabelSeverityProvider() );
  }

  /**
   * Adds a column that shows the message of a status.
   */
  public static void addMessageColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn messageColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.LEFT );
    final ViewerColumnItem messageCol = new ViewerColumnItem( messageColumn );
    messageCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.4" ) );
    messageCol.setWidth( 500 );
    messageCol.setResizable( true );
    messageCol.setMoveable( false );
    messageColumn.setLabelProvider( new StatusLabelMessageProvider() );
  }

  /**
   * Adds a column that shows the time of a status.
   */
  public static void addTimeColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn timeColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.LEFT );
    final ViewerColumnItem timeCol = new ViewerColumnItem( timeColumn );
    timeCol.setText( Messages.getString( "org.kalypso.util.swt.StatusLabelProvider.5" ) );
    timeCol.setWidth( 150 );
    timeCol.setResizable( false );
    timeCol.setMoveable( false );
    timeColumn.setLabelProvider( new StatusLabelTimeProvider() );
  }

  public static void addNavigationColumn( final ColumnViewer columnViewer )
  {
    final ViewerColumn naviColumn = ColumnViewerUtil.createViewerColumn( columnViewer, SWT.LEFT );
    final ViewerColumnItem naviCol = new ViewerColumnItem( naviColumn );
    naviCol.setWidth( 50 );
    naviCol.setResizable( true );
    naviCol.setMoveable( false );
    naviColumn.setLabelProvider( new StatusLabelProvider()
    {
    } );
  }

  protected IStatus statusForElement( final Object element )
  {
    if( element instanceof IStatus )
      return (IStatus) element;

    if( element instanceof IAdaptable )
      return (IStatus) ((IAdaptable) element).getAdapter( IStatus.class );

    return null;
  }

  // Needed in order to overwrite standard behaviour
  @Override
  public String getText( final Object element )
  {
    return null;
  }

}
