package org.kalypso.debug.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.kalypso.contribs.eclipse.ui.MementoUtils;
import org.kalypso.contribs.eclipse.ui.MementoWithUrlResolver;
import org.kalypso.debug.KalypsoDebugPlugin;

/**
 * action to store the present perspective in a eclipse-memento file.<br>
 * replaces:<br>
 * absolute paths with key-word <br>
 * project-path with key-word <br>
 * <br>
 * 
 * @see org.kalypso.contribs.eclipse.ui.MementoWithUrlResolver#PATH_KEY
 * @see org.kalypso.contribs.eclipse.ui.MementoWithUrlResolver#PROJECT_KEY
 * @author kuepfer
 */
public class SavePerspectivePageAction implements IWorkbenchWindowActionDelegate
{

  private static IWorkbenchWindow m_window;

  public static final String[] FILTER_EXTENSION = new String[] { "*.xml" };

  // XXX: HACK: diese Zeile wurde einfach aus der Workbench.java Klasse kopiert
  private static final String VERSION_STRING[] = { "0.046", "2.0" }; //$NON-NLS-1$ //$NON-NLS-2$

  private IProject m_project;

  public void dispose( )
  {

  }

  public void init( IWorkbenchWindow window )
  {
    m_window = window;
  }

  public void run( IAction action )
  {

    final FileDialog dialog = new FileDialog( m_window.getShell(), SWT.SAVE );
    dialog.setFilterExtensions( FILTER_EXTENSION );
    dialog.setText( "Save workbench state to file.." );
    dialog.setFileName( "pc_perspective_config.xml" );
    String path = dialog.open();

    if( path == null )
      return;
    final File targetFile = new File( path );

    final IWorkbench workbench = m_window.getWorkbench();

    final MultiStatus result = new MultiStatus( KalypsoDebugPlugin.getDefault().getID(), IStatus.OK, "", null );
    // create new Memento to store elements in
    final XMLMemento memento = XMLMemento.createWriteRoot( IWorkbenchConstants.TAG_WORKBENCH );
    final Properties props = new Properties();
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    props.setProperty( root.getLocation().toString(), MementoWithUrlResolver.PATH_KEY );
    props.setProperty( m_project.getName(), MementoWithUrlResolver.PROJECT_KEY );

    final MementoWithUrlResolver replacableMemento = MementoUtils.createMementoWithUrlResolver( memento, props, null );
    // Save the version number.
    replacableMemento.putString( IWorkbenchConstants.TAG_VERSION, VERSION_STRING[1] );

    // Save the advisor state.
    replacableMemento.createChild( IWorkbenchConstants.TAG_WORKBENCH_ADVISOR );

    // Save the workbench windows.
    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
    for( int nX = 0; nX < windows.length; nX++ )
    {
      WorkbenchWindow window = (WorkbenchWindow) windows[nX];
      IMemento childMem = replacableMemento.createChild( IWorkbenchConstants.TAG_WINDOW );
      result.merge( window.saveState( childMem ) );
    }

    replacableMemento.createChild( IWorkbenchConstants.TAG_MRU_LIST ); //$NON-NLS-1$

    try
    {
      // save memento
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final OutputStreamWriter writer = new OutputStreamWriter( bos );
      replacableMemento.save( writer );

      final FileWriter fileWriter = new FileWriter( targetFile );
      fileWriter.write( bos.toString() );
      fileWriter.close();
    }
    catch( IOException e )
    {
      e.printStackTrace();
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    // try
    // {
    // // save memento
    // final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    // final OutputStreamWriter writer = new OutputStreamWriter( bos );
    // memento.save( writer );
    //      
    // // replace absolute file path with the project protocol
    // final StringReader reader = new StringReader( bos.toString() );
    // final Properties props = new Properties();
    // props.setProperty( m_project.getLocation().toString(), "project:" );
    // props.setProperty( m_project.getName(), DssMemento.PROJECT_KEY );
    // final String resultString = ReaderUtilities.readAndReplace( reader, props );
    // final FileWriter fileWriter = new FileWriter( targetFile );
    // fileWriter.write( resultString );
    // fileWriter.close();
    // }
    // catch( IOException e )
    // {
    // e.printStackTrace();
    // }
  }

  public void selectionChanged( IAction action, ISelection selection )
  {
    action.setEnabled( false );
    if( selection instanceof IStructuredSelection )
    {
      Object firstElement = ((IStructuredSelection) selection).getFirstElement();
      if( firstElement instanceof IProject )
      {
        action.setEnabled( true );
        m_project = (IProject) firstElement;
      }
    }
  }

}
