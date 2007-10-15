package org.kalypso.chart.ui.editor;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
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
import org.kalypso.chart.factory.configuration.ChartConfigurationLoader;
import org.kalypso.chart.factory.configuration.ChartConfigurationSaver;
import org.kalypso.chart.factory.configuration.ChartFactory;
import org.kalypso.chart.factory.configuration.exception.ConfigurationException;
import org.kalypso.chart.framework.model.IChartModel;
import org.kalypso.chart.framework.model.event.impl.AbstractChartModelEventListener;
import org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import org.kalypso.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import org.kalypso.chart.framework.model.impl.ChartModel;
import org.kalypso.chart.framework.model.layer.IChartLayer;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IMapper;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.chart.framework.util.ChartUtilities;
import org.kalypso.chart.framework.view.ChartComposite;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.chart.ui.editor.actions.ChartActionsEnum;
import org.kalypso.chart.ui.editor.actions.ChartStandardActions;
import org.kalypso.chart.ui.editor.mouse.DragPanHandler;
import org.kalypso.chart.ui.editor.mouse.DragZoomHandler;
import org.kalypso.chart.ui.editor.mouse.EditHandler;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.ksp.chart.factory.AxisType;
import org.ksp.chart.factory.ChartConfigurationDocument;
import org.ksp.chart.factory.ChartType;

/**
 * @author Gernot Belger
 * @author alibu
 */
public class ChartEditor extends EditorPart
{
  private Composite m_composite = null;

  private ChartConfigurationDocument m_configuration = null;

  private IChartModel m_chartModel = null;

  private ChartType m_chartType = null;

  private ChartConfigurationLoader m_chartConfigurationLoader = null;

  private ChartEditorOutlinePage m_outlinePage = null;

// private boolean m_isSaving;

  private boolean m_dirty = false;

  private ChartComposite m_chartComposite = null;

  private final ChartStandardActions m_actions = null;

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

