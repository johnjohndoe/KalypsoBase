package org.kalypso.ui.editor.styleeditor.stroke;

import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * @author Gernot Belger
 */
final class GraphicStrokeRemoveAction extends StyleElementAction<Stroke>
{
  public GraphicStrokeRemoveAction( final IStyleInput<Stroke> input )
  {
    super( input );

    setText( Messages.getString( "GraphicStrokeRemoveAction_0" ) ); //$NON-NLS-1$
    setToolTipText( Messages.getString( "GraphicStrokeRemoveAction_1" ) ); //$NON-NLS-1$
    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_REMOVE );
  }

  @Override
  protected boolean checkEnabled( final Stroke data )
  {
    return data.getGraphicStroke() != null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.StyleElementAction#changeElement(java.lang.Object)
   */
  @Override
  protected void changeElement( final Stroke data )
  {
    data.setGraphicStroke( null );
  }
}