/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.view.wq.table;

import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.java.swing.table.NumberTableCellRenderer;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTable;
import org.kalypso.ogc.sensor.timeseries.wq.wqtable.WQTableSet;

/**
 * @author schlienger
 */
public class WQRelationTableViewer extends Composite
{
  private WQTable[] m_tables;

  private final JTable m_table;

  private String m_fromType;

  private String m_toType;

  private final ComboViewer m_tableCombo;

  public WQRelationTableViewer( final Composite parent )
  {
    super( parent, SWT.NONE );

    GridLayoutFactory.fillDefaults().applyTo( this );

    m_tableCombo = new ComboViewer( this, SWT.DROP_DOWN | SWT.READ_ONLY );
    m_tableCombo.setContentProvider( new ArrayContentProvider() );
    m_tableCombo.setLabelProvider( new LabelProvider() );

    m_tableCombo.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    // SWT-AWT Brücke für die Darstellung von JTable
    final Composite embCmp = new Composite( this, SWT.RIGHT | SWT.EMBEDDED | SWT.BORDER );
    embCmp.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    final Frame vFrame = SWT_AWT.new_Frame( embCmp );

    m_table = new JTable();
    vFrame.setVisible( true );
    m_table.setVisible( true );
    m_table.setAutoResizeMode( JTable.AUTO_RESIZE_ALL_COLUMNS );

    m_table.setDefaultRenderer( Number.class, new NumberTableCellRenderer( "%.3g" ) ); //$NON-NLS-1$

    final JTableHeader header = m_table.getTableHeader();
    header.setReorderingAllowed( false );
    header.setEnabled( false );

    final JScrollPane pane = new JScrollPane( m_table );
    pane.setBorder( BorderFactory.createEmptyBorder() );
    vFrame.add( pane );

    m_tableCombo.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection)event.getSelection();
        comboSelected( selection.getFirstElement() );
      }
    } );
  }

  protected void comboSelected( final Object element )
  {
    final WQTable table = (WQTable)element;
    m_table.setModel( WQRelationFactory.createTableModel( m_fromType, m_toType, table ) );
  }

  public void setInput( final WQTableSet wqs )
  {
    m_table.setModel( new DefaultTableModel() );

    if( wqs == null )
      return;

    m_fromType = wqs.getFromType();
    m_toType = wqs.getToType();

    m_tables = wqs.getTables();
    m_tableCombo.setInput( m_tables );

    if( m_tables.length > 0 )
      m_tableCombo.setSelection( new StructuredSelection( m_tables[0] ) );
  }
}
