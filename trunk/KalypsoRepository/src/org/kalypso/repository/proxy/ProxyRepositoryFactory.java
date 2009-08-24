package org.kalypso.repository.proxy;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.KalypsoRepositoryPlugin;
import org.kalypso.repository.RepositoryException;
import org.kalypso.repository.factory.AbstractRepositoryFactory;
import org.kalypso.repository.factory.IRepositoryFactory;
import org.kalypso.repository.proxy.preferences.KalypsoRepositoryPreferencesHelper;

/**
 * WiskiRepositoryFactory
 * 
 * @author schlienger
 */
public class ProxyRepositoryFactory extends AbstractRepositoryFactory
{
  /**
   * @see org.kalypso.repository.factory.IRepositoryFactory#configureRepository()
   */
  public boolean configureRepository( ) throws RepositoryException
  {
    try
    {
      final IRepositoryFactory factory = KalypsoRepositoryPreferencesHelper.resolveFactory();

      return factory.configureRepository();
    }
    catch( final CoreException e )
    {
      KalypsoRepositoryPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return false;
  }

  /**
   * @see org.kalypso.repository.factory.IRepositoryFactory#createRepository()
   */
  public IRepository createRepository( ) throws RepositoryException
  {
    try
    {
      final IRepositoryFactory factory = KalypsoRepositoryPreferencesHelper.resolveFactory();

      return factory.createRepository();
    }
    catch( final CoreException e )
    {
      KalypsoRepositoryPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    return null;
  }
}
