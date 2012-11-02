package org.kalypso.ui.editor.gmleditor.part;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IPostSelectionProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.editor.AbstractWorkbenchPart;
import org.kalypso.ui.editor.actions.INewScope;
import org.kalypso.ui.editor.actions.NewScopeFactory;
import org.kalypso.ui.editorLauncher.GmlEditorTemplateLauncher;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;

/**
 * Common code for parts showing a .gmv file.
 * 
 * @author Gernot Belger
 */
public class AbstractGmvPart extends AbstractWorkbenchPart implements IGmvPart
{
  public static final String EXTENSIN_GMV = ".gmv"; //$NON-NLS-1$

  private GmlTreeView m_viewer = null;

  @Override
  public void dispose( )
  {
    if( m_viewer != null )
      m_viewer.dispose();

    // unregister site selection provider
    getSite().setSelectionProvider( null );

    super.dispose();
  }

  @Override
  protected IStorageEditorInput tweakInput( final IStorageEditorInput input )
  {
    if( input instanceof IFileEditorInput )
    {
      final IFile file = ((IFileEditorInput)input).getFile();
      if( file == null )
        return input;

      final String ext = file.getFileExtension();
      if( "gml".equalsIgnoreCase( ext ) || "shp".equalsIgnoreCase( ext ) || "gmlz".equalsIgnoreCase( ext ) ) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      {
        try
        {
          return GmlEditorTemplateLauncher.createInputForGml( file );
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
        }
      }
    }

    return input;
  }

  @Override
  protected void doSaveInternal( final IProgressMonitor monitor, final IFile file ) throws CoreException
  {
    ByteArrayInputStream bis = null;
    OutputStreamWriter writer = null;

    try
    {
      final String charset = file.getCharset();

      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writer = new OutputStreamWriter( bos );
      m_viewer.saveInput( bos, charset, monitor );
      writer.close();

      bis = new ByteArrayInputStream( bos.toByteArray() );
      bos.close();
      monitor.worked( 1000 );

      if( file.exists() )
        file.setContents( bis, false, true, monitor );
      else
        file.create( bis, false, monitor );
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
    finally
    {
      IOUtils.closeQuietly( bis );
      IOUtils.closeQuietly( writer );
    }
  }

  @Override
  public GmlTreeView getTreeView( )
  {
    return m_viewer;
  }

  @Override
  protected void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws CoreException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.gmleditor.part.GmlEditor.1" ), 1000 ); //$NON-NLS-1$
    try
    {
      final IStorage storage = input.getStorage();

      final Reader r;
      final InputStream contents = storage.getContents();
      if( storage instanceof IEncodedStorage )
        r = new InputStreamReader( contents, ((IEncodedStorage)storage).getCharset() );
      else
        r = new InputStreamReader( contents );

      final URL context = findContext( storage );

      // FIXME: why in the ui thread?
      getSite().getShell().getDisplay().asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          try
          {
            final GmlTreeView treeView = getTreeView();
            if( treeView != null )
              treeView.loadInput( r, context, monitor );
          }
          catch( final CoreException e )
          {
            e.printStackTrace();
          }
        }
      } );
    }
    catch( final UnsupportedEncodingException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.gmleditor.part.GmlEditor.3" ) ) ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.gmleditor.part.GmlEditor.4" ) ) ); //$NON-NLS-1$
    }
    finally
    {
      monitor.done();
    }
  }

  private URL findContext( final IStorage storage )
  {
    final IPath fullPath = storage.getFullPath();
    if( fullPath == null )
      return null;

    IFile file;
    if( storage instanceof IFile )
      file = (IFile)storage;
    else
      file = ResourcesPlugin.getWorkspace().getRoot().getFile( fullPath );

    if( file == null )
      return null;

    try
    {
      return ResourceUtilities.createURL( file );
    }
    catch( final MalformedURLException | URIException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public synchronized void createPartControl( final Composite parent )
  {
    super.createPartControl( parent );

    m_viewer = new GmlTreeView( parent, KalypsoCorePlugin.getDefault().getSelectionManager() );
    final GridData layoutData = new GridData();
    layoutData.grabExcessHorizontalSpace = true;
    layoutData.grabExcessVerticalSpace = true;
    layoutData.horizontalAlignment = GridData.FILL;
    layoutData.verticalAlignment = GridData.FILL;
    m_viewer.getTreeViewer().getControl().setLayoutData( layoutData );

    // register as site selection provider
    final IWorkbenchPartSite site = getSite();
    site.setSelectionProvider( m_viewer );

    setSourceProvider( new GmltreeSourceProvider( site, m_viewer ) );

    createContextMenu();
  }

  protected void createContextMenu( )
  {
    // create context menu for editor
    final MenuManager menuManager = new MenuManager();
    menuManager.setRemoveAllWhenShown( true );
    menuManager.addMenuListener( new IMenuListener()
    {
      @Override
      public void menuAboutToShow( final IMenuManager manager )
      {
        handleMenuAboutToShow( manager );
      }
    } );

    final TreeViewer treeViewer = m_viewer.getTreeViewer();
    final Menu menu = menuManager.createContextMenu( treeViewer.getControl() );
    treeViewer.getControl().setMenu( menu );

    registerContextMenu( menuManager );
  }

  protected void registerContextMenu( final MenuManager menuManager )
  {
    final IWorkbenchPartSite site = getSite();
    if( site instanceof IEditorSite )
      ((IEditorSite)site).registerContextMenu( menuManager, m_viewer, false );
    else
      site.registerContextMenu( menuManager, m_viewer );
  }

  /**
   * Add some special actions to the menuManager, dependent on the current selection.
   */
  protected void handleMenuAboutToShow( final IMenuManager manager )
  {
    final IStructuredSelection selection = m_viewer.getSelection();
    final IFeatureSelectionManager selectionManager = m_viewer.getSelectionManager();

    final CommandableWorkspace workspace = m_viewer.getWorkspace();
    final INewScope scope = NewScopeFactory.createFromTreeSelection( workspace, selection, selectionManager );
    if( scope != null )
      manager.add( scope.createMenu() );

    // add additions seperator: if not, eclipse whines
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == IPostSelectionProvider.class )
      return m_viewer;

    if( adapter == ISelectionProvider.class )
      return m_viewer;

    if( adapter == ModellEventProvider.class )
      return m_viewer;

    return super.getAdapter( adapter );
  }

  protected GmlTreeView getViewer( )
  {
    return m_viewer;
  }

  @Override
  public void setFocus( )
  {
    m_viewer.getTreeViewer().getControl().setFocus();
  }

  @Override
  public boolean isDirty( )
  {
    return super.isDirty() || m_viewer.isDirty();
  }
}