/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.ui.views.propertysheet;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.kalypso.contribs.eclipse.i18n.Messages;
import org.kalypso.contribs.eclipse.jface.viewers.DefaultTableViewer;

/**
 * @author schlienger (24.05.2005)
 */
public class SimplePropertySheetViewer extends Viewer
{
  private DefaultTableViewer m_viewer;

  private final int m_propColSize;

  private final int m_valueColSize;

  public SimplePropertySheetViewer( final Composite parent )
  {
    this( parent, 100, 300 );
  }

  public SimplePropertySheetViewer( final Composite parent, final int propColSize, final int valueColSize )
  {
    m_propColSize = propColSize;
    m_valueColSize = valueColSize;
    createControl( parent );
  }

  private void createControl( final Composite parent )
  {
    m_viewer = new DefaultTableViewer( parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
    m_viewer.addColumn( "property", Messages.getString( "org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer.1" ), null, m_propColSize, -1, false, SWT.CENTER, true, false ); //$NON-NLS-1$ //$NON-NLS-2$
    m_viewer.addColumn( "value", Messages.getString( "org.kalypso.contribs.eclipse.ui.views.propertysheet.SimplePropertySheetViewer.3" ), null, m_valueColSize, -1, false, SWT.CENTER, true, false ); //$NON-NLS-1$ //$NON-NLS-2$

    m_viewer.setLabelProvider( new PropertySheetTableLabelProvider( null ) );
    m_viewer.setContentProvider( new PropertySheetTableContentProvider() );
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#getControl()
   */
  @Override
  public Control getControl( )
  {
    return m_viewer.getControl();
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#getInput()
   */
  @Override
  public Object getInput( )
  {
    return m_viewer.getInput();
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    return m_viewer.getSelection();
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#refresh()
   */
  @Override
  public void refresh( )
  {
    m_viewer.refresh();
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setInput(java.lang.Object)
   */
  @Override
  public void setInput( final Object input )
  {
    m_viewer.setInput( input );
  }

  /**
   * @see org.eclipse.jface.viewers.Viewer#setSelection(org.eclipse.jface.viewers.ISelection, boolean)
   */
  @Override
  public void setSelection( final ISelection selection, final boolean reveal )
  {
    m_viewer.setSelection( selection, reveal );
  }
}
