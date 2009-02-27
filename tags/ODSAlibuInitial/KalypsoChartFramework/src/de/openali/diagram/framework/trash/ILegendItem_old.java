package de.openali.diagram.framework.trash;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author burtscher
 * 
 * the LegendItem is part of the legend representing 1 layer;
 * it consists of an icon (describing the visual representation of the layer data),
 * a name and an description;
 * 
 */
public interface ILegendItem_old
{
  /**
   * @return the LegendItems title 
   */
  public String getTitle( );

  /**
   * @return the LegendItems description 
   */
  public String getDescription( );

  /**
   * paints the LegendItems icon into an image
   */
  public void drawIcon( Image img );

  /**
   * @return Point object whose x- and y-values  describe the drawing space 
   * needed by the item
   */
  public Point computeSize( int whint, int hhint );

  /**
   * paints the LegendItem into an image
   */
  public void paintImage( Image img );

}
