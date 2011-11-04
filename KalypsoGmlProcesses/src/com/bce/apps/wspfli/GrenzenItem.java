package com.bce.apps.wspfli;

/**
 * Item f�r die GrenzenCombo
 * 
 * @author belger
 */
public class GrenzenItem
{
  public final String name;

  public final double[] grenzen;

  @SuppressWarnings("hiding")
  public GrenzenItem( final String name, final double[] grenzen )
  {
    this.name = name;
    this.grenzen = grenzen;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return name;
  }
}
