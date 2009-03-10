/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sebastian Davids <sdavids@gmx.de> - Fix for bug 90273 - [Dialogs] 
 * 			ListSelectionDialog dialog alignment
 *******************************************************************************/
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A standard dialog which solicits a list of selections from the user. This class is configured with an arbitrary data
 * model represented by content and label provider objects. The <code>getResult</code> method returns the selected
 * elements.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
@SuppressWarnings("restriction")
public class TreeSelectionDialog extends SelectionDialog
{
  static String SELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_selectLabel;

  static String DESELECT_ALL_TITLE = WorkbenchMessages.SelectionDialog_deselectLabel;

  // sizing constants
  private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;

  private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;

  // the root element to populate the viewer with
  private final Object m_inputElement;

  // providers for populating this dialog
  private final ILabelProvider m_labelProvider;

  private final IStructuredContentProvider m_contentProvider;

  // the visual selection widget group
  CheckboxTreeViewer m_treeViewer;

  /**
   * Creates a tree selection dialog.
   * 
   * @param parentShell
   *            the parent shell
   * @param input
   *            the root element to populate this dialog with
   * @param contentProvider
   *            the content provider for navigating the model
   * @param labelProvider
   *            the label provider for displaying model elements
   * @param message
   *            the message to be displayed at the top of this dialog, or <code>null</code> to display a default
   *            message
   */
  public TreeSelectionDialog( final Shell parentShell, final Object input, final ITreeContentProvider contentProvider, final ILabelProvider labelProvider, final String message )
  {
    super( parentShell );
    setTitle( WorkbenchMessages.ListSelection_title );

    m_inputElement = input;

    m_contentProvider = contentProvider;
    m_labelProvider = labelProvider;
    if( message != null )
    {
      setMessage( message );
    }
    else
    {
      setMessage( WorkbenchMessages.ListSelection_message );
    }
  }

  /**
   * Add the selection and deselection buttons to the dialog.
   * 
   * @param composite
   *            org.eclipse.swt.widgets.Composite
   */
  private void addSelectionButtons( final Composite composite )
  {
    final Composite buttonComposite = new Composite( composite, SWT.NONE );
    final GridLayout layout = new GridLayout();
    layout.numColumns = 0;
    layout.marginWidth = 0;
    layout.horizontalSpacing = convertHorizontalDLUsToPixels( IDialogConstants.HORIZONTAL_SPACING );
    buttonComposite.setLayout( layout );
    buttonComposite.setLayoutData( new GridData( SWT.END, SWT.TOP, true, false ) );

    final Button selectButton = createButton( buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false );

    SelectionListener listener = new SelectionAdapter()
    {
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        m_treeViewer.setAllChecked( true );
      }
    };
    selectButton.addSelectionListener( listener );

    final Button deselectButton = createButton( buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false );

    listener = new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_treeViewer.setAllChecked( false );
      }
    };
    deselectButton.addSelectionListener( listener );
  }

  /**
   * Visually checks the previously-specified elements in this dialog's list viewer.
   */
  private void checkInitialSelections( )
  {
    final List< ? > initialElementSelections = getInitialElementSelections();
    for( final Object item : initialElementSelections )
    {
      m_treeViewer.reveal( item );
      m_treeViewer.setChecked( item, true );
    }
  }

  /**
   * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
   */
  @Override
  protected void configureShell( final Shell shell )
  {
    super.configureShell( shell );
// PlatformUI.getWorkbench().getHelpSystem().setHelp( shell, IWorkbenchHelpContextIds.LIST_SELECTION_DIALOG );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    // page group
    final Composite composite = (Composite) super.createDialogArea( parent );

    initializeDialogUnits( composite );

    createMessageArea( composite );

    m_treeViewer = new CheckboxTreeViewer( composite, SWT.BORDER );
    final GridData data = new GridData( GridData.FILL_BOTH );
    data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
    data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
    m_treeViewer.getControl().setLayoutData( data );

    m_treeViewer.setLabelProvider( m_labelProvider );
    m_treeViewer.setContentProvider( m_contentProvider );

    addSelectionButtons( composite );

    m_treeViewer.setInput( m_inputElement );

    checkInitialSelections();

    m_treeViewer.addCheckStateListener( new ICheckStateListener()
    {
      @SuppressWarnings("synthetic-access")
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        setSelectionResult( m_treeViewer.getCheckedElements() );
      }
    } );

    Dialog.applyDialogFont( composite );

    return composite;
  }

  /**
   * Returns the viewer used to show the list.
   * 
   * @return the viewer, or <code>null</code> if not yet created
   */
  protected CheckboxTreeViewer getViewer( )
  {
    return m_treeViewer;
  }
}
