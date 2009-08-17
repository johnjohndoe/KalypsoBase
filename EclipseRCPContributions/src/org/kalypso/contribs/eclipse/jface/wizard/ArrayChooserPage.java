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
package org.kalypso.contribs.eclipse.jface.wizard;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This page lists a list of objects from which the user can choose from.
 *
 * @author Gernot Belger, Holger Albert
 */
public class ArrayChooserPage extends WizardPage
{
  /**
   * The objects, that are available to the user.
   */
  private Object m_chooseables = null;

  /**
   * The viewer, that presents the objects to choose from.
   */
  protected CheckboxTableViewer m_viewer = null;

  /**
   * The list of the selected objects.
   */
  private Object[] m_selected = null;

  /**
   * The list of the checked objects.
   */
  private Object[] m_checked = null;

  /**
   * The number of objects, that must be selected, before the page can continue.
   */
  private int m_numToSelect = 0;

  /**
   * The dialog-settings for saving the state of the wizard.
   */
  private IDialogSettings m_dialogSettings = null;

  /**
   * This listener updates the internal list of the checked objects, if a selection change has occured.
   */
  private final ICheckStateListener m_checkStateListener = new ICheckStateListener()
  {
    public void checkStateChanged( final CheckStateChangedEvent event )
    {
      /* Update the list of checked elements. */
      setChecked( m_viewer.getCheckedElements() );

      /* Update the dialog settings. */
      updateDialogSettings();

      /* Check, if the page can continue. */
      chkPageComplete( false );
    }
  };

  /**
   * A default LabelProvider.
   */
  private IBaseLabelProvider m_labelProvider = new LabelProvider();

  private final boolean m_useDialogSettings;

  /**
   * @param chooseables
   *          Used as input for {@link ArrayContentProvider}
   * @param pageName
   *          The name of this page (internal use).
   * @param title
   *          The title is displayed in the title bar of the wizard window.
   * @param titleImage
   *          This image is displayed in the page.
   */
  public ArrayChooserPage( final Object chooseables, final String pageName, final String title, final ImageDescriptor titleImage )
  {
    this( chooseables, null, null, 0, pageName, title, titleImage, true );
  }

  /**
   * The constructor.
   *
   * @param chooseables
   *          Used as input for {@link ArrayContentProvider}
   * @param selected
   *          A list of objects from the chooseables-list, which should be preselected.
   * @param checked
   *          A list of objects from the chooseables-list, which should be prechecked.
   * @param numToSelect
   *          This number specifies, how much of the objects are to be selected, before the page can continue. If 0, the
   *          page will continue without having an object selected.
   * @param pageName
   *          The name of this page (internal use).
   * @param title
   *          The title is displayed in the title bar of the wizard window.
   * @param titleImage
   *          This image is displayed in the page.
   */
  public ArrayChooserPage( final Object chooseables, final Object[] selected, final Object[] checked, final int numToSelect, final String pageName, final String title, final ImageDescriptor titleImage )
  {
    this( chooseables, selected, checked, numToSelect, pageName, title, titleImage, true );
  }

  /**
   * @param chooseables
   *          Used as input for {@link ArrayContentProvider}
   * @param selected
   *          A list of objects from the chooseables-list, which should be preselected.
   * @param checked
   *          A list of objects from the chooseables-list, which should be prechecked.
   * @param numToSelect
   *          This number specifies, how much of the objects are to be selected, before the page can continue. If 0, the
   *          page will continue without having an object selected.
   * @param pageName
   *          The name of this page (internal use).
   * @param title
   *          The title is displayed in the title bar of the wizard window.
   * @param titleImage
   *          This image is displayed in the page.
   * @param useDialogSettings
   *          If <code>false</code>, the page does not use the dialog settings to restore it's state.
   */
  public ArrayChooserPage( final Object chooseables, final Object[] selected, final Object[] checked, final int numToSelect, final String pageName, final String title, final ImageDescriptor titleImage, final boolean useDialogSettings )
  {
    super( pageName, title, titleImage );

    m_chooseables = chooseables;
    m_selected = selected;
    m_checked = checked;
    m_numToSelect = numToSelect;
    m_useDialogSettings = useDialogSettings;
  }

  /**
   * This function sets a LabelProvider, that will be used generate the list names of the objects.
   *
   * @param labelProvider
   *          The new LabelProvider.
   */
  public void setLabelProvider( final IBaseLabelProvider labelProvider )
  {
    m_labelProvider = labelProvider;
  }

  /**
   * This function returns the current LabelProvider, that will be used to generate the list names of the objects.
   *
   * @return The current LabelProvider.
   */
  public IBaseLabelProvider getLabelProvider( )
  {
    return m_labelProvider;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_viewer != null )
      m_viewer.removeCheckStateListener( m_checkStateListener );

    super.dispose();
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    /* Init the dialog settings. */
    initDialogSettings();

    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    m_viewer = CheckboxTableViewer.newCheckList( panel, SWT.BORDER );
    final GridData viewerData = new GridData( SWT.FILL, SWT.FILL, true, true );
    viewerData.heightHint = 400; // maybe we want to get that from outside
    m_viewer.getTable().setLayoutData( viewerData );
    m_viewer.setLabelProvider( m_labelProvider );
    m_viewer.setContentProvider( new ArrayContentProvider() );
    m_viewer.setInput( m_chooseables );
    m_viewer.addCheckStateListener( m_checkStateListener );

    if( m_selected != null )
      m_viewer.setSelection( new StructuredSelection( m_selected ) );
    if( m_checked != null )
      m_viewer.setCheckedElements( m_checked );

