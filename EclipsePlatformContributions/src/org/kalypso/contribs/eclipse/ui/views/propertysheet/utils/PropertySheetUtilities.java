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
package org.kalypso.contribs.eclipse.ui.views.propertysheet.utils;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.i18n.Messages;
import org.kalypso.contribs.eclipse.swt.widgets.ColumnViewerSorter;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.comparator.PropertyColumnViewerComparator;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.provider.PropertyColumnLabelProvider;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.provider.PropertySheetTableContentProvider;
import org.kalypso.contribs.eclipse.ui.views.propertysheet.provider.ValueColumnLabelProvider;

/**
 * The property sheet utilities.
 * 
 * @author Holger Albert
 */
public class PropertySheetUtilities
{
  /**
   * This function creates the details viewer.
   * 
   * @param parent
   *          The parent composite.
   * @return The details viewer.
   */
  public static TableViewer createDetailsViewer( Composite parent )
  {
    /* Create the details viewer. */
    TableViewer detailsViewer = new TableViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
    detailsViewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    detailsViewer.getTable().setLinesVisible( true );
    detailsViewer.getTable().setHeaderVisible( true );
    detailsViewer.setContentProvider( new PropertySheetTableContentProvider() );

    /* Create a column. */
    TableViewerColumn propertyColumn = new TableViewerColumn( detailsViewer, SWT.LEFT );
    propertyColumn.setLabelProvider( new PropertyColumnLabelProvider() );
    propertyColumn.getColumn().setText( Messages.getString( "org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer.1" ) );
    propertyColumn.getColumn().setWidth( 200 );
    propertyColumn.getColumn().setResizable( true );
    ColumnViewerSorter.registerSorter( propertyColumn, new PropertyColumnViewerComparator() );

    /* Create a column. */
    TableViewerColumn valueColumn = new TableViewerColumn( detailsViewer, SWT.LEFT );
    valueColumn.setLabelProvider( new ValueColumnLabelProvider( detailsViewer ) );
    valueColumn.getColumn().setText( Messages.getString( "org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer.3" ) );
    valueColumn.getColumn().setWidth( 150 );
    valueColumn.getColumn().setResizable( true );

    /* Define a initial order. */
    ColumnViewerSorter.setSortState( propertyColumn, Boolean.FALSE );

    return detailsViewer;
  }
}