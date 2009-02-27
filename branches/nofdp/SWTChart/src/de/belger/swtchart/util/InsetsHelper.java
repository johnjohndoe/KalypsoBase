package de.belger.swtchart.util;

import java.awt.Insets;

/**
 * @author gernot
 */
public class InsetsHelper extends Insets
{
  public InsetsHelper( final Insets insets )
  {
    this( insets.top, insets.left, insets.bottom, insets.right );
  }

  public InsetsHelper( final int itop, final int ileft, final int ibottom, final int iright )
  {
    super( itop, ileft, ibottom, iright );
  }
  
  public InsetsHelper invert(  )
  {
    return new InsetsHelper( top, right, bottom, left );
  }
  
  public InsetsHelper opposite( )
  {
    return new InsetsHelper( bottom, left, top, right );
  }
  
  public InsetsHelper hor2vert( )
  {
    return new InsetsHelper( left, bottom, right, top );
  }

}
