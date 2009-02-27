package org.kalypso.chart.framework.model.styles.impl;

import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

/**
 * @author alibu Dummy-Implementierung für IStyledElement; wird vom StyleContainer zurückgegeben, falls ein nicht
 *         vorhandene StyledElement angefordert wird
 */
public class StyleDummy implements IStyledElement
{

  /**
   * gibt DUMMY als Type zurück
   */
  public SE_TYPE getType( )
  {
    // TODO Auto-generated method stub
    return SE_TYPE.DUMMY;
  }

  /**
   * schreibt eine Nachricht ins Log, in der auf die nicht korrekte Verwendung hingewiesen wird
   */
  public void paint( GCWrapper gc )
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
    Logger.logWarning( Logger.TOPIC_LOG_STYLE, "setData() not implemented: this is a dummy object" );
  }

  /**
   * @see org.kalypso.chart.framework.model.styles.IStyledElement#getData(java.lang.String)
   */
  public Object getData( String identifier )
  {
    // not implemented: this is a dummy
    Logger.logWarning( Logger.TOPIC_LOG_STYLE, "getData() not implemented: this is a dummy object" );
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
