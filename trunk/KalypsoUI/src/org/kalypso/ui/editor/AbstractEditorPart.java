/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * TODO: why not directly inherit from {@link org.eclipse.ui.part.EditorPart}?
 * 
 * @author bce
 */
public abstract class AbstractEditorPart extends WorkbenchPart implements IResourceChangeListener, ICommandTarget
{
  /**
   * Editor input, or <code>null</code> if none.
   */
  private IStorageEditorInput m_editorInput = null;

  private final Runnable m_dirtyRunnable = new Runnable()
  {
    public void run( )
    {
      getSite().getShell().getDisplay().asyncExec( new Runnable()
      {
        public void run( )
        {
          fireDirty();
        }
      } );
    }
  };

  private final JobExclusiveCommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), m_dirtyRunnable );

  /**
   * This flag prevents reload on save.
   */
  private boolean m_isSaving = false;

  public AbstractEditorPart( )
  {
    ResourcesPlugin.getWorkspace().addResourceChangeListener( this );
  }

  public IStorageEditorInput getEditorInput( )
  {
    return m_editorInput;
  }

  public IEditorSite getEditorSite( )
  {
    return (IEditorSite) getSite();
  }

  /**
   * Returns whether the contents of this editor should be saved when the editor is closed. <br/>
   * This method returns <code>true</code> if and only if the editor is dirty (<code>isDirty</code>).
   */
  public boolean isSaveOnCloseNeeded( )
  {
    return isDirty();
  }

  @Override
  public void dispose( )
  {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );

    m_commandTarget.dispose();
    super.dispose();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  public final void doSave( final IProgressMonitor monitor )
  {
    final IEditorInput eInput = getEditorInput();

    if( !(eInput instanceof FileEditorInput) )
    {
      // If input is not a file, allow user to save as
      doSaveAs();
      return;
    }

    final IFileEditorInput input = (IFileEditorInput) eInput;
    try
    {
      m_isSaving = true;
      doSaveInternal( monitor, input );
      m_commandTarget.resetDirty();
      fireDirty();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      ErrorDialog.openError( getSite().getShell(), Messages.getString( "org.kalypso.ui.editor.AbstractEditorPart.5" ), Messages.getString( "org.kalypso.ui.editor.AbstractEditorPart.6" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    finally
    {
      m_isSaving = false;
    }
  }

  protected abstract void doSaveInternal( final IProgressMonitor monitor, final IFileEditorInput input ) throws CoreException;

  protected IActionBars getActionBars( final IWorkbenchPartSite site )
  {
    final IActionBars actionBars;
    if( site instanceof IViewSite )
    {
      actionBars = ((IViewSite) site).getActionBars();
    }
    else
    {
      actionBars = ((IEditorSite) site).getActionBars();
    }
    return actionBars;
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isDirty()
   */
  public boolean isDirty( )
  {
    return (!(getEditorInput() instanceof IFileEditorInput)) || m_commandTarget.isDirty();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#doSaveAs()
   */
  public void doSaveAs( )
  {
    final Shell shell = getSite().getShell();
    final IStorageEditorInput input = getEditorInput();

    final SaveAsDialog dialog = new SaveAsDialog( shell );

    final IFile currentFile = getSaveAsFile( input );
    if( currentFile == null )
      dialog.setOriginalName( getTitle() );
    else
      dialog.setOriginalFile( currentFile );

    dialog.open();
    final IPath filePath = dialog.getResult();
    if( filePath == null )
      return;

    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IFile file = workspace.getRoot().getFile( filePath );
    final JobExclusiveCommandTarget commandTarget = m_commandTarget;
    final Job job = new Job( Messages.getString("org.kalypso.ui.editor..AbstractEditorPart.0") + filePath.toPortableString() ) //$NON-NLS-1$
    {
      @SuppressWarnings("synthetic-access")
      // ignore: we do not want to make it more public
      @Override
      public IStatus run( final IProgressMonitor monitor )
      {
        try
        {
          monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.AbstractEditorPart.7" ), 1000 ); //$NON-NLS-1$
          final IFileEditorInput newInput = new FileEditorInput( file );
          doSaveInternal( new SubProgressMonitor( monitor, 1000 ), newInput );
          commandTarget.resetDirty();
          setInput( newInput );

          monitor.done();
          return Status.OK_STATUS;
        }
        catch( final CoreException e )
        {
          e.printStackTrace();
          return e.getStatus();
        }
      }
    };
    job.setUser( true );
    job.setPriority( Job.LONG );
    job.schedule();
  }

  private IFile getSaveAsFile( final IStorageEditorInput input )
  {
    try
    {
      if( input instanceof FileEditorInput )
        ((IFileEditorInput) input).getFile();

      final IStorage storage = input.getStorage();
      final IPath fullPath = storage.getFullPath();

      final IWorkspace workspace = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot root = workspace.getRoot();

      if( fullPath == null )
        return null;

      return root.getFile( fullPath );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

    return null;
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
   */
  public void init( final IEditorSite site, final IEditorInput input )
  {
    setSite( site );

    if( !(input instanceof IStorageEditorInput) )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ui.editor.AbstractEditorPart.10" ) ); //$NON-NLS-1$
    setInput( (IStorageEditorInput) input );
    load();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  public boolean isSaveAsAllowed( )
  {
    return true;
  }

  private final void setInput( final IStorageEditorInput input )
  {
    m_editorInput = input;

    getSite().getShell().getDisplay().syncExec( new Runnable()
    {
      @SuppressWarnings("synthetic-access")
      public void run( )
      {
        setPartName( input.getName() );
        firePropertyChange( IWorkbenchPart.PROP_TITLE );
      }
    } );
  }

  protected final void load( )
  {
    final IStorageEditorInput input = getEditorInput();
    try
    {
      loadInternal( new NullProgressMonitor(), input );
      fireDirty();
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), getPartName(), Messages.getString( "org.kalypso.ui.editor.AbstractEditorPart.11" ), status ); //$NON-NLS-1$
    }

    m_commandTarget.resetDirty();
  }

  protected abstract void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws Exception, CoreException;

  /**
   * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
   */
  public void resourceChanged( final IResourceChangeEvent event )
  {
    if( !(getEditorInput() instanceof IFileEditorInput) )
      return;

    final IFileEditorInput input = (IFileEditorInput) getEditorInput();

    if( event.getType() == IResourceChangeEvent.POST_CHANGE && input != null )
    {
      final IResourceDelta delta = event.getDelta().findMember( input.getFile().getFullPath() );
      if( delta != null && delta.getKind() == IResourceDelta.CHANGED )
      {
        // if its only a marker change, do not reload
        if( delta.getFlags() != IResourceDelta.MARKERS )

          // TODO: ask user?
          // if( !m_isSaving
          // && MessageDialog.openQuestion( getSite().getShell(),
          // "FeatureEditor",
          // "Die Vorlagendatei hat sich geändert. Neu laden?" ) )
          if( !m_isSaving )
            load();
      }
    }
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    final IActionBars actionBars = getActionBars( getSite() );
    actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(), m_commandTarget.undoAction );
    actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(), m_commandTarget.redoAction );

    actionBars.updateActionBars();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    // nix
  }

  public void fireDirty( )
  {
    final UIJob job = new UIJob( Messages.getString("org.kalypso.ui.editor..AbstractEditorPart.1") ) //$NON-NLS-1$
    {
      @SuppressWarnings("synthetic-access")
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        firePropertyChange( IWorkbenchPartConstants.PROP_DIRTY );
        return Status.OK_STATUS;
      }
    };
    job.schedule();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#getTitleToolTip()
   */
  @Override
  public String getTitleToolTip( )
  {
    if( m_editorInput == null )
      return super.getTitleToolTip();

    return m_editorInput.getToolTipText();
  }

  /**
   * @see org.kalypso.commons.command.ICommandTarget#postCommand(org.kalypso.commons.command.ICommand,
   *      java.lang.Runnable)
   */
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  public final JobExclusiveCommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == ICommandTarget.class )
      return m_commandTarget;

    return super.getAdapter( adapter );
  }
}