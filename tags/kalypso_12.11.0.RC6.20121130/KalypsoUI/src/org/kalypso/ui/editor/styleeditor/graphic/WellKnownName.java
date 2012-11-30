package org.kalypso.ui.editor.styleeditor.graphic;

/**
 * TODO: move into separate class, or Mark
 */
public enum WellKnownName
{
  square( "square" ), //$NON-NLS-1$
  circle( "circle" ), //$NON-NLS-1$
  triangle( "triangle" ), //$NON-NLS-1$
  star( "star" ), //$NON-NLS-1$
  cross( "cross" ), //$NON-NLS-1$
  kalypsoArrow( "kalypsoArrow" ), //$NON-NLS-1$
  x( "x" ); //$NON-NLS-1$

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