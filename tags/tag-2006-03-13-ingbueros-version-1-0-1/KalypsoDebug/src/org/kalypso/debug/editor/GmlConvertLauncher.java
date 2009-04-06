package org.kalypso.debug.editor;

import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorLauncher;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.kalypso.commons.java.net.UrlResolver;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.InformSuccessJobChangeAdapter;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.debug.KalypsoDebugPlugin;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.convert.GmlConvertFactory;
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
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final Shell shell = workbench.getDisplay().getActiveShell();

    final Job job = new Job( "GML Konvertieren" )
    {
      @Override
      protected IStatus run( IProgressMonitor monitor )
      {
        try
        {
          final IWorkspace workspace = ResourcesPlugin.getWorkspace();
          final IWorkspaceRoot root = workspace.getRoot();

          final IFile convertFile = root.getFileForLocation( filePath );
          if( convertFile == null )
            throw new Exception( "Datei nicht im Workspace gefunden: " + filePath.toOSString() );

          final InputStreamReader reader = new InputStreamReader( convertFile.getContents(), convertFile.getCharset() );
          final InputSource inputSource = new InputSource( reader );

          final IUrlResolver resolver = new UrlResolver();
          final URL context = ResourceUtilities.createURL( convertFile );

          return GmlConvertFactory.convertXml( inputSource, resolver, context, new HashMap() );
        }
        catch( final GmlConvertException e )
        {
          e.printStackTrace();

          return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0, e.getLocalizedMessage(), e
              .getCause() );
        }
        catch( final Exception e )
        {
          e.printStackTrace();

          return new Status( IStatus.ERROR, KalypsoDebugPlugin.getDefault().getID(), 0, "Fehler beim Konvertieren", e );
        }
      }
    };
    job.setUser( true );
    job.addJobChangeListener( new InformSuccessJobChangeAdapter( shell, "GML Konvertierung",
        "GML-Konvertierung erfolgreich abgeschlossen." ) );
    job.schedule();
  }
}
