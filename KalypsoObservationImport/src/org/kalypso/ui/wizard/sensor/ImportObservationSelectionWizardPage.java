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
package org.kalypso.ui.wizard.sensor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ui.wizard.sensor.i18n.Messages;

/**
 * @author doemming
 */
public class ImportObservationSelectionWizardPage extends WizardPage implements FocusListener, ISelectionProvider, ISelectionChangedListener
{
  private static final String DEFAUL_FILE_LABEL = ""; //$NON-NLS-1$

  private final List<INativeObservationAdapter> m_adapter;

  final List<ISelectionChangedListener> m_selectionListener = new ArrayList<ISelectionChangedListener>();

  private Composite m_topLevel = null;

  private Text m_textFileSource;

  private Text m_textFileTarget;

  private Button m_buttonRetainMeta;

  private Button m_buttonAppend;

  private ComboViewer m_formatCombo;

  File m_targetFile = null;

  File m_sourceFile = null;

  private boolean m_controlFinished = false;

  public ImportObservationSelectionWizardPage( final String pageName )
  {
    this( pageName, null, null );
  }

  public ImportObservationSelectionWizardPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    setDescription( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage0") ); //$NON-NLS-1$
    setTitle( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage1") ); //$NON-NLS-1$
    setPageComplete( false );

    m_adapter = createNativeAdapters();
  }

  private List<INativeObservationAdapter> createNativeAdapters( )
  {
    final List<INativeObservationAdapter> adapters = new ArrayList<INativeObservationAdapter>();

    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( "org.kalypso.core.nativeObsAdapter" ); //$NON-NLS-1$

    if( extensionPoint == null )
      return adapters;

    final IExtension[] extensions = extensionPoint.getExtensions();
    for( final IExtension extension : extensions )
    {
      final IConfigurationElement[] elements = extension.getConfigurationElements();

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final INativeObservationAdapter adapter = (INativeObservationAdapter) element.createExecutableExtension( "class" ); //$NON-NLS-1$
          adapters.add( adapter );
        }
        catch( final CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return adapters;
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    initializeDialogUnits( parent );
    m_topLevel = new Composite( parent, SWT.NONE );

    final GridLayout gridLayout = new GridLayout();
    m_topLevel.setLayout( gridLayout );

    final GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    m_topLevel.setLayoutData( data );

    createControlSource( m_topLevel );
    createControlTarget( m_topLevel );
    setControl( m_topLevel );
    validate();
    m_controlFinished = true;
  }

  public void createControlSource( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage2") ); //$NON-NLS-1$

    final GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    group.setLayoutData( data );

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    group.setLayout( gridLayout );

    // line 1
    final Label label = new Label( group, SWT.NONE );
    label.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage3") ); //$NON-NLS-1$

    m_textFileSource = new Text( group, SWT.BORDER );
    m_textFileSource.setText( DEFAUL_FILE_LABEL );
    m_textFileSource.addFocusListener( this );

    final GridData data1 = new GridData();
    data1.horizontalAlignment = GridData.FILL;
    data1.grabExcessHorizontalSpace = true;
    m_textFileSource.setLayoutData( data1 );

    final Button button = new Button( group, SWT.PUSH );
    button.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage4") ); //$NON-NLS-1$
    final GridData data2 = new GridData();
    data2.horizontalAlignment = GridData.END;
    button.setLayoutData( data2 );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_sourceFile = chooseFile( m_sourceFile );
        validate();
      }
    } );
    // line 2

    final Label formatLabel = new Label( group, SWT.NONE );
    formatLabel.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage5") ); //$NON-NLS-1$

