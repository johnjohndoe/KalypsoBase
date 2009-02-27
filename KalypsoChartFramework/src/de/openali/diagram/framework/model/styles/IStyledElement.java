package de.openali.diagram.framework.model.styles;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

import de.openali.diagram.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher
 *
 * an ISTyledElement is a helper for drawing geometric primitives according to an
 * external configuration; there are several types of StyledElements: Points, Fonts,
 * Polygons, Lines
 */
public interface IStyledElement
{
  /**
   * sets the path used to draw the element; this can be used in different ways for the diverse
   * element types:
   * 1.) Point, Font:  the element is drawn at each point of the path
   * 2.) Line:  the path describes the line path
   * 3.) Polygon: the path describes the outline of the element
   */
  public void setPath( List<Point> path );

  /**
   * paints the element into the given GC
   */
  public void paint( GCWrapper gc);

  /**
   * @return type of the element (point, line, polygon, etc.)
   */
  public SE_TYPE getType( );


}
