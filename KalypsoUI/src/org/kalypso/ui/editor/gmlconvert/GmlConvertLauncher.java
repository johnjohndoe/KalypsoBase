package org.kalypso.ui.editor.gmlconvert;

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
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.InformSuccessJobChangeAdapter;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.convert.GmlConvertFactory;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.xml.sax.InputSource;

/**
 * @author belger
 */
public class GmlConvertLauncher implements IEditorLauncher
{
  /**
   * @see org.eclipse.ui.IEditorLauncher#open(org.eclipse.core.runtime.IPath)
   */
  @Override
  public void open( final IPath filePath )
  {
    final IWorkbench workbench = PlatformUI.getWorkbench();
    final Shell shell = workbench.getDisplay().getActiveShell();

    final Job job = new Job( Messages.getString( "GmlConvertLauncher_0" ) ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          final IWorkspace workspace = ResourcesPlugin.getWorkspace();
          final IWorkspaceRoot root = workspace.getRoot();

          final IFile convertFile = root.getFileForLocation( filePath );
          if( convertFile == null )
            throw new Exception( Messages.getString( "GmlConvertLauncher_1" ) + filePath.toOSString() ); //$NON-NLS-1$

          final InputStreamReader reader = new InputStreamReader( convertFile.getContents(), convertFile.getCharset() );
          final InputSource inputSource = new InputSource( reader );

          final IUrlResolver resolver = new UrlResolver();
          final URL context = ResourceUtilities.createURL( convertFile );

          return GmlConvertFactory.convertXml( inputSource, resolver, context, new HashMap<>() );
        }
        catch( final GmlConvertException e )
        {
          e.printStackTrace();

          return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, e.getLocalizedMessage(), e.getCause() );
        }
        catch( final Exception e )
        {
          e.printStackTrace();

          return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, Messages.getString( "GmlConvertLauncher_2" ), e ); //$NON-NLS-1$
        }
      }
    };
    job.setUser( true );
    job.addJobChangeListener( new InformSuccessJobChangeAdapter( shell, Messages.getString( "GmlConvertLauncher_3" ), Messages.getString( "GmlConvertLauncher_4" ) ) ); //$NON-NLS-1$ //$NON-NLS-2$
    job.schedule();
  }
}
