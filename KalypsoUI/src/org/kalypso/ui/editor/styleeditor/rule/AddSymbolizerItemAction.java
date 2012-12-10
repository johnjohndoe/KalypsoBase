package org.kalypso.ui.editor.styleeditor.rule;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ui.ImageProvider;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author Gernot Belger
 */
public final class AddSymbolizerItemAction extends Action
{
  private final SymbolizerTabList m_list;

  public AddSymbolizerItemAction( final SymbolizerTabList list )
  {
    m_list = list;

    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE );
    setToolTipText( "Add Symbolizer" );
  }

  /**
   * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final Event event )
  {
    final ToolItem item = (ToolItem) event.widget;
    final ToolBar control = item.getParent();

    final Shell shell = control.getShell();

    final IFeatureType featureType = m_list.getFeatureType();

    final Point initialLocation = control.toDisplay( new Point( event.x, event.y ) );

    final AddSymbolizerDialog dialog = new AddSymbolizerDialog( shell, featureType, initialLocation );
    dialog.open();

    final Symbolizer symbolizer = dialog.getResult();
    if( symbolizer == null )
      return;

    m_list.addSymbolizer( symbolizer );
  }
}