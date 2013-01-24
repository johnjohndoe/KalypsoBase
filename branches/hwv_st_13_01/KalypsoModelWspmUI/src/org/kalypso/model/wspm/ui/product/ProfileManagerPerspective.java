package org.kalypso.model.wspm.ui.product;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;
import org.kalypso.featureview.views.FeatureView;
import org.kalypso.model.wspm.ui.view.LayerViewPart;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartViewPart;
import org.kalypso.model.wspm.ui.view.legend.LegendViewPart;
import org.kalypso.model.wspm.ui.view.table.TableView;
import org.kalypso.ui.createGisMapView.CreateGisMapViewWizard;
import org.kalypso.ui.wizard.NewGMLFileWizard;

/**
 * Perspective for editing profiles. Used, when profile are selected as features in a feature based editor/view.
 * 
 * @author Gernot Belger
 */
public class ProfileManagerPerspective implements IPerspectiveFactory
{
  public final static String ID = "org.kalypso.model.wspm.ui.product.ProfileManagerPerspective"; //$NON-NLS-1$

  public final static String OUTLINE_FOLDER_ID = ProfileManagerPerspective.class.getName() + ".outlineFolder"; //$NON-NLS-1$

  public final static String TABLE_FOLDER_ID = ProfileManagerPerspective.class.getName() + ".tableFolder"; //$NON-NLS-1$

  /**
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
   */
  @Override
  public void createInitialLayout( final IPageLayout layout )
  {
    defineActions( layout );
    defineLayout( layout );
  }

  /**
   * Defines the initial actions for a page.
   * 
   * @param layout
   *          The layout we are filling
   */
  private void defineActions( final IPageLayout layout )
  {
    // Add "new wizards".
    layout.addNewWizardShortcut( BasicNewFolderResourceWizard.WIZARD_ID );
    layout.addNewWizardShortcut( BasicNewFileResourceWizard.WIZARD_ID );
    layout.addNewWizardShortcut( NewGMLFileWizard.WIZARD_ID );
    layout.addNewWizardShortcut( CreateGisMapViewWizard.WIZARD_ID );

    /* Shortcuts */
    layout.addPerspectiveShortcut( WspmPerspectiveFactory.ID );

    layout.addShowViewShortcut( IPageLayout.ID_PROJECT_EXPLORER );
    layout.addShowViewShortcut( IPageLayout.ID_OUTLINE );

    layout.addShowViewShortcut( LegendViewPart.class.getName() );
    layout.addShowViewShortcut( LayerViewPart.class.getName() );
    layout.addShowViewShortcut( TableView.class.getName() );
    layout.addShowViewShortcut( ProfilChartViewPart.ID );
    layout.addShowViewShortcut( IPageLayout.ID_PROBLEM_VIEW );
    layout.addShowViewShortcut( FeatureView.ID );
  }

  /**
   * Defines the initial layout for a page.
   * 
   * @param layout
   *          The layout we are filling
   */
  private void defineLayout( final IPageLayout layout )
  {
    layout.setEditorAreaVisible( true );

    final IPlaceholderFolderLayout topLeft = layout.createPlaceholderFolder( "topLeft", IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA ); //$NON-NLS-1$
    topLeft.addPlaceholder( IPageLayout.ID_PROJECT_EXPLORER );
    topLeft.addPlaceholder( IPageLayout.ID_RES_NAV );

    final IFolderLayout tableFolder = layout.createFolder( TABLE_FOLDER_ID, IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA );
    tableFolder.addView( TableView.class.getName() );
    tableFolder.addPlaceholder( FeatureView.ID );

    layout.addView( ProfilChartViewPart.ID, IPageLayout.BOTTOM, 0.66f, TableView.class.getName() );
    layout.addPlaceholder( IPageLayout.ID_PROBLEM_VIEW, IPageLayout.TOP, 0.25f, TableView.class.getName() );

    final IFolderLayout outlineFolder = layout.createFolder( OUTLINE_FOLDER_ID, IPageLayout.BOTTOM, 0.66f, IPageLayout.ID_EDITOR_AREA );
    outlineFolder.addView( LegendViewPart.class.getName() );
    outlineFolder.addPlaceholder( IPageLayout.ID_OUTLINE );

    layout.addView( LayerViewPart.class.getName(), IPageLayout.RIGHT, 0.5f, LegendViewPart.class.getName() );

  }

}