    try
    {
      if( input instanceof IFileEditorInput )
      {
        final IStorage storage = ((IStorageEditorInput) input).getStorage();

        m_chartConfigurationLoader = new ChartConfigurationLoader( storage );
        m_configuration = m_chartConfigurationLoader.getChartConfigurationDocument();
        final ChartType[] charts = m_chartConfigurationLoader.getCharts();
        m_chartType = charts[0];
      }
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getSite().getShell(), "Load input", "Failed to load editor input", status );
    }

    updateControl();

    m_dirty = false;
    firePropertyChange( PROP_DIRTY );
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
// m_isSaving = true;

      final ChartConfigurationDocument config = m_configuration;

      XmlOptions options = new XmlOptions();
      // Ausgabe menschenlesbar formatieren
      // Namespace prefixes setzen
      final Map<String, String> prefixes = (new HashMap<String, String>());
      prefixes.put( "http://www.ksp.org/chart/factory", "" );
      options = options.setSaveSuggestedPrefixes( prefixes );
      options = options.setSavePrettyPrint();
      final ChartConfigurationDocument savedConfig = ChartConfigurationSaver.createChartConfiguration( getChartComposite().getModel() );
      final XmlOptions xmloptions = options;

      final SetContentHelper helper = new SetContentHelper()
      {
        @Override
        protected void write( final OutputStreamWriter writer ) throws Throwable
        {
          savedConfig.save( writer, xmloptions );
        }
      };

      helper.setFileContents( input.getFile(), false, true, monitor );

      m_dirty = false;
      firePropertyChange( PROP_DIRTY );
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      KalypsoChartUiPlugin.getDefault().getLog().log( status );
      ErrorDialog.openError( getEditorSite().getShell(), "Fehler", "Fehler beim Speichern der Ansicht", status );
    }
    finally
    {
// m_isSaving = false;
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
    return m_dirty;
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

    final boolean isDirty = m_dirty;

    updateControl();

    /*
     * Update controle sets dirty to true, so we reset it here, in order to have a non dirty editor when opened.
     */
    m_dirty = isDirty;
    firePropertyChange( PROP_DIRTY );
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

    if( m_chartModel == null )
    {

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

          m_chartModel = new ChartModel();

          AbstractMapperRegistryEventListener mapperRegistryListener = new AbstractMapperRegistryEventListener()
          {
            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractMapperRegistryEventListener#onMapperRangeChanged(org.kalypso.chart.framework.model.mapper.IMapper)
             */
            @Override
            public void onMapperRangeChanged( IMapper mapper )
            {
              m_dirty = true;
              firePropertyChange( PROP_DIRTY );
            }
          };
          m_chartModel.getMapperRegistry().addListener( mapperRegistryListener );

          AbstractLayerManagerEventListener layerManagerListener = new AbstractLayerManagerEventListener()
          {
            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
             */
            @Override
            public void onLayerVisibilityChanged( IChartLayer layer )
            {
              m_dirty = true;
              firePropertyChange( PROP_DIRTY );
            }

            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerMoved(org.kalypso.chart.framework.model.layer.IChartLayer)
             */
            @Override
            public void onLayerMoved( IChartLayer layer )
            {
              m_dirty = true;
              firePropertyChange( PROP_DIRTY );
            }
          };
          m_chartModel.getLayerManager().addListener( layerManagerListener );

          AbstractChartModelEventListener chartModelEventListener = new AbstractChartModelEventListener()
          {
            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractChartModelEventListener#onModelChanged()
             */
            @Override
            public void onModelChanged( )
            {
              m_dirty = true;
              firePropertyChange( PROP_DIRTY );
            }
          };
          m_chartModel.addListener( chartModelEventListener );

          try
          {
            m_chartConfigurationLoader = new ChartConfigurationLoader( file );
            ChartFactory.configureChartModel( m_chartModel, m_chartConfigurationLoader, m_chartType.getId(), context );
          }
          // TODO: provide message to user instead of eating the exceptions.
          catch( final ConfigurationException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          catch( final CoreException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          catch( final XmlException e )
          {
            // TODO -generated catch block
            e.printStackTrace();
          }
          catch( final IOException e )
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          if( m_chartModel != null )
          {
            m_chartComposite = new ChartComposite( m_composite, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );
            DragZoomHandler dragHandler = new DragZoomHandler( m_chartComposite );
            m_chartComposite.getPlot().addMouseListener( dragHandler );
            m_chartComposite.getPlot().addMouseMoveListener( dragHandler );
            DragPanHandler dragHandler2 = new DragPanHandler( m_chartComposite );
            m_chartComposite.getPlot().addMouseListener( dragHandler2 );
            m_chartComposite.getPlot().addMouseMoveListener( dragHandler2 );

            /**
             * edit stuff
             */
            EditHandler eh = new EditHandler( m_chartComposite );
            m_chartComposite.getPlot().addMouseListener( eh );
            m_chartComposite.getPlot().addMouseMoveListener( eh );

            // Wenn die Achsenintervalle nicht in der Konfigurationsdatei gesetzt sind, muss ge-autorange-t werden
            AxisType[] axisArray = m_configuration.getChartConfiguration().getAxisArray();
            List<IAxis> autoscaledAxes = new ArrayList<IAxis>();
            IMapperRegistry mapperRegistry = m_chartModel.getMapperRegistry();
            for( AxisType axisType : axisArray )
            {
              String min = axisType.getMinVal();
              String max = axisType.getMaxVal();
              if( min.trim().equals( "" ) || max.trim().equals( "" ) )
                autoscaledAxes.add( mapperRegistry.getAxis( axisType.getId() ) );
            }
            m_chartModel.autoscale( autoscaledAxes.toArray( new IAxis[] {} ) );

            setPartName( m_chartModel.getTitle() );
          }
          // else: TODO: what?
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
      m_outlinePage.updateControl();

  }

  public ChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }

  public ChartType getChartType( )
  {
    return m_chartType;
  }

  public ChartConfigurationLoader getChartConfigurationLoader( )
  {
    return m_chartConfigurationLoader;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @SuppressWarnings("unchecked")
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

  public void drawLegendIcon( final String title, final Image img )
  {
    final IChartLayer[] layers = m_chartModel.getLayerManager().getLayers();
    for( final IChartLayer layer : layers )
    {
      // TODO: change this to get id!
      if( layer.getTitle().equals( title ) )
      {
        final Rectangle bounds = img.getBounds();
        layer.drawIcon( img );
        break;
      }
    }
  }

  public ChartConfigurationDocument getConfiguration( )
  {
    return m_configuration;
  }

  public void setConfiguration( ChartConfigurationDocument doc )
  {
    m_configuration = doc;
  }

  public void runChartAction( final ChartActionsEnum chartAction )
  {
    if( m_chartComposite != null && !m_chartComposite.isDisposed() )
    {
      switch( chartAction )
      {
        case MAXIMIZE:
          ChartUtilities.maximize( m_chartComposite.getModel() );
          break;
        case ZOOM_IN:
          m_actions.getAction( ChartStandardActions.Action.ZOOM_IN ).setChecked( true );
          break;
        case ZOOM_OUT:
          m_actions.getAction( ChartStandardActions.Action.ZOOM_OUT ).setChecked( true );
          break;
        case PAN:
          m_actions.getAction( ChartStandardActions.Action.PAN ).setChecked( true );
          break;

        case EDIT:
        {
          final IAction editAction = m_actions.getAction( ChartStandardActions.Action.EDIT );
          editAction.setChecked( !editAction.isChecked() );
        }
          break;

        case EXPORT_IMAGE:
          saveChartAsImage( m_chartComposite );
          break;

      }
    }
  }

  private static void saveChartAsImage( ChartComposite chart )
  {
    // TODO Auto-generated method stub

  }
}
