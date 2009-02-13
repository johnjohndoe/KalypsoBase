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

package org.kalypso.simulation.ui.wizards.exporter.featureWithTemplate;

import java.util.HashMap;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A wizard page, that lets the user choose from a tree. <br>
 * TODO: move into contrib plug-ins
 * 
 * @author Gernot Belger
 */
public class TreeSelectionPage extends WizardPage implements IWizardPage
{
  /**
   * Collection of buttons created by the <code>createButton</code> method.
   */
  private HashMap buttons = new HashMap();

  static String SELECT_ALL_TITLE = WorkbenchMessages.getString( "SelectionDialog.selectLabel" ); //$NON-NLS-1$
  static String DESELECT_ALL_TITLE = WorkbenchMessages.getString( "SelectionDialog.deselectLabel" ); //$NON-NLS-1$

  private final IBaseLabelProvider m_labelProvider;
  private final ITreeContentProvider m_contentProvider;
  private Object m_input;

  protected Object[] m_checkedElements;

  protected Object[] m_grayedElements;

  private ViewerSorter m_viewerSorter;

  private CheckboxTreeViewer m_viewer;

  public TreeSelectionPage( String pageName, final ITreeContentProvider contentProvider,
      final IBaseLabelProvider labelProvider )
  {
    this( pageName, null, null, contentProvider, labelProvider );
  }

  public TreeSelectionPage( String pageName, String title, ImageDescriptor titleImage,
      final ITreeContentProvider contentProvider, final IBaseLabelProvider labelProvider )
  {
    super( pageName, title, titleImage );
    m_contentProvider = contentProvider;
    m_labelProvider = labelProvider;
  }

  public void setViewerSorter( final ViewerSorter viewerSorter )
  {
    m_viewerSorter = viewerSorter;
  }

  public void setInput( final Object input )
  {
    m_input = input;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( Composite parent )
  {
    initializeDialogUnits( parent );

    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout() );

    final CheckboxTreeViewer viewer = new CheckboxTreeViewer( composite, SWT.BORDER );
    viewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    viewer.setContentProvider( m_contentProvider );
    viewer.setLabelProvider( m_labelProvider );
    viewer.setSorter( m_viewerSorter );
    viewer.setInput( m_input );

    addSelectionButtons( composite, viewer );

    setControl( composite );

    //
    if( m_checkedElements != null )
    {
      for( int i = 0; i < m_checkedElements.length; i++ )
        viewer.expandToLevel( m_checkedElements[i], 0 );

      viewer.setCheckedElements( m_checkedElements );
    }

    if( m_grayedElements != null )
      viewer.setGrayedElements( m_grayedElements );

    // Show topmost element in tree
    viewer.setSelection( new StructuredSelection( m_contentProvider.getElements( m_input )[0] ), true );

    m_viewer = viewer;

    updateState( viewer );
  }

  public CheckboxTreeViewer getViewer()
  {
    return m_viewer;
  }

  protected void updateState( CheckboxTreeViewer viewer )
  {
    m_checkedElements = viewer.getCheckedElements();
    m_grayedElements = viewer.getGrayedElements();

    setPageComplete( m_checkedElements != null && m_checkedElements.length > 0 );
  }

  public Object getInput()
  {
    return m_input;
  }

  /**
   * Add the selection and deselection buttons to the dialog.
   * 
   * @param composite
   *          org.eclipse.swt.widgets.Composite
   */
  private void addSelectionButtons( Composite composite, final CheckboxTreeViewer viewer )
  {
    Composite buttonComposite = new Composite( composite, SWT.RIGHT );

    GridLayout layout = new GridLayout();
    layout.numColumns = 2;
    buttonComposite.setLayout( layout );
    GridData data = new GridData( GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL );
    data.grabExcessHorizontalSpace = true;
    composite.setData( data );

    Button selectButton = createButton( buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false );

    final ITreeContentProvider treeContentProvider = m_contentProvider;

    selectButton.addSelectionListener( new SelectionAdapter()
    {
      public void widgetSelected( SelectionEvent e )
      {
        handleSelectAll( viewer, treeContentProvider );
      }
    } );

    final Button deselectButton = createButton( buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE,
        false );
    deselectButton.addSelectionListener( new SelectionAdapter()
    {
      public void widgetSelected( SelectionEvent e )
      {
        handleDeselectAll( viewer, treeContentProvider );
      }
    } );
  }

  /**
   * Creates a new button with the given id.
   * <p>
   * The <code>Dialog</code> implementation of this framework method creates a standard push button, registers it for
   * selection events including button presses, and registers default buttons with its shell. The button id is stored as
   * the button's client data. If the button id is <code>IDialogConstants.CANCEL_ID</code>, the new button will be
   * accessible from <code>getCancelButton()</code>. If the button id is <code>IDialogConstants.OK_ID</code>, the
   * new button will be accesible from <code>getOKButton()</code>. Note that the parent's layout is assumed to be a
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
   * 
   * @return the new button
   *  
   */
  protected Button createButton( Composite parent, int id, String label, boolean defaultButton )
  {
    // increment the number of columns in the button bar
    ( (GridLayout)parent.getLayout() ).numColumns++;
    Button button = new Button( parent, SWT.PUSH );
    button.setText( label );
    button.setFont( JFaceResources.getDialogFont() );
    button.setData( new Integer( id ) );
    if( defaultButton )
    {
      Shell shell = parent.getShell();
      if( shell != null )
      {
        shell.setDefaultButton( button );
      }
    }
    buttons.put( new Integer( id ), button );
    setButtonLayoutData( button );
    return button;
  }

  public void setChecked( Object[] initiallyChecked )
  {
    m_checkedElements = initiallyChecked;
  }

  public Object[] getCheckedElements()
  {
    return m_checkedElements;
  }

  public Object[] getGrayedElements()
  {
    return m_grayedElements;
  }

  public void setGrayed( Object[] initiallyGrayed )
  {
    m_grayedElements = initiallyGrayed;
  }

  /**
   * @param viewer
   * @param treeContentProvider
   */
  protected void handleSelectAll( final CheckboxTreeViewer viewer, final ITreeContentProvider treeContentProvider )
  {
    viewer.expandAll();

    final Object[] items = treeContentProvider.getElements( viewer.getInput() );
    for( int i = 0; i < items.length; i++ )
    {
      Object item = items[i];
      viewer.setSubtreeChecked( item, true );
    }

    updateState( viewer );
  }

  /**
   * @param viewer
   * @param treeContentProvider
   */
  protected void handleDeselectAll( final CheckboxTreeViewer viewer, final ITreeContentProvider treeContentProvider )
  {
    viewer.expandAll();

    final Object[] items = treeContentProvider.getElements( viewer.getInput() );
    for( int i = 0; i < items.length; i++ )
    {
      Object item = items[i];
      viewer.setSubtreeChecked( item, false );
    }

    updateState( viewer );
  }

  // TODO: refactor out in to abstract methods, if TreeSelectionPage is moved into contrib plug-ins

}
