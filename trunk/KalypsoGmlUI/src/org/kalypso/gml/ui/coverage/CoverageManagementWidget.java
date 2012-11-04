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
package org.kalypso.gml.ui.coverage;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gml.ui.KalypsoGmlUiExtensions;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gml.ui.internal.coverage.AddCoverageAction;
import org.kalypso.gml.ui.internal.coverage.CoverageColorRangeAction;
import org.kalypso.gml.ui.internal.coverage.CoverageColormapHandler;
import org.kalypso.gml.ui.internal.coverage.ExportCoverageAction;
import org.kalypso.gml.ui.internal.coverage.JumpToCoverageAction;
import org.kalypso.gml.ui.internal.coverage.MoveCoverageDownAction;
import org.kalypso.gml.ui.internal.coverage.MoveCoverageUpAction;
import org.kalypso.gml.ui.internal.coverage.RemoveCoverageAction;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
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
import org.kalypso.ui.editor.gmleditor.command.MoveFeatureCommand;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypso.ui.editor.styleeditor.viewer.ColorMapViewer;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * A widget with option pane, which allows the user to edit a coverage collection.<BR>
 * The user can add / remove coverage data to / from the collection and change the order of the elements of the
 * collection. In addition he can jump to the extent of an collection element in the map.
 * 
 * @author Thomas Jung
 * @author Gernot Belger
 */
public class CoverageManagementWidget extends AbstractWidget implements IWidgetWithOptions
{
  /** Allows to define on the theme, if the user is allowed to change the grid folder for this theme */
  private static final String THEME_PROPERTY_ALLOW_USER_CHANGE_GRID_FOLDER = "allowUserChangeGridFolder"; //$NON-NLS-1$

