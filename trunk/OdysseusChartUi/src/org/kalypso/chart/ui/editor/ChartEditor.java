package org.kalypso.chart.ui.editor;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
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
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.KalypsoChartUiPlugin;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.chart.ui.i18n.Messages;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

import de.openali.odysseus.chart.factory.config.ChartConfigurationLoader;
import de.openali.odysseus.chart.factory.config.ChartConfigurationSaver;
import de.openali.odysseus.chart.factory.config.ChartExtensionLoader;
import de.openali.odysseus.chart.factory.config.ChartFactory;
import de.openali.odysseus.chart.factory.config.IExtensionLoader;
import de.openali.odysseus.chart.factory.config.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractMapperRegistryEventListener;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.impl.ChartImageComposite;
import de.openali.odysseus.chartconfig.x020.AxisDateRangeType;
import de.openali.odysseus.chartconfig.x020.AxisDurationRangeType;
import de.openali.odysseus.chartconfig.x020.AxisNumberRangeType;
import de.openali.odysseus.chartconfig.x020.AxisStringRangeType;
import de.openali.odysseus.chartconfig.x020.AxisType;
import de.openali.odysseus.chartconfig.x020.ChartConfigurationDocument;
import de.openali.odysseus.chartconfig.x020.ChartType;

/**
 * @author Gernot Belger
 * @author alibu
 */
public class ChartEditor extends EditorPart implements IChartPart
{
  private Composite m_composite = null;

  private ChartConfigurationDocument m_configuration = null;

  private IChartModel m_chartModel = null;

  private ChartType m_chartType = null;

  private ChartConfigurationLoader m_chartConfigurationLoader = null;

  private ChartEditorTreeOutlinePage m_outlinePage = null;

  private boolean m_dirty = false;

  private IChartComposite m_chartComposite = null;

  private PlotDragHandlerDelegate m_plotDragHandler;

  private ChartPartListener m_chartPartListener;

