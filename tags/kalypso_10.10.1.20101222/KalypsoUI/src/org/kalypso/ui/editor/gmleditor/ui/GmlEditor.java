package org.kalypso.ui.editor.gmleditor.ui;

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

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IEncodedStorage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.editor.AbstractEditorPart;
import org.kalypso.ui.editor.actions.NewFeatureScope;
import org.kalypso.ui.editorLauncher.GmlEditorTemplateLauncher;
import org.kalypsodeegree.model.feature.event.ModellEventProvider;

/**
 * @author Küpferle
 */
public class GmlEditor extends AbstractEditorPart implements IEditorPart, ICommandTarget
{
  public static final String EXTENSIN_GMV = ".gmv"; //$NON-NLS-1$

  public static final String ID = "org.kalypso.ui.editor.GmlEditor"; //$NON-NLS-1$

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

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#tweakInput(org.eclipse.ui.IStorageEditorInput)
   */
  @Override
  protected IStorageEditorInput tweakInput( final IStorageEditorInput input )
  {
    if( input instanceof IFileEditorInput )
    {
      final IFile file = ((IFileEditorInput) input).getFile();
      final String ext = file.getFileExtension();
      if( "gml".equalsIgnoreCase( ext ) || "shp".equalsIgnoreCase( ext ) )
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

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#doSaveInternal(org.eclipse.core.runtime.IProgressMonitor,
   *      org.eclipse.core.resources.IFile)
   */
  @Override
  protected void doSaveInternal( final IProgressMonitor monitor, final IFile file ) throws CoreException
  {
    ByteArrayInputStream bis = null;
    OutputStreamWriter writer = null;

    try
    {
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      writer = new OutputStreamWriter( bos );
      m_viewer.saveInput( writer, monitor );
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

  public GmlTreeView getTreeView( )
  {
    return m_viewer;
  }

  @Override
  protected void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws Exception
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.gmleditor.ui.GmlEditor.1" ), 1000 ); //$NON-NLS-1$
    try
    {
      final IStorage storage = input.getStorage();

      final Reader r;
      final InputStream contents = storage.getContents();
      if( storage instanceof IEncodedStorage )
        r = new InputStreamReader( contents, ((IEncodedStorage) storage).getCharset() );
      else
        r = new InputStreamReader( contents );

      final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( storage.getFullPath() );
      final URL context = ResourceUtilities.createURL( file );

      getEditorSite().getShell().getDisplay().asyncExec( new Runnable()
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
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.gmleditor.ui.GmlEditor.2" ) ) ); //$NON-NLS-1$
    }
    catch( final UnsupportedEncodingException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.gmleditor.ui.GmlEditor.3" ) ) ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ui.editor.gmleditor.ui.GmlEditor.4" ) ) ); //$NON-NLS-1$
    }
    finally
    {
      monitor.done();
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
    getSite().setSelectionProvider( m_viewer );

    createContextMenu();
  }

  private void createContextMenu( )
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
    ((IEditorSite) getSite()).registerContextMenu( menuManager, m_viewer, false );
    treeViewer.getControl().setMenu( menu );
  }

  /**
   * Add some special actions to the menuManager, dependent on the current selection.
   */
  public void handleMenuAboutToShow( final IMenuManager manager )
  {
    final IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
    final IFeatureSelectionManager selectionManager = m_viewer.getSelectionManager();

    final CommandableWorkspace workspace = m_viewer.getWorkspace();
    final NewFeatureScope scope = NewFeatureScope.createFromTreeSelection( workspace, selection, selectionManager );

    if( scope != null )
      manager.add( scope.createMenu() );

    // add additions seperator: if not, eclipse whines
    manager.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == IPostSelectionProvider.class )
      return m_viewer;

    if( adapter == ISelectionProvider.class )
      return m_viewer;

    if( adapter == ModellEventProvider.class )
      return m_viewer;

    return super.getAdapter( adapter );
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    m_viewer.getTreeViewer().getControl().setFocus();
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#isDirty()
   */
  @Override
  public boolean isDirty( )
  {
    return super.isDirty() || m_viewer.isDirty();
  }
}