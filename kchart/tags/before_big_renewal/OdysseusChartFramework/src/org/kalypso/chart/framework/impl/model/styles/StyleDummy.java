package org.kalypso.chart.framework.impl.model.styles;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.impl.logging.Logger;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.eclipse.swt.graphics.GC;

/**
 * @author alibu Dummy-Implementierung f�r IStyledElement; wird vom StyleContainer zur�ckgegeben, falls ein nicht
 *         vorhandene StyledElement angefordert wird
 */
public class StyleDummy implements IStyledElement
{

  /**
   * gibt DUMMY als Type zur�ck
   */
  public SE_TYPE getType( )
  {
    // TODO Auto-generated method stub
    return SE_TYPE.DUMMY;
  }

  /**
   * schreibt eine Nachricht ins Log, in der auf die nicht korrekte Verwendung hingewiesen wird
   */
  public void paint( GC gc )
  {
    Logger.logWarning( Logger.TOPIC_LOG_STYLE, "Trying to paint layer without correct style setting" );
  }

  /**
   * nicht implementiert
   */
  public void setPath( List<Point> path )
  {

  }

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data )
  {
    // not implemented: this is a dummy
  }

  /**
   * @see org.kalypso.chart.framework.model.styles.IStyledElement#getData(java.lang.String)
   */
  public Object getData( String identifier )
  {
    // not implemented: this is a dummy
    return null;
  }

  /**
   * returns a dynamically generated id which should be unique
   * 
   * @see org.kalypso.chart.framework.model.styles.IStyledElement#getId()
   */
  public String getId( )
  {
    return "dummy_" + System.currentTimeMillis() + Math.random() * 10000;
  }

}