  public static final IKalypsoThemePredicate COVERAGE_PREDICATE = new IKalypsoThemePredicate()
  {
    @Override
    public boolean decide( final IKalypsoTheme theme )
    {
      if( !(theme instanceof IKalypsoFeatureTheme) )
        return false;

      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme)theme;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getOwner();

      if( coveragesFeature == null )
        return false;

      final IRelationType targetPropertyType = featureList.getPropertyType();
      final IFeatureType targetFeatureType = targetPropertyType.getTargetFeatureType();

      return GMLSchemaUtilities.substitutes( targetFeatureType, ICoverage.FEATURE__COVERAGE );
    }
  };

  private final AbstractThemeInfoWidget m_infoWidget = new AbstractThemeInfoWidget( "", "" ) //$NON-NLS-1$ //$NON-NLS-2$
  {
  };

  private final IMapModellListener m_mapModelListener = new MapModellAdapter()
  {
    @Override
    public void themeActivated( final IMapModell source, final IKalypsoTheme previouslyActive, final IKalypsoTheme nowActive )
    {
      refreshThemeCombo();
    }

    @Override
    public void themeAdded( final IMapModell source, final IKalypsoTheme theme )
    {
      refreshThemeCombo();
    }

    @Override
    public void themeRemoved( final IMapModell source, final IKalypsoTheme theme, final boolean lastVisibility )
    {
      refreshThemeCombo();
    }
  };

  private final Runnable m_refreshCoverageViewerRunnable = new Runnable()
  {
    @Override
    public void run( )
    {
      ViewerUtilities.refresh( m_coverageViewer, true );
    }
  };

  private ICoverageCollection m_coverages;

  private ICoverage m_selectedCoverage;

  /** If set, data files get imported by default into this folder */
  private IFolder m_dataFolder;

  /**
   * If set to <code>true</code>, the user is allowed to change the data import folder, where data files are imported.<br>
   * Will default to <code>true</code>, if {@link #m_dataFolder} is <code>null</code>.
   */
  private boolean m_allowUserChangeDataFolder = true;

  protected ListViewer m_coverageViewer;

  protected IKalypsoFeatureTheme m_theme;

  private ColorMapViewer m_colorMapViewer;

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

  private final Set<IUpdateable> m_actions = new HashSet<>();

  private final IAction[] m_customActions;

  private final String m_partName;

  public CoverageManagementWidget( )
  {
    this( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.0" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
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
   * @param name
   *          The name of the widget.
   * @param tooltip
   *          The tooltip of the widget.
   * @param customActions
   *          Additional actions to be added to the toolbar of this widget. CustomActions may implement {@link CoverageManagementAction} in order to get informed about selection changes.
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
   *          Additional actions to be added to the toolbar of this widget. CustomActions may implement {@link CoverageManagementAction} in order to get informed about selection changes.
   * @param partName
   *          The name of the part.
   */
  public CoverageManagementWidget( final String name, final String tooltip, final Action[] customActions, final String partName )
  {
    super( name, tooltip );

    m_customActions = customActions;
    m_partName = partName;
  }

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
      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme)activeTheme;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getOwner();
      if( coveragesFeature != null )
        setCoverages( (ICoverageCollection)coveragesFeature.getAdapter( ICoverageCollection.class ), ft );
    }
  }

  private void setCoverages( final ICoverageCollection coverages, final IKalypsoFeatureTheme theme )
  {
    // remove listener
    if( m_theme != null )
    {
      final CommandableWorkspace workspace = m_theme.getWorkspace();
      if( workspace != null )
        workspace.removeModellListener( m_modellistener );
    }

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

            final IFeatureBindingCollection<ICoverage> coverageList = coverages.getCoverages();
            coverageViewer.setInput( coverageList );
            if( coverageList != null && coverageList.size() > 0 )
              coverageViewer.setSelection( new StructuredSelection( coverageList.get( 0 ) ), true );
          }
        }
      } );
    }
  }

  // TODO: move control and everything into separate class
  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final Composite panel = toolkit.createComposite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    /* Theme selection combo */
    final Composite themeSelectionPanel = toolkit.createComposite( panel, SWT.NONE );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).applyTo( themeSelectionPanel );

    themeSelectionPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    toolkit.createLabel( themeSelectionPanel, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.2" ), SWT.NONE ); //$NON-NLS-1$
    m_themeCombo = new ComboViewer( themeSelectionPanel, SWT.READ_ONLY | SWT.DROP_DOWN );
    final GridData comboGridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    m_themeCombo.getControl().setLayoutData( comboGridData );

    /* Coverage table + info pane */
    final Composite coveragePanel = toolkit.createComposite( panel, SWT.NONE );
    // REMEARK: no height hint needed: we never need more height than the toolbar on the left.
    coveragePanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).applyTo( coveragePanel );

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
    GridLayoutFactory.swtDefaults().applyTo( coverageInfoGroup );
    final GridData infoGroupData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    coverageInfoGroup.setLayoutData( infoGroupData );
    toolkit.adapt( coverageInfoGroup );
    coverageInfoGroup.setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.3" ) ); //$NON-NLS-1$

    final CachedFeatureviewFactory featureviewFactory = new CachedFeatureviewFactory( new FeatureviewHelper() );
    featureviewFactory.addView( AddCoverageAction.class.getResource( m_featureTemplateGft ) );
    final FeatureComposite featureComposite = new FeatureComposite( null, null, featureviewFactory );
    featureComposite.setFormToolkit( toolkit );

    featureComposite.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        m_theme.postCommand( changeCommand, null );
        updateCoverageProperties();
        updateButtons();
      }

      @Override
      public void openFeatureRequested( final Feature feature, final IPropertyType pt )
      {
      }
    } );

    /* Color Map table */
    final Composite colormapPanel = toolkit.createComposite( panel, SWT.NONE );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).equalWidth( false ).applyTo( colormapPanel );

    final GridData colormapPanelData = new GridData( SWT.FILL, SWT.FILL, true, true );
    colormapPanelData.exclude = !m_showStyle;
    colormapPanel.setVisible( m_showStyle );
    colormapPanel.setLayoutData( colormapPanelData );

    /* Create the color map viewer. */
    m_colorMapViewer = new ColorMapViewer( colormapPanel, SWT.NONE, toolkit );
    m_colorMapViewer.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    /* Create the color map toolbar. */
    final ToolBar colormapToolbar = new ToolBar( colormapPanel, SWT.VERTICAL | SWT.FLAT );
    toolkit.adapt( colormapToolbar );
    colormapToolbar.setLayoutData( new GridData( SWT.CENTER, SWT.BEGINNING, false, true ) );

    /* Initialize the coverage viewer. */
    initalizeCoverageViewer( m_coverageViewer );

    /* Initialize the coverage toolbar. */
    initalizeCoverageActions( new ToolBarManager( coverageToolbar ) );

    /* Initialize the color map viewer. */
    updateStylePanel();

    /* Initialize the color map toolbar. */
    initalizeColorMapActions( new ToolBarManager( colormapToolbar ) );

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

    final IFeatureBindingCollection<ICoverage> coverages = m_coverages == null ? null : m_coverages.getCoverages();
    if( coverages != null && coverages.size() > 0 )
      m_coverageViewer.setSelection( new StructuredSelection( coverages.get( 0 ) ) );

    final Point size = panel.computeSize( SWT.DEFAULT, SWT.DEFAULT );
    panel.setSize( size );

    updateButtons();

    refreshThemeCombo();

    return panel;
  }

  protected void refreshControl( )
  {
    ViewerUtilities.refresh( m_coverageViewer, true );

    updateButtons();
  }

  protected void updateCoverageProperties( )
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
      @Override
      public String getText( final Object element )
      {
        final IKalypsoTheme theme = (IKalypsoTheme)element;
        return theme.getLabel();
      }
    } );

    refreshThemeCombo();
  }

  public void refreshThemeCombo( )
  {
    if( m_themeCombo == null || m_themeCombo.getControl().isDisposed() )
      return;

    final IKalypsoTheme[] themesForCombo = findThemesForCombo();

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

        if( themesForCombo.length > 0 )
          themeCombo.setSelection( new StructuredSelection( themesForCombo[0] ) );
      }
    } );

  }

  public IKalypsoFeatureTheme[] findThemesForCombo( )
  {
    final IMapPanel mapPanel = getMapPanel();
    final IMapModell mapModell = mapPanel == null ? null : mapPanel.getMapModell();
    final IKalypsoTheme activeTheme = mapModell == null ? null : mapModell.getActiveTheme();

    final List<IKalypsoFeatureTheme> themesForCombo = new ArrayList<>();

    if( COVERAGE_PREDICATE.decide( activeTheme ) )
      themesForCombo.add( (IKalypsoFeatureTheme)activeTheme );
    else if( activeTheme instanceof IMapModell )
    {
      final IKalypsoTheme[] allThemes = ((IMapModell)activeTheme).getAllThemes();
      for( final IKalypsoTheme kalypsoTheme : allThemes )
      {
        if( COVERAGE_PREDICATE.decide( kalypsoTheme ) )
          themesForCombo.add( (IKalypsoFeatureTheme)kalypsoTheme );
      }
    }
    else
    {
      final IKalypsoTheme[] allThemes = mapModell.getAllThemes();
      for( final IKalypsoTheme kalypsoTheme : allThemes )
      {
        if( COVERAGE_PREDICATE.decide( kalypsoTheme ) )
          themesForCombo.add( (IKalypsoFeatureTheme)kalypsoTheme );
      }
    }
    return themesForCombo.toArray( new IKalypsoFeatureTheme[themesForCombo.size()] );
  }

  protected void handleThemeComboSelected( final SelectionChangedEvent event )
  {
    setCoverages( null, null );

    final IStructuredSelection selection = (IStructuredSelection)event.getSelection();
    final Object firstElement = selection.getFirstElement();

    if( firstElement instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme)firstElement;

      final String property = ft.getProperty( THEME_PROPERTY_ALLOW_USER_CHANGE_GRID_FOLDER, null );
      if( property != null )
        m_allowUserChangeDataFolder = Boolean.valueOf( property );

      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getOwner();
      if( coveragesFeature != null )
        setCoverages( (ICoverageCollection)coveragesFeature.getAdapter( ICoverageCollection.class ), ft );
    }

    updateButtons();
  }

  protected void handleListSelectionChanged( final Composite parent, final Group coverageInfoGroup, final FeatureComposite featureComposite, final SelectionChangedEvent event )
  {
    final IStructuredSelection selection = (IStructuredSelection)event.getSelection();
    m_selectedCoverage = (ICoverage)selection.getFirstElement();

    featureComposite.disposeControl();

    if( m_selectedCoverage != null )
    {
      featureComposite.setFeature( m_selectedCoverage );
      featureComposite.createControl( coverageInfoGroup, SWT.NONE );
      parent.layout( true, true );
    }

    repaintMap();

    updateButtons();
  }

  protected void updateButtons( )
  {
    /* Let actions update themselves */
    for( final IUpdateable action : m_actions )
    {
      SafeRunnable.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          action.update();
        }
      } );
    }
  }

  /**
   * sets the input of the style panel by fetching the style from the selected theme
   */
  public void updateStylePanel( )
  {
    if( m_colorMapViewer == null )
      return;

    if( m_theme == null )
      return;

    final IKalypsoFeatureTheme[] allCoverageThemes = findThemesForCombo();

    final CoverageColormapHandler colormapHandler = new CoverageColormapHandler( m_theme, allCoverageThemes );

    final RasterSymbolizer symb = colormapHandler.getRasterSymbolizer();
    if( symb != null )
      m_colorMapViewer.setInput( symb.getColorMap().values().toArray( new ColorMapEntry[] {} ) );
  }

  /**
   * initializes the button action for the style panel.
   */
  private void initalizeColorMapActions( final IToolBarManager manager )
  {
    addAction( manager, new CoverageColorRangeAction( this ) );

    manager.update( true );
  }

  private void initalizeCoverageActions( final IToolBarManager manager )
  {
    /* Add the default actions. */
    addDefaultActions( manager );

    /* Add actions registered by the extension point. */
    addExtensionActions( manager );

    /* Add the custom actions. */
    addCustomActions( manager );

    /* Update the toolbar manager. */
    manager.update( true );
  }

  private void addDefaultActions( final IToolBarManager manager )
  {
    if( m_showAddRemoveButtons )
    {
      addAction( manager, new AddCoverageAction( this ) );
      addAction( manager, new RemoveCoverageAction( this ) );
    }

    addAction( manager, new ExportCoverageAction( this ) );

    if( m_showAddRemoveButtons )
    {
      /* Changeing the order of grids only makes sense, if the user is allowed to add/remove them. */
      addAction( manager, new MoveCoverageUpAction( this ) );
      addAction( manager, new MoveCoverageDownAction( this ) );
    }

    addAction( manager, new JumpToCoverageAction( this ) );
  }

  private void addExtensionActions( final IToolBarManager manager )
  {
    try
    {
      final CoverageManagementAction[] extensionActions = KalypsoGmlUiExtensions.createCoverageManagementActions();
      for( final CoverageManagementAction extensionAction : extensionActions )
      {
        if( !CoverageManagementAction.ROLE_WIDGET.equals( extensionAction.getActionRole() ) )
          continue;

        /* Init the extension action. */
        extensionAction.init( getShell(), this );

        if( !extensionAction.isVisible() )
          continue;

        /* Get the action. */
        final IAction action = extensionAction.getAction();

        /* Add the action. */
        addAction( manager, action );
      }
    }
    catch( final CoreException ex )
    {
      ex.printStackTrace();
    }
  }

  private void addCustomActions( final IToolBarManager manager )
  {
    /* Should some custom action be added? */
    if( m_customActions == null )
      return;

    /* Add custom actions. */
    for( final IAction customAction : m_customActions )
      addAction( manager, customAction );
  }

  private void addAction( final IToolBarManager manager, final IAction action )
  {
    if( action instanceof IUpdateable )
      m_actions.add( (IUpdateable)action );

    manager.add( action );
  }

  public void handleCoverageMove( final int step )
  {
    if( m_selectedCoverage == null )
      return;

    final Feature parentFeature = m_coverages;
    final IPropertyType pt = parentFeature.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
    final Feature coverageFeature = m_selectedCoverage;

    final List< ? > featureList = (List< ? >)parentFeature.getProperty( pt );
    final int newIndex = featureList.indexOf( coverageFeature ) + step;
    if( newIndex < 0 || newIndex >= featureList.size() )
      return;

    final MoveFeatureCommand command = new MoveFeatureCommand( parentFeature, pt, coverageFeature, step );

    m_theme.postCommand( command, m_refreshCoverageViewerRunnable );
  }

  public IContainer findGridFolder( )
  {
    if( m_dataFolder != null )
      return m_dataFolder;

    if( m_theme == null )
      return null;

    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
      return null;

    final Feature parentFeature = featureList.getOwner();
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

  public void handleCoveragesAdded( final ICoverage[] newCoverages )
  {
    // set selection to new coverages
    final StructuredSelection selection = new StructuredSelection( newCoverages );
    m_coverageViewer.setSelection( selection );

    if( m_theme == null )
      return;

    final CoverageColormapHandler colormapHandler = new CoverageColormapHandler( m_theme, findThemesForCombo() );

    colormapHandler.guessInitialColormap( getShell(), getCoverages() );

    updateStylePanel();
  }

  private void initalizeCoverageViewer( final StructuredViewer viewer )
  {
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final ICoverage coverage = (ICoverage)element;
        return coverage.getName();
      }
    } );
    if( m_coverages != null )
      viewer.setInput( m_coverages.getCoverages() );
  }

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

  @Override
  public void mouseMoved( final MouseEvent e )
  {
    m_infoWidget.mouseMoved( e );
  }

  @Override
  public void paint( final Graphics g )
  {
    super.paint( g );

    if( m_selectedCoverage != null )
    {
      try
      {
        /* Paint bbox of selected coverage */
        final GM_Envelope boundingBox = m_selectedCoverage.getBoundedBy();

        final GM_Position minPoint = getMapPanel().getProjection().getDestPoint( boundingBox.getMin() );
        final GM_Position maxPoint = getMapPanel().getProjection().getDestPoint( boundingBox.getMax() );

        final int x = (int)Math.min( minPoint.getX(), maxPoint.getX() );
        final int y = (int)Math.min( minPoint.getY(), maxPoint.getY() );

        final int width = (int)Math.abs( minPoint.getX() - maxPoint.getX() );
        final int height = (int)Math.abs( minPoint.getY() - maxPoint.getY() );

        g.setColor( Color.RED );
        g.drawRect( x, y, width, height );
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

  /**
   * If add/remove buttons are shown (i.e. if the list of coverages is editable).
   */
  public boolean isShowAddRemoveButton( )
  {
    return m_showAddRemoveButtons;
  }

  public void setFeatureTemplateGft( final String featureTemplateGft )
  {
    m_featureTemplateGft = featureTemplateGft;
  }

  public void setGridFolder( final IFolder gridFolder )
  {
    m_dataFolder = gridFolder;
  }

  public void setAllowUserChangeGridFolder( final boolean allowUserChangeGridFolder )
  {
    m_allowUserChangeDataFolder = allowUserChangeGridFolder;
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

  @Override
  public String getPartName( )
  {
    return m_partName;
  }

  public ICoverage[] getCoverages( )
  {
    if( m_coverages == null )
      return new ICoverage[0];

    final IFeatureBindingCollection<ICoverage> coverages = m_coverages.getCoverages();
    if( coverages == null )
      return new ICoverage[0];

    return coverages.toArray( new ICoverage[coverages.size()] );
  }

  public ICoverage[] getSelectedCoverages( )
  {
    if( m_selectedCoverage == null )
      return new ICoverage[0];

    return new ICoverage[] { m_selectedCoverage };
  }

  public Shell getShell( )
  {
    return m_themeCombo.getControl().getShell();
  }

  public boolean isAllowUserChangeDataFolder( )
  {
    return m_allowUserChangeDataFolder;
  }

  public ICoverageCollection getCoverageCollection( )
  {
    return m_coverages;
  }

  public Runnable getRefreshRunnable( )
  {
    return m_refreshCoverageViewerRunnable;
  }
}