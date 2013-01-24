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
package org.kalypso.contribs.eclipse.jface.dialog;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.kalypso.contribs.eclipse.jface.viewers.ColumnViewerTooltipListener;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
public class ListSelectionComposite
{
  private final Collection<ICheckStateListener> m_listeners = new HashSet<ICheckStateListener>();

  private final IStructuredContentProvider m_contentProvider;

  private final ILabelProvider m_labelProvider;

  private Object[] m_checkedElements;

  private CheckboxTableViewer m_viewer;

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), new LabelProvier())}
   */
  public ListSelectionComposite( )
  {
    this( new ArrayContentProvider(), new LabelProvider() );
  }

  /**
   * Same as {@link ProfileFilterComposite#ProfileFilterComposite(new ArrayContentProvider(), ILabelProvider)}
   */
  public ListSelectionComposite( final ILabelProvider labelProvider )
  {
    this( new ArrayContentProvider(), labelProvider );
  }

  public ListSelectionComposite( final IStructuredContentProvider contentProvider, final ILabelProvider labelProvider )
  {
    m_contentProvider = contentProvider;
    m_labelProvider = labelProvider;
  }

  /**
   * Must be called, after {@link #createControl(Composite, int)} has been invoked.
   */
  public void setInput( final Object input )
  {
    m_viewer.setInput( input );
  }

  public Control createControl( final Composite parent, final int style )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    final Table table = new Table( panel, style | SWT.CHECK );
    table.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    m_viewer = new CheckboxTableViewer( table );

    ColumnViewerTooltipListener.hookViewer( m_viewer, false );

    m_viewer.setContentProvider( m_contentProvider );
    m_viewer.setLabelProvider( m_labelProvider );
    if( m_checkedElements != null )
      m_viewer.setCheckedElements( m_checkedElements );

    addSelectionButtons( m_viewer, panel );

    m_viewer.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        fireCheckStateChanged( event );
      }
    } );

    return panel;
  }

  public void addCheckStateListener( final ICheckStateListener listener )
  {
    m_listeners.add( listener );
  }

  protected void fireCheckStateChanged( final CheckStateChangedEvent event )
  {
    m_checkedElements = m_viewer.getCheckedElements();

    final ICheckStateListener[] ls = m_listeners.toArray( new ICheckStateListener[m_listeners.size()] );
    for( final ICheckStateListener l : ls )
      l.checkStateChanged( event );
  }

  public void setCheckedElements( final Object[] elements )
  {
    m_checkedElements = elements;

    if( m_viewer != null )
      m_viewer.setCheckedElements( elements );

    fireCheckStateChanged( new CheckStateChangedEvent( m_viewer, null, false ) );
  }

  public Object[] getCheckedElements( )
  {
    return m_checkedElements;
  }

  /**
   * Add the selection and deselection buttons to the page.
   */
  private void addSelectionButtons( final CheckboxTableViewer checkboxViewer, final Composite composite )
  {
    final Composite buttonComposite = new Composite( composite, SWT.NONE );
    final GridLayout layout = new GridLayout();
    layout.numColumns = 0;
    layout.marginWidth = 0;
// layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
    layout.horizontalSpacing = IDialogConstants.HORIZONTAL_SPACING;
    buttonComposite.setLayout( layout );
    buttonComposite.setLayoutData( new GridData( SWT.END, SWT.TOP, true, false ) );

    final String SELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_selectLabel;
    final String DESELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_deselectLabel;

    final Button selectButton = createButton( buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false );

    selectButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        checkboxViewer.setAllChecked( true );
        fireCheckStateChanged( new CheckStateChangedEvent( checkboxViewer, null, true ) );
      }
    } );

    final Button deselectButton = createButton( buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false );

    deselectButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        checkboxViewer.setAllChecked( false );
        fireCheckStateChanged( new CheckStateChangedEvent( checkboxViewer, null, true ) );
      }
    } );
  }

  /**
   * Creates a new button with the given id.
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates a standard push button, registers it for
   * selection events including button presses, and registers default buttons with its shell. The button id is stored as
   * the button's client data. If the button id is <code>IDialogConstants.CANCEL_ID</code>, the new button will be
   * accessible from <code>getCancelButton()</code>. If the button id is <code>IDialogConstants.OK_ID</code>, the new
   * button will be accessible from <code>getOKButton()</code>. Note that the parent's layout is assumed to be a
   * <code>GridLayout</code> and the number of columns in this layout is incremented. Subclasses may override.
   * </p>
   * 
   * @param parent
   *          the parent composite
   * @param id
   *          the id of the button (see <code>IDialogConstants.*_ID</code> constants for standard dialog button ids)
   * @param label
   *          the label from the button
   * @param defaultButton
   *          <code>true</code> if the button is to be the default button, and <code>false</code> otherwise
   * @return the new button
   * @see #getCancelButton
   * @see #getOKButton()
   */
  protected Button createButton( final Composite parent, final int id, final String label, final boolean defaultButton )
  {
    // increment the number of columns in the button bar
    ((GridLayout) parent.getLayout()).numColumns++;
    final Button button = new Button( parent, SWT.PUSH );
    button.setText( label );
    button.setFont( JFaceResources.getDialogFont() );
    button.setData( new Integer( id ) );
    if( defaultButton )
    {
      final Shell shell = parent.getShell();
      if( shell != null )
      {
        shell.setDefaultButton( button );
      }
    }
    setButtonLayoutData( button );
    return button;
  }

  /**
   * Sets the <code>GridData</code> on the specified button to be one that is spaced for the current dialog page units.
   * The method <code>initializeDialogUnits</code> must be called once before calling this method for the first time.
   * 
   * @param button
   *          the button to set the <code>GridData</code>
   * @return the <code>GridData</code> set on the specified button
   */
  protected GridData setButtonLayoutData( final Button button )
  {
    final GridData data = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
    final int widthHint = IDialogConstants.BUTTON_WIDTH;
// int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
    final Point minSize = button.computeSize( SWT.DEFAULT, SWT.DEFAULT, true );
    data.widthHint = Math.max( widthHint, minSize.x );
    button.setLayoutData( data );
    return data;
  }

  public void setComparator( final ViewerComparator viewerComparator )
  {
    m_viewer.setComparator( viewerComparator );
  }

  protected void refresh( )
  {
    ViewerUtilities.refresh( m_viewer, true );
  }

}
