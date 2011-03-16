package de.openali.odysseus.chart.framework.util;

import java.util.List;

import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.figure.impl.IDefaultStyles;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.AreaStyle;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.PointStyle;
import de.openali.odysseus.chart.framework.model.style.impl.TextStyle;

/**
 * static helper methods to ease the handling of Style-concerns
 * 
 * @author burtscher
 */
public final class StyleUtils
{
  private StyleUtils( )
  {
  }

  /**
   * transforms an array of points into an array of alternating x- and y- values
   */
  public static int[] pointListToIntArray( final List<Point> path )
  {
    int[] newpath;
    if( path != null )
    {
      newpath = new int[path.size() * 2];
      for( int i = 0; i < path.size(); i++ )
      {
        final Point p = path.get( i );
        if( p != null )
        {
          newpath[2 * i] = p.x;
          newpath[2 * i + 1] = p.y;
        }
      }
    }
    else
      newpath = new int[0];
    return newpath;
  }

  @SuppressWarnings("unchecked")
  public static <T_style extends IStyle> T_style getDefaultStyle( final Class<T_style> clazz )
  {
    if( IPointStyle.class.isAssignableFrom( clazz ) )
      return (T_style) getDefaultPointStyle();
    else if( ILineStyle.class.isAssignableFrom( clazz ) )
      return (T_style) getDefaultLineStyle();
    else if( ITextStyle.class.isAssignableFrom( clazz ) )
      return (T_style) getDefaultTextStyle();
    else if( IAreaStyle.class.isAssignableFrom( clazz ) )
      return (T_style) getDefaultAreaStyle();
    else
      return null;
  }

  public static ILineStyle getDefaultLineStyle( )
  {
    return new LineStyle( IDefaultStyles.DEFAULT_STROKE_WIDTH, IDefaultStyles.DEFAULT_RGB_FOREGROUND, IDefaultStyles.DEFAULT_ALPHA, IDefaultStyles.DEFAULT_DASHOFFSET, IDefaultStyles.DEFAULT_DASHARRAY, IDefaultStyles.DEFAULT_LINEJOIN, IDefaultStyles.DEFAULT_LINECAP, IDefaultStyles.DEFAULT_MITERLIMIT, IDefaultStyles.DEFAULT_VISIBILITY );
  }

  public static IPointStyle getDefaultPointStyle( )
  {
    return new PointStyle( getDefaultLineStyle(), IDefaultStyles.DEFAULT_WIDTH, IDefaultStyles.DEFAULT_HEIGHT, IDefaultStyles.DEFAULT_ALPHA, IDefaultStyles.DEFAULT_RGB_BACKGROUND, IDefaultStyles.DEFAULT_FILL_VISIBILITY, IDefaultStyles.DEFAULT_MARKER, IDefaultStyles.DEFAULT_VISIBILITY );
  }

  public static IAreaStyle getDefaultAreaStyle( )
  {
    return new AreaStyle( IDefaultStyles.DEFAULT_FILL, IDefaultStyles.DEFAULT_ALPHA, getDefaultLineStyle(), IDefaultStyles.DEFAULT_VISIBILITY );
  }

  public static ITextStyle getDefaultTextStyle( )
  {
    return new TextStyle( IDefaultStyles.DEFAULT_FONT_HEIGHT, IDefaultStyles.DEFAULT_FONT_FAMILY, IDefaultStyles.DEFAULT_RGB_TEXT, IDefaultStyles.DEFAULT_RGB_TEXT_BACKGROUND, IDefaultStyles.DEFAULT_FONT_STYLE, IDefaultStyles.DEFAULT_FONT_WEIGHT, IDefaultStyles.DEFAULT_TEXT_ALIGNMENT, IDefaultStyles.DEFAULT_ALPHA, IDefaultStyles.DEFAULT_VISIBILITY );
  }

}
