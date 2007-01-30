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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
  protected Object[] m_checked = null;

  /**
   * The number of objects, that must be selected, before the page can continue.
   */
  protected int m_numToSelect = 0;

  /**
   * This listener updates the internal list of the checked objects, if a selection change has occured.
   */
  private final ICheckStateListener m_checkStateListener = new ICheckStateListener()
  {
    public void checkStateChanged( final CheckStateChangedEvent event )
    {
      /* Update the list of checked elements. */
      m_checked = m_viewer.getCheckedElements();

      /* Check, if the page can continue. */
      chkPageComplete( false );
    }
  };

  /**
   * A default LabelProvider.
   */
  private IBaseLabelProvider m_labelProvider = new LabelProvider();

  /**
   * The constructor.
   * 
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
    this( chooseables, null, null, 0, pageName, title, titleImage );
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
    super( pageName, title, titleImage );

    m_chooseables = chooseables;
    m_selected = selected;
    m_checked = checked;
    m_numToSelect = numToSelect;
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
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    m_viewer = CheckboxTableViewer.newCheckList( panel, SWT.BORDER );
    m_viewer.getTable().setLayoutData( new GridData( GridData.FILL_BOTH ) );
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
      public void widgetSelected( SelectionEvent e )
      {
        viewer.setAllChecked( select );

        /* Update the list of checked elements. */
        m_checked = viewer.getCheckedElements();

        /* Check, if the page can continue. */
        chkPageComplete( false );
      }
    } );
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
  public void chkPageComplete( boolean firstTime )
  {
    /* Reset all messages. */
    WizardPageUtilities.appendWarning( null, this );
    WizardPageUtilities.appendError( null, this );
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
}