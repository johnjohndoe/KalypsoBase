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
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.widgets.Group;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate.FILE_CHOOSER_GROUP_TYPE;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.serializer.IProfilSink;

/**
 * @author kimwerner
 */
public class ExportFileChooserPage extends WizardPage implements IWizardPage
{
  private File m_file = null;

  protected ComboViewer m_comboViewer;

  private FileChooserGroup m_fileChooserGroup;

  public ExportFileChooserPage( )
  {
    super( "exportProfileFileChooserPage", "Datei w‰hlen", null );

    setMessage( "Auswahl des Export-Filters" );
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

    m_fileChooserGroup.setDialogSettings( getDialogSettings() );

    setControl( comp );
  }

  protected Group createFileGroup( final Composite parent )
  {
    m_fileChooserGroup = new FileChooserGroup( new FileChooserDelegate( FILE_CHOOSER_GROUP_TYPE.eSave )
    {
      /**
       * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate#getFilterExtensions()
       */
      @SuppressWarnings("unchecked")
      @Override
      public String[] getFilterNames( )
      {
        if( m_comboViewer.getInput() == null )
          return new String[] { "all Files" };
        final Map<String, String> map = ((Map<String, String>) m_comboViewer.getInput());
        final ArrayList<String> list = new ArrayList( map.size() );
        for( final String name : map.keySet() )
        {
          list.add( map.get( name ) );
        }
        return list.toArray( new String[] {} );
      }

      /**
       * @see org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate#getFilterNames()
       */
      @SuppressWarnings("unchecked")
      @Override
      public String[] getFilterExtensions( )
      {
        if( m_comboViewer.getInput() == null )
          return new String[] { "*.*" };
        final Map<String, String> map = ((Map<String, String>) m_comboViewer.getInput());
        final ArrayList<String> list = new ArrayList( map.size() );
        for( final String filter : map.keySet() )
        {
          list.add( "*." + filter );
        }
        return list.toArray( new String[] {} );
      }

    } );

    m_fileChooserGroup.addFileChangedListener( new FileChooserGroup.FileChangedListener()
    {
      public void fileChanged( final File file )
      {
        setFile( file );
      }
    } );
    final Group group = m_fileChooserGroup.createControl( parent, SWT.NONE );
    return group;
  }

  private void setFile( final File file, final String errorMessage )
  {
    setErrorMessage( errorMessage );
    m_file = file;
  }

  protected void setFile( final File file )
  {
    if( file.equals( m_file ) )
      return;
    /* Validate file */
    final String path = file.getPath().trim();
    if( path.length() == 0 )
      setFile( null, "Es muss ein Vetzeichnis oder eine Datei angegeben werden" );
    else
    {
      setFile( file, null );
    }
    /* Choose type corresponding to file */
    final boolean comboEnabled = m_file != null;
    final ISelection comboSelection;
    if( m_file == null )
    {
      setPageComplete( false );
      comboSelection = m_comboViewer.getSelection();
    }
    else
    {
      final int index = file.getAbsolutePath().lastIndexOf( '.' );
      comboSelection = index < 0 ? new StructuredSelection( FileChooserGroup.DIRECTORY_FILTER_SUFFIX ) : new StructuredSelection( file.getAbsolutePath().substring( index + 1 ) );
    }

    /* Invalidate combo */

    final ComboViewer comboViewer = m_comboViewer;
    final Control combo = comboViewer.getControl();
    if( combo != null && !combo.isDisposed() )
      combo.getDisplay().asyncExec( new Runnable()
      {
        public void run( )
        {
          if( !combo.isDisposed() )
            combo.setEnabled( comboEnabled );
          if( comboSelection != null )
            comboViewer.setSelection( comboSelection );
        }
      } );
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
    } );

    m_comboViewer.setLabelProvider( new LabelProvider()
    {

      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @SuppressWarnings("unchecked")
      @Override
      public String getText( Object element )
      {
        return ((Map<String, String>) m_comboViewer.getInput()).get( element.toString() );
      }
    } );
    m_comboViewer.setInput( KalypsoModelWspmCoreExtensions.getProfilSinks() );
    m_comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @SuppressWarnings("synthetic-access")
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IDialogSettings settings = getDialogSettings();
        final String fileName = settings.get( FileChooserGroup.SETTINGS_FILENAME );
        final String suffix = ((StructuredSelection) event.getSelection()).getFirstElement().toString();
        final String newFileName = FileChooserGroup.setSuffix( fileName, suffix );
        if( !newFileName.equalsIgnoreCase( fileName ) )
        {
          settings.put( FileChooserGroup.SETTINGS_FILENAME, newFileName );
          m_fileChooserGroup.setDialogSettings( settings );
        }
      }
    } );
    return typeGroup;
  }

  public File getFile( )
  {
    return m_file;
  }

  public final IProfilSink getProfilSink( ) throws CoreException
  {
    final Object selection = ((StructuredSelection) m_comboViewer.getSelection()).getFirstElement();
    return selection == null ? null : KalypsoModelWspmCoreExtensions.createProfilSink( selection.toString() );
  }

  /**
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete( )
  {

    return m_file != null;
  }

}
