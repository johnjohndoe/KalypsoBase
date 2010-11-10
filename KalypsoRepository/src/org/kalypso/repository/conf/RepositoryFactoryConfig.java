/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.repository.conf;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.KalypsoRepository;
import org.kalypso.repository.RepositoriesExtensions;
import org.kalypso.repository.factory.IRepositoryFactory;

/**
 * Using such an object you can create the <code>IRepositoryFactory</code> for which it delivers the initial
 * configuration.
 * 
 * @author schlienger
 */
public class RepositoryFactoryConfig
{
  private final String m_name;

  private final String m_factory;

  private final String m_conf;

  private final boolean m_readOnly;

  private static final String SEPARATOR = ";"; //$NON-NLS-1$

  /** factory can be specified in constructor */
  private IRepositoryFactory m_rf = null;

  private final String m_label;

  private final boolean m_cached;

  /**
   * Constructor with:
   * 
   * @param name
   *          name of the repository
   * @param label
   *          display name of the repository
   * @param factory
   *          name of the IRepositoryFactory class
   * @param conf
   *          configuration used when instanciating
   * @param readOnly
   *          when true repository should be read only
   */
  protected RepositoryFactoryConfig( final String name, final String label, final String factory, final String conf, final boolean readOnly, final boolean cached, final IRepositoryFactory rf )
  {
    m_name = name;
    m_label = label;
    m_factory = factory;
    m_conf = conf;
    m_readOnly = readOnly;
    m_cached = cached;
    m_rf = rf;
  }

  /**
   * Constructor with repository
   */
  public RepositoryFactoryConfig( final IRepository rep )
  {
    this( rep.getName(), rep.getLabel(), rep.getFactory(), rep.getConfiguration(), rep.isReadOnly(), rep.isCached(), null );
  }

  /**
   * Shortcut constructor with factory. if createFactory() is called, it will return this given factory configured with
   * the given arguments.
   */
  public RepositoryFactoryConfig( final IRepositoryFactory rf, final String name, final String conf, final boolean readOnly, final boolean cached )
  {
    this( name, name, rf.getClass().getName(), conf, readOnly, cached, rf );
  }

  /**
   * Creates the underlying factory.
   */
  public IRepositoryFactory getFactory( ) throws CoreException
  {
    if( m_rf != null )
      return m_rf;

    final IRepositoryFactory rf = RepositoriesExtensions.retrieveFactoryFor( m_factory );
    if( rf == null )
    {
      final String msg = String.format( "Factory not found for repository: %s", m_factory ); //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, KalypsoRepository.PLUGIN_ID, msg ) );
    }

    rf.setReadOnly( m_readOnly );
    rf.setCached( m_cached );
    rf.setConfiguration( m_conf );
    rf.setRepositoryName( m_name );
    rf.setRepositoryLabel( m_label );

    return rf;
  }

  /**
   * Saves the state of this object in a simple string representation.
   * 
   * @return state
   */
  public String saveState( )
  {
    final StringBuffer bf = new StringBuffer();

    bf.append( m_name ).append( SEPARATOR );
    bf.append( m_label ).append( SEPARATOR );
    bf.append( m_factory ).append( SEPARATOR );
    bf.append( m_conf ).append( SEPARATOR );
    bf.append( String.valueOf( m_readOnly ) ).append( SEPARATOR );
    bf.append( String.valueOf( m_cached ) );

    return bf.toString();
  }

  /**
   * Restores a <code>RepositoryConfigItem</code> from the state provided as a string. This is the pendant to the
   * saveState() method.
   * 
   * @return a repository config item
   */
  public static RepositoryFactoryConfig restore( final String state )
  {
    final String[] splits = state.split( SEPARATOR );

    if( splits.length != 5 )
      return null;

    final String repositoryName = splits[0];
    final String repositoryLabel = splits[1];
    final String factoryClassName = splits[2];
    final String conf = splits[3];
    final boolean readOnly = Boolean.valueOf( splits[4] ).booleanValue();
    final boolean cached = Boolean.valueOf( splits[5] ).booleanValue();

    return new RepositoryFactoryConfig( repositoryName, repositoryLabel, factoryClassName, conf, readOnly, cached, null );
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    if( m_conf != null && m_conf.length() > 0 )
      return m_name + " (" + m_conf + ")"; //$NON-NLS-1$ //$NON-NLS-2$

    return m_name;
  }

  public String getName( )
  {
    return m_name;
  }

  public String getLabel( )
  {
    if( m_label == null )
      return m_name;

    return m_label;
  }
}