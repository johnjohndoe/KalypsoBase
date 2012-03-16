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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.IStorageWithContext;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.ui.partlistener.PartAdapter2;
import org.kalypso.i18n.Messages;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * Abstract implementation of {@link WorkbenchPart}, impolementing some common behaviour of Kalypso parts.
 */
public abstract class AbstractWorkbenchPart extends WorkbenchPart implements IResourceChangeListener, ICommandTarget
{
  /**
   * Editor input, or <code>null</code> if none.
   */
  private IStorageEditorInput m_editorInput = null;

  private final Runnable m_dirtyRunnable = new Runnable()
  {
    @Override
    public void run( )
    {
      getSite().getShell().getDisplay().asyncExec( new Runnable()
      {
        @Override
        public void run( )
        {
          fireDirty();
        }
      } );
    }
  };

  private final PartAdapter2 m_partListener = new PartAdapter2()
  {
    @Override
    public void partActivated( final IWorkbenchPartReference partRef )
    {
      handlePartActivated( partRef );
    }
  };

  private final JobExclusiveCommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), m_dirtyRunnable );

  /**
   * This flag prevents reload on save.
   */
  private boolean m_isSaving = false;

  private ISourceProvider2 m_sourceProvider;

  public AbstractWorkbenchPart( )
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
    getSite().getPage().removePartListener( m_partListener );

    if( m_sourceProvider != null )
      m_sourceProvider.dispose();

    ResourcesPlugin.getWorkspace().removeResourceChangeListener( this );

    m_commandTarget.dispose();
    super.dispose();
  }

  @Override
  protected void setSite( final IWorkbenchPartSite site )
  {
    final IWorkbenchPartSite currentSite = getSite();
    if( currentSite != null )
      currentSite.getPage().addPartListener( m_partListener );

    super.setSite( site );

    if( site != null )
      site.getPage().addPartListener( m_partListener );
  }

  public void doSave( final IProgressMonitor monitor )
  {
    doSave( true, monitor );
  }

  /**
   * @param allowSaveAs
   *          If <code>false</code>, view will not show a 'save as' dialog if the file for saving the map cnanot be
   *          determined. If this case, the dirty map is selently disposed off.
   */
  public void doSave( final boolean allowSaveAs, final IProgressMonitor monitor )
  {
    final IEditorInput eInput = getEditorInput();
    if( eInput == null )
      return;

    final IFile file = findfile( eInput );
    if( file == null )
    {
      // If input is not a file, allow user to save as
      if( allowSaveAs )
        doSaveAs();
      return;
    }

    try
    {
      synchronized( this )
      {
        setSaving( true );
        doSaveInternal( monitor, file );
        m_commandTarget.resetDirty();
      }

      fireDirty();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      ErrorDialog.openError( getSite().getShell(), Messages.getString( "org.kalypso.ui.editor.AbstractWorkbenchPart.5" ), Messages.getString( "org.kalypso.ui.editor.AbstractWorkbenchPart.6" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    finally
    {
      setSaving( false );
    }
  }

  protected IFile findfile( final IEditorInput eInput )
  {
    if( eInput == null )
      return null;

    if( eInput instanceof IFileEditorInput )
    {
      final IFileEditorInput input = (IFileEditorInput) eInput;
      return input.getFile();
    }

    return null;
  }

  protected abstract void doSaveInternal( final IProgressMonitor monitor, final IFile file ) throws CoreException;

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
    // If the underlying file does not yet exists (i.e. template created from data file)
    // we need to be saved
    final IStorageEditorInput editorInput = getEditorInput();
    if( editorInput instanceof IFileEditorInput )
    {
      final IFile file = ((IFileEditorInput) editorInput).getFile();
      if( file == null || !file.exists() )
        return true;
    }

    return m_commandTarget.isDirty();
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

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.AbstractWorkbenchPart.7" ), 1000 ); //$NON-NLS-1$
        try
        {
          final IFileEditorInput newInput = new FileEditorInput( file );
          doSaveInternal( new SubProgressMonitor( monitor, 1000 ), newInput.getFile() );
          commandTarget.resetDirty();
          setSaving( true );
          setInput( newInput );

          return Status.OK_STATUS;
        }
        finally
        {
          setSaving( false );
          monitor.done();
        }
      }
    };

    // REMARK: we cannot use a job here, the method must block until finished. Else
    // The content may already be disposed before the doSaveInternal is called.
    final IProgressService progressService = (IProgressService) getSite().getService( IProgressService.class );
    RunnableContextHelper.execute( progressService, true, true, operation );
  }

  protected void setSaving( final boolean isSaving )
  {
    m_isSaving = isSaving;
  }

  private IFile getSaveAsFile( final IStorageEditorInput input )
  {
    if( input instanceof IFileEditorInput )
      return ((IFileEditorInput) input).getFile();

    return null;
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
   */
  public void init( final IEditorSite site, final IEditorInput input )
  {
    setSite( site );

    if( !(input instanceof IStorageEditorInput) )
      throw new IllegalArgumentException( "input must be instanceof IStorageEditorInput" ); //$NON-NLS-1$
    setInput( (IStorageEditorInput) input );
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  public boolean isSaveAsAllowed( )
  {
    return true;
  }

  public void setInput( final IStorageEditorInput inp )
  {
    final IStorageEditorInput input = tweakInput( inp );

    if( ObjectUtils.equals( input, m_editorInput ) )
      return;

    m_editorInput = input;

    getSite().getShell().getDisplay().syncExec( new Runnable()
    {
      @Override
      @SuppressWarnings("synthetic-access")
      public void run( )
      {
        firePropertyChange( IEditorPart.PROP_INPUT );
        if( input == null )
          return;

        setPartName( input.getName() );
        firePropertyChange( IWorkbenchPart.PROP_TITLE );
      }
    } );

    load( input );
  }

  /**
   * Allows implementors to tweak the given input.<br/>
   * Intended to be overwritten by implementors. Default implementation returns the given input unchanged.
   */
  protected IStorageEditorInput tweakInput( final IStorageEditorInput input )
  {
    return input;
  }

  protected final void load( final IStorageEditorInput input )
  {
    if( m_isSaving )
      return;

    try
    {
      loadInternal( new NullProgressMonitor(), input );
      fireDirty();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();

      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), getPartName(), Messages.getString( "org.kalypso.ui.editor.AbstractWorkbenchPart.11" ), status ); //$NON-NLS-1$
    }

    m_commandTarget.resetDirty();
  }

  protected abstract void loadInternal( final IProgressMonitor monitor, final IStorageEditorInput input ) throws CoreException;

  @Override
  public void resourceChanged( final IResourceChangeEvent event )
  {
    if( m_isSaving )
      return;

    final IStorageEditorInput editorInput = getEditorInput();
    if( !(editorInput instanceof IFileEditorInput) )
      return;

    if( event.getType() != IResourceChangeEvent.POST_CHANGE )
      return;

    final IFile file = ((IFileEditorInput) editorInput).getFile();
    if( file == null )
      return;

    final IResourceDelta rootDelta = event.getDelta();
    final IResourceDelta fileDelta = rootDelta.findMember( file.getFullPath() );
    if( fileDelta == null )
      return;

    if( fileDelta.getKind() == IResourceDelta.CHANGED )
    {
      if( (fileDelta.getFlags() & IResourceDelta.CONTENT) != 0 )
        load( editorInput );
    }
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final IActionBars actionBars = getActionBars( getSite() );
    actionBars.setGlobalActionHandler( ActionFactory.UNDO.getId(), m_commandTarget.undoAction );
    actionBars.setGlobalActionHandler( ActionFactory.REDO.getId(), m_commandTarget.redoAction );

    actionBars.updateActionBars();
  }

  @Override
  public void setFocus( )
  {
    // nix
  }

  public void fireDirty( )
  {
    final UIJob job = new UIJob( "Fire Dirty" ) //$NON-NLS-1$
    {
      @SuppressWarnings("synthetic-access")
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        firePropertyChange( IWorkbenchPartConstants.PROP_DIRTY );
        return Status.OK_STATUS;
      }
    };
    job.setSystem( true );
    job.schedule();
  }

  /**
   * Made public in order to allow clients to set the title of this part.
   */
  @Override
  public void setPartName( final String partName )
  {
    // TODO Auto-generated method stub
    super.setPartName( partName );
  }

  /**
   * Made public in order to allow clients to set tooltip.
   */
  @Override
  public void setTitleToolTip( final String toolTip )
  {
    super.setTitleToolTip( toolTip );
  }

  @Override
  public String getTitleToolTip( )
  {
    final String titleToolTip = super.getTitleToolTip();
    if( !StringUtils.isBlank( titleToolTip ) )
      return titleToolTip;

    if( m_editorInput == null )
      return super.getTitleToolTip();

    return m_editorInput.getToolTipText();
  }

  @Override
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  public final JobExclusiveCommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == ICommandTarget.class )
      return m_commandTarget;

    return super.getAdapter( adapter );
  }

  public void setSourceProvider( final ISourceProvider2 sourceProvider )
  {
    m_sourceProvider = sourceProvider;
  }

  /**
   * We need to fire a source change event, in order to tell the map context which panel is the currently active one.
   */
  protected void handlePartActivated( final IWorkbenchPartReference partRef )
  {
    if( m_sourceProvider == null )
      return;

    final IWorkbenchPart part = partRef.getPart( false );
    if( part == this )
      m_sourceProvider.fireSourceChanged();
  }

  public static URL findContext( final IStorageEditorInput input ) throws MalformedURLException, CoreException
  {
    if( input == null )
      return null;

    final IStorage storage = input.getStorage();
    if( storage == null )
      return null;

    if( storage instanceof IStorageWithContext )
      return ((IStorageWithContext) storage).getContext();

    if( storage instanceof IResource )
      return ResourceUtilities.createURL( (IResource) storage );

    final IFile file = (IFile) storage.getAdapter( IFile.class );
    if( file == null )
      return null;

    return ResourceUtilities.createQuietURL( file );
  }
}