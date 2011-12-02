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
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;
import org.kalypso.ui.wizard.sensor.i18n.Messages;

/**
 * @author doemming
 */
public class ImportObservationSelectionWizardPage extends WizardPage implements /* FocusListener, */ISelectionProvider, ISelectionChangedListener
{
  private final List<ISelectionChangedListener> m_selectionListener = new ArrayList<ISelectionChangedListener>();

  private final List<INativeObservationAdapter> m_adapter;

  private Button m_buttonRetainMeta;

  private Button m_buttonAppend;

  private ComboViewer m_formatCombo;

  private boolean m_controlFinished = false;

  private TimeZone m_timezone;

  private String m_sourcePath;

  private IPath m_targetPath;

  public ImportObservationSelectionWizardPage( final String pageName )
  {
    this( pageName, null, null );

    m_timezone = KalypsoCorePlugin.getDefault().getTimeZone();
  }

  public ImportObservationSelectionWizardPage( final String pageName, final String title, final ImageDescriptor titleImage )
  {
    super( pageName, title, titleImage );

    setDescription( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage0" ) ); //$NON-NLS-1$
    setTitle( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage1" ) ); //$NON-NLS-1$
    setPageComplete( false );

    m_adapter = createNativeAdapters();
  }

  // FIXME: move into spearate extension class
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

    final Composite topLevel = new Composite( parent, SWT.NONE );
    topLevel.setLayout( new GridLayout() );
    setControl( topLevel );

    createControlSource( topLevel );
    createControlTarget( topLevel );

    m_controlFinished = true;
  }

  public void createControlSource( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( 3, false ) );
    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    group.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage2" ) ); //$NON-NLS-1$

    // line 1
    final Label label = new Label( group, SWT.NONE );
    label.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage3" ) ); //$NON-NLS-1$

    final Text textFileSource = new Text( group, SWT.BORDER );
    textFileSource.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    textFileSource.setText( StringUtils.EMPTY );
    textFileSource.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        handleSourcePathModified( textFileSource.getText() );
      }
    } );

    final Button button = new Button( group, SWT.PUSH );
    button.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage4" ) ); //$NON-NLS-1$
    button.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        chooseSourceFile( textFileSource );
      }
    } );

    // line 2
    final Label formatLabel = new Label( group, SWT.NONE );
    formatLabel.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage5" ) ); //$NON-NLS-1$

    m_formatCombo = new ComboViewer( group, SWT.DROP_DOWN | SWT.READ_ONLY );
    m_formatCombo.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_formatCombo.setContentProvider( new ArrayContentProvider() );
    m_formatCombo.setLabelProvider( new LabelProvider() );
    m_formatCombo.setInput( m_adapter );

    m_formatCombo.addSelectionChangedListener( this );

    if( m_adapter.size() > 0 )
      m_formatCombo.setSelection( new StructuredSelection( m_adapter.get( 0 ) ) );

    new Label( group, SWT.NONE );

    // TimeZone
    /* time zone selection */
    final Label timezoneLabel = new Label( group, SWT.NONE );
    timezoneLabel.setText( Messages.getString("ImportObservationSelectionWizardPage.0") ); //$NON-NLS-1$

    final String[] tz = TimeZone.getAvailableIDs();
    Arrays.sort( tz );

    final ComboViewer comboTimeZones = new ComboViewer( group, SWT.BORDER | SWT.SINGLE );
    comboTimeZones.getControl().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    comboTimeZones.setContentProvider( new ArrayContentProvider() );
    comboTimeZones.setLabelProvider( new LabelProvider() );
    comboTimeZones.setInput( tz );

    comboTimeZones.addFilter( new ViewerFilter()
    {
      @Override
      public boolean select( final Viewer viewer, final Object parentElement, final Object element )
      {
        if( element instanceof String )
        {
          final String name = (String) element;
          return !name.toLowerCase().startsWith( "etc/" ); //$NON-NLS-1$
        }

        return true;
      }
    } );

    comboTimeZones.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) comboTimeZones.getSelection();
        updateTimeZone( (String) selection.getFirstElement() );
      }
    } );

    comboTimeZones.getCombo().addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        updateTimeZone( comboTimeZones.getCombo().getText() );
      }
    } );

    if( m_timezone != null )
    {
      final String id = m_timezone.getID();
      if( ArrayUtils.contains( tz, id ) )
        comboTimeZones.setSelection( new StructuredSelection( id ) );
      else
        comboTimeZones.getCombo().setText( id );
    }
  }

  protected void handleSourcePathModified( final String sourcePath )
  {
    m_sourcePath = sourcePath;

    validate();
  }

  public void createControlTarget( final Composite parent )
  {
    final Group group = new Group( parent, SWT.NONE );
    group.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage6" ) ); //$NON-NLS-1$
    group.setLayout( new GridLayout() );

    group.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    final Composite top = new Composite( group, SWT.NONE );
    top.setLayout( new GridLayout( 3, false ) );
    top.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final Label label = new Label( top, SWT.NONE );
    label.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage7" ) ); //$NON-NLS-1$

    final Text textFileTarget = new Text( top, SWT.BORDER );
    textFileTarget.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    textFileTarget.setText( StringUtils.EMPTY );
    textFileTarget.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        handleTargetModifed( textFileTarget.getText() );
      }
    } );

    final Button button = new Button( top, SWT.PUSH );
    button.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );
    button.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage8" ) ); //$NON-NLS-1$

    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        chooseFileZML( textFileTarget );
      }
    } );

    final Composite bottom = new Composite( group, SWT.NONE );
    bottom.setLayout( new GridLayout() );
    bottom.setLayoutData( new GridData( SWT.BEGINNING, SWT.FILL, true, false ) );

    m_buttonRetainMeta = new Button( bottom, SWT.CHECK );
    m_buttonRetainMeta.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage9" ) ); //$NON-NLS-1$
    m_buttonRetainMeta.setSelection( true );

    m_buttonAppend = new Button( bottom, SWT.CHECK );
    m_buttonAppend.setText( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage10" ) ); //$NON-NLS-1$
    m_buttonAppend.setSelection( true );
  }

  protected void handleTargetModifed( final String text )
  {
    if( StringUtils.isBlank( text ) || !new Path( text ).isValidPath( text ) )
      m_targetPath = null;
    else
      m_targetPath = new Path( text );

    validate();
  }

  protected void chooseSourceFile( final Text textFileSource )
  {
    final FileDialog dialog = new FileDialog( getShell(), SWT.SINGLE );

    final File sourceFile = getSourceFile();
    if( sourceFile != null )
    {
      dialog.setFileName( sourceFile.getName() );
      dialog.setFilterPath( sourceFile.getParent() );
    }

    if( dialog.open() == null )
      return;

    final String fileName = dialog.getFileName();
    final String filterPath = dialog.getFilterPath();
    final File newSourceFile = new File( filterPath, fileName );
    textFileSource.setText( newSourceFile.getAbsolutePath() );
  }

  private File getSourceFile( )
  {
    if( StringUtils.isBlank( m_sourcePath ) )
      return null;

    return new File( m_sourcePath );
  }

  protected void chooseFileZML( final Text textFileTarget )
  {
    final IFile targetFile = getTargetFile();

    final SaveAsDialog saveAsDialog = new SaveAsDialog( getShell() )
    {
      /**
       * @see org.eclipse.ui.dialogs.SaveAsDialog#configureShell(org.eclipse.swt.widgets.Shell)
       */
      @Override
      protected void configureShell( final Shell shell )
      {
        super.configureShell( shell );

        // shell.setText( "XXXX" );
        PlatformUI.getWorkbench().getHelpSystem().setHelp( shell, null );
      }

      /**
       * @see org.eclipse.ui.dialogs.SaveAsDialog#createContents(org.eclipse.swt.widgets.Composite)
       */
      @Override
      protected Control createContents( final Composite parent )
      {
        final Control contents = super.createContents( parent );
        setTitle( Messages.getString("ImportObservationSelectionWizardPage.2") ); //$NON-NLS-1$
        setMessage( Messages.getString("ImportObservationSelectionWizardPage.3") ); //$NON-NLS-1$
        return contents;
      }

    };
    saveAsDialog.setHelpAvailable( false );

    if( targetFile != null )
      saveAsDialog.setOriginalFile( targetFile );
    else
    {
      final File sourceFile = getSourceFile();
      if( sourceFile != null )
        saveAsDialog.setOriginalName( sourceFile.getName() );
    }

    if( saveAsDialog.open() != Window.OK )
      return;

    final IPath resultPath = saveAsDialog.getResult();

    textFileTarget.setText( resultPath.toString() );

    validate();
  }

  private IFile getTargetFile( )
  {
    if( m_targetPath == null )
      return null;

    try
    {
      final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      return root.getFile( m_targetPath );
    }
    catch( final java.lang.IllegalArgumentException e )
    {
      return null;
    }
  }

  /**
   * validates the page
   */
  void validate( )
  {
    // Do not validate until page was created
    if( !m_controlFinished )
      return;

    final IMessageProvider message = doValidate();
    if( message == null )
      setMessage( null );
    else
      setMessage( message.getMessage(), message.getMessageType() );
    setPageComplete( message == null );

    // TODO: does not really belong here
    final IFile targetFile = getTargetFile();
    final boolean targetFileExists = targetFile != null && targetFile.exists();
    m_buttonAppend.setEnabled( targetFileExists );
    m_buttonRetainMeta.setEnabled( targetFileExists );

    fireSelectionChanged();
  }

  private IMessageProvider doValidate( )
  {
    final File sourceFile = getSourceFile();
    if( sourceFile == null )
      return new MessageProvider( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage11" ), ERROR ); //$NON-NLS-1$
    if( !sourceFile.isFile() )
      return new MessageProvider( Messages.getString("ImportObservationSelectionWizardPage.4"), ERROR ); //$NON-NLS-1$

    final IFile targetFile = getTargetFile();
    if( targetFile == null )
      return new MessageProvider( Messages.getString( "org.kalypso.ui.wizard.sensor.ImportObservationSelectionWizardPage12" ), ERROR ); //$NON-NLS-1$

    if( m_timezone == null )
      return new MessageProvider( Messages.getString("ImportObservationSelectionWizardPage.5"), ERROR ); //$NON-NLS-1$

    return null;
  }

  private void fireSelectionChanged( )
  {
    for( final Object element : m_selectionListener )
      ((ISelectionChangedListener) element).selectionChanged( new SelectionChangedEvent( this, getSelection() ) );
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

    final File sourceFile = getSourceFile();
    final IFile targetFile = getTargetFile();
    return new ObservationImportSelection( sourceFile, targetFile, (INativeObservationAdapter) formatSelection.getFirstElement(), m_buttonAppend.getSelection(), m_buttonRetainMeta.getSelection(), m_timezone );
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

      final File sourceFile = s.getFileSource();
      if( sourceFile == null )
        m_sourcePath = null;
      else
        m_sourcePath = sourceFile.getAbsolutePath();

      final IFile targetFile = s.getFileTarget();
      if( targetFile == null )
        m_targetPath = null;
      else
        m_targetPath = targetFile.getFullPath();

      if( m_buttonAppend != null )
        m_buttonAppend.setSelection( s.isAppend() );
      if( m_buttonRetainMeta != null )
        m_buttonRetainMeta.setSelection( s.isRetainMetadata() );
    }
    else if( selection instanceof IStructuredSelection )
    {
      final Object firstElement = ((StructuredSelection) selection).getFirstElement();
      if( firstElement instanceof IFile )
        m_targetPath = ((IFile) firstElement).getFullPath();
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    fireSelectionChanged();
  }

  protected void updateTimeZone( final String timeZoneID )
  {
    m_timezone = null;

    if( timeZoneID != null )
    {
      final TimeZone timeZone = TimeZone.getTimeZone( timeZoneID.toUpperCase() );
      // Only set, if timezone could be parsed
      if( !timeZone.getID().equals( "GMT" ) || timeZoneID.toUpperCase().equals( "GMT" ) ) //$NON-NLS-1$ //$NON-NLS-2$
        m_timezone = timeZone;
    }

    validate();
  }

}