    m_formatCombo = new ComboViewer( group, SWT.NONE );
    m_formatCombo.add( m_adapter );
    final ArrayContentProvider provider = new ArrayContentProvider();
    m_formatCombo.setContentProvider( provider );
    m_formatCombo.setLabelProvider( new ILabelProvider()
    {
      @Override
      public Image getImage( final Object element )
      {
        return null;
      }

      @Override
      public String getText( final Object element )
      {
        return element.toString();
      }

      @Override
      public void addListener( final ILabelProviderListener listener )
      {
        // nothing as labelprovider will not change
      }

      @Override
      public void dispose( )
      {
        // nothing as labelprovider will not change
      }

      @Override
      public boolean isLabelProperty( final Object element, final String property )
      {
        return true;
      }

      @Override
      public void removeListener( final ILabelProviderListener listener )
      {
        // nothing
      }
    } );

    m_formatCombo.setInput( m_adapter );
    m_formatCombo.addSelectionChangedListener( this );

    if( m_adapter.size() > 0 )
      m_formatCombo.setSelection( new StructuredSelection( m_adapter.get( 0 ) ) );
  }

  public void createControlTarget( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage6") ); //$NON-NLS-1$
    final GridLayout gridLayout3 = new GridLayout();
    group.setLayout( gridLayout3 );
    final GridData data4 = new GridData();
    data4.horizontalAlignment = GridData.FILL;
    data4.grabExcessHorizontalSpace = true;
    group.setLayoutData( data4 );

    final Composite top = new Composite( group, SWT.NONE );
    final GridData data = new GridData();
    data.horizontalAlignment = GridData.FILL;
    data.grabExcessHorizontalSpace = true;
    top.setLayoutData( data );

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    top.setLayout( gridLayout );

    final Label label = new Label( top, SWT.NONE );
    label.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage7") ); //$NON-NLS-1$

    m_textFileTarget = new Text( top, SWT.BORDER );
    m_textFileTarget.setText( DEFAUL_FILE_LABEL );
    m_textFileTarget.addFocusListener( this );
    final GridData data1 = new GridData();
    data1.horizontalAlignment = GridData.FILL;
    data1.grabExcessHorizontalSpace = true;
    m_textFileTarget.setLayoutData( data1 );
    final Button button = new Button( top, SWT.PUSH );
    button.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage8") ); //$NON-NLS-1$
    final GridData data2 = new GridData();
    data2.horizontalAlignment = GridData.END;
    button.setLayoutData( data2 );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_targetFile = chooseFileZML( m_targetFile );
        validate();
      }
    } );

    final Composite bottom = new Composite( group, SWT.NONE );
    final GridData data3 = new GridData();
    data3.horizontalAlignment = GridData.FILL;
    data3.grabExcessHorizontalSpace = true;
    bottom.setLayoutData( data3 );

    final GridLayout gridLayout2 = new GridLayout();

    bottom.setLayout( gridLayout2 );

    m_buttonRetainMeta = new Button( bottom, SWT.CHECK );
    m_buttonRetainMeta.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage9") ); //$NON-NLS-1$
    m_buttonRetainMeta.setSelection( true );
    m_buttonAppend = new Button( bottom, SWT.CHECK );
    m_buttonAppend.setText( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage10") ); //$NON-NLS-1$
    m_buttonAppend.setSelection( true );
  }

  File chooseFile( final File selectedFile )
  {
    final FileDialog dialog = new FileDialog( getShell(), SWT.SINGLE );
    if( selectedFile != null )
    {
      dialog.setFileName( selectedFile.getName() );
      dialog.setFilterPath( selectedFile.getParent() );
    }
    dialog.open();
    final String fileName = dialog.getFileName();
    final String filterPath = dialog.getFilterPath();
    return new File( filterPath, fileName );
  }

  File chooseFileZML( final File selectedFile )
  {
    final FileDialog dialog = new FileDialog( getShell(), SWT.SINGLE | SWT.SAVE );
    dialog.setOverwrite( true );
    dialog.setFilterExtensions( new String[] { "*.zml" } ); //$NON-NLS-1$
    if( selectedFile != null )
    {
      dialog.setFileName( selectedFile.getName() );
      dialog.setFilterPath( selectedFile.getParent() );
    }

    final String selectedPath = dialog.open();
    if( selectedPath == null )
      return null;

    return new File( selectedPath );
  }

  /**
   * validates the page
   */
  void validate( )
  {
    setErrorMessage( null );
    setMessage( null );
    setPageComplete( true );
    final StringBuffer error = new StringBuffer();
    if( m_sourceFile != null )
      m_textFileSource.setText( m_sourceFile.getAbsolutePath() );
    else
    {
      m_textFileSource.setText( DEFAUL_FILE_LABEL );
      error.append( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage11") ); //$NON-NLS-1$
      setPageComplete( false );
    }
    m_buttonAppend.setEnabled( false );
    m_buttonRetainMeta.setEnabled( false );
    if( m_targetFile != null )
    {
      m_textFileTarget.setText( m_targetFile.getAbsolutePath() );
      if( m_targetFile.exists() )
      {
        m_buttonAppend.setEnabled( true );
        m_buttonRetainMeta.setEnabled( true );
      }
    }
    else
    {
      m_textFileTarget.setText( DEFAUL_FILE_LABEL );
      error.append( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage12") ); //$NON-NLS-1$
      setPageComplete( false );
    }
    if( error.length() > 0 )
      setErrorMessage( error.toString() );
    else
      setMessage( Messages.getString("org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage13") ); //$NON-NLS-1$
    fireSelectionChanged();
  }

  /**
   * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
   */
  @Override
  public boolean canFlipToNextPage( )
  {
    return isPageComplete();
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();
    if( m_topLevel != null && !m_topLevel.isDisposed() )
    {
      m_topLevel.dispose();
      m_topLevel = null;
    }
  }

  /**
   * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
   */
  @Override
  public void focusGained( final FocusEvent e )
  {
    // nothing
  }

  /**
   * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
   */
  @Override
  public void focusLost( final FocusEvent e )
  {
    m_sourceFile = new File( m_textFileSource.getText() );
    m_targetFile = new File( m_textFileTarget.getText() );

    validate();
  }

  private void fireSelectionChanged( )
  {
    for( final Object element : m_selectionListener )
    {
      ((ISelectionChangedListener) element).selectionChanged( new SelectionChangedEvent( this, getSelection() ) );
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void addSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_selectionListener.add( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
   */
  @Override
  public ISelection getSelection( )
  {
    final IStructuredSelection formatSelection = (IStructuredSelection) m_formatCombo.getSelection();
    if( !m_controlFinished )
      return StructuredSelection.EMPTY;

    return new ObservationImportSelection( m_sourceFile, m_targetFile, (INativeObservationAdapter) formatSelection.getFirstElement(), m_buttonAppend.getSelection(), m_buttonRetainMeta.getSelection() );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
   */
  @Override
  public void removeSelectionChangedListener( final ISelectionChangedListener listener )
  {
    m_selectionListener.remove( listener );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void setSelection( final ISelection selection )
  {
    if( selection instanceof ObservationImportSelection )
    {
      final ObservationImportSelection s = ((ObservationImportSelection) selection);
      if( m_formatCombo != null )
        m_formatCombo.setSelection( new StructuredSelection( s.getNativeAdapter() ) );
      m_sourceFile = s.getFileSource();
      m_targetFile = s.getFileTarget();
      if( m_buttonAppend != null )
        m_buttonAppend.setSelection( s.isAppend() );
      if( m_buttonRetainMeta != null )
        m_buttonRetainMeta.setSelection( s.isRetainMetadata() );
    }
    else if( selection instanceof IStructuredSelection )
    {
      final Object firstElement = ((StructuredSelection) selection).getFirstElement();
      if( firstElement instanceof IFile )
      {
        m_targetFile = ResourceUtilities.makeFileFromPath( ((IFile) firstElement).getFullPath() );
      }
    }
    // nothing
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    fireSelectionChanged();
  }

}