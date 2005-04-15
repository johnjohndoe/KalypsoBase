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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.debug.KalypsoDebugPlugin;
import org.kalypso.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gml.util.GmlconvertType;
import org.kalypso.gml.util.ObjectFactory;
import org.kalypso.java.net.IUrlResolver;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.convert.GmlConvertFactory;
import org.kalypso.util.url.UrlResolver;
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
          
          final IUrlResolver resolver = new UrlResolver();
          
          final GMLWorkspace gml = GmlConvertFactory.loadSource( resolver, context, convert.getSource() );
          GmlConvertFactory.writeIntoTarget( resolver, context, gml, convert.getTarget() );

          final Action showStatusAction = new Action( "View Convert Status" )
          {
            /**
             * @see org.eclipse.jface.action.Action#run()
             */
            public void run()
            {
              final Shell shell = workbench.getDisplay().getActiveShell();
              
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
            workbench.getDisplay().asyncExec( new Runnable()
            {
              public void run()
              {
                showStatusAction.run();
              }
            } );
        }
        catch( final GmlConvertException e )
        {
          e.printStackTrace();
          
          return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0,
              e.getLocalizedMessage(), e.getCause() );
        }
        catch( final Exception e )
        {
          e.printStackTrace();
          
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
