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
package org.kalypso.metadoc.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * This wizard page displays two trees, whose elements are checkable. The show the same content.
 * 
 * @author Holger Albert
 */
public class ExportAndSendPage extends WizardPage
{
  private final ExportableTreeItem[] m_input;

  protected CheckboxTreeViewer m_leftTree;

  protected CheckboxTreeViewer m_rightTree;

  protected Object[] m_checkedElements;

  public ExportAndSendPage( final String pageName, final ExportableTreeItem[] input )
  {
    this( pageName, null, null, input );
  }

  public ExportAndSendPage( final String pageName, final String title, final ImageDescriptor titleImage, final ExportableTreeItem[] input )
  {
    super( pageName, title, titleImage );

    m_input = input;
    m_leftTree = null;
    m_rightTree = null;
    m_checkedElements = null;
  }

  @Override
  public void createControl( final Composite parent )
  {
    /* Create the main composite. */
    final Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout( 2, true ) );

    /* Create a label. */
    final Label leftLabel = new Label( main, SWT.NONE );
    leftLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    leftLabel.setText( "Bericht(e) ablegen" );

    /* Create a label. */
    final Label rightLabel = new Label( main, SWT.NONE );
    rightLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    rightLabel.setText( "Bericht(e) versenden" );

    /* Create the layout data for the scrolled form. */
    final GridData scrolledFormData = new GridData( SWT.FILL, SWT.FILL, true, true, 2, 1 );
    scrolledFormData.heightHint = 250;

    /* Create the scrolled form. */
    final ScrolledForm scrolledForm = new ScrolledForm( main, SWT.V_SCROLL );
    scrolledForm.setLayoutData( scrolledFormData );
    scrolledForm.setExpandHorizontal( true );
    scrolledForm.setExpandVertical( true );

    /* Configure the body of the scrolled form. */
    final Composite body = scrolledForm.getBody();
    GridLayoutFactory.fillDefaults().numColumns( 2 ).equalWidth( true ).applyTo( body );

    /* Create the both tree viewers. */
    m_leftTree = createTreeViewer( body, true, m_input );
    m_rightTree = createTreeViewer( body, false, m_input );

    /* Create the selection buttons. */
    createSelectionButtons( main, m_leftTree, true );
    createSelectionButtons( main, m_rightTree, false );

    /* Add a listener. */
    m_leftTree.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        /* Set the check state. */
        final ExportableTreeItem element = (ExportableTreeItem) event.getElement();

        if( event.getChecked() )
          setCheckStateParent( element, true );

        setCheckStateChildren( new ExportableTreeItem[] { element }, true, event.getChecked() );

