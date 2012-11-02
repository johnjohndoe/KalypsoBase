package org.kalypso.ui.editor.styleeditor.fill;

import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Fill;

/**
 * @author Gernot Belger
 */
final class GraphicFillRemoveAction extends StyleElementAction<Fill>
{
  public GraphicFillRemoveAction( final IStyleInput<Fill> input )
  {
    super( input );

    setText( Messages.getString( "GraphicFillRemoveAction_0" ) ); //$NON-NLS-1$
    setToolTipText( Messages.getString( "GraphicFillRemoveAction_1" ) ); //$NON-NLS-1$
    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_REMOVE );
  }

  @Override
  protected boolean checkEnabled( final Fill data )
  {
    return data.getGraphicFill() != null;
  }

  @Override
  protected void changeElement( final Fill data )
  {
    data.setGraphicFill( null );
  }
}