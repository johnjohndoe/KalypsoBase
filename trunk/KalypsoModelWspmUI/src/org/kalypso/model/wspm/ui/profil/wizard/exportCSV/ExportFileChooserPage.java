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
package org.kalypso.model.wspm.ui.profil.wizard.exportCSV;

import java.io.File;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate.FILE_CHOOSER_GROUP_TYPE;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;

/**
 * @author kimwerner
 */
public class ExportFileChooserPage extends WizardPage implements IWizardPage
{

  /**
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete( )
  {

    return m_filePanel.getFile() != null;
  }
   
  private static String DEFAULT_MSG = "Auf dieser Seite w‰hlen Sie die Datei, welche importiert werden soll.";

  private FileChooserGroup m_filePanel;

  protected ComboViewer m_comboViewer;

  public ExportFileChooserPage( )
  {
    super( "wsvFileChooserPage", "Datei w‰hlen", null );

    setMessage( DEFAULT_MSG );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Composite comp = new Composite( parent, SWT.NONE );
    comp.setLayout( new GridLayout() );

    createTypeGroup( comp ).setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    createFileGroup( comp ).setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    /* Called after type-combo is created, because this eventually leads to a 'setFile' */
    m_filePanel.setDialogSettings( getDialogSettings() );
    setControl( comp );
  }

  final protected void updateControl( )
  {
   
  }

  private Group createFileGroup( final Composite parent )
  {
    m_filePanel = new FileChooserGroup( new FileChooserDelegate( FILE_CHOOSER_GROUP_TYPE.eSave )
    {

    
      

      /**
       * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate#updateFileName(org.eclipse.swt.widgets.FileDialog,
       *      java.lang.String)
       */
      @Override
      public String updateFileName( FileDialog dialog, String newFilename )
      {
        return FileUtilities.setSuffix( newFilename, dialog.getFilterExtensions()[dialog.getFilterIndex()].substring( 1 ) );
      }

      /**
       * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate#getFilterNames()
       */
      @SuppressWarnings("unchecked")
      @Override
      public String[] getFilterNames( )
      {
        final Object selection= ((StructuredSelection)m_comboViewer.getSelection()).getFirstElement();
        return new String[] { ((Map< String , String >)m_comboViewer.getInput()).get( selection.toString() ) };
      }

      /**
       * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate#getFilterExtensions()
       */
      @Override
      public String[] getFilterExtensions( )
      {

// final String[] ext = new String[sinks.size()];
// int i = 0;
// for( String s : sinks.keySet() )
// {
// ext[i++] = "*." + s;
// }
        final Object selection = ((StructuredSelection)m_comboViewer.getSelection()).getFirstElement();
        return new String[] { "*." + selection };
      }

    } );
    return m_filePanel.createControl( parent, SWT.NONE );
  }

  private Control createTypeGroup( final Composite parent )
  {
    final Group typeGroup = new Group( parent, SWT.NONE );
    typeGroup.setText( "Dateiformat" );
    typeGroup.setLayout( new GridLayout( 1, false ) );

    m_comboViewer = new ComboViewer( typeGroup, SWT.DROP_DOWN | SWT.READ_ONLY );
    m_comboViewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_comboViewer.setContentProvider( new ArrayContentProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.ArrayContentProvider#getElements(java.lang.Object)
       */
      @Override
      public Object[] getElements( Object inputElement )
      {
        return ((Map< ? , ? >) inputElement).keySet().toArray();
      }
    } 
    );

    m_comboViewer.setLabelProvider( new LabelProvider(){

      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @SuppressWarnings("unchecked")
      @Override
      public String getText( Object element )
      {
        return ((Map< String , String >)m_comboViewer.getInput()).get( element.toString() );
      }} );
    m_comboViewer.setInput( KalypsoModelWspmCoreExtensions.getProfilSinks() );
    m_comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        updateControl();
      }
    } );

    return typeGroup;
  }

// protected void setFile( final File file )
// {
// /* Validate file */
// if( file.getPath().trim().length() == 0 )
// setErrorMessage( "Es muss eine Datei angegeben werden" );
//    
// else if( !file.isFile() )
// setErrorMessage( "Der angegebene Pfad verweist nicht auf eine Datei" );
// else
// setPageComplete( true );
//
// /* Choose type corresponding to file */
// final boolean comboEnabled = m_filePanel.getFile() != null;
// // final ISelection comboSelection;
// if( m_filePanel.getFile() == null )
// {
//    
// setPageComplete( false );
// // comboSelection = StructuredSelection.EMPTY;
// }
//   
//
// /* Invalidate combo */
// final ComboViewer comboViewer = m_comboViewer;
// final Control combo = comboViewer.getControl();
// if( combo != null && !combo.isDisposed() )
// combo.getDisplay().asyncExec( new Runnable()
// {
// public void run( )
// {
// if( !combo.isDisposed() )
// combo.setEnabled( comboEnabled );
// // comboViewer.setSelection( comboSelection );
// }
// } );
//
// }

  public File getFile( )
  {
    return m_filePanel.getFile();
  }
} // @jve:decl-index=0:visual-constraint="258,97"
