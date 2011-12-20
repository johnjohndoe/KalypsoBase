package org.kalypso.afgui.perspective;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.kalypso.afgui.views.WorkflowView;
import org.kalypso.featureview.views.FeatureView;
import org.kalypso.ogc.gml.outline.ViewContentOutline;
import org.kalypso.ui.editor.featureeditor.FeatureTemplateView;
import org.kalypso.ui.editor.mapeditor.views.MapWidgetView;
import org.kalypso.ui.repository.view.RepositoryExplorerPart;
import org.kalypso.ui.views.map.MapView;

public class Perspective implements IPerspectiveFactory
{
  // REMARK: still using kalypso1d2d.pjt as namespace in order to ensure backwards compatibility.
  // If this should ever be changed, make sure all workflow.xml's got updated as well.
  public final static String ID = "org.kalypso.kalypso1d2d.pjt.perspective.Perspective"; //$NON-NLS-1$

  // REMARK: see #ID
  public final static String SCENARIO_VIEW_ID = "org.kalypso.kalypso1d2d.pjt.views.ScenarioView"; //$NON-NLS-1$

  public final static String TIMESERIES_REPOSITORY_VIEW_ID = "org.kalypso.kalypso1d2d.pjt.views.TimeseriesRepositoryView"; //$NON-NLS-1$

  @Override
  public void createInitialLayout( final IPageLayout layout )
  {
    // Get the editor area.
    final String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible( false );

    final IFolderLayout leftTop = layout.createFolder( "leftTop", IPageLayout.LEFT, 0.3f, editorArea ); //$NON-NLS-1$
    final IFolderLayout leftBottom = layout.createFolder( "leftBottom", IPageLayout.BOTTOM, 0.7f, "leftTop" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IFolderLayout rightTop = layout.createFolder( "rightTop", IPageLayout.RIGHT, 1.0f, editorArea ); //$NON-NLS-1$
    final IPlaceholderFolderLayout rightBottom = layout.createPlaceholderFolder( "rightBottom", IPageLayout.BOTTOM, 0.7f, "rightTop" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IPlaceholderFolderLayout veryRight = layout.createPlaceholderFolder( "veryRight", IPageLayout.RIGHT, 0.7f, "rightTop" ); //$NON-NLS-1$ //$NON-NLS-2$

    leftTop.addView( WorkflowView.ID );
    leftBottom.addView( SCENARIO_VIEW_ID );

    leftBottom.addPlaceholder( ViewContentOutline.ID );
    leftBottom.addPlaceholder( TIMESERIES_REPOSITORY_VIEW_ID );

    rightTop.addPlaceholder( MapView.ID );
    rightTop.addPlaceholder( FeatureTemplateView.ID );
    rightTop.addPlaceholder( "org.kalypso.zml.ui.chart.view.DiagramViewPart" );//$NON-NLS-1$
    rightTop.addPlaceholder( "org.kalypso.risk.views.RiskStatisticsResultView" ); //$NON-NLS-1$

    rightBottom.addPlaceholder( FeatureView.ID );
    rightBottom.addPlaceholder( "org.kalypso.zml.ui.table.view.TableViewPart" );//$NON-NLS-1$

    veryRight.addPlaceholder( MapWidgetView.ID );

    /* Moveability and closeability of the views. */
    layout.getViewLayout( FeatureView.ID ).setMoveable( true );
    layout.getViewLayout( FeatureTemplateView.ID ).setCloseable( false );
    layout.getViewLayout( FeatureTemplateView.ID ).setMoveable( false );
    layout.getViewLayout( MapWidgetView.ID ).setCloseable( false );
    layout.getViewLayout( MapWidgetView.ID ).setMoveable( false );
    layout.getViewLayout( WorkflowView.ID ).setCloseable( false );
    layout.getViewLayout( WorkflowView.ID ).setMoveable( false );
    layout.getViewLayout( SCENARIO_VIEW_ID ).setCloseable( false );
    layout.getViewLayout( SCENARIO_VIEW_ID ).setMoveable( false );
    layout.getViewLayout( RepositoryExplorerPart.ID ).setCloseable( false );
    layout.getViewLayout( RepositoryExplorerPart.ID ).setMoveable( false );
    layout.getViewLayout( "org.kalypso.zml.ui.chart.view.DiagramViewPart" ).setCloseable( false );//$NON-NLS-1$
    layout.getViewLayout( "org.kalypso.zml.ui.chart.view.DiagramViewPart" ).setMoveable( true );//$NON-NLS-1$
    layout.getViewLayout( "org.kalypso.zml.ui.table.view.TableViewPart" ).setCloseable( false );//$NON-NLS-1$
    layout.getViewLayout( "org.kalypso.zml.ui.table.view.TableViewPart" ).setMoveable( true );//$NON-NLS-1$

    // TODO: secondary id does not work here: gives assertion failed
    // layout.getViewLayout( MapView.ID + ":*").setCloseable( false );
    layout.getViewLayout( MapView.ID ).setCloseable( false );
    layout.getViewLayout( MapView.ID ).setMoveable( false );

    layout.getViewLayout( "org.kalypso.risk.views.RiskStatisticsResultView" ).setMoveable( false ); //$NON-NLS-1$
    layout.getViewLayout( "org.kalypso.risk.views.RiskStatisticsResultView" ).setCloseable( false ); //$NON-NLS-1$
  }
}