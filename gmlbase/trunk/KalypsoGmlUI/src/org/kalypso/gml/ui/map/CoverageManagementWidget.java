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

import java.net.MalformedURLException;
import java.net.URL;

import ogc31.www.opengis.net.gml.RangeSetType;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.swt.custom.ScrolledCompositeCreator;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.wizard.grid.RectifiedGridCoverageImportWizard;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.map.MapPanel;
import org.kalypso.ogc.gml.map.widgets.AbstractWidget;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.mapmodel.IMapModellListener;
import org.kalypso.ogc.gml.mapmodel.MapModellAdapter;
import org.kalypso.ui.editor.gmleditor.util.command.MoveFeatureCommand;
import org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

import com.vividsolutions.jts.geom.Envelope;

/**
 * A widget with option pane, which allows the user to edit a coverage collection.
 * 
 * @author Thomas Jung
 */
public class CoverageManagementWidget extends AbstractWidget implements IWidgetWithOptions
{
  private ICoverageCollection m_coverages;

  private ICoverage m_selectedCoverage;

  private final IMapModellListener m_mapModelListener = new MapModellAdapter()
  {
    /**
     * @see org.kalypso.ogc.gml.mapmodel.MapModellAdapter#themeActivated(org.kalypso.ogc.gml.mapmodel.IMapModell,
     *      org.kalypso.ogc.gml.IKalypsoTheme, org.kalypso.ogc.gml.IKalypsoTheme)
     */
    @Override
    public void themeActivated( IMapModell source, IKalypsoTheme previouslyActive, IKalypsoTheme nowActive )
    {
      handleThemeActivated( nowActive );
    }
  };

  private ListViewer m_coverageViewer;

  public CoverageManagementWidget( )
  {
    super( "Höhenmodelle verwalten", "Höhenmodelle verwalten" );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( ICommandTarget commandPoster, MapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    /* Search for a coverage collectino */
    final IMapModell mapModell = mapPanel == null ? null : mapPanel.getMapModell();
    mapModell.addMapModelListener( m_mapModelListener );

    final IKalypsoTheme activeTheme = mapModell == null ? null : mapModell.getActiveTheme();
    handleThemeActivated( activeTheme );
  }

  protected void handleThemeActivated( IKalypsoTheme activeTheme )
  {
    setCoverages( null );

    if( activeTheme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme ft = (IKalypsoFeatureTheme) activeTheme;
      final FeatureList featureList = ft.getFeatureList();
      final Feature coveragesFeature = featureList == null ? null : featureList.getParentFeature();
      if( coveragesFeature != null )
      {
        setCoverages( (ICoverageCollection) coveragesFeature.getAdapter( ICoverageCollection.class ) );
      }
    }
  }

  private void setCoverages( final ICoverageCollection coverages )
  {
    m_coverages = coverages;
    if( m_coverageViewer != null && !m_coverageViewer.getControl().isDisposed() )
      m_coverageViewer.setInput( m_coverages );
  }

  /**
   * @see org.kalypso.ui.editor.mapeditor.views.IWidgetWithOptions#createControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final Composite panel = toolkit.createComposite( parent );

    panel.setLayout( new TableWrapLayout() );

    // Basic Layout

    /* Coverage table + info pane */
    final Composite coveragePanel = toolkit.createComposite( panel, SWT.NONE );
    final TableWrapLayout coveragePanelLayout = new TableWrapLayout();
    coveragePanel.setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB ) );
    coveragePanelLayout.numColumns = 2;
    coveragePanelLayout.rightMargin = 0;
    coveragePanelLayout.topMargin = 0;
    coveragePanelLayout.bottomMargin = 0;
    coveragePanelLayout.leftMargin = 0;
    coveragePanel.setLayout( coveragePanelLayout );

    final ScrolledCompositeCreator s = new ScrolledCompositeCreator( null )
    {
      @Override
      protected Control createContents( Composite parent, int style )
      {
        m_coverageViewer = new ListViewer( parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL );
        m_coverageViewer.getControl().setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL ) );
// toolkit.adapt( m_coverageViewer.getControl(), true, true );
        return m_coverageViewer.getControl();
      }
    };

    final Composite coverageButtonPanel = toolkit.createComposite( coveragePanel );
    final FillLayout coverageButtonPanelLayout = new FillLayout( SWT.VERTICAL );
    coverageButtonPanelLayout.spacing = 4;
    coverageButtonPanel.setLayout( coverageButtonPanelLayout );
