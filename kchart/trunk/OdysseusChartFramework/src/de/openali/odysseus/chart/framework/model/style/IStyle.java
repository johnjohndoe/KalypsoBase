package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;

public interface IStyle
{
  public void apply( GC gc );

  public void setVisible( boolean isVisible );

  public boolean isVisible( );

  public String getTitle( );

  public void setTitle( String title );

  public void dispose( );

  public void setAlpha( int alpha );

  public int getAlpha( );

  /**
   * method to store arbitrary data objects;
   */
  public void setData( String identifier, Object data );

  /**
   * get stored data objects
   */
  public Object getData( String identifier );

}
