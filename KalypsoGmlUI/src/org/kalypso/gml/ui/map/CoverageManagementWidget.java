/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gml.ui.map;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.EmptyCommand;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.wizard.grid.AddRectifiedGridCoveragesWizard;
import org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageExportWizard;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.GisTemplateUserStyle;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.ogc.gml.featureview.maker.CachedFeatureviewFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewHelper;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.AbstractThemeInfoWidget;
import org.kalypso.ogc.gml.map.widgets.AbstractWidget;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ui.editor.gmleditor.util.command.MoveFeatureCommand;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypso.ui.editor.sldEditor.RasterColorMapContentProvider;
import org.kalypso.ui.editor.sldEditor.RasterColorMapLabelProvider;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.sld.UserStyle;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * A widget with option pane, which allows the user to edit a coverage collection.<BR>
 * The user can add / remove raster data to / from the collection and change the order of the elements of the
 * collection. In addition he can jump to the extent of an collection element in the map.
 *
 * @author Thomas Jung
 */
public class CoverageManagementWidget extends AbstractWidget implements IWidgetWithOptions
{
  private static final IKalypsoThemePredicate COVERAGE_PREDICATE = new IKalypsoThemePredicate()
  {
    public boolean decide( final IKalypsoTheme theme )
    {
      if( !(theme instanceof IKalypsoFeatureTheme) )
        return false;

      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme) theme;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getParentFeature();

      if( coveragesFeature == null )
        return false;

      final IRelationType targetPropertyType = featureList.getParentFeatureTypeProperty();
      final IFeatureType targetFeatureType = targetPropertyType.getTargetFeatureType();

      return GMLSchemaUtilities.substitutes( targetFeatureType, ICoverage.QNAME );
    }
  };

  private final AbstractThemeInfoWidget m_infoWidget = new AbstractThemeInfoWidget( "", "" )
  {
  };

  private final IMapModellListener m_mapModelListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      refreshThemeCombo();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeAdded(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeAdded( final IMapModell source, final IKalypsoTheme theme )
    {
      refreshThemeCombo();
    }

    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeRemoved(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, boolean)
     */
    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      refreshThemeCombo();
    }
  };

  private final Runnable m_refreshCoverageViewerRunnable = new Runnable()
  {
    @SuppressWarnings("synthetic-access")
    public void run( )
    {
      ViewerUtilities.refresh( m_coverageViewer, true );
    }
  };

  private ICoverageCollection m_coverages;

  private ICoverage m_selectedCoverage;

  private IFolder m_gridFolder;

  private ListViewer m_coverageViewer;

  private IKalypsoFeatureTheme m_theme;

  private TableViewer m_colorMapTableViewer;

  private ComboViewer m_themeCombo;

  /** If <code>true</code>, a colormap editor is shown for the current raster style. Default to <code>true</code>. */
  private boolean m_showStyle = true;

  /** If <code>true</code>, Add and Remove coverage buttons are shown. Default to <code>true</code>. */
  private boolean m_showAddRemoveButtons = true;

  private String m_featureTemplateGft = "resources/coverage.gft";

  private final ModellEventListener m_modellistener = new ModellEventListener()
  {
    public void onModellChange( final ModellEvent modellEvent )
    {
      refreshControl();
    }
  };

  private final IAction[] m_customActions;

  private final String m_partName;

  /**
   * The constructor.
   */
  public CoverageManagementWidget( )
  {
    this( "Höhenmodelle verwalten", "Höhenmodelle verwalten" );
  }

  /**
   * The constructor.
   *
   * @param name
   *          The name of the widget.
   * @param tooltip
   *          The tooltip of the widget.
   */
  public CoverageManagementWidget( final String name, final String tooltip )
  {
    this( name, tooltip, null );
  }

  /**
   * The constructor.
   *
   * @param name
   *          The name of the widget.
   * @param tooltip
   *          The tooltip of the widget.
   */
  public CoverageManagementWidget( final String name, final String tooltip, final Action[] customActions )
  {
    this( name, tooltip, customActions, null );
  }

  /**
   * The constructor.
   *
   * @param name
   *          The name of the widget.
   * @param tooltip
   *          The tooltip of the widget.
   */
  public CoverageManagementWidget( final String name, final String tooltip, final Action[] customActions, final String partName )
  {
    super( name, tooltip );

    m_customActions = customActions;
    m_partName = partName;
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.IMapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    /* Search for a coverage collection. */
    final IMapModell mapModell = mapPanel == null ? null : mapPanel.getMapModell();
    mapModell.addMapModelListener( m_mapModelListener );

    refreshThemeCombo();

    m_infoWidget.activate( commandPoster, mapPanel );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#finish()
   */
  @Override
  public void finish( )
  {
    super.finish();

    m_infoWidget.finish();
  }

  protected void handleThemeActivated( final IKalypsoTheme activeTheme )
  {
    setCoverages( null, null );

    if( activeTheme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme) activeTheme;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getParentFeature();
      if( coveragesFeature != null )
        setCoverages( (ICoverageCollection) coveragesFeature.getAdapter( ICoverageCollection.class ), ft );
    }
  }

  private void setCoverages( final ICoverageCollection coverages, final IKalypsoFeatureTheme theme )
  {
    // remove listener
    if( m_theme != null )
      m_theme.getWorkspace().removeModellListener( m_modellistener );

    m_coverages = coverages;
    m_theme = theme;

    // add listener
    if( m_theme != null )
      m_theme.getWorkspace().addModellListener( m_modellistener );

    if( m_theme == null )
      m_infoWidget.setThemes( null );
    else
      m_infoWidget.setThemes( new IKalypsoTheme[] { m_theme } );

    updateStylePanel();

    final ListViewer coverageViewer = m_coverageViewer;
    if( coverageViewer != null && !coverageViewer.getControl().isDisposed() )
    {
      coverageViewer.getControl().getDisplay().syncExec( new Runnable()
      {
        public void run( )
        {
          if( !coverageViewer.getControl().isDisposed() )
          {
            coverageViewer.setInput( coverages );
            if( coverages != null && coverages.size() > 0 )
              coverageViewer.setSelection( new StructuredSelection( coverages.get( 0 ) ), true );
          }
        }
      } );
    }
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#createControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final ScrolledComposite sc = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
    sc.setMinWidth( 200 );
    sc.setExpandVertical( true );
    sc.setExpandHorizontal( true );

    final Composite panel = toolkit.createComposite( sc, SWT.NONE );
    panel.setLayout( new GridLayout() );

    sc.setContent( panel );
    parent.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
        panel.setSize( size );
        sc.setMinHeight( size.y );
      }
    } );
    // Basic Layout

    /* Theme selection combo */
    final Composite themeSelectionPanel = toolkit.createComposite( panel, SWT.NONE );
    themeSelectionPanel.setLayout( new GridLayout( 2, false ) );
    themeSelectionPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.createLabel( themeSelectionPanel, "Thema: ", SWT.NONE );
    m_themeCombo = new ComboViewer( themeSelectionPanel, SWT.READ_ONLY | SWT.DROP_DOWN );
    final GridData comboGridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    m_themeCombo.getControl().setLayoutData( comboGridData );

    /* Coverage table + info pane */
    final Composite coveragePanel = toolkit.createComposite( panel, SWT.NONE );
    final GridLayout coveragePanelLayout = new GridLayout( 2, false );
    final GridData coveragePanelData = new GridData( SWT.FILL, SWT.FILL, true, false );
    coveragePanelData.heightHint = 200;
    coveragePanel.setLayoutData( coveragePanelData );
    coveragePanelLayout.marginHeight = 0;
    coveragePanelLayout.marginWidth = 0;
    coveragePanel.setLayout( coveragePanelLayout );

    m_coverageViewer = new ListViewer( coveragePanel, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );

    final GridData coverageViewerData = new GridData( SWT.FILL, SWT.FILL, true, false );
    coverageViewerData.heightHint = 100;
    m_coverageViewer.getControl().setLayoutData( coverageViewerData );
    toolkit.adapt( m_coverageViewer.getControl(), true, false );

    final Composite coverageButtonPanel = toolkit.createComposite( coveragePanel );
    final FillLayout coverageButtonPanelLayout = new FillLayout( SWT.VERTICAL );
    coverageButtonPanelLayout.spacing = 4;
    coverageButtonPanel.setLayout( coverageButtonPanelLayout );
    coverageButtonPanel.setLayoutData( new GridData( SWT.CENTER, SWT.BEGINNING, false, true ) );

    /* Info view */
    final Group coverageInfoGroup = new Group( panel, SWT.H_SCROLL );
    coverageInfoGroup.setLayout( new GridLayout() );
    final GridData infoGroupData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    coverageInfoGroup.setLayoutData( infoGroupData );
    toolkit.adapt( coverageInfoGroup );
    coverageInfoGroup.setText( "Info" );

    final CachedFeatureviewFactory featureviewFactory = new CachedFeatureviewFactory( new FeatureviewHelper() );
    featureviewFactory.addView( getClass().getResource( m_featureTemplateGft ) );
    final FeatureComposite featureComposite = new FeatureComposite( null, null, featureviewFactory );
    featureComposite.setFormToolkit( toolkit );

    featureComposite.addChangeListener( new IFeatureChangeListener()
    {
      @SuppressWarnings("synthetic-access")
      public void featureChanged( final ICommand changeCommand )
      {
        m_theme.postCommand( changeCommand, null );
        updateGridProperties();
      }

      public void openFeatureRequested( final Feature feature, final IPropertyType pt )
      {
      }
    } );

    /* Color Map table */
    final Composite colormapPanel = toolkit.createComposite( panel, SWT.NONE );
    final GridLayout colormapPanelLayout = new GridLayout();
    colormapPanelLayout.numColumns = 2;
    colormapPanelLayout.makeColumnsEqualWidth = false;
    colormapPanelLayout.marginWidth = 0;
    colormapPanelLayout.marginHeight = 0;

    colormapPanel.setLayout( colormapPanelLayout );
    final GridData colormapPanelData = new GridData( SWT.FILL, SWT.FILL, true, true );
    colormapPanelData.exclude = !m_showStyle;
    colormapPanel.setVisible( m_showStyle );
    colormapPanel.setLayoutData( colormapPanelData );

    m_colorMapTableViewer = new TableViewer( colormapPanel, SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL );
    final GridData colormapTableData = new GridData( SWT.FILL, SWT.FILL, true, true );

    m_colorMapTableViewer.getControl().setLayoutData( colormapTableData );
    toolkit.adapt( m_colorMapTableViewer.getControl(), true, true );

    final Composite colormapPanelButtonPanel = toolkit.createComposite( colormapPanel, SWT.NONE );
    final GridLayout colormapButtonPanelLayout = new GridLayout();
    colormapButtonPanelLayout.marginHeight = 0;
    colormapButtonPanelLayout.marginWidth = 0;
    colormapPanelButtonPanel.setLayout( colormapButtonPanelLayout );
    colormapPanelButtonPanel.setLayoutData( new GridData( SWT.CENTER, SWT.BEGINNING, false, false ) );

    // Fill contents
    initalizeCoverageViewer( m_coverageViewer );
    initalizeCoverageActions( toolkit, coverageButtonPanel, m_customActions );

    initializeColorMapTableViewer( m_colorMapTableViewer );
    initalizeColorMapActions( toolkit, colormapPanelButtonPanel );

    /* Hook Events */
    m_coverageViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleListSelectionChanged( parent, sc, panel, coverageInfoGroup, featureComposite, event );
      }
    } );

    m_themeCombo.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleThemeComboSelected( event );
      }
    } );

    initializeThemeCombo();

    if( m_coverages != null && m_coverages.size() > 0 )
      m_coverageViewer.setSelection( new StructuredSelection( m_coverages.get( 0 ) ) );

    final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    panel.setSize( size );
    sc.setMinHeight( size.y );

    return panel;
  }

  protected void refreshControl( )
  {
    ViewerUtilities.refresh( m_coverageViewer, true );
  }

  protected void updateGridProperties( )
  {
    if( m_theme == null )
      m_infoWidget.setThemes( null );
    else
      m_infoWidget.setThemes( new IKalypsoTheme[] { m_theme } );

  }

  private void initializeThemeCombo( )
  {
    m_themeCombo.setContentProvider( new ArrayContentProvider() );
    m_themeCombo.setLabelProvider( new LabelProvider() );

    refreshThemeCombo();
  }

  public void refreshThemeCombo( )
  {
    if( m_themeCombo == null || m_themeCombo.getControl().isDisposed() )
      return;

    final IMapPanel mapPanel = getMapPanel();
    final IMapModell mapModell = mapPanel == null ? null : mapPanel.getMapModell();
    final IKalypsoTheme activeTheme = mapModell == null ? null : mapModell.getActiveTheme();

    final List<IKalypsoTheme> themesForCombo = new ArrayList<IKalypsoTheme>();

    if( COVERAGE_PREDICATE.decide( activeTheme ) )
      themesForCombo.add( activeTheme );
    else if( activeTheme instanceof IMapModell )
    {
      final IKalypsoTheme[] allThemes = ((IMapModell) activeTheme).getAllThemes();
      for( final IKalypsoTheme kalypsoTheme : allThemes )
      {
        if( COVERAGE_PREDICATE.decide( kalypsoTheme ) )
          themesForCombo.add( kalypsoTheme );
      }
    }

    final Control control = m_themeCombo.getControl();
    final ComboViewer themeCombo = m_themeCombo;
    control.getDisplay().asyncExec( new Runnable()
    {
      public void run( )
      {
        if( control.isDisposed() )
          return; // may be disposed meanwhile

        themeCombo.setInput( themesForCombo );

        if( themesForCombo.size() > 0 )
          themeCombo.setSelection( new StructuredSelection( themesForCombo.get( 0 ) ) );
      }
    } );

  }

  protected void handleThemeComboSelected( final SelectionChangedEvent event )
  {
    setCoverages( null, null );

    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    final Object firstElement = selection.getFirstElement();

    if( firstElement instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme) firstElement;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getParentFeature();
      if( coveragesFeature != null )
        setCoverages( (ICoverageCollection) coveragesFeature.getAdapter( ICoverageCollection.class ), ft );
    }
  }

  protected void handleListSelectionChanged( final Composite parent, final ScrolledComposite sc, final Composite panel, final Group coverageInfoGroup, final FeatureComposite featureComposite, final SelectionChangedEvent event )
  {
    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    m_selectedCoverage = (ICoverage) selection.getFirstElement();

    featureComposite.disposeControl();

    if( m_selectedCoverage != null )
    {
      featureComposite.setFeature( m_selectedCoverage.getFeature() );
      featureComposite.createControl( coverageInfoGroup, SWT.NONE );
      parent.layout( true, true );
    }

    final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    panel.setSize( size );
    sc.setMinHeight( size.y );

    getMapPanel().repaintMap();
  }

  /**
   * sets the input of the style panel by reading the raster sld.
   */
  protected void updateStylePanel( )
  {
    /* get sld from theme */
    if( m_theme == null )
      return;

    final UserStyle[] styles = m_theme.getStyles();

    final RasterSymbolizer symb = findRasterSymbolizer( styles );

    if( m_colorMapTableViewer != null && !m_colorMapTableViewer.getControl().isDisposed() )
      m_colorMapTableViewer.setInput( symb );
  }

  /**
   * returns the first {@link RasterSymbolizer} from the given user styles
   *
   * @param styles
   *          The styles in which the raster symbolizer is
   * @return a {@link RasterSymbolizer}
   */
  private RasterSymbolizer findRasterSymbolizer( final UserStyle[] styles )
  {
    for( final UserStyle userStyle : styles )
    {
      final FeatureTypeStyle[] featureTypeStyles = userStyle.getFeatureTypeStyles();
      for( final FeatureTypeStyle featureTypeStyle : featureTypeStyles )
      {
        final Rule[] rules = featureTypeStyle.getRules();
        for( final Rule rule : rules )
        {
          final Symbolizer[] symbolizers = rule.getSymbolizers();
          for( final Symbolizer symbolizer : symbolizers )
          {
            if( symbolizer instanceof RasterSymbolizer )
              return (RasterSymbolizer) symbolizer;
          }
        }
      }
    }

    return null;
  }

  /**
   * initializes the button action for the style panel.
   */
  private void initalizeColorMapActions( final FormToolkit toolkit, final Composite parent )
  {
    // We are reusing images of KalypsoGmlUi here
    final ImageDescriptor generateID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.STYLE_EDIT );

    createButton( toolkit, parent, new Action( "Generate ColorMap", generateID )
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( final Event event )
      {
        handleGenerateColorMap( event );
      }
    } );

  }

  /**
   * handles the creation of a RasterColorMap via a {@link GridStyleDialog}
   */
  protected void handleGenerateColorMap( final Event event )
  {
    final RasterSymbolizer symb = (RasterSymbolizer) m_colorMapTableViewer.getInput();
    final TreeMap<Double, ColorMapEntry> input = symb.getColorMap();

    if( input != null )
    {
      final List<ColorMapEntry> entryList = new LinkedList<ColorMapEntry>();

      // convert map into an array
      for( final ColorMapEntry entry : input.values() )
      {
        entryList.add( entry );
      }
      final ColorMapEntry[] entries = entryList.toArray( new ColorMapEntry[entryList.size()] );

      // get min / max
      BigDecimal min = new BigDecimal( Double.MAX_VALUE );
      BigDecimal max = new BigDecimal( Double.MIN_VALUE );

      for( final ICoverage coverage : m_coverages )
      {
        try
        {
          final IGeoGrid geoGrid = GeoGridUtilities.toGrid( coverage );
          min = min.min( geoGrid.getMin() );
          max = max.max( geoGrid.getMax() );

          // dispose it
          geoGrid.dispose();
        }
        catch( final Exception e )
        {
          e.printStackTrace();
        }
      }

      // open dialog
      final GridStyleDialog dialog = new GridStyleDialog( event.display.getActiveShell(), entries, min, max );
      if( dialog.open() == Window.OK )
      {
        updateRasterSymbolizer( symb, dialog.getColorMap() );

        saveStyle();

        m_colorMapTableViewer.refresh();
        m_colorMapTableViewer.getControl().getParent().getParent().layout( true, true );

      }
      getMapPanel().invalidateMap();
    }
  }

  private void saveStyle( )
  {
    if( m_theme == null )
      return;

    final UserStyle[] styles = m_theme.getStyles();

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( "Speichere Styles", styles.length );

        for( final UserStyle userStyle : styles )
        {
          if( userStyle instanceof GisTemplateUserStyle )
          {
            final GisTemplateUserStyle style = (GisTemplateUserStyle) userStyle;

            style.save( new SubProgressMonitor( monitor, 1 ) );
            style.fireStyleChanged();
          }
        }

        return Status.OK_STATUS;
      }
    };

    final IStatus result = ProgressUtilities.busyCursorWhile( operation );
    final Shell shell = m_colorMapTableViewer.getControl().getShell();
    ErrorDialog.openError( shell, "Styles speichern", "Fehler beim Speichern eines Style", result );
  }

  /**
   * update the colorMap of the {@link RasterSymbolizer}
   */
  private void updateRasterSymbolizer( final RasterSymbolizer symb, final ColorMapEntry[] entries )
  {
    final TreeMap<Double, ColorMapEntry> new_colorMap = new TreeMap<Double, ColorMapEntry>();

    try
    {
      for( final ColorMapEntry colorMapEntry : entries )
      {
        if( !new_colorMap.containsKey( new Double( colorMapEntry.getQuantity() ) ) )
          new_colorMap.put( new Double( colorMapEntry.getQuantity() ), colorMapEntry.clone() );
        else
        {
          throw new Exception();
        }
      }
    }
    catch( final Exception e )
    {
      MessageDialog.openError( m_colorMapTableViewer.getControl().getShell(), "Fehler bei Erstellung der Farbtabelle", "Werte dürfen nicht doppelt vorkommen." );
    }

    symb.setColorMap( new_colorMap );
  }

  private void initializeColorMapTableViewer( final TableViewer viewer )
  {
    viewer.setContentProvider( new RasterColorMapContentProvider() );
    viewer.setLabelProvider( new RasterColorMapLabelProvider( viewer ) );

    final Table viewerTable = viewer.getTable();
    viewerTable.setLinesVisible( true );
    viewerTable.setHeaderVisible( true );
    viewerTable.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    updateStylePanel();
  }

  private void initalizeCoverageActions( final FormToolkit toolkit, final Composite parent, final IAction[] customActions )
  {
    final ImageDescriptor upID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_UP );
    final ImageDescriptor downID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_DOWN );
    final ImageDescriptor jumptoID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_JUMP );
    final ImageDescriptor exportID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_EXPORT );

    if( m_showAddRemoveButtons )
    {
      final ImageDescriptor addID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_ADD );
      final ImageDescriptor removeID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_REMOVE );

      final Action addAction = new Action( "Add Coverage", addID )
      {
        /**
         * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
         */
        @Override
        public void runWithEvent( final Event event )
        {
          handleCoverageAdded( event );
        }
      };
      addAction.setDescription( "Kachel hinzufügen" );

      final Action removeAction = new Action( "Remove Coverage", removeID )
      {
        /**
         * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
         */
        @Override
        public void runWithEvent( final Event event )
        {
          handleCoverageRemoved( event );
        }
      };
      removeAction.setDescription( "Kachel löschen" );

      createButton( toolkit, parent, addAction );
      createButton( toolkit, parent, removeAction );
    }

    final Action exportAction = new Action( "Export Coverage", exportID )
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( final Event event )
      {
        handleCoverageExport( event );
      }
    };
    exportAction.setDescription( "Kachel exportieren" );

    final Action moveUpAction = new Action( "Move Coverage Up", upID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageMove( -1 );
      }
    };
    moveUpAction.setDescription( "Kachel nach oben verschieben" );

    final Action moveDownAction = new Action( "Move Coverage Down", downID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageMove( 1 );
      }
    };
    moveDownAction.setDescription( "Kachel nach unten verschieben" );

    final Action jumpToAction = new Action( "Jump To Coverage", jumptoID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageJumpTo();
      }
    };
    jumpToAction.setDescription( "Springe zu Kachel" );

    createButton( toolkit, parent, exportAction );
    createButton( toolkit, parent, moveUpAction );
    createButton( toolkit, parent, moveDownAction );
    createButton( toolkit, parent, jumpToAction );

    /* Should some custom action be added. */
    if( customActions == null || customActions.length == 0 )
      return;

    /* Add custom actions. */
    for( final IAction customAction : customActions )
      createButton( toolkit, parent, customAction );
  }

  private void createButton( final FormToolkit toolkit, final Composite parent, final IAction action )
  {
    /* Create the button. */
    final Button button = toolkit.createButton( parent, null, SWT.PUSH );

    /* If an image is set in the action, use it for the button. */
    final ImageDescriptor imageDescriptor = action.getImageDescriptor();
    if( imageDescriptor != null )
    {
      final Image image = imageDescriptor.createImage( true );
      button.setImage( image );
      button.addDisposeListener( new DisposeListener()
      {
        public void widgetDisposed( final DisposeEvent e )
        {
          image.dispose();
        }
      } );
    }

    /* Set a tooltip. */
    button.setToolTipText( action.getDescription() );

    /* What happens, if the button is pressed? */
    button.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final Event event = new Event();
        event.display = e.display;
        // ...

        action.runWithEvent( event );
      }
    } );
  }

  protected void handleCoverageJumpTo( )
  {
    if( m_selectedCoverage == null )
      return;

    try
    {
      // final GM_Envelope envelope = m_selectedCoverage.getEnvelope();
      final GM_Surface< ? > surface = GeoGridUtilities.createSurface( GeoGridUtilities.toGrid( m_selectedCoverage ), KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
      final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( surface.getEnvelope(), 1.05 );
      getMapPanel().setBoundingBox( scaledBox );
    }
    catch( final GeoGridException e )
    {
      e.printStackTrace();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

  }

  @SuppressWarnings("unchecked")
  protected void handleCoverageMove( final int step )
  {
    if( m_selectedCoverage == null )
      return;

    final Feature parentFeature = m_coverages.getFeature();
    final IPropertyType pt = parentFeature.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
    final Feature coverageFeature = m_selectedCoverage.getFeature();

    final List featureList = (List) parentFeature.getProperty( pt );
    final int newIndex = featureList.indexOf( coverageFeature ) + step;
    if( newIndex < 0 || newIndex >= featureList.size() )
      return;

    final MoveFeatureCommand command = new MoveFeatureCommand( parentFeature, pt, coverageFeature, step );

    m_theme.postCommand( command, m_refreshCoverageViewerRunnable );
  }

  protected void handleCoverageAdded( final Event event )
  {
    final IKalypsoFeatureTheme theme = m_theme;
    final ICoverageCollection coverages = m_coverages;
    final Runnable refreshRunnable = m_refreshCoverageViewerRunnable;

    final Shell shell = event.display.getActiveShell();
    final AddRectifiedGridCoveragesWizard wizard = new AddRectifiedGridCoveragesWizard( coverages, m_gridFolder );
    final WizardDialog wizardDialog = new WizardDialog( shell, wizard );
    if( wizardDialog.open() != Window.OK )
      return;

    final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( wizard.getBoundingBox(), 1.05 );
    getMapPanel().setBoundingBox( scaledBox );

    // TODO: move into finish method? / very slow, why?
    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          /* save the model */
          monitor.beginTask( "GML wird gespeichert", IProgressMonitor.UNKNOWN );
          // we cannot allow the model not to be saved, as the underlying files will then not be deleted
          theme.postCommand( new EmptyCommand( "", false ), refreshRunnable );
          final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
          final CommandableWorkspace workspace = theme.getWorkspace();
          pool.saveObject( workspace, monitor );

          return Status.OK_STATUS;
        }
        catch( final LoaderException e )
        {
          e.printStackTrace();

          throw new InvocationTargetException( e );
        }
      }
    };

    final IStatus status = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( shell, "Kachel hinzufügen", "Die Kachel konnte nicht hinzugeladen werden.", status );
  }

  protected void handleCoverageExport( final Event event )
  {
    if( m_selectedCoverage == null )
      return;

    // open wizard to locate destination
    final RectifiedGridCoverageExportWizard wizard = new RectifiedGridCoverageExportWizard();
    wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( m_selectedCoverage ) );

    final WizardDialog wizardDialog = new WizardDialog( event.display.getActiveShell(), wizard );
    wizardDialog.open();
  }

  protected void handleCoverageRemoved( final Event event )
  {
    if( m_selectedCoverage == null )
      return;

    final ICoverage selectedCoverage = m_selectedCoverage;
    final IKalypsoFeatureTheme theme = m_theme;
    final Runnable refreshRunnable = m_refreshCoverageViewerRunnable;

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          final CommandableWorkspace workspace = theme.getWorkspace();

          /* Delete coverage from collection */
          final Feature coverageFeature = selectedCoverage.getFeature();

          final DeleteFeatureCommand command = new DeleteFeatureCommand( coverageFeature );
          theme.postCommand( command, refreshRunnable );

          /* Delete underlying grid file */
          final IStatus status = CoverageManagementHelper.deleteGridFile( selectedCoverage );
          if( !status.isOK() )
            return status;

          /* save the model */
          final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
          pool.saveObject( workspace, monitor );

          return Status.OK_STATUS;
        }
        catch( final LoaderException e )
        {
          e.printStackTrace();

          throw new InvocationTargetException( e );
        }
      }
    };

    final IStatus status = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( event.display.getActiveShell(), "Kachel entfernen", "Fehler beim Entfernen der Kachel", status );
  }

  private void initalizeCoverageViewer( final StructuredViewer viewer )
  {
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        final RectifiedGridCoverage coverage = (RectifiedGridCoverage) element;
        return coverage.getName();
      }
    } );
    viewer.setInput( m_coverages );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#disposeControl()
   */
  public void disposeControl( )
  {
    if( m_theme != null && m_modellistener != null )
    {
      final CommandableWorkspace workspace = m_theme.getWorkspace();
      if( workspace != null )
        workspace.removeModellListener( m_modellistener );
    }
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#moved(java.awt.Point)
   */
  @Override
  public void moved( final java.awt.Point p )
  {
    super.moved( p );

    m_infoWidget.moved( p );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#paint(java.awt.Graphics)
   */
  @Override
  public void paint( final Graphics g )
  {
    super.paint( g );

    if( m_selectedCoverage != null )
    {
      try
      {
        /* Paint bbox of selected coverage */
// final GM_Envelope envelope = m_selectedCoverage.getEnvelope();
        final GM_Surface< ? > surface = GeoGridUtilities.createSurface( GeoGridUtilities.toGrid( m_selectedCoverage ), KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
        final GM_Envelope envelope = surface.getEnvelope();

        final GM_Position minPoint = getMapPanel().getProjection().getDestPoint( envelope.getMin() );
        final GM_Position maxPoint = getMapPanel().getProjection().getDestPoint( envelope.getMax() );

        final int x = (int) Math.min( minPoint.getX(), maxPoint.getX() );
        final int y = (int) Math.min( minPoint.getY(), maxPoint.getY() );

        final int width = (int) Math.abs( minPoint.getX() - maxPoint.getX() );
        final int height = (int) Math.abs( minPoint.getY() - maxPoint.getY() );

        g.setColor( Color.RED );
        g.drawRect( x, y, width, height );
      }
      catch( final GeoGridException e )
      {
        e.printStackTrace();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }

    m_infoWidget.paint( g );
  }

  /**
   * Sets the showStyle flag of this widget. Must be called before createControl is invoked.
   */
  public void setShowStyle( final boolean showStyle )
  {
    m_showStyle = showStyle;
  }

  /**
   * Sets the showAddRemoveButtons flag of this widget. Must be called before createControl is invoked.
   */
  public void setShowAddRemoveButtons( final boolean showAddRemoveButtons )
  {
    m_showAddRemoveButtons = showAddRemoveButtons;
  }

  public void setFeatureTemplateGft( final String featureTemplateGft )
  {
    m_featureTemplateGft = featureTemplateGft;
  }

  public void setGridFolder( final IFolder gridFolder )
  {
    m_gridFolder = gridFolder;
  }

  /**
   * This function returns the selected feature theme.
   *
   * @return The selected feature theme.
   */
  public IKalypsoFeatureTheme getSelectedTheme( )
  {
    return m_theme;
  }

  /**
   * This function clears the theme selection.
   */
  public void clearThemeSelection( )
  {
    if( m_themeCombo == null || m_themeCombo.getCombo().isDisposed() )
      return;

    /* Empty the selection. */
    m_themeCombo.setSelection( new StructuredSelection() );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#getPartName()
   */
  @Override
  public String getPartName( )
  {
    return m_partName;
  }
}