    final Composite buttonpanel = new Composite( panel, SWT.RIGHT );
    buttonpanel.setLayout( new GridLayout( 2, true ) );
    final GridData buttonpaneldata = new GridData( GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL );
    buttonpaneldata.grabExcessHorizontalSpace = true;
    buttonpanel.setData( buttonpaneldata );

    createSelectButton( buttonpanel, m_viewer, true );
    createSelectButton( buttonpanel, m_viewer, false );

    setControl( panel );

    /* Apply the dialog settings. */
    chkDialogSettings();

    /* Check if the page could be completed. */
    chkPageComplete( true );
  }

  /**
   * This function returns a list of selected objects from the chooseable list.
   *
   * @return All selected objects.
   */
  public Object[] getChoosen( )
  {
    if( m_checked == null )
      return new Object[0];

    return m_checked;
  }

  /**
   * This function creates one type of button from two available types. Each type of button will change the check-state
   * of all chooseables.
   *
   * @param parent
   *          The parent composite.
   * @param viewer
   *          The viewer, which will be manipulated from this button.
   * @param select
   *          The type of the created button.<br>
   *          If true, a button which will select all chooseables, will be created.<br>
   *          If false, a button which will unselect all chooseables, will be created.
   */
  private void createSelectButton( final Composite parent, final CheckboxTableViewer viewer, final boolean select )
  {
    final Button button = new Button( parent, SWT.PUSH );

    button.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    button.setText( select ? "&Alle wählen" : "Alle a&bwählen" );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        viewer.setAllChecked( select );

        /* Update the list of checked elements. */
        setChecked( viewer.getCheckedElements() );

        /* Update the dialog settings. */
        updateDialogSettings();

        /* Check, if the page can continue. */
        chkPageComplete( false );
      }
    } );
  }

  protected void setChecked( final Object[] checkedElements )
  {
    m_checked = checkedElements;
  }

  /**
   * This function sets the list of chooseables. If one is already set via the construtor, it will be replaced by this
   * list.
   *
   * @param input
   *          The new list of chooseables.
   */
  public void setInput( final Object input )
  {
    /* Also set chooseable for the case this method is invoked before the page was created. */
    m_chooseables = input;

    if( m_viewer != null )
      m_viewer.setInput( input );
  }

  /**
   * This function will check, if all requirements of this page are met.
   *
   * @param firstTime
   *          This parameter should be true, if the page calls the function the first time. If true, the finish-state
   *          will be checked, but no error-message will be displayed.
   */
  protected void chkPageComplete( final boolean firstTime )
  {
    /* Reset all messages. */
    setErrorMessage( null );
    setPageComplete( true );

    /* If the number of selected objects does not matter, the page will always be finishable. */
    if( m_numToSelect == 0 )
      return;

    /* If there are less objects selected, than required, set an error message. */
    if( m_checked.length < m_numToSelect )
    {
      if( !firstTime )
        WizardPageUtilities.appendError( "Sie müssen mindestens " + String.valueOf( m_numToSelect ) + " Element(e) auswählen.", this );

      setPageComplete( false );
    }
  }

  /**
   * This function will init the dialog settings, if any.
   */
  private void initDialogSettings( )
  {
    if( !m_useDialogSettings )
      return;

    final IWizard wizard = getWizard();

    final IDialogSettings dialogSettings = wizard.getDialogSettings();
    if( dialogSettings == null )
    {
      m_dialogSettings = null;
      return;
    }

    m_dialogSettings = dialogSettings;
  }

  /**
   * This function will check the dialog settings, if any, and applys the settings to the page.
   */
  private void chkDialogSettings( )
  {
    /* Check the dialog settings. */
    if( m_dialogSettings == null )
      return;

    /* Get the section for this page. */
    final IDialogSettings section = m_dialogSettings.getSection( "ChooserPage" + getName() );
    if( section == null )
      return;

    final ArrayList<Object> checked = new ArrayList<Object>();
    final Object[] elements = ((IStructuredContentProvider) m_viewer.getContentProvider()).getElements( m_viewer.getInput() );

    /* Check all elements of the viewer. */
    for( final Object element : elements )
    {
      String name = "";
      if( m_labelProvider instanceof ILabelProvider )
      {
        final ILabelProvider labelProvider = (ILabelProvider) m_labelProvider;
        name = labelProvider.getText( element );
      }
      else if( m_labelProvider instanceof ITableLabelProvider )
      {
        final ITableLabelProvider labelProvider = (ITableLabelProvider) m_labelProvider;
        name = labelProvider.getColumnText( element, 0 );
      }

      if( name.equals( "" ) )
        continue;

      /* If they do exist in the dialog settings, the element is added. */
      final String state = section.get( name );

      if( state == null )
        continue;

      /* The element is added, regardless the value it has. */
      checked.add( element );
    }

    m_viewer.setCheckedElements( checked.toArray() );
    m_checked = m_viewer.getCheckedElements();
  }

  /**
   * This function applies the settings of the page to the dialog settings, if any.
   */
  protected void updateDialogSettings( )
  {
    if( m_dialogSettings == null )
      return;

    /* Get the section for this page in creating a new emtpy one, so that no old values will remain. */
    final IDialogSettings section = m_dialogSettings.addNewSection( "ChooserPage" + getName() );

    for( final Object element : m_checked )
    {
      String name = "";
      if( m_labelProvider instanceof ILabelProvider )
      {
        final ILabelProvider labelProvider = (ILabelProvider) m_labelProvider;
        name = labelProvider.getText( element );
      }
      else if( m_labelProvider instanceof ITableLabelProvider )
      {
        final ITableLabelProvider labelProvider = (ITableLabelProvider) m_labelProvider;
        name = labelProvider.getColumnText( element, 0 );
      }

      section.put( name, "checked" );
    }
  }
}