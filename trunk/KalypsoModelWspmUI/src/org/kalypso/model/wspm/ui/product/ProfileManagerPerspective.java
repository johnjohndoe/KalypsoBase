package org.kalypso.model.wspm.ui.product;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.kalypso.model.wspm.ui.view.LayerViewPart;
import org.kalypso.model.wspm.ui.view.chart.TuhhProfilChartView;
import org.kalypso.model.wspm.ui.view.legend.LegendViewPart;
import org.kalypso.model.wspm.ui.view.table.TableView;

/**
 * Perspective for editing profiles. Used, when profile are selected as features in a feature based editor/view.
 * 
 * @author Gernot Belger
 */
public class ProfileManagerPerspective implements IPerspectiveFactory
{
  public final static String ID = "org.kalypso.model.wspm.ui.product.ProfileManagerPerspective"; //$NON-NLS-1$

  /**
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
   */
  @Override
  public void createInitialLayout( final IPageLayout layout )
  {
    /* Layout */
    layout.setEditorAreaVisible( true );

    layout.addView( TableView.class.getName(), IPageLayout.RIGHT, 0.5f, IPageLayout.ID_EDITOR_AREA );
    layout.addView( TuhhProfilChartView.ID, IPageLayout.BOTTOM, 0.66f, TableView.class.getName() );
    layout.addPlaceholder( IPageLayout.ID_PROBLEM_VIEW, IPageLayout.TOP, 0.25f, TableView.class.getName() );

    layout.addView( LegendViewPart.class.getName(), IPageLayout.BOTTOM, 0.66f, IPageLayout.ID_EDITOR_AREA );
    layout.addView( LayerViewPart.class.getName(), IPageLayout.RIGHT, 0.5f, LegendViewPart.class.getName() );

    layout.addPlaceholder( IPageLayout.ID_RES_NAV, IPageLayout.LEFT, 0.25f, IPageLayout.ID_EDITOR_AREA );

    /* Shortcuts */
    layout.addPerspectiveShortcut( WspmPerspectiveFactory.ID );

    layout.addShowViewShortcut( LegendViewPart.class.getName() );
    layout.addShowViewShortcut( LayerViewPart.class.getName() );
    layout.addShowViewShortcut( TableView.class.getName() );
    layout.addShowViewShortcut( TuhhProfilChartView.ID );
    layout.addShowViewShortcut( IPageLayout.ID_PROBLEM_VIEW );
  }
}
