package com.bce.util.progressbar;

/**
 * Interface für Klassen, die einen Fortschritt anzeigen können
 * 
 * @author belger
 */
public interface Progressable
{
  public void setNote( final String string );

  public void reset( final int min, final int max );

  public void setCurrent( final int current );

  boolean isCanceled( );

  public void cancel( );
}
