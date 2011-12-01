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
package org.kalypso.repository;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.repository.utils.RepositoryVisitors;

/**
 * Abstract implementation of <code>IRepository</code> to provide basic functionality.
 * 
 * @author schlienger
 */
public abstract class AbstractRepository implements IRepository
{
  private final String m_name;

  private final String m_factory;

  private boolean m_readOnly;

  private final List<IRepositoryListener> m_listeners;

  private final Properties m_properties;

  private final String m_conf;

  private final String m_identifier;

  private final String m_label;

  private final boolean m_cached;

  public AbstractRepository( final String name, final String label, final String factory, final String conf, final boolean readOnly, final boolean cached, final String identifier )
  {
    m_name = name;
    m_label = label;
    m_factory = factory;
    m_conf = conf;
    m_readOnly = readOnly;
    m_cached = cached;
    m_identifier = identifier;

    m_listeners = new Vector<IRepositoryListener>();
    m_properties = new Properties();
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#accept(org.kalypso.repository.IRepositoryItemVisitor)
   */
  @Override
  public final void accept( final IRepositoryItemVisitor visitor ) throws RepositoryException
  {
    RepositoryVisitors.accept( this, visitor );
  }

  /**
   * @see org.kalypso.repository.IRepository#dispose()
   */
  @Override
  public void dispose( )
  {
    m_listeners.clear();
    m_properties.clear();
  }

  @Override
  public String getFactory( )
  {
    return m_factory;
  }

  @Override
  public String getConfiguration( )
  {
    return m_conf;
  }

  @Override
  public boolean isReadOnly( )
  {
    return m_readOnly;
  }

  /**
   * @see org.kalypso.repository.IRepository#isCached()
   */
  @Override
  public boolean isCached( )
  {
    return m_cached;
  }

  public void setReadOnly( final boolean ro )
  {
    m_readOnly = ro;
  }

  /**
   * @see org.kalypso.repository.IRepository#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return ""; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.repository.IRepository#addRepositoryListener(org.kalypso.repository.IRepositoryListener)
   */
  @Override
  public void addRepositoryListener( final IRepositoryListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.repository.IRepository#fireRepositoryStructureChanged()
   */
  @Override
  public void fireRepositoryStructureChanged( )
  {
    for( final Object element2 : m_listeners )
    {
      final IRepositoryListener element = (IRepositoryListener) element2;
      element.onRepositoryStructureChanged();
    }
  }

  /**
   * @see org.kalypso.repository.IRepository#removeRepositoryListener(org.kalypso.repository.IRepositoryListener)
   */
  @Override
  public void removeRepositoryListener( final IRepositoryListener l )
  {
    m_listeners.remove( l );
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getName()
   */
  @Override
  public String getName( )
  {
    return m_name;
  }

  /**
   * @see org.kalypso.repository.IRepository#getLabel()
   */
  @Override
  public String getLabel( )
  {
    if( m_label == null )
      return m_name;

    return m_label;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getParent()
   */
  @Override
  public IRepositoryItem getParent( )
  {
    return null;
  }

  /**
   * This default implementation uses recursion to find an item with the requested id. Subclasses may use this method if
   * they want to implement findItem using recursion.
   * 
   * @return item if found, else null
   */
  protected final IRepositoryItem findItemRecursive( final IRepositoryItem item, final String id ) throws RepositoryException
  {
    if( item.getIdentifier().equalsIgnoreCase( id ) )
      return item;

    final IRepositoryItem[] items = item.getChildren();
    if( items == null )
      return null;

    for( final IRepositoryItem item3 : items )
    {
      final IRepositoryItem item2 = findItemRecursive( item3, id );

      if( item2 != null )
        return item2;
    }

    return null;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    final String desc = getDescription();
    if( desc != null && desc.length() > 0 )
      return getLabel() + " (" + desc + ")"; //$NON-NLS-1$ //$NON-NLS-2$

    return getLabel();
  }

  /**
   * This default implementation always returns null.
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class anotherClass )
  {
    return null;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#hasAdapter(java.lang.Class)
   */
  @Override
  public boolean hasAdapter( final Class< ? > adapter )
  {
    return false;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#getRepository()
   */
  @Override
  public IRepository getRepository( )
  {
    return this;
  }

  /**
   * @see org.kalypso.repository.IRepository#dumpStructure(java.io.Writer, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void dumpStructure( final Writer writer, final IProgressMonitor monitor ) throws RepositoryException, InterruptedException
  {
    dumpRecursive( writer, this, "", monitor ); //$NON-NLS-1$
  }

  /**
   * Dumps the contents of this item and all its children using recursion
   */
  private static void dumpRecursive( final Writer writer, final IRepositoryItem item, final String indent, final IProgressMonitor monitor ) throws RepositoryException, InterruptedException
  {
    if( monitor.isCanceled() )
      throw new InterruptedException();

    if( item == null )
      return;

    monitor.subTask( item.getIdentifier() );

    try
    {
      // let's look if the item can be adapted to properties. In the positive,
      // we dump the properties too.
      final Properties props = (Properties) item.getAdapter( Properties.class );
      if( props != null )
        writer.write( indent + item.toString() + " Properties: " + PropertiesHelper.format( props, ';' ) ); //$NON-NLS-1$
      else
        writer.write( indent + item.toString() );

      writer.write( "\n" ); //$NON-NLS-1$
    }
    catch( final IOException e )
    {
      throw new RepositoryException( e );
    }

    final String recIndent = indent + "\t"; //$NON-NLS-1$

    final IRepositoryItem[] items = item.getChildren();
    if( items == null )
      return;

    for( final IRepositoryItem item2 : items )
      dumpRecursive( writer, item2, recIndent, monitor );

    monitor.worked( 1 );
  }

  /**
   * @see org.kalypso.repository.IRepository#getProperty(java.lang.String, java.lang.String)
   */
  @Override
  public String getProperty( final String name, final String defaultValue )
  {
    return m_properties.getProperty( name, defaultValue );
  }

  /**
   * @see org.kalypso.repository.IRepository#getProperty(java.lang.String)
   */
  @Override
  public String getProperty( final String name )
  {
    return m_properties.getProperty( name );
  }

  /**
   * @see org.kalypso.repository.IRepository#getProperties()
   */
  @Override
  public Properties getProperties( )
  {
    return m_properties;
  }

  /**
   * @see org.kalypso.repository.IRepository#setProperties(java.util.Properties)
   */
  @Override
  public void setProperties( final Properties props )
  {
    m_properties.clear();
    m_properties.putAll( props );
  }

  /**
   * @see org.kalypso.repository.IRepository#setProperty(java.lang.String, java.lang.String)
   */
  @Override
  public void setProperty( final String name, final String value )
  {
    m_properties.setProperty( name, value );
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  /**
   * @see org.kalypso.repository.IRepositoryItem#isMultipleSourceItem()
   */
  @Override
  public boolean isMultipleSourceItem( )
  {
    return false;
  }
}