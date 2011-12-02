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
package org.kalypso.ui.editor.styleeditor.viewer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;

/**
 * This is a helper class for creating and manageing a table viewer, for editing color maps.
 * 
 * @author Holger Albert
 */
public class ColorMapViewer
{
  /**
   * The color map viewer.
   */
  private TableViewer m_viewer;

  /**
   * The constructor.
   * 
   * @param parent
   *          The parent composite.
   * @param style
   *          The additional style.
   * @param toolkit
   *          The form toolkit. May be null.
   */
  public ColorMapViewer( Composite parent, int style, FormToolkit toolkit )
  {
    m_viewer = null;

    createControls( parent, style, toolkit );
  }

  /**
   * This function creates the controls.
   * 
   * @param parent
   *          The parent composite.
   * @param style
   *          The additional style.
   * @param toolkit
   *          The form toolkit. May be null.
   */
  private void createControls( Composite parent, int style, FormToolkit toolkit )
  {
    m_viewer = new TableViewer( parent, SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | style );
    m_viewer.setContentProvider( new ArrayContentProvider() );
    m_viewer.setLabelProvider( new RasterColorMapLabelProvider( m_viewer ) );
    m_viewer.getTable().setLinesVisible( true );
    m_viewer.getTable().setHeaderVisible( true );

    configure( m_viewer );

    if( toolkit != null )
      toolkit.adapt( m_viewer.getTable() );
  }

  private void configure( TableViewer viewer )
  {
    Table table = viewer.getTable();

    TableColumn labelColumn = new TableColumn( table, SWT.NONE );
    labelColumn.setText( "Bezeichnung" );
    labelColumn.setWidth( 100 );

    TableColumn quantityColumn = new TableColumn( table, SWT.NONE );
    quantityColumn.setText( "Wert" );
    quantityColumn.setWidth( 100 );

    viewer.setColumnProperties( new String[] { "label", "quantity" } );
  }

  /**
   * @see org.eclipse.swt.widgets.Table#setLayoutData(Object)
   */
  public void setLayoutData( Object layoutData )
  {
    if( m_viewer != null && !m_viewer.getTable().isDisposed() )
      m_viewer.getTable().setLayoutData( layoutData );
  }

  /**
   * @see TableViewer#setInput(Object)
   */
  public void setInput( ColorMapEntry[] input )
  {
    if( m_viewer != null && !m_viewer.getTable().isDisposed() )
      m_viewer.setInput( input );
  }

  /**
   * @see TableViewer#getInput()
   */
  public ColorMapEntry[] getInput( )
  {
    if( m_viewer != null && !m_viewer.getTable().isDisposed() )
      return (ColorMapEntry[]) m_viewer.getInput();

    return null;
  }

  /**
   * @see TableViewer#refresh()
   */
  public void refresh( )
  {
    if( m_viewer != null && !m_viewer.getTable().isDisposed() )
      m_viewer.refresh();
  }
}