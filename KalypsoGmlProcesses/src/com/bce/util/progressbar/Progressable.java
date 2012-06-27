package com.bce.util.progressbar;

/**
 * Interface f�r Klassen, die einen Fortschritt anzeigen k�nnen
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
