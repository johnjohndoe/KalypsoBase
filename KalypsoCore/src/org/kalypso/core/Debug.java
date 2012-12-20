package org.kalypso.core;

/**
 * This class is a helper for debugging.
 * 
 * @author Holger Albert
 */
public class Debug
{
  public static final org.kalypso.contribs.eclipse.core.runtime.Debug TOKEN_READER = new org.kalypso.contribs.eclipse.core.runtime.Debug( KalypsoCorePlugin.getDefault(), "/debug/token/reader" ); //$NON-NLS-1$

  /**
   * The constructor.
   */
  private Debug( )
  {
  }
}