// coverageButtonPanel.setLayout( new ColumnLayout() );
    coverageButtonPanel.setLayoutData( new TableWrapData( TableWrapData.FILL, TableWrapData.FILL_GRAB ) );

    final Group coverageInfoGroup = new Group( panel, SWT.NONE );
    coverageInfoGroup.setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB ) );
    toolkit.adapt( coverageInfoGroup );
    coverageInfoGroup.setText( "Info" );

    /* Color Map table */
    final Composite createColorMapPanel = toolkit.createComposite( panel, SWT.BORDER );
    createColorMapPanel.setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB ) );

    final Composite colormapPanel = toolkit.createComposite( panel, SWT.BORDER );
    final TableWrapLayout colormapPanelLayout = new TableWrapLayout();
    colormapPanelLayout.numColumns = 2;
    colormapPanelLayout.makeColumnsEqualWidth = false;
    colormapPanel.setLayout( colormapPanelLayout );
    colormapPanel.setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB ) );

    final TableViewer colorMapTableViewer = new TableViewer( colormapPanel, SWT.BORDER );
    colorMapTableViewer.getControl().setLayoutData( new TableWrapData( TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB ) );
    toolkit.adapt( colorMapTableViewer.getControl(), true, true );

    final Composite colormapPanelButtonPanel = toolkit.createComposite( colormapPanel, SWT.BORDER );
    // Fill contents
    initalizeCoverageViewer( m_coverageViewer );
    initalizeCoverageActions( toolkit, coverageButtonPanel );

    // Hook Events

    m_coverageViewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      public void selectionChanged( SelectionChangedEvent event )
      {
        handleCoverageSelected( (IStructuredSelection) event.getSelection() );
      }
    } );

    return panel;
  }

  private void initalizeCoverageActions( FormToolkit toolkit, Composite parent )
  {
    final ImageDescriptor addID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_ADD );
    final ImageDescriptor removeID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_REMOVE );
    final ImageDescriptor upID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_UP );
    final ImageDescriptor downID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_DOWN );
    final ImageDescriptor jumptoID = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_JUMP );

    createButton( toolkit, parent, new Action( "Add Coverage", addID )
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( Event event )
      {
        handleCoverageAdded( event );
      }
    } );

    createButton( toolkit, parent, new Action( "Remove Coverage", removeID )
    {
      /**
       * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
       */
      @Override
      public void runWithEvent( Event event )
      {
        handleCoverageRemoved( event );
      }
    } );

    createButton( toolkit, parent, new Action( "Move Coverage Up", upID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageMove( -1 );
      }
    } );

    createButton( toolkit, parent, new Action( "Move Coverage Down", downID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageMove( 1 );
      }
    } );

    createButton( toolkit, parent, new Action( "Jump To Coverage", jumptoID )
    {
      /**
       * @see org.eclipse.jface.action.Action#run()
       */
      @Override
      public void run( )
      {
        handleCoverageJumpTo();
      }
    } );
  }

  private void createButton( final FormToolkit toolkit, final Composite parent, final IAction action )
  {
    final Button button = toolkit.createButton( parent, null, SWT.PUSH );
    final Image image = action.getImageDescriptor().createImage( true );
    button.setImage( image );
    button.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        action.run();
      }
    } );

    button.addDisposeListener( new DisposeListener()
    {
      public void widgetDisposed( final DisposeEvent e )
      {
        image.dispose();
      }
    } );
  }

  protected void handleCoverageJumpTo( )
  {
    try
    {
      final IGeoGrid grid = getGrid();
      final Envelope boundingBox = grid.getBoundingBox();
      final GM_Envelope bbox = JTSAdapter.wrap( boundingBox );
      final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( bbox, 1.05 );
      getMapPanel().setBoundingBox( scaledBox );
    }
    catch( GeoGridException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private IGeoGrid getGrid( )
  {
    try
    {
      if( m_selectedCoverage == null )
        return null;

      return GeoGridUtilities.toGrid( m_selectedCoverage );
    }
    catch( Exception e )
    {
      e.printStackTrace();
    }

    return null;
  }

  protected void handleCoverageMove( final int step )
  {
    final Feature parentFeature = m_coverages.getWrappedFeature();
    final IPropertyType pt = parentFeature.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
    final Feature coverageFeature = m_selectedCoverage.getWrappedFeature();
    MoveFeatureCommand command = new MoveFeatureCommand( parentFeature, pt, coverageFeature, step );

    final IKalypsoTheme activeTheme = getMapPanel().getMapModell().getActiveTheme();
    final IKalypsoFeatureTheme theme = (IKalypsoFeatureTheme) activeTheme;
    theme.postCommand( command, null );

    m_coverageViewer.refresh();
  }

  protected void handleCoverageAdded( Event event )
  {
    final RectifiedGridCoverageImportWizard wizard = new RectifiedGridCoverageImportWizard();
    wizard.setCoverageCollection( m_coverages );
    wizard.init( PlatformUI.getWorkbench(), null );

    final WizardDialog wizardDialog = new WizardDialog( event.display.getActiveShell(), wizard );
    wizardDialog.open();
  }

  protected void handleCoverageRemoved( Event event )
  {
    try
    {
      final IKalypsoFeatureTheme theme = (IKalypsoFeatureTheme) getMapPanel().getMapModell().getActiveTheme();
      final CommandableWorkspace workspace = theme.getWorkspace();

      /* Delete underlying grid grid file */
      final RectifiedGridCoverage coverage = (RectifiedGridCoverage) m_selectedCoverage;
      final RangeSetType rangeSet = coverage.getRangeSet();
      final String fileName = rangeSet.getFile().getFileName();
      final URL url = new URL( workspace.getContext(), fileName );
      // TODO : make file from url (see binaryGeoGrid)
      // TODO: delete file
      // TODO: but into helper class

      /* Delete coverage from collection */
      final Feature parentFeature = m_coverages.getWrappedFeature();
      final IRelationType pt = (IRelationType) parentFeature.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
      final Feature coverageFeature = m_selectedCoverage.getWrappedFeature();

      final DeleteFeatureCommand command = new DeleteFeatureCommand( workspace, parentFeature, pt, coverageFeature );
      theme.postCommand( command, null );
    }
    catch( MalformedURLException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  protected void handleCoverageSelected( IStructuredSelection selection )
  {
    m_selectedCoverage = (ICoverage) selection.getFirstElement();

    updateInfoBox();
  }

  private void updateInfoBox( )
  {
    // TODO Auto-generated method stub
  }

  private void initalizeCoverageViewer( StructuredViewer viewer )
  {
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( Object element )
      {
        RectifiedGridCoverage coverage = (RectifiedGridCoverage) element;
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
  }

}
