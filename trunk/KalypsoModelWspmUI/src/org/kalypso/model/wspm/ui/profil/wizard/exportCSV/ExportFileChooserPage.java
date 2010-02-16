/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
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
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserDelegateSave;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.serializer.IProfilSink;

/**
 * FIXME bitte Rücksprache mit Gernot!
 * 
 * @author kimwerner
 */
public class ExportFileChooserPage extends WizardPage implements IWizardPage
{
  protected ComboViewer m_comboViewer;

  private FileChooserGroup m_fileChooserGroup;

  private IProfilSink m_currentSink;

  public ExportFileChooserPage( )
  {
    super( "exportProfileFileChooserPage", "Datei wählen", null );

    setMessage( "Auswahl des Export-Filters" );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  public void createControl( final Composite parent )
  {
    final Composite comp = new Composite( parent, SWT.NONE );
    comp.setLayout( new GridLayout() );

    final Map<String, String> profilSinks = KalypsoModelWspmCoreExtensions.getProfilSinks();
    createTypeGroup( comp, profilSinks ).setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    createFileGroup( comp, profilSinks ).setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    m_fileChooserGroup.setDialogSettings( getDialogSettings() );

    setControl( comp );
  }

  protected Group createFileGroup( final Composite parent, final Map<String, String> filters )
  {
    final FileChooserDelegateSave saveDelegate = new FileChooserDelegateSave();

    for( final Entry<String, String> element : filters.entrySet() )
      saveDelegate.addFilter( element.getValue(), "*." + element.getKey() );

    m_fileChooserGroup = new FileChooserGroup( saveDelegate );
    m_fileChooserGroup.addFileChangedListener( new FileChooserGroup.FileChangedListener()
    {
      public void fileChanged( final File file )
      {
        setFile( file );
      }
    } );
    final Group group = m_fileChooserGroup.createControl( parent, SWT.NONE );
    group.setText( "Export File" );
    return group;
  }

  protected void setFile( final File file )
  {
    /* Validate file */
    final String path = file == null ? "" : file.getPath().trim();
    if( path.length() == 0 )
    {
      setErrorMessage( "Es muss ein Verzeichnis oder eine Datei angegeben werden" );
      setPageComplete( false );
    }
    else
    {
      setPageComplete( true );
      setErrorMessage( null );
    }

    /* Choose type corresponding to file */
    final boolean comboEnabled = file != null;
    final ISelection comboSelection;
    if( file == null )
      comboSelection = m_comboViewer.getSelection();
    else
    {
      final int index = file.getAbsolutePath().lastIndexOf( '.' );
      comboSelection = index < 0 ? new StructuredSelection( FileChooserGroup.DIRECTORY_FILTER_SUFFIX ) : new StructuredSelection( file.getAbsolutePath().substring( index + 1 ) );
    }

    /* Invalidate combo */
    final ComboViewer comboViewer = m_comboViewer;
    final Control combo = comboViewer.getControl();
    if( combo != null && !combo.isDisposed() )
    {
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
  }

  private Control createTypeGroup( final Composite parent, final Map<String, String> profilSinks )
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
      public Object[] getElements( final Object inputElement )
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
      public String getText( final Object element )
      {
        return ((Map<String, String>) m_comboViewer.getInput()).get( element.toString() );
      }
    } );

    m_comboViewer.setInput( profilSinks );
    m_comboViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @SuppressWarnings("synthetic-access")
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final String suffix = ((StructuredSelection) event.getSelection()).getFirstElement().toString();

        try
        {
          m_currentSink = KalypsoModelWspmCoreExtensions.createProfilSink( suffix );
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
        }

        final File file = m_fileChooserGroup.getFile();
        if( file != null )
        {
          final String fileName = file.getAbsolutePath();
          final String newFileName = FileChooserGroup.setSuffix( fileName, suffix );
          if( !newFileName.equalsIgnoreCase( fileName ) )
            m_fileChooserGroup.setFile( new File( newFileName ) );
        }
      }
    } );
    return typeGroup;
  }

  public File getFile( )
  {
    return m_fileChooserGroup.getFile();
  }

  public final IProfilSink getProfilSink( )
  {
    return m_currentSink;
  }

  /**
   * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
   */
  @Override
  public boolean isPageComplete( )
  {
    return getFile() != null;
  }

}
