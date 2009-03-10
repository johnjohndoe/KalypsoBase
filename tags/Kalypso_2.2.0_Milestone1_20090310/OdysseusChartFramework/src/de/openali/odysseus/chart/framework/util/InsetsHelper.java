package de.openali.odysseus.chart.framework.util;

import java.awt.Insets;

/**
 * @author burtscher extends AWT-Insets by some methods to mirror and transform inset-characteristics
 */
public class InsetsHelper extends Insets
{
  /**
   * 
   */
  private static final long serialVersionUID = 3705594929052083569L;

  public InsetsHelper( final Insets insets )
  {
    this( insets.top, insets.bottom, insets.left, insets.right );
  }

  public InsetsHelper( final int itop, final int ibottom, final int ileft, final int iright )
  {
    super( itop, ibottom, ileft, iright );
  }

  /** left <--> right */
  public InsetsHelper mirrorLeftRight( )
  {
    return new InsetsHelper( top, right, bottom, left );
  }

  /** top <--> bottom */
  public InsetsHelper mirrorTopBottom( )
  {
    return new InsetsHelper( bottom, left, top, right );
  }

  /** switch from horizontal to vertical representation */
  public InsetsHelper hor2vert( )
  {
    return new InsetsHelper( left, bottom, right, top );
  }

}
