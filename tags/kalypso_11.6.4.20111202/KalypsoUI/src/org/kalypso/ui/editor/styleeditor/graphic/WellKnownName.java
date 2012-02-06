package org.kalypso.ui.editor.styleeditor.graphic;

/**
 * TODO: move into separate class, or Mark
 */
public enum WellKnownName
{
  square("square"),
  circle("circle"),
  triangle("triangle"),
  star("star"),
  cross("cross"),
  kalypsoArrow("kalypsoArrow"),
  x("x");

  private final String m_label;

  private WellKnownName( final String label )
  {
    m_label = label;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString( )
  {
    return m_label;
  }
}