package org.kalypso.chart.ui.editor;

import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.chart.layer.IChartLayer;
import org.kalypso.swtchart.configuration.ChartLoader;
import org.kalypso.swtchart.configuration.ConfigLoader;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.ConfigurationType;

/**
 * @author Gernot Belger
 */
public class ChartEditor extends EditorPart
{
  private Composite m_composite = null;

  private ConfigurationType m_configuration = null;

  private Chart m_chart = null;

  private ChartType m_chartType = null;

  private ChartEditorOutlinePage m_outlinePage = null;

  private boolean m_isSaving;

  /**
   * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
   */
  @Override
  public void init( final IEditorSite site, final IEditorInput input ) throws PartInitException
  {
    if( !(input instanceof IStorageEditorInput) )
      throw new PartInitException( "Invalid Input: Must be IStorageEditorInput" );

    setSite( site );
    setInput( input );
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
   */
  @Override
  protected void setInput( final IEditorInput input )
  {
    super.setInput( input );

    // TODO: release old input and control

    // prepare for exception
    m_chartType = null;
    m_configuration = null;

    InputStream is = null;
    try
    {
      if( input instanceof IFileEditorInput )
        is = ((IStorageEditorInput) input).getStorage().getContents();

      if( is == null )
        return;

      final Unmarshaller unmarshaller = ConfigLoader.JC.createUnmarshaller();

      final Object o = unmarshaller.unmarshal( is );

      final ConfigurationType config;
      if( o instanceof JAXBElement )
        config = (ConfigurationType) ((JAXBElement) o).getValue();
      else if( o instanceof ChartType )
        config = (ConfigurationType) o;
      else
        config = null;

      m_chartType = findCurrentChart( config );
      m_configuration = config;
    }
    catch( final JAXBException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), "Load input", "Failed to load editr input", status );
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), "Load input", "Failed to load editr input", status );
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }

    updateControl();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_outlinePage != null )
    {
      m_outlinePage.dispose();
      m_outlinePage = null;
    }

    super.dispose();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void doSave( final IProgressMonitor monitor )
  {
    // save only possible when input is a file
    if( !(getEditorInput() instanceof FileEditorInput) )
    {
      // given user a chance to use save-as
      MessageDialog.openInformation( getSite().getShell(), "Speichern", "Der Inhalt kann nicht direkt gespeichert werden weil noch\n" + "keine grundliegende Vorlagedatei vorhanden ist. Es handelt\n"
          + "sich möglicherweise um eine 'virtuelle' Vorlage\n" + "Bitte benutzen Sie das 'Speichern als' Kommando." );

      return;
    }

    final IFileEditorInput input = (IFileEditorInput) getEditorInput();
    if( input == null )
      return; // should never happen

    try
    {
      m_isSaving = true;
      
      final ConfigurationType config = m_configuration;
      SetContentHelper helper = new SetContentHelper()
      {
        @Override
        protected void write( final OutputStreamWriter writer ) throws Throwable
        {
          ConfigLoader.writeConfig( config, writer );
        }
      };
      
      helper.setFileContents( input.getFile(), false, true, monitor );

      firePropertyChange( PROP_DIRTY );
    }
    catch( CoreException e )
    {
      e.printStackTrace();

      ErrorDialog.openError( getEditorSite().getShell(), "Fehler", "Fehler beim Speichern der Ansicht", e.getStatus() );
    }
    finally
    {
      m_isSaving = false;
    }
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#doSaveAs()
   */
  @Override
  public void doSaveAs( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isDirty()
   */
  @Override
  public boolean isDirty( )
  {
    return true;
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  @Override
  public boolean isSaveAsAllowed( )
  {
    return false;
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    m_composite = new Composite( parent, SWT.NONE );
    m_composite.setLayout( new FillLayout() );

    updateControl();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    m_composite.setFocus();
  }

  /**
   * Totally refreshes the control besed on the contents of {@link m_config.}
   */
  public void updateControl( )
  {
    if( m_composite == null || m_composite.isDisposed() )
      return;

    m_chart = null;

    /* Reset controls */
    final Control[] children = m_composite.getChildren();
    for( final Control control : children )
      control.dispose();

    if( m_chartType == null )
    {
      final Label label = new Label( m_composite, SWT.NONE );
      label.setText( "No chart set" );
    }
    else
    {
      /* Create chart */
      final IEditorInput editorInput = getEditorInput();
      final IFile file = (IFile) editorInput.getAdapter( IFile.class );
      try
      {
        final URL context = ResourceUtilities.createURL( file );
        m_chart = new Chart( m_composite, SWT.BORDER );
        ChartLoader.configureChart( m_chart, m_chartType, context );

        m_chart.setAutoscale( true );
      }
      catch( final MalformedURLException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

    m_composite.layout();

    if( m_outlinePage != null )
      m_outlinePage.updateControl();
  }

  private ChartType findCurrentChart( final ConfigurationType config )
  {
    if( config == null )
      return null;

    ChartType ct = null;

    final List<Object> chartOrLayerOrAxis = config.getChartOrLayerOrAxis();
    for( final Object o : chartOrLayerOrAxis )
    {
      if( o instanceof ChartType )
      {
        ct = (ChartType) o;
        break;
      }
    }
    return ct;
  }

  public Chart getChart( )
  {
    return m_chart;
  }

  public ChartType getChartType( )
  {
    return m_chartType;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( final Class adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
    {
      if( m_outlinePage == null )
        m_outlinePage = new ChartEditorOutlinePage( this );

      return m_outlinePage;
    }

    return super.getAdapter( adapter );
  }

  public void drawLegendIcon( final String name, final Image img )
  {
    final List<IChartLayer> layers = m_chart.getLayers();
    for( final IChartLayer layer : layers )
    {
      // TODO: change this to get id!
      if( layer.getName().equals( name ) )
      {
        final Rectangle bounds = img.getBounds();
        layer.drawIcon( img, bounds.width, bounds.height );
        break;
      }
    }
  }
}
