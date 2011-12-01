package org.kalypso.ui.editor.styleeditor.stroke;

import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.graphic.WellKnownName;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.GraphicStroke;
import org.kalypsodeegree.graphics.sld.Mark;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
final class GraphicStrokeAddAction extends StyleElementAction<Stroke>
{
  public GraphicStrokeAddAction( final IStyleInput<Stroke> input )
  {
    super( input );

    setText( "Create Graphic" );
    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE );
    setToolTipText( "Create graphics element" );
  }

  @Override
  protected boolean checkEnabled( final Stroke data )
  {
    return data.getGraphicStroke() == null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.StyleElementAction#changeElement(java.lang.Object)
   */
  @Override
  protected void changeElement( final Stroke data )
  {
    final Mark circleMark = StyleFactory.createMark( WellKnownName.circle.name() );
    final Graphic graphic = StyleFactory.createGraphic( null, circleMark, 1.0, 10, 0 );
    final GraphicStroke graphicStroke = StyleFactory.createGraphicStroke( graphic );
    data.setGraphicStroke( graphicStroke );
  }
}