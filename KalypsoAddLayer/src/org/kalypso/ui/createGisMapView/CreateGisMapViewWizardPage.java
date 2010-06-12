package org.kalypso.ui.createGisMapView;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.i18n.Messages;

public class CreateGisMapViewWizardPage extends WizardPage
{
  private Text containerText;

  private Text fileText;

  private ISelection m_selection;

  public CreateGisMapViewWizardPage( ISelection selection )
  {
    super( "wizardPage" ); //$NON-NLS-1$
    setTitle( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.0") ); //$NON-NLS-1$
    setDescription( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.1") ); //$NON-NLS-1$
    this.setImageDescriptor( ImageProvider.IMAGE_ICON_GMT );
    m_selection = selection;
  }

  @Override
  public void createControl( Composite parent )
  {
    Composite container = new Composite( parent, SWT.NULL );
    GridLayout layout = new GridLayout();
    container.setLayout( layout );
    layout.numColumns = 3;
    layout.verticalSpacing = 9;
    Label label = new Label( container, SWT.NULL );
    label.setText( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.2") ); //$NON-NLS-1$

    containerText = new Text( container, SWT.BORDER | SWT.SINGLE );
    GridData gd = new GridData( GridData.FILL_HORIZONTAL );
    containerText.setLayoutData( gd );
    containerText.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( ModifyEvent e )
      {
        dialogChanged();
      }
    } );

    Button button = new Button( container, SWT.PUSH );
    button.setText( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.3") ); //$NON-NLS-1$
    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        handleBrowse();
      }
    } );
    label = new Label( container, SWT.NULL );
    label.setText( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.4") ); //$NON-NLS-1$

    fileText = new Text( container, SWT.BORDER | SWT.SINGLE );
    gd = new GridData( GridData.FILL_HORIZONTAL );
    fileText.setLayoutData( gd );
    fileText.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( ModifyEvent e )
      {
        dialogChanged();
      }
    } );
    initialize();
    dialogChanged();
    setControl( container );
  }

  /**
   * Tests if the current workbench selection is a suitable container to use.
   */

  private void initialize( )
  {
    if( m_selection != null && m_selection.isEmpty() == false && m_selection instanceof IStructuredSelection )
    {
      IStructuredSelection ssel = (IStructuredSelection) m_selection;
      if( ssel.size() > 1 )
        return;
      Object obj = ssel.getFirstElement();
      if( obj instanceof IResource )
      {
        IContainer container;
        if( obj instanceof IContainer )
          container = (IContainer) obj;
        else
          container = ((IResource) obj).getParent();
        containerText.setText( container.getFullPath().toString() );
      }
    }
    fileText.setText( "Karte.gmt" ); //$NON-NLS-1$
  }

  /**
   * Uses the standard container selection dialog to choose the new value for the container field.
   */

  void handleBrowse( )
  {
    ContainerSelectionDialog dialog = new ContainerSelectionDialog( getShell(), ResourcesPlugin.getWorkspace().getRoot(), false, Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.5") ); //$NON-NLS-1$
    if( dialog.open() == Window.OK )
    {
      Object[] result = dialog.getResult();
      if( result.length == 1 )
      {
        containerText.setText( ((Path) result[0]).toOSString() );
      }
    }
  }

  /**
   * Ensures that both text fields are set.
   */

  void dialogChanged( )
  {
    String container = getContainerName();
    String fileName = getFileName();

    if( container.length() == 0 )
    {
      updateStatus( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.6") ); //$NON-NLS-1$
      return;
    }
    if( fileName.length() == 0 )
    {
      updateStatus( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.7") ); //$NON-NLS-1$
      return;
    }
    int dotLoc = fileName.lastIndexOf( '.' );
    if( dotLoc != -1 )
    {
      String ext = fileName.substring( dotLoc + 1 );
      if( ext.equalsIgnoreCase( "gmt" ) == false ) //$NON-NLS-1$
      {
        updateStatus( Messages.getString("org.kalypso.ui.createGisMapView.CreateGisMapViewWizardPage.8") ); //$NON-NLS-1$
        return;
      }
    }
    updateStatus( null );
  }

  private void updateStatus( String message )
  {
    setErrorMessage( message );
    setPageComplete( message == null );
  }

  public String getContainerName( )
  {
    return containerText.getText();
  }

  public String getFileName( )
  {
    return fileText.getText();
  }
}