package de.openali.diagram.framework.model.styles;

/**
 * @author burtscher
 */
public interface IStyleConstants
{

  /**
   * different types of StyledElements 
   */
  public enum SE_TYPE
  {
    POINT,
    LINE,
    POLYGON,
    TEXT,
    DUMMY;
  }

  /**
   * different styles which can be used on line elements
   */
  public enum SE_LINESTYLE
  {
    SOLID,
    DOT,
    DASH,
    DASHDOT,
    DASHDOTDOT;
  }

}