  /**
   * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
   */
  @Override
  public void init( final IEditorSite site, final IEditorInput input ) throws PartInitException
  {
    if( !(input instanceof IStorageEditorInput) )
      throw new PartInitException( "Invalid Input: Must be IStorageEditorInput" ); //$NON-NLS-1$

    m_chartPartListener = new ChartPartListener( this, site );
    site.getPage().addPartListener( m_chartPartListener );

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
      ErrorDialog.openError( getSite().getShell(), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.0" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.1" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
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
    if( m_chartPartListener != null )
    {
      m_chartPartListener.dispose();
      getSite().getPage().removePartListener( m_chartPartListener );
      m_chartPartListener = null;
    }

    if( m_outlinePage != null )
    {
      m_outlinePage.dispose();
      m_outlinePage = null;
    }

    if( m_chartComposite != null )
    {
      m_chartComposite.getPlot().dispose();
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
    final IEditorInput editorInput = getEditorInput();
    if( !(editorInput instanceof FileEditorInput) )
    {
      // given user a chance to use save-as
      // TODO: sehr witzig! Save as geht nämlich nicht....
      MessageDialog.openInformation( getSite().getShell(), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.2" ), Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.3" ) + "" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
          + "" + "" ); //$NON-NLS-1$ //$NON-NLS-2$

      return;
    }
    setDirty( false );

    try
    {

      final IFileEditorInput input = (IFileEditorInput) editorInput;
      final IFile file = input.getFile();

      final XmlOptions options = ChartConfigurationLoader.configureXmlOptions( file.getCharset() );

      final ChartConfigurationDocument savedConfig = ChartConfigurationSaver.createChartConfiguration( getChartComposite().getChartModel() );

      final InputStream is = savedConfig.newInputStream( options );
      file.setContents( is, false, true, monitor );
      is.close();
      System.out.println( savedConfig.toString() );

      // setDirty( false );
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
    m_composite = new Composite( parent, SWT.FILL );
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
   * Totally refreshes the control based on the contents of {@link m_config.}
   */
  protected void updateControl( )
  {
    if( m_composite == null || m_composite.isDisposed() )
    {
      return;
    }

    if( m_chartModel == null )
    {
      /* Reset controls */
      final Control[] children = m_composite.getChildren();
      for( final Control control : children )
      {
        control.dispose();
      }

      if( m_chartType == null )
      {
        final Label label = new Label( m_composite, SWT.NONE );
        label.setText( Messages.getString( "org.kalypso.chart.ui.editor.ChartEditor.6" ) ); //$NON-NLS-1$
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

          final AbstractMapperRegistryEventListener mapperRegistryListener = new AbstractMapperRegistryEventListener()
          {
            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractMapperRegistryEventListener#onMapperRangeChanged(org.kalypso.chart.framework.model.mapper.IMapper)
             */
            @Override
            public void onMapperChanged( final IMapper mapper )
            {
              setDirty( true );
            }
          };
          m_chartModel.getMapperRegistry().addListener( mapperRegistryListener );

          final AbstractLayerManagerEventListener layerManagerListener = new AbstractLayerManagerEventListener()
          {
            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerVisibilityChanged(org.kalypso.chart.framework.model.layer.IChartLayer)
             */
            @Override
            public void onLayerVisibilityChanged( final IChartLayer layer )
            {
              setDirty( true );
            }

            /**
             * @see org.kalypso.chart.framework.model.event.impl.AbstractLayerManagerEventListener#onLayerMoved(org.kalypso.chart.framework.model.layer.IChartLayer)
             */
            @Override
            public void onLayerMoved( final IChartLayer layer )
            {
              setDirty( true );
            }
          };
          m_chartModel.getLayerManager().addListener( layerManagerListener );

          try
          {
            m_chartConfigurationLoader = new ChartConfigurationLoader( file );
            final IExtensionLoader cel = ChartExtensionLoader.getInstance();
            ChartFactory.configureChartModel( m_chartModel, m_chartConfigurationLoader, m_chartType.getId(), cel, context );
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
          final List<IAxis> autoscaledAxes = new ArrayList<IAxis>();
          if( m_chartModel != null )
          {
            m_chartComposite = new ChartImageComposite( m_composite, SWT.BORDER, m_chartModel, new RGB( 255, 255, 255 ) );

            // Wenn die Achsenintervalle nicht in der Konfigurationsdatei gesetzt sind, muss ge-autorange-t werden
            final AxisType[] axisArray = m_chartType.getMappers().getAxisArray();
            final IMapperRegistry mapperRegistry = m_chartModel.getMapperRegistry();
            for( final AxisType axisType : axisArray )
            {
              Object min = null;
              Object max = null;

              if( axisType.isSetDateRange() )
              {
                final AxisDateRangeType range = axisType.getDateRange();
                min = range.getMinValue();
                max = range.getMaxValue();
              }
              else if( axisType.isSetDurationRange() )
              {
                final AxisDurationRangeType range = axisType.getDurationRange();
                min = range.getMinValue();
                max = range.getMaxValue();
              }
              else if( axisType.isSetStringRange() )
              {
                final AxisStringRangeType range = axisType.getStringRange();
                min = range.getMinValue();
                max = range.getMaxValue();
              }
              else if( axisType.isSetNumberRange() )
              {
                final AxisNumberRangeType range = axisType.getNumberRange();
                min = range.getMinValue();
                max = range.getMaxValue();
                if( Double.isNaN( (Double) min ) )
                  min = null;
                if( Double.isNaN( (Double) max ) )
                  max = null;
              }

              if( min == null || max == null )
              {
                autoscaledAxes.add( mapperRegistry.getAxis( axisType.getId() ) );
              }
            }

            // Name des Parts
            final TitleTypeBean[] title = m_chartModel.getTitles();
            if( !ArrayUtils.isEmpty( title ) )
              setPartName( title[0].getText() );
            else
              setPartName( null );

            // drag delegates
            m_plotDragHandler = new PlotDragHandlerDelegate( m_chartComposite );
            m_composite.layout();
            m_chartModel.autoscale( autoscaledAxes.toArray( new IAxis[] {} ) );
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
    m_composite.layout( true, true );
    if( m_outlinePage != null )
    {
      m_outlinePage.updateControl();
    }
  }

  @Override
  public IChartComposite getChartComposite( )
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
   * @see org.kalypso.chart.ui.IChartPart#getOutlinePage()
   */
  @Override
  public IContentOutlinePage getOutlinePage( )
  {
    if( m_outlinePage == null && getChartComposite() != null )
    {
      final IChartModel model = getChartComposite().getChartModel();
      m_outlinePage = new ChartEditorTreeOutlinePage();
      m_outlinePage.setModel( model );
    }

    return m_outlinePage;
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( IContentOutlinePage.class.equals( adapter ) )
    {
      return getOutlinePage();
    }

    if( IChartComposite.class.equals( adapter ) )
    {
      return m_chartComposite;
    }

    if( IChartPart.class.equals( adapter ) )
    {
      return this;
    }

    return super.getAdapter( adapter );
  }

  public ChartConfigurationDocument getConfiguration( )
  {
    return m_configuration;
  }

  public void setConfiguration( final ChartConfigurationDocument doc )
  {
    m_configuration = doc;
  }

  @Override
  public PlotDragHandlerDelegate getPlotDragHandler( )
  {
    return m_plotDragHandler;
  }

  protected void setDirty( final boolean dirty )
  {
    if( m_dirty == dirty )
    {
      return;
    }

    m_dirty = dirty;
    firePropertyChange( PROP_DIRTY );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  @Override
  public void addListener( final IChartModelEventListener listener )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  @Override
  public void removeListener( final IChartModelEventListener listener )
  {
    // TODO Auto-generated method stub

  }

}
