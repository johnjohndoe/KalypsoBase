package org.kalypso.contribs.eclipse.ui.browser;

public interface ILocationChangedHandler
{
  /**
   * Handle the change to this location.
   * 
   * @param return
   *          true, if the brwoser should handle the href himself.
   */
  public boolean handleLocationChange( final String href );
}
