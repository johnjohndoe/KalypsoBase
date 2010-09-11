package org.kalypso.model.wspm.ui.product;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPlaceholderFolderLayout;
import org.eclipse.ui.internal.PageLayout;
import org.kalypso.model.wspm.ui.view.LayerViewPart;
import org.kalypso.model.wspm.ui.view.legend.LegendViewPart;
import org.kalypso.model.wspm.ui.view.table.TableView;

/**
 * @author Gernot Belger
 */
public class ProfileditorPerspective implements IPerspectiveFactory
{
  public final static String ID = "org.kalypso.model.wspm.ui.product.ProfileditorPerspective"; //$NON-NLS-1$

  /**
   * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
   */
  @Override
  public void createInitialLayout( final IPageLayout layout )
  {
    layout.setEditorAreaVisible( true );

    final IFolderLayout leftFolder = layout.createFolder( "left", IPageLayout.LEFT, 0.4f, IPageLayout.ID_EDITOR_AREA ); //$NON-NLS-1$
    leftFolder.addView( LegendViewPart.class.getName() );
    layout.addView( LayerViewPart.class.getName(), IPageLayout.BOTTOM, 0.4f, "left" ); //$NON-NLS-1$

    final IPlaceholderFolderLayout topTable = layout.createPlaceholderFolder( "tableview", IPageLayout.TOP, 0.5f, IPageLayout.ID_EDITOR_AREA ); //$NON-NLS-1$
    topTable.addPlaceholder( TableView.class.getName() );
    topTable.addPlaceholder( TableView.class.getName() + ":*" ); //$NON-NLS-1$

    layout.addShowViewShortcut( LegendViewPart.class.getName() );
    layout.addShowViewShortcut( LayerViewPart.class.getName() );
    layout.addShowViewShortcut( TableView.class.getName() );
    layout.addShowViewShortcut( IPageLayout.ID_PROBLEM_VIEW );

    // a bit dirty, but this perspective should be minimalistic
    if( layout instanceof PageLayout )
      ((PageLayout) layout).getActionSets().clear();
  }
}
