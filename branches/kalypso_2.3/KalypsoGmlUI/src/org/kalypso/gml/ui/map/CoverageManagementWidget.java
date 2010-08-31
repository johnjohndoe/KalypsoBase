/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.math.Range;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jfree.util.ObjectUtils;
import org.kalypso.commons.command.EmptyCommand;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.commands.exportgrid.RectifiedGridCoverageExportWizard;
import org.kalypso.gml.ui.commands.importgrid.AddRectifiedGridCoveragesWizard;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTypeStyle;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.ogc.gml.featureview.maker.CachedFeatureviewFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewHelper;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.widgets.AbstractThemeInfoWidget;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ogc.gml.widgets.AbstractWidget;
import org.kalypso.ui.editor.gmleditor.util.command.MoveFeatureCommand;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypso.ui.editor.sldEditor.RasterColorMapContentProvider;
import org.kalypso.ui.editor.sldEditor.RasterColorMapLabelProvider;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.SldHelper;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
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
  public static final IKalypsoThemePredicate COVERAGE_PREDICATE = new IKalypsoThemePredicate()
  {
    @Override
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

  private final AbstractThemeInfoWidget m_infoWidget = new AbstractThemeInfoWidget( "", "" ) //$NON-NLS-1$ //$NON-NLS-2$
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
    @Override
    @SuppressWarnings("synthetic-access")
    public void run( )
    {
      ViewerUtilities.refresh( m_coverageViewer, true );
    }
  };

  private ICoverageCollection m_coverages;

  private ICoverage m_selectedCoverage;

  /** If set, grids get imported by default into this folder */
  private IFolder m_gridFolder;

  /**
   * If set to <code>true</code>, the user is allowed to change the grid import folder, where grids are imported.<br>
   * Will default to <code>true</code>, if {@link #m_gridFolder} is <code>null</code>.
   */
  private boolean m_allowUserChangeGridFolder = true;

  private ListViewer m_coverageViewer;

  private IKalypsoFeatureTheme m_theme;

  private TableViewer m_colorMapTableViewer;

  private ComboViewer m_themeCombo;

  /** If <code>true</code>, a colormap editor is shown for the current raster style. Default to <code>true</code>. */
  private boolean m_showStyle = true;

  /** If <code>true</code>, Add and Remove coverage buttons are shown. Default to <code>true</code>. */
  private boolean m_showAddRemoveButtons = true;

  private String m_featureTemplateGft = "resources/coverage.gft"; //$NON-NLS-1$

  private final ModellEventListener m_modellistener = new ModellEventListener()
  {
    @Override
    public void onModellChange( final ModellEvent modellEvent )
    {
      refreshControl();
    }
  };

  private final Set<CoverageManagementAction> m_actions = new HashSet<CoverageManagementAction>();

  private final IAction[] m_customActions;

  private final String m_partName;

  /**
   * The constructor.
   */
  public CoverageManagementWidget( )
  {
    this( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.0" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
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
   * @param name
   *          The name of the widget.
   * @param tooltip
   *          The tooltip of the widget.
   * @param customActions
   *          Additional actions to be added to the toolbar of this widget. CustomActions may implement
   *          {@link CoverageManagementAction} in order to get inrformed about selection changes.
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
        @Override
        public void run( )
        {
          if( !coverageViewer.getControl().isDisposed() )
          {
            if( coverages == null )
            {
              coverageViewer.setInput( null );
              return;
            }

            IFeatureBindingCollection<ICoverage> coverageList = coverages.getCoverages();
            coverageViewer.setInput( coverageList );
            if( coverageList != null && coverageList.size() > 0 )
              coverageViewer.setSelection( new StructuredSelection( coverageList.get( 0 ) ), true );
          }
        }
      } );
    }
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#createControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
// final ScrolledComposite sc = new ScrolledComposite( parent, SWT.V_SCROLL | SWT.H_SCROLL );
// sc.setMinWidth( 200 );
// sc.setExpandVertical( true );
// sc.setExpandHorizontal( true );

    final Composite panel = toolkit.createComposite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