        /* Update the checked elements. */
        updateCheckedElements();

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );

    /* Add a listener. */
    m_rightTree.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        /* Set the check state. */
        final ExportableTreeItem element = (ExportableTreeItem) event.getElement();

        if( event.getChecked() )
          setCheckStateParent( element, false );

        setCheckStateChildren( new ExportableTreeItem[] { element }, false, event.getChecked() );

        /* Update the checked elements. */
        updateCheckedElements();

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );

    /* Add listeners to synchronize the expanded states. */
    m_leftTree.addTreeListener( new ExportAndSendTreeViewerListener( m_rightTree ) );
    m_rightTree.addTreeListener( new ExportAndSendTreeViewerListener( m_leftTree ) );

    /* Add listeners to update the scrolled form. */
    m_leftTree.addTreeListener( new ExportAndSendUpdateTreeViewerListener( scrolledForm ) );
    m_rightTree.addTreeListener( new ExportAndSendUpdateTreeViewerListener( scrolledForm ) );

    /* Set the control. */
    setControl( main );

    /* Update the checked elements. */
    updateCheckedElements();

    /* Check, if the page can be completed. */
    checkPageComplete();
  }

  /**
   * This function creates one tree viewer.
   * 
   * @param parent
   *          The parent composite.
   * @param export
   *          True, if the check state provider should be used for the export tree. False, if the check state provider
   *          should be used for the send tree.
   * @param input
   *          The input.
   * @return The tree viewer.
   */
  private CheckboxTreeViewer createTreeViewer( final Composite parent, final boolean export, final ExportableTreeItem[] input )
  {
    final CheckboxTreeViewer viewer = new CheckboxTreeViewer( parent, SWT.BORDER | SWT.NO_SCROLL );
    viewer.getTree().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    viewer.setContentProvider( new ExportableContentProvider() );
    viewer.setLabelProvider( getLabelProvider( viewer ) );
    viewer.setCheckStateProvider( new ExportAndSendCheckStateProvider( export ) );
    viewer.setSorter( new ViewerSorter() );
    viewer.setInput( input );

    return viewer;
  }

  private LabelProvider getLabelProvider( final CheckboxTreeViewer viewer )
  {
    final Font grayedFont = getFont();
    final Color grayedForeground = viewer.getTree().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    final Color grayedBackground = viewer.getControl().getBackground();

    return new ExportableLabelProvider( grayedFont, grayedForeground, grayedBackground );
  }

  private void createSelectionButtons( final Composite composite, final CheckboxTreeViewer viewer, final boolean export )
  {
    final Composite buttonComposite = new Composite( composite, SWT.NONE );
    buttonComposite.setLayout( new GridLayout( 2, false ) );
    buttonComposite.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );

    final Button selectButton = createButton( buttonComposite, "Alles ausw‰hlen" );
    selectButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Set the check state. */
        handleSelectAll( viewer, export );

        /* Update the checked elements. */
        updateCheckedElements();

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );

    final Button deselectButton = createButton( buttonComposite, "Alles abw‰hlen" );
    deselectButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        /* Set the check state. */
        handleDeselectAll( viewer, export );

        /* Update the checked elements. */
        updateCheckedElements();

        /* Check, if the page can be completed. */
        checkPageComplete();
      }
    } );
  }

  private Button createButton( final Composite parent, final String label )
  {
    final Button button = new Button( parent, SWT.PUSH );
    button.setText( label );

    setButtonLayoutData( button );

    return button;
  }

  protected void handleSelectAll( final CheckboxTreeViewer viewer, final boolean export )
  {
    final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
    final Object[] elements = contentProvider.getElements( viewer.getInput() );

    if( elements == null || elements.length == 0 )
      return;

    setCheckStateParent( elements[0], export );
    setCheckStateChildren( elements, export, true );
  }

  protected void handleDeselectAll( final CheckboxTreeViewer viewer, final boolean export )
  {
    final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
    final Object[] elements = contentProvider.getElements( viewer.getInput() );

    if( elements == null || elements.length == 0 )
      return;

    setCheckStateChildren( elements, export, false );
  }

  /**
   * This function checks the check state of the parents of an item to <code>true</code>.
   * 
   * @param element
   *          The element, whose parents should be checked.
   * @param export
   *          True, if the check state should be set for the export tree. False, if the check state should be set for
   *          the send tree.
   */
  protected void setCheckStateParent( final Object element, final boolean export )
  {
    /* Cast. */
    final ExportableTreeItem item = (ExportableTreeItem) element;

    /* Get the parent. If there is none, we are at the root item. */
    final ExportableTreeItem parent = item.getParent();
    if( parent == null )
      return;

    /* Set the checked state. */
    if( export )
      setCheckState( parent, true );
    else
      setCheckStateSend( parent, true );

    /* Check the parent of the parent. */
    setCheckStateParent( parent, export );
  }

  protected void setCheckStateChildren( final Object[] elements, final boolean export, final boolean checked )
  {
    for( final Object object : elements )
    {
      final ExportableTreeItem item = (ExportableTreeItem) object;

      if( export )
        setCheckState( item, checked );
      else
        setCheckStateSend( item, checked );

      final ExportableTreeItem[] children = item.getChildren();
      setCheckStateChildren( children, export, checked );
    }
  }

  private void setCheckState( final ExportableTreeItem item, final boolean checked )
  {
    item.setChecked( checked );
    m_leftTree.update( item, null );
    m_rightTree.update( item, null );
  }

  private void setCheckStateSend( final ExportableTreeItem item, final boolean checked )
  {
    /* Is the item in the left tree checked? */
    /* If so, simply set the check state of the item in the right tree. */
    if( item.isChecked() || !checked )
      item.setCheckedSend( checked );
    else
    {
      /* SPECIAL CASE: We always set both elements to true. */
      /* The user sees only the tree states (not the model states). */
      /* If the element in the left tree is not checked, */
      /* the right tree will always show this element unchecked to, */
      /* regardless of the state in the data model. */
      item.setChecked( true );
      item.setCheckedSend( true );
    }

    m_leftTree.update( item, null );
    m_rightTree.update( item, null );
  }

  protected void updateCheckedElements( )
  {
    final ITreeContentProvider contentProvider = (ITreeContentProvider) m_leftTree.getContentProvider();
    final Object[] children = contentProvider.getElements( m_leftTree.getInput() );

    final List<Object> checkedElements = new ArrayList<Object>();

    collectCheckedElements( children, checkedElements );

    m_checkedElements = checkedElements.toArray( new Object[] {} );
  }

  private void collectCheckedElements( final Object[] children, final List<Object> checkedElements )
  {
    if( children == null || children.length == 0 )
      return;

    for( final Object object : children )
    {
      final ExportableTreeItem item = (ExportableTreeItem) object;
      if( item.isChecked() )
      {
        checkedElements.add( item );
        collectCheckedElements( item.getChildren(), checkedElements );
      }
    }
  }

  protected void checkPageComplete( )
  {
    setPageComplete( m_checkedElements != null && m_checkedElements.length > 0 );
  }

  /**
   * This function returns the checked elements, which should be exported. It will filter checked and grayed elements.
   * 
   * @return The selected elements.
   */
  public ExportableTreeItem[] getSelectedElements( )
  {
    if( m_checkedElements == null )
      return new ExportableTreeItem[] {};

    final List<ExportableTreeItem> items = new ArrayList<ExportableTreeItem>();

    for( final Object element : m_checkedElements )
    {
      final ExportableTreeItem item = (ExportableTreeItem) element;
      if( item.isChecked() && !item.isGrayed() )
        items.add( item );
    }

    return items.toArray( new ExportableTreeItem[] {} );
  }
}