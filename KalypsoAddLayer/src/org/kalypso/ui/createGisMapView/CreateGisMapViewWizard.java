package org.kalypso.ui.createGisMapView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.template.gismapview.Gismapview;
import org.kalypso.template.gismapview.ObjectFactory;
import org.kalypso.ui.i18n.Messages;

public class CreateGisMapViewWizard extends Wizard implements INewWizard
{
  public final static String WIZARD_ID = "org.kalypso.ui.createGisMapView.CreateGisMapViewWizard"; //$NON-NLS-1$

  private CreateGisMapViewWizardPage page;

  private ISelection m_selection;

  public CreateGisMapViewWizard( )
  {
    super();
    setNeedsProgressMonitor( true );
  }

  /**
   * Adding the page to the wizard.
   */
  @Override
  public void addPages( )
  {
    page = new CreateGisMapViewWizardPage( m_selection );
    addPage( page );
  }

  @Override
  public boolean performFinish( )
  {
    final String containerName = page.getContainerName();
    final String fileName = page.getFileName();
    final IRunnableWithProgress op = new IRunnableWithProgress()
    {
      @Override
      public void run( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          doFinish( containerName, fileName, monitor );
        }
        catch( final CoreException e )
        {
          throw new InvocationTargetException( e );
        }
        finally
        {
          monitor.done();
        }
      }
    };
    try
    {
      getContainer().run( true, false, op );
    }
    catch( final InterruptedException e )
    {
      return false;
    }
    catch( final InvocationTargetException e )
    {
      final Throwable realException = e.getTargetException();
      MessageDialog.openError( getShell(), Messages.getString( "org.kalypso.ui.createGisMapView.CreateGisMapViewWizard.0" ), realException.getMessage() ); //$NON-NLS-1$
      return false;
    }
    return true;
  }

  void doFinish( final String containerName, final String fileName, final IProgressMonitor monitor ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ui.createGisMapView.CreateGisMapViewWizard.1" ) + fileName, 2 ); //$NON-NLS-1$
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource resource = root.findMember( new Path( containerName ) );
    if( !resource.exists() || !(resource instanceof IContainer) )
    {
      throwCoreException( Messages.getString( "org.kalypso.ui.createGisMapView.CreateGisMapViewWizard.2", containerName ) ); //$NON-NLS-1$
    }
    final IContainer container = (IContainer) resource;
    final IFile file = container.getFile( new Path( fileName ) );
    try
    {
      final InputStream stream = openContentStream();
      if( !file.exists() )
      {
        file.create( stream, true, monitor );
      }
      stream.close();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    catch( final JAXBException e )
    {
      e.printStackTrace();
    }
    monitor.worked( 1 );
    monitor.setTaskName( Messages.getString( "org.kalypso.ui.createGisMapView.CreateGisMapViewWizard.4" ) ); //$NON-NLS-1$
    getShell().getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        final IWorkbenchPage workbenchPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        try
        {
          IDE.openEditor( workbenchPage, file, true );
        }
        catch( final PartInitException e )
        {
          e.printStackTrace();
        }
      }
    } );
    monitor.worked( 1 );
  }

  private InputStream openContentStream( ) throws JAXBException, IOException
  {
    // Create GisMapView
    final Gismapview gismapview = GisTemplateHelper.emptyGisView();

    final JAXBContext jc = JaxbUtilities.createQuiet( ObjectFactory.class );
    final Marshaller marshaller = JaxbUtilities.createMarshaller( jc );
    marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
    final StringWriter stringWriter = new StringWriter();
    marshaller.marshal( gismapview, stringWriter );
    final String contents = stringWriter.toString();
    stringWriter.close();
    return new ByteArrayInputStream( contents.getBytes() );
  }

  private void throwCoreException( final String message ) throws CoreException
  {
    final IStatus status = new Status( IStatus.ERROR, "org.kalypso.ui.createGisMapView", IStatus.OK, message, null ); //$NON-NLS-1$
    throw new CoreException( status );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    m_selection = selection;
  }
}