package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;

public interface IStyle
{
  void apply( GC gc );

  IStyle copy( );

  int getAlpha( );

  /**
   * get stored data objects
   */
  Object getData( String identifier );

  String getTitle( );

  boolean isVisible( );

  void setAlpha( int alpha );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  void setTitle( String title );

  void setVisible( boolean isVisible );

}
