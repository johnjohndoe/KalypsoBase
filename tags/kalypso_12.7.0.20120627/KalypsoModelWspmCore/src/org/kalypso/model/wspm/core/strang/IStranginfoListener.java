package org.kalypso.model.wspm.core.strang;

/**
 * @author gernot
 */
public interface IStranginfoListener
{
  /** Called, if someone tries to change the index, return false, if you don't want to */
  boolean onTryChangeIndex( final StrangInfo source );

  void onIndexChanged( final StrangInfo source );
}
