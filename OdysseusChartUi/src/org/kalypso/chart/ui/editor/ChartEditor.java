package org.kalypso.chart.ui.editor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.chart.ui.i18n.Messages;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartConfigurationSaver;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;

/**
 * @author Gernot Belger
 * @author alibu
 */
public class ChartEditor extends EditorPart
{
  private final ChartPartComposite m_chartPartComposite = new ChartPartComposite( this );

  private boolean m_dirty = false;

  @Override
  public void init( final IEditorSite site, final IEditorInput input ) throws PartInitException
  {
    if( !(input instanceof IStorageEditorInput) )
      throw new PartInitException( "Invalid Input: Must be IStorageEditorInput" ); //$NON-NLS-1$

    m_chartPartComposite.init( site );

    setSite( site );

    setInput( input );
  }

  @Override
  protected void setInput( final IEditorInput input )
  {
    super.setInput( input );

    m_chartPartComposite.loadInput( input );
  }

  @Override
  public void dispose( )
  {
    m_chartPartComposite.dispose();

    super.dispose();
  }

  @Override
  public void doSave( final IProgressMonitor monitor )
  {
    // save only possible when input is a file
    final IEditorInput editorInput = getEditorInput();
    if( !(editorInput instanceof FileEditorInput) )
    {
      // given user a chance to use save-as
      // TODO: sehr witzig! Save as geht n�mlich nicht....
      MessageDialog.openInformation( getSite().getShell(), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.2" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.3" ) + "" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + "" + "" ); //$NON-NLS-1$ //$NON-NLS-2$

      return;
    }

    try
    {
      final IFileEditorInput input = (IFileEditorInput) editorInput;
      final IFile file = input.getFile();

      final XmlOptions options = ChartConfigurationLoader.configureXmlOptions( file.getCharset() );

      final IChartModel model = m_chartPartComposite.getChartModel();

      final ChartConfigurationDocument savedConfig = ChartConfigurationSaver.createChartConfiguration( model );

      final InputStream is = savedConfig.newInputStream( options );
      file.setContents( is, false, true, monitor );
      is.close();
      // System.out.println( savedConfig.toString() );

      setDirty( false );
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getEditorSite().getShell(), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.4" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.5" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch( final IOException e )
    {
      final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, e.toString(), e );
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getEditorSite().getShell(), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.4" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.5" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Override
  public void doSaveAs( )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDirty( )
  {
    return m_dirty;
  }

  @Override
  public boolean isSaveAsAllowed( )
  {
    return false;
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    m_chartPartComposite.createControl( parent );
  }

  @Override
  public void setFocus( )
  {
    m_chartPartComposite.setFocus();
  }

  @Override
  public void setPartName( final String partName )
  {
    super.setPartName( partName );
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
      return m_chartPartComposite.getOutlinePage();

    if( IChartComposite.class.equals( adapter ) )
      return m_chartPartComposite.getChartComposite();

    if( IChartPart.class.equals( adapter ) )
      return m_chartPartComposite;

    return super.getAdapter( adapter );
  }

  protected void setDirty( final boolean dirty )
  {
    if( m_dirty == dirty )
      return;

    m_dirty = dirty;
    firePropertyChange( PROP_DIRTY );
  }
}