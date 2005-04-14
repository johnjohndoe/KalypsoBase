package org.kalypso.debug.editor;

import java.io.InputStreamReader;
import java.net.URL;

import javax.xml.bind.Unmarshaller;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.debug.KalypsoDebugPlugin;
import org.kalypso.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gml.util.GmlconvertType;
import org.kalypso.gml.util.ObjectFactory;
import org.kalypso.ogc.gml.convert.GmlConvertFactory;
import org.kalypso.ogc.gml.convert.SourceHandlerException;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
public class GmlConvertLauncher implements IEditorLauncher
{

  /**
   * @see org.eclipse.ui.IEditorLauncher#open(org.eclipse.core.runtime.IPath)
   */
  public void open( final IPath filePath )
  {
    final Job job = new Job( "GML Konvertieren" )
    {
      protected IStatus run( IProgressMonitor monitor )
      {
        try
        {
          final IWorkbench workbench = PlatformUI.getWorkbench();
          final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
          final Shell shell = window == null ? null : window.getShell();

          final IWorkspace workspace = ResourcesPlugin.getWorkspace();
          final IWorkspaceRoot root = workspace.getRoot();

          final IFile convertFile = root.getFileForLocation( filePath );
          if( convertFile == null )
            throw new Exception( "Datei nicht im Workspace gefunden: " + filePath.toOSString() );

          final ObjectFactory jc = new ObjectFactory();
          final Unmarshaller unmarshaller = jc.createUnmarshaller();

          final InputStreamReader reader = new InputStreamReader( convertFile.getContents(),
              convertFile.getCharset() );
          final GmlconvertType convert = (GmlconvertType)unmarshaller.unmarshal( new InputSource(
              reader ) );

          final URL context = ResourceUtilities.createURL( convertFile );
          final GMLWorkspace gml = GmlConvertFactory.loadSource( context, convert.getSource() );
          GmlConvertFactory.writeIntoTarget( KalypsoGisPlugin.getDefault().getLoaderFactory(), context, gml, convert.getTarget() );

          final Action showStatusAction = new Action( "View Convert Status" )
          {
            /**
             * @see org.eclipse.jface.action.Action#run()
             */
            public void run()
            {
              final String outHref = convert.getTarget().getHref();
              MessageDialog.openInformation( shell, "GML Konvertierung abgeschlossen",
                  "Das GML wurde erfolgreich konvertiert und nach " + outHref + " geschrieben." );
            }
          };

          if( ( (Boolean)getProperty( IProgressConstants.PROPERTY_IN_DIALOG ) ).booleanValue() )
          {
            setProperty( IProgressConstants.ACTION_PROPERTY, showStatusAction );
            setProperty( IProgressConstants.KEEP_PROPERTY, Boolean.TRUE );
          }
          else
            shell.getDisplay().asyncExec( new Runnable()
            {
              public void run()
              {
                showStatusAction.run();
              }
            } );
        }
        catch( final SourceHandlerException e )
        {
          return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
              e.getLocalizedMessage(), e.getCause() );
        }
        catch( final Exception e )
        {
          return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
              "Fehler beim Konvertieren", e );
        }

        return new Status( IStatus.INFO, KalypsoDebugPlugin.getDefault().getID(), 0,
            "Die Konvertierung wurde erfolgreich abgeschlossen.", null );
      }
    };
    job.setUser( true );
    job.schedule();
  }
}
