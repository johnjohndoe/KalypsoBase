/**
 * 
 */
package org.kalypso.chart.ui.editor;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
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
import org.eclipse.swt.SWT;
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
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.swtchart.chart.Chart;
import org.kalypso.swtchart.configuration.NewChartLoader;
import org.kalypso.swtchart.configuration.NewConfigLoader;
import org.ksp.chart.configuration.ChartType;
import org.ksp.chart.configuration.ConfigurationType;

/**
 * @author Gernot Belger
 */
public class ChartEditor extends EditorPart
{
  private ConfigurationType m_config;

  private Composite m_composite;

  private Chart m_chart;

  private ChartEditorOutlinePage m_outlinePage;

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
    m_config = null;

    InputStream is = null;
    try
    {
      if( input instanceof IFileEditorInput )
        is = ((IStorageEditorInput) input).getStorage().getContents();

      if( is == null )
        return;

      final Unmarshaller unmarshaller = NewConfigLoader.JC.createUnmarshaller();

      final Object o = unmarshaller.unmarshal( is );

      if( o instanceof JAXBElement )
        m_config = (ConfigurationType) ((JAXBElement) o).getValue();
      else if( o instanceof ChartType )
        m_config = (ConfigurationType) o;
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
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.ui.part.EditorPart#doSaveAs()
   */
  @Override
  public void doSaveAs( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isDirty()
   */
  @Override
  public boolean isDirty( )
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
   */
  @Override
  public boolean isSaveAsAllowed( )
  {
    return true;
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

  private void updateControl( )
  {
    if( m_composite == null || m_composite.isDisposed() )
      return;

    m_chart = null;

    /* Reset controls */
    final Control[] children = m_composite.getChildren();
    for( final Control control : children )
      control.dispose();

    final ChartType chartType = getCurrentChart();
    if( m_config == null )
    {
      /* No config set */
      final Label label = new Label( m_composite, SWT.NONE );
      label.setText( "No configuration set" );
    }
    else
    {
      if( chartType == null )
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
          NewChartLoader.configureChart( m_chart, chartType, context );

          m_chart.setAutoscale( true );
        }
        catch( final MalformedURLException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      }
    }

    m_composite.layout();

    if( m_outlinePage != null )
      m_outlinePage.setChart( m_chart );
  }

  private ChartType getCurrentChart( )
  {
    if( m_config == null )
      return null;
    
    
    ChartType ct=null;
    
    List<Object> chartOrLayerOrAxis = m_config.getChartOrLayerOrAxis();
    for( Object o: chartOrLayerOrAxis )
    {
      if (o instanceof ChartType)
      {
        ct=(ChartType) o;
        break;
      }
    }
    return ct;
  }

  public Chart getChart( )
  {
    return m_chart;
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
      {
        m_outlinePage = new ChartEditorOutlinePage( );
        m_outlinePage.setChart( m_chart );
      }

      return m_outlinePage;
    }

    return super.getAdapter( adapter );
  }
}
