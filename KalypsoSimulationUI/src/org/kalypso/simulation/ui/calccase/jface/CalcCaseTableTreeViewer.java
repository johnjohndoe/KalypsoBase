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
package org.kalypso.simulation.ui.calccase.jface;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.kalypso.contribs.eclipse.jface.viewers.FolderModifiedSorter;
import org.kalypso.contribs.eclipse.jface.viewers.FolderNameSorter;
import org.kalypso.contribs.eclipse.swt.widgets.ColumnViewerSorter;
import org.kalypso.simulation.ui.i18n.Messages;

/**
 * A Table tree viewer, showing calc cases in a tree with additional column 'modiifed since'.
 * 
 * @author Gernot Belger
 */
public final class CalcCaseTableTreeViewer
{
  public static void configureTreeViewer( final TreeViewer viewer )
  {
    configureTreeViewer( viewer, null );
  }

  public static void configureTreeViewer( final TreeViewer viewer, final IFolder markedCalcCase )
  {
    final Tree tree = viewer.getTree();
    tree.setHeaderVisible( true );

    final Color markedColor = tree.getDisplay().getSystemColor( SWT.COLOR_YELLOW );
    viewer.setContentProvider( new CalcCaseTreeContentProvider() );

    final TreeViewerColumn forecastColumn = new TreeViewerColumn( viewer, SWT.NONE );
    final String forecastLabel = Messages.getString( "org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer.1" ); //$NON-NLS-1$
    forecastColumn.getColumn().setText( forecastLabel );

    ColumnViewerSorter.registerSorter( forecastColumn, new FolderNameSorter() );

    final TreeViewerColumn timeColumn = new TreeViewerColumn( viewer, SWT.NONE );
    final String timeLabel = Messages.getString( "org.kalypso.simulation.ui.calccase.jface.CalcCaseTableTreeViewer.2" ); //$NON-NLS-1$
    timeColumn.getColumn().setText( timeLabel );
    ColumnViewerSorter.registerSorter( timeColumn, new FolderModifiedSorter() );

    // Always sort by time initially
    ColumnViewerSorter.setSortState( timeColumn, Boolean.TRUE );

    viewer.setLabelProvider( new CalcCaseTableLabelProvider( markedCalcCase, markedColor ) );
  }

  public static boolean isEmpty( final TreeViewer viewer )
  {
    final Object input = viewer.getInput();
    final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
    return contentProvider.getElements( input ).length == 0;
  }

  public static void selectFirst( final TreeViewer viewer )
  {
    final TreeItem[] items = viewer.getTree().getItems();
    if( items.length > 0 )
      viewer.setSelection( new StructuredSelection( items[0].getData() ) );
  }
}