// sc.setContent( panel );
// parent.addControlListener( new ControlAdapter()
// {
// /**
// * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
// */
// @Override
// public void controlResized( final ControlEvent e )
// {
// final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
// panel.setSize( size );
// sc.setMinHeight( size.y );
// }
// } );

    /* Theme selection combo */
    final Composite themeSelectionPanel = toolkit.createComposite( panel, SWT.NONE );
    themeSelectionPanel.setLayout( new GridLayout( 2, false ) );
    themeSelectionPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toolkit.createLabel( themeSelectionPanel, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.2" ), SWT.NONE ); //$NON-NLS-1$
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

    final ToolBar coverageToolbar = new ToolBar( coveragePanel, SWT.VERTICAL | SWT.FLAT );
    toolkit.adapt( coverageToolbar );
    coverageToolbar.getLayout();
    coverageToolbar.setLayoutData( new GridData( SWT.CENTER, SWT.FILL, false, true ) );

    /* Info view */
    final Group coverageInfoGroup = new Group( panel, SWT.H_SCROLL );
    coverageInfoGroup.setLayout( new GridLayout() );
    final GridData infoGroupData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    coverageInfoGroup.setLayoutData( infoGroupData );
    toolkit.adapt( coverageInfoGroup );
    coverageInfoGroup.setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.3" ) ); //$NON-NLS-1$

    final CachedFeatureviewFactory featureviewFactory = new CachedFeatureviewFactory( new FeatureviewHelper() );
    featureviewFactory.addView( getClass().getResource( m_featureTemplateGft ) );
    final FeatureComposite featureComposite = new FeatureComposite( null, null, featureviewFactory );
    featureComposite.setFormToolkit( toolkit );

    featureComposite.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      @SuppressWarnings("synthetic-access")
      public void featureChanged( final ICommand changeCommand )
      {
        m_theme.postCommand( changeCommand, null );
        updateGridProperties();
        updateButtons();
      }

      @Override
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

    final ToolBar colormapToolbar = new ToolBar( colormapPanel, SWT.VERTICAL | SWT.FLAT );
    toolkit.adapt( colormapToolbar );
    colormapToolbar.setLayoutData( new GridData( SWT.CENTER, SWT.BEGINNING, false, true ) );

    // Fill contents
    initalizeCoverageViewer( m_coverageViewer );
    final ToolBarManager coverageToolbarManager = new ToolBarManager( coverageToolbar );
    initalizeCoverageActions( coverageToolbarManager, m_customActions );

    initializeColorMapTableViewer( m_colorMapTableViewer );
    final ToolBarManager colormapToolbarManager = new ToolBarManager( colormapToolbar );
    initalizeColorMapActions( colormapToolbarManager );

    /* Hook Events */
    m_coverageViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleListSelectionChanged( parent, coverageInfoGroup, featureComposite, event );
      }
    } );

    m_themeCombo.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        handleThemeComboSelected( event );
      }
    } );

    initializeThemeCombo();

    IFeatureBindingCollection<ICoverage> coverages = m_coverages == null ? null : m_coverages.getCoverages();
    if( coverages != null && coverages.size() > 0 )
      m_coverageViewer.setSelection( new StructuredSelection( coverages.get( 0 ) ) );

    final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    panel.setSize( size );
