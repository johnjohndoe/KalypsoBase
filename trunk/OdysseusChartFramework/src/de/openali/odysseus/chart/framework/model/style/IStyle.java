package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.GC;

public interface IStyle
{
  void apply( GC gc );

  void setVisible( boolean isVisible );

  boolean isVisible( );

  String getTitle( );

  void setTitle( String title );

  void setAlpha( int alpha );

  int getAlpha( );

  IStyle copy( );

  /**
   * method to store arbitrary data objects;
   */
  void setData( String identifier, Object data );

  /**
   * get stored data objects
   */
  Object getData( String identifier );

}
