package org.kalypso.chart.framework.util;

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
    this( insets.top, insets.left, insets.bottom, insets.right );
  }

  public InsetsHelper( final int itop, final int ileft, final int ibottom, final int iright )
  {
    super( itop, ileft, ibottom, iright );
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
