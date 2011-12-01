package org.kalypso.ui.editor.styleeditor.fill;

import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.graphic.WellKnownName;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.GraphicFill;
import org.kalypsodeegree.graphics.sld.Mark;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
final class GraphicFillAddAction extends StyleElementAction<Fill>
{
  public GraphicFillAddAction( final IStyleInput<Fill> input )
  {
    super( input );

    setText( "Create Graphic" );
    setImageDescriptor( ImageProvider.IMAGE_STYLEEDITOR_ADD_RULE );
    setToolTipText( "Create graphics element" );
  }

  @Override
  protected boolean checkEnabled( final Fill data )
  {
    return data.getGraphicFill() == null;
  }

  @Override
  protected void changeElement( final Fill data )
  {
    final Mark circleMark = StyleFactory.createMark( WellKnownName.circle.name() );
    final Graphic graphic = StyleFactory.createGraphic( null, circleMark, 1.0, 10, 0 );
    final GraphicFill graphicFill = StyleFactory.createGraphicFill( graphic );
    data.setGraphicFill( graphicFill );
  }
}