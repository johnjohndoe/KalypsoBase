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
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * This wizard page displays two trees, whose elements are checkable. The show the same content.
 * 
 * @author Holger Albert
 */
public class ExportAndSendPage extends WizardPage
{
  private final ExportableTreeItem[] m_input;

  private CheckboxTreeViewer m_leftTree;

  private CheckboxTreeViewer m_rightTree;

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
  }

  @Override
  public void createControl( final Composite parent )
  {
    /* Create the main composite. */
    final Composite main = new Composite( parent, SWT.NONE );
    main.setLayout( new GridLayout( 2, true ) );

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
    m_leftTree = createTreeViewer( body, m_input );
    m_rightTree = createTreeViewer( body, m_input );

    /* Create the selection buttons. */
    createSelectionButtons( main, m_leftTree );
    createSelectionButtons( main, m_rightTree );

    /* Set the control. */
    setControl( main );
  }

  private CheckboxTreeViewer createTreeViewer( final Composite parent, final ExportableTreeItem[] input )
  {
    final CheckboxTreeViewer viewer = new CheckboxTreeViewer( parent, SWT.BORDER );
    viewer.getTree().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    viewer.setContentProvider( new ExportableContentProvider() );
    viewer.setLabelProvider( getLabelProvider( viewer ) );
    viewer.setSorter( new ViewerSorter() );
    viewer.setInput( input );
    viewer.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        final Object element = event.getElement();
        final boolean checked = event.getChecked();
        // TODO
        viewer.setSubtreeChecked( element, checked );
      }
    } );

    // TODO expand listener to sync expanding states...

    return viewer;
  }

  private LabelProvider getLabelProvider( final CheckboxTreeViewer viewer )
  {
    final Font grayedFont = getFont();
    final Color grayedForeground = viewer.getTree().getDisplay().getSystemColor( SWT.COLOR_GRAY );
    final Color grayedBackground = viewer.getControl().getBackground();

    return new ExportableLabelProvider( grayedFont, grayedForeground, grayedBackground );
  }

  private void createSelectionButtons( final Composite composite, final CheckboxTreeViewer viewer )
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
        handleSelectAll( viewer );
      }
    } );

    final Button deselectButton = createButton( buttonComposite, "Alles abw‰hlen" );
    deselectButton.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleDeselectAll( viewer );
      }
    } );
  }

  protected Button createButton( final Composite parent, final String label )
  {
    final Button button = new Button( parent, SWT.PUSH );
    button.setText( label );

    setButtonLayoutData( button );

    return button;
  }

  protected void handleSelectAll( final CheckboxTreeViewer viewer )
  {
    viewer.expandAll();

    final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
    final Object[] items = contentProvider.getElements( viewer.getInput() );
    for( int i = 0; i < items.length; i++ )
    {
      final Object item = items[i];
      viewer.setSubtreeChecked( item, true );
    }
  }

  protected void handleDeselectAll( final CheckboxTreeViewer viewer )
  {
    viewer.expandAll();

    final ITreeContentProvider contentProvider = (ITreeContentProvider) viewer.getContentProvider();
    final Object[] items = contentProvider.getElements( viewer.getInput() );
    for( int i = 0; i < items.length; i++ )
    {
      final Object item = items[i];
      // TODO Set checkstate for all exportable tree items and update the tree (recursive)...
      // TODO Use checkstate provider
      viewer.setSubtreeChecked( item, false );
    }
  }

  public Object[] getCheckedElements( )
  {
    // Get items from tree...
    return null;
  }
}