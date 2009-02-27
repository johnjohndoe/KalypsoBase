package de.openali.diagram.framework.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;

import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.diagram.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.diagram.framework.view.ChartComposite;

/**
 * @author burtscher
 * 
 * some helper methods to ease your everyday life programming chart stuff
 */
public class ChartUtilities
{
  private ChartUtilities( )
  {
    // not to be instanciated
  }

  /**
   * @return true if the screen coordinates should be inverted
   */
  public static boolean isInverseScreenCoords( final IAxis< ? > axis )
  {
    final ORIENTATION ori = axis.getPosition().getOrientation();
    final DIRECTION dir = axis.getDirection();

    return ori == ORIENTATION.VERTICAL && dir == DIRECTION.POSITIVE || ori == ORIENTATION.HORIZONTAL && dir == DIRECTION.NEGATIVE;
  }

  
  /**
   * sets the given GC to an initial state - this methosd should be called before any 
   * chart painting action is processed
   */
  public static void resetGC( final GC gc)
  {
	Device dev=gc.getDevice();
    gc.setForeground( dev.getSystemColor( SWT.COLOR_BLACK ) );
    gc.setBackground( dev.getSystemColor( SWT.COLOR_WHITE ) );
    gc.setLineWidth( 1 );
    gc.setLineStyle( SWT.LINE_SOLID );
    gc.setAlpha( 255 );
  }



}