// sc.setMinHeight( size.y );

    updateButtons();

    return panel;
  }

  protected void refreshControl( )
  {
    ViewerUtilities.refresh( m_coverageViewer, true );

    updateButtons();
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
    m_themeCombo.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        final IKalypsoTheme theme = (IKalypsoTheme) element;
        return theme.getLabel();
      }
    } );

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
      @Override
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

    updateButtons();
  }

  protected void handleListSelectionChanged( final Composite parent, final Group coverageInfoGroup, final FeatureComposite featureComposite, final SelectionChangedEvent event )
  {
    final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
    m_selectedCoverage = (ICoverage) selection.getFirstElement();

    featureComposite.disposeControl();

    if( m_selectedCoverage != null )
    {
      featureComposite.setFeature( m_selectedCoverage );
      featureComposite.createControl( coverageInfoGroup, SWT.NONE );
      parent.layout( true, true );
    }

// final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
// panel.setSize( size );
// sc.setMinHeight( size.y );

    getMapPanel().repaintMap();
    updateButtons();
  }

  private void updateButtons( )
  {
    /* Let actions update themselves */
    IFeatureBindingCollection<ICoverage> coverages = m_coverages == null ? null : m_coverages.getCoverages();
    final ICoverage[] allCoverages = coverages == null ? null : coverages.toArray( new ICoverage[coverages.size()] );
    final ICoverage[] selectedCoverages = m_selectedCoverage == null ? new ICoverage[0] : new ICoverage[] { m_selectedCoverage };
    for( final CoverageManagementAction action : m_actions )
    {
      SafeRunnable.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          action.update( allCoverages, selectedCoverages );
        }
      } );
    }
  }

  /**
   * sets the input of the style panel by fetching the style from the selected theme
   */
  protected void updateStylePanel( )
  {
    final RasterSymbolizer symb = findRasterSymbolizer();
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
  private RasterSymbolizer findRasterSymbolizer( )
  {
    if( m_theme == null )
      return null;

    final IKalypsoStyle[] styles = m_theme.getStyles();

    for( final IKalypsoStyle style : styles )
    {
      final FeatureTypeStyle[] featureTypeStyles = findFeatureTypeStyles( style );
      for( final FeatureTypeStyle fts : featureTypeStyles )
      {
        final Rule[] rules = fts.getRules();
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

  private FeatureTypeStyle[] findFeatureTypeStyles( final IKalypsoStyle style )
  {
    if( style instanceof IKalypsoUserStyle )
      return ((IKalypsoUserStyle) style).getFeatureTypeStyles();

    if( style instanceof IKalypsoFeatureTypeStyle )
      return new FeatureTypeStyle[] { (FeatureTypeStyle) style };

    return new FeatureTypeStyle[] {};
  }

  /**
   * initializes the button action for the style panel.
   */
  private void initalizeColorMapActions( final IToolBarManager manager )
  {
    // We are reusing images of KalypsoGmlUi here
    final ImageDescriptor generateID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.STYLE_EDIT );

    createButton( manager, new CoverageManagementAction( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.4" ), "", generateID ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( final Event event )
      {
        handleGenerateColorMap( event );
      }

      @Override
      public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
      {
        setEnabled( allCoverages != null && allCoverages.length > 0 );
      }
    } );

    manager.update( true );
  }

  /**
   * handles the creation of a RasterColorMap via a {@link GridStyleDialog}
   */
  protected void handleGenerateColorMap( final Event event )
  {
    final RasterSymbolizer symb = findRasterSymbolizer();
    if( symb == null )
    {
      MessageDialog.openWarning( event.display.getActiveShell(), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.4" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    final SortedMap<Double, ColorMapEntry> input = symb.getColorMap();

    if( input != null )
    {
      // convert map into an array
      final Collection<ColorMapEntry> values = input.values();
      final ColorMapEntry[] entries = values.toArray( new ColorMapEntry[values.size()] );

      final Range minMax = GeoGridUtilities.calculateRange( m_coverages.getCoverages() );
      final BigDecimal min = (BigDecimal) minMax.getMinimumNumber();
      final BigDecimal max = (BigDecimal) minMax.getMaximumNumber();

      // open dialog
      final GridStyleDialog dialog = new GridStyleDialog( event.display.getActiveShell(), entries, min, max );
      if( dialog.open() == Window.OK )
        updateRasterSymbolizer( dialog.getColorMap() );
    }
  }

  private void saveStyle( )
  {
    if( m_theme == null )
      return;

    final IKalypsoStyle[] styles = m_theme.getStyles();

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        monitor.beginTask( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.5" ), styles.length ); //$NON-NLS-1$

        for( final IKalypsoStyle style : styles )
        {
          // HACK: also fire style change in order to update the map
          style.fireStyleChanged();
          style.save( new SubProgressMonitor( monitor, 1 ) );
        }

        return Status.OK_STATUS;
      }
    };

    final IStatus result = ProgressUtilities.busyCursorWhile( operation );
    final Shell shell = m_colorMapTableViewer.getControl().getShell();
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.7" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.8" ), result ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * update the colorMap of the {@link RasterSymbolizer}
   */
  private void updateRasterSymbolizer( final ColorMapEntry[] entries )
  {
    final RasterSymbolizer symb = findRasterSymbolizer();
    final TreeMap<Double, ColorMapEntry> new_colorMap = new TreeMap<Double, ColorMapEntry>();

    try
    {
      for( final ColorMapEntry colorMapEntry : entries )
      {
        // WHY? why do we not just ignore duplicate entries
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
      MessageDialog.openError( m_colorMapTableViewer.getControl().getShell(), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.9" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.10" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    symb.setColorMap( new_colorMap );

    saveStyle();

    m_colorMapTableViewer.refresh();
    m_colorMapTableViewer.getControl().getParent().getParent().layout( true, true );
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

  private void initalizeCoverageActions( final IToolBarManager manager, final IAction[] customActions )
  {
    final ImageDescriptor jumptoID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_JUMP );
    final ImageDescriptor exportID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_EXPORT );

    if( m_showAddRemoveButtons )
    {
      final ImageDescriptor addID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_ADD );
      final ImageDescriptor removeID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_REMOVE );

      final Action addAction = new CoverageManagementAction( "Add Coverage", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.11" ), addID ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        /**
         * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
         */
        @Override
        public void runWithEvent( final Event event )
        {
          handleCoverageAdd( event );
        }

        /**
         * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
         *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
         */
        @Override
        public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
        {
          setEnabled( allCoverages != null );
        }
      };

      final Action removeAction = new CoverageManagementAction( "Remove Coverage", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.12" ), removeID ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        /**
         * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
         */
        @Override
        public void runWithEvent( final Event event )
        {
          handleCoverageRemove( event );
        }

        /**
         * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
         *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
         */
        @Override
        public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
        {
          setEnabled( selectedCoverages.length > 0 );
        }
      };

      createButton( manager, addAction );
      createButton( manager, removeAction );
    }

    final Action exportAction = new CoverageManagementAction( "Export Coverage", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.13" ), exportID ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( final Event event )
      {
        handleCoverageExport( event );
      }

      /**
       * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
       *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
       */
      @Override
      public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
      {
        setEnabled( selectedCoverages.length > 0 );
      }
    };

    createButton( manager, exportAction );

    if( m_showAddRemoveButtons )
    {
      // Changeing the order of grids only makes sense, if the user is allowed to add/remove them.
      final ImageDescriptor upID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_UP );
      final ImageDescriptor downID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_DOWN );

      final Action moveUpAction = new CoverageManagementAction( "Move Coverage Up", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.14" ), upID ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run( )
        {
          handleCoverageMove( -1 );
        }

        /**
         * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
         *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
         */
        @Override
        public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
        {
          setEnabled( allCoverages != null && selectedCoverages.length > 0 && allCoverages.length > 0 && !ObjectUtils.equal( selectedCoverages[0], allCoverages[0] ) );
        }
      };

      final Action moveDownAction = new CoverageManagementAction( "Move Coverage Down", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.15" ), downID ) //$NON-NLS-1$ //$NON-NLS-2$
      {
        /**
         * @see org.eclipse.jface.action.Action#run()
         */
        @Override
        public void run( )
        {
          handleCoverageMove( 1 );
        }

        /**
         * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
         *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
         */
        @Override
        public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
        {
          setEnabled( allCoverages != null && selectedCoverages.length > 0 && allCoverages.length > 0
              && !ObjectUtils.equal( selectedCoverages[selectedCoverages.length - 1], allCoverages[allCoverages.length - 1] ) );
        }
      };

      createButton( manager, moveUpAction );
      createButton( manager, moveDownAction );
    }

    final IAction jumpToAction = new CoverageManagementAction( "Jump To Coverage", Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.16" ), jumptoID ) //$NON-NLS-1$ //$NON-NLS-2$
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageJumpTo();
      }

      /**
       * @see org.kalypso.gml.ui.CoverageManagementAction#update(org.kalypsodeegree_impl.gml.binding.commons.ICoverage[],
       *      org.kalypsodeegree_impl.gml.binding.commons.ICoverage[])
       */
      @Override
      public void update( final ICoverage[] allCoverages, final ICoverage[] selectedCoverages )
      {
        setEnabled( selectedCoverages.length > 0 );
      }
    };

    createButton( manager, jumpToAction );

    /* Should some custom action be added. */
    if( customActions != null )
    {
      /* Add custom actions. */
      for( final IAction customAction : customActions )
        createButton( manager, customAction );
    }

    manager.update( true );
  }

  private void createButton( final IToolBarManager manager, final IAction action )
  {
    if( action instanceof CoverageManagementAction )
      m_actions.add( (CoverageManagementAction) action );

    final ActionContributionItem item = new ActionContributionItem( action );
// item.setId( "" + System.currentTimeMillis() );
    manager.add( item );
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

  protected void handleCoverageMove( final int step )
  {
    if( m_selectedCoverage == null )
      return;

    final Feature parentFeature = m_coverages;
    final IPropertyType pt = parentFeature.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
    final Feature coverageFeature = m_selectedCoverage;

    final List< ? > featureList = (List< ? >) parentFeature.getProperty( pt );
    final int newIndex = featureList.indexOf( coverageFeature ) + step;
    if( newIndex < 0 || newIndex >= featureList.size() )
      return;

    final MoveFeatureCommand command = new MoveFeatureCommand( parentFeature, pt, coverageFeature, step );

    m_theme.postCommand( command, m_refreshCoverageViewerRunnable );
  }

  protected void handleCoverageAdd( final Event event )
  {
    final IKalypsoFeatureTheme theme = m_theme;
    final ICoverageCollection coverages = m_coverages;
    final Runnable refreshRunnable = m_refreshCoverageViewerRunnable;

    final Shell shell = event.display.getActiveShell();

    final IContainer gridFolder = determineGridFolder();

    final AddRectifiedGridCoveragesWizard wizard = new AddRectifiedGridCoveragesWizard( coverages, gridFolder, m_allowUserChangeGridFolder );
    final WizardDialog wizardDialog = new WizardDialog( shell, wizard );
    if( wizardDialog.open() != Window.OK )
      return;

    final ICoverage[] newCoverages = wizard.getNewCoverages();
    final GM_Envelope bbox = FeatureHelper.getEnvelope( newCoverages );

    final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( bbox, 1.05 );
    getMapPanel().setBoundingBox( scaledBox );

    // TODO: move into finish method? / very slow, because all the tins are saved as well...
    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          /* save the model */
          monitor.beginTask( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.17" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$
          // we cannot allow the model not to be saved, as the underlying files will then not be deleted
          theme.postCommand( new EmptyCommand( "", false ), refreshRunnable ); //$NON-NLS-1$
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

    handleCoveragesAdded( newCoverages );

    final IStatus status = ProgressUtilities.busyCursorWhile( operation );
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.11" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.19" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private IContainer determineGridFolder( )
  {
    if( m_gridFolder != null )
      return m_gridFolder;

    if( m_allowUserChangeGridFolder == false )
      return null;

    if( m_theme == null )
      return null;

    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
      return null;

    final Feature parentFeature = featureList.getParentFeature();
    if( parentFeature == null )
      return null;

    final GMLWorkspace workspace = parentFeature.getWorkspace();
    if( workspace == null )
      return null;

    final URL context = workspace.getContext();
    final IFile themeDataFile = ResourceUtilities.findFileFromURL( context );
    if( themeDataFile != null )
      return themeDataFile.getParent();

    return null;
  }

  private void handleCoveragesAdded( final ICoverage[] newCoverages )
  {
    // set selection to new coverages
    final StructuredSelection selection = new StructuredSelection( newCoverages );
    m_coverageViewer.setSelection( selection );

    final RasterSymbolizer symb = findRasterSymbolizer();
    if( symb == null )
      return;

    final SortedMap<Double, ColorMapEntry> colorMap = symb.getColorMap();
    if( colorMap.isEmpty() )
    {
      /* IN order to show anything to the user, create a default colour map, if no colours have been defined yet */
      final Range minMax = GeoGridUtilities.calculateRange( m_coverages.getCoverages() );
      final BigDecimal min = (BigDecimal) minMax.getMinimumNumber();
      final BigDecimal max = (BigDecimal) minMax.getMaximumNumber();
      final BigDecimal stepWidth = new BigDecimal( 0.1 );
      final Color fromColor = new Color( 0, 255, 0, 200 );
      final Color toColor = new Color( 255, 0, 0, 200 );
      final ColorMapEntry[] colors = SldHelper.createColorMap( fromColor, toColor, stepWidth, min, max );
      updateRasterSymbolizer( colors );
    }
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

  protected void handleCoverageRemove( final Event event )
  {
    if( m_selectedCoverage == null )
      return;

    final ICoverage selectedCoverage = m_selectedCoverage;
    final IKalypsoFeatureTheme theme = m_theme;
    final Runnable refreshRunnable = m_refreshCoverageViewerRunnable;

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          final CommandableWorkspace workspace = theme.getWorkspace();

          /* Delete coverage from collection */
          final Feature coverageFeature = selectedCoverage;

          final DeleteFeatureCommand command = new DeleteFeatureCommand( coverageFeature );
          theme.postCommand( command, refreshRunnable );

          /* save the model */
          final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
          pool.saveObject( workspace, monitor );

          /*
           * Delete underlying grid file: we do it in a job, later, in order to let the map give-up the handle to the
           * file
           */
          final Job job = new Job( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.20" ) ) //$NON-NLS-1$
          {
            @Override
            protected IStatus run( final IProgressMonitor progress )
            {
              return CoverageManagementHelper.deleteGridFile( selectedCoverage );
            }
          };
          job.setUser( false );
          job.setSystem( true );
          job.schedule( 5000 );

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
    ErrorDialog.openError( event.display.getActiveShell(), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.12" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.22" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
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
    if( m_coverages != null )
      viewer.setInput( m_coverages.getCoverages() );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#disposeControl()
   */
  @Override
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

  public void setAllowUserChangeGridFolder( final boolean allowUserChangeGridFolder )
  {
    m_allowUserChangeGridFolder = allowUserChangeGridFolder;
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