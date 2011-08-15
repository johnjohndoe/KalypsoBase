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
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * This dialog displays a list of elements and lets the user select them.
 * 
 * @author Holger Albert
 */
public class ListSelectionDialog<T> extends Dialog
{
  /**
   * The description text will be displayed to the user. May be null.
   */
  private String m_description;

  /**
   * The array of all elements.
   */
  private T[] m_allElements;

  /**
   * The array of elements, which should be selected by default. May be null.
   */
  protected T[] m_selectedElements;

  /**
   * The label provider. May be null.
   */
  private IBaseLabelProvider m_labelProvider;

  /**
   * The class.
   */
  protected Class<T> m_clazz;

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The parent shell, or null to create a top-level shell.
   * @param description
   *          The description text will be displayed to the user. May be null.
   * @param allElements
   *          The array of all elements.
   * @param selectedElements
   *          The array of elements, which should be selected by default. May be null.
   * @param labelProvider
   *          The label provider. May be null.
   * @param clazz
   *          The class.
   */
  public ListSelectionDialog( Shell parentShell, String description, T[] allElements, T[] selectedElements, IBaseLabelProvider labelProvider, Class<T> clazz )
  {
    super( parentShell );

    m_description = description;
    m_allElements = allElements;
    m_selectedElements = selectedElements;
    m_labelProvider = labelProvider;
    m_clazz = clazz;
  }

  /**
   * The constructor.
   * 
   * @param parentShell
   *          The object that returns the current parent shell.
   * @param description
   *          The description text will be displayed to the user. May be null.
   * @param allElements
   *          The array of all elements.
   * @param selectedElements
   *          The array of elements, which should be selected by default. May be null.
   * @param labelProvider
   *          The label provider. May be null.
   * @param clazz
   *          The class.
   */
  public ListSelectionDialog( IShellProvider parentShell, String description, T[] allElements, T[] selectedElements, IBaseLabelProvider labelProvider, Class<T> clazz )
  {
    super( parentShell );

    m_description = description;
    m_allElements = allElements;
    m_selectedElements = selectedElements;
    m_labelProvider = labelProvider;
    m_clazz = clazz;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( Composite parent )
  {
    /* Set the title. */
    getShell().setText( "Auswahl erforderlich" );

    /* Create the main composite. */
    Composite main = (Composite) super.createDialogArea( parent );
    main.setLayout( new GridLayout( 1, false ) );
    GridData mainData = new GridData( SWT.FILL, SWT.FILL, true, true );
    mainData.widthHint = 400;
    main.setLayoutData( mainData );

    /* Create a label. */
    Label descriptionLabel = new Label( main, SWT.WRAP );
    descriptionLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    descriptionLabel.setText( "Bitte w‰hlen Sie die gew¸nschten Elemente aus." );
    if( m_description != null && m_description.length() > 0 )
      descriptionLabel.setText( m_description );

    /* Create the list viewer. */
    final CheckboxTableViewer listViewer = CheckboxTableViewer.newCheckList( main, SWT.BORDER );
    listViewer.getTable().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    listViewer.getTable().setHeaderVisible( false );
    listViewer.getTable().setLinesVisible( true );
    listViewer.setContentProvider( new ArrayContentProvider() );
    listViewer.setInput( m_allElements );
    listViewer.setLabelProvider( new LabelProvider() );
    if( m_labelProvider != null )
      listViewer.setLabelProvider( m_labelProvider );

    /* Add a check state provider. */
    listViewer.setCheckStateProvider( new ICheckStateProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
       */
      @Override
      public boolean isGrayed( Object element )
      {
        return false;
      }

      /**
       * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
       */
      @Override
      public boolean isChecked( Object element )
      {
        if( m_selectedElements == null || m_selectedElements.length == 0 )
          return false;

        List<T> checkedElements = Arrays.asList( m_selectedElements );
        return checkedElements.contains( element );
      }
    } );

    /* Add a listener. */
    listViewer.addCheckStateListener( new ICheckStateListener()
    {
      /**
       * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
       */
      @Override
      public void checkStateChanged( CheckStateChangedEvent event )
      {
        /* Store the new selection. */
        Object[] checkedElements = listViewer.getCheckedElements();
        Object newInstance = java.lang.reflect.Array.newInstance( m_clazz, checkedElements.length );
        System.arraycopy( checkedElements, 0, newInstance, 0, checkedElements.length );
        m_selectedElements = (T[]) newInstance;

        /* Check if the dialog can be completed. */
        checkDialogComplete();
      }
    } );

    return main;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected void createButtonsForButtonBar( Composite parent )
  {
    super.createButtonsForButtonBar( parent );

    /* Check, if the dialog is allowed to be completed. */
    checkDialogComplete();
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#isResizable()
   */
  @Override
  protected boolean isResizable( )
  {
    return true;
  }

  /**
   * This function returns the selected elements.
   * 
   * @return The selected elements.
   */
  public T[] getSelectedElements( )
  {
    return m_selectedElements;
  }

  /**
   * This function checks, if the dialog is allowed to be completed.
   */
  protected void checkDialogComplete( )
  {
    /* Get the OK button. */
    Button okButton = getButton( IDialogConstants.OK_ID );

    /* First of all, it should be allowed to complete. */
    okButton.setEnabled( true );

    if( m_selectedElements == null || m_selectedElements.length == 0 )
    {
      okButton.setEnabled( false );
      return;
    }
  }
}