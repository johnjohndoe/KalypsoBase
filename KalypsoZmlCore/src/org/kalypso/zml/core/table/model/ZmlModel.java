/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.zml.core.table.model;

import java.net.URL;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.JAXBElement;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.zml.core.base.IZmlSourceElement;
import org.kalypso.zml.core.base.IndexedTsLink;
import org.kalypso.zml.core.debug.KalypsoZmlCoreDebug;
import org.kalypso.zml.core.table.model.event.IZmlModelColumnListener;
import org.kalypso.zml.core.table.model.event.ZmlModelColumnChangeType;
import org.kalypso.zml.core.table.model.loader.ZmlModelColumnLoader;
import org.kalypso.zml.core.table.model.loader.ZmlRowBuilder;
import org.kalypso.zml.core.table.model.memento.IZmlMemento;
import org.kalypso.zml.core.table.model.memento.ZmlMemento;
import org.kalypso.zml.core.table.model.utils.ZmlModelColumns;
import org.kalypso.zml.core.table.schema.AbstractColumnType;
import org.kalypso.zml.core.table.schema.ZmlTableType;

/**
 * @author Dirk Kuch
 */
public class ZmlModel implements IZmlModel, IZmlModelColumnListener
{
  private final IZmlMemento m_memento = new ZmlMemento();

  private final Map<Integer, IZmlModelColumn> m_columns = Collections.synchronizedMap( new TreeMap<Integer, IZmlModelColumn>() );

  protected final Set<IZmlColumnModelListener> m_listeners = Collections.synchronizedSet( new HashSet<IZmlColumnModelListener>() );

  private Map<Date, IZmlModelRow> m_rows = Collections.synchronizedMap( new TreeMap<Date, IZmlModelRow>() );

  private final ZmlTableType m_type;

  private final URL m_context;

  private final ZmlModelColumnLoader m_loader;

  private final Map<String, AbstractColumnType> m_columnTypeMap = new LinkedHashMap<String, AbstractColumnType>();

  private String[] m_ignoreTypes;

  public ZmlModel( final ZmlTableType type, final URL context )
  {
    m_type = type;
    m_context = context;
    m_loader = new ZmlModelColumnLoader( this );

    doInitColumnTypes();
  }

  /** trigger loading of .kod based columns from the outside. */
  public void loadInitialColumns( )
  {
    synchronized( this )
    {
      final ZmlModelInitializer initializer = new ZmlModelInitializer( this );
      initializer.execute( new NullProgressMonitor() );
    }
  }

  private void doInitColumnTypes( )
  {
    final List<JAXBElement< ? extends AbstractColumnType>> columnTypes = getTableType().getColumns().getAbstractColumn();
    for( final JAXBElement< ? extends AbstractColumnType> columnType : columnTypes )
    {
      final AbstractColumnType column = columnType.getValue();
      m_columnTypeMap.put( column.getId(), column );
    }
  }

  @Override
  public IZmlMemento getMemento( )
  {
    return m_memento;
  }

  @Override
  public void accept( final IZmlModelRowVisitor visitor )
  {
    synchronized( this )
    {
      if( m_rows.isEmpty() )
        getRows();
    }

    final Set<Entry<Date, IZmlModelRow>> entries = m_rows.entrySet();
    for( final Entry<Date, IZmlModelRow> entry : entries )
    {
      visitor.visit( entry.getValue() );
    }
  }

  @Override
  public void add( final IZmlSourceElement source, final IZmlModelColumn column )
  {
    synchronized( this )
    {
      int index;
      if( source instanceof IndexedTsLink )
        index = ((IndexedTsLink) source).getIndex();
      else
        index = findIndex( column );

      if( m_columns.containsKey( index ) )
      {
        final IZmlModelColumn obsolete = m_columns.get( index );
        obsolete.dispose();
        obsolete.removeListener( this );
      }

      column.addListener( this );

      m_columns.put( index, column );
    }

    fireModelChanged( new ZmlModelColumnChangeType( STRUCTURE_CHANGE ) );
  }

  private int findIndex( final IZmlModelColumn column )
  {
    int base = m_columns.size();

    /**
     * normally this will happen if a column is pre-loaded by .kot <DataSources> elements. To keep the order of the .kot
     * definition we have to guess the correct column index
     */
    if( ZmlModelColumns.isCloned( column ) )
    {
      base = ZmlModelColumns.getCloneIndex( column );
    }
    else
      base = 0;

    while( m_columns.containsKey( base ) )
      base++;

    return base;
  }

  @Override
  public void addListener( final IZmlColumnModelListener listener )
  {
    m_listeners.add( listener );
  }

  public void doClean( )
  {
    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlTableModel - doClean()-ing model" );
    m_loader.cancel();

    /** remove cloned columns */
    final Object[] entries = m_columns.entrySet().toArray();
    for( final Object objEntry : entries )
    {
      final Map.Entry<Integer, IZmlModelColumn> entry = (Entry<Integer, IZmlModelColumn>) objEntry;
      final IZmlModelColumn column = entry.getValue();
      column.dispose();

      if( ZmlModelColumns.isCloned( column ) )
      {
        column.removeListener( this );
        m_columns.remove( entry.getKey() );
      }
    }

    m_rows.clear();

    fireModelChanged( new ZmlModelColumnChangeType( STRUCTURE_CHANGE ) );
  }

  @Override
  public void dispose( )
  {
    m_memento.dispose();

    final ZmlModelColumn[] columns = m_columns.values().toArray( new ZmlModelColumn[] {} );
    m_columns.clear();

    for( final IZmlModelColumn column : columns )
    {
      column.removeListener( this );
      column.dispose();
    }

    m_rows.clear();
  }

  final Set<IZmlModelColumn> m_stack = new LinkedHashSet<IZmlModelColumn>();

  int m_stackEvent = 0;

  private Job m_fireModelChangedJob;

  private static final MutexRule MUTEX_FIRE_MODEL_CHANGED = new MutexRule( "mutex - fire zml model changed" );

  @Override
  public synchronized void fireModelChanged( final ZmlModelColumnChangeType event )
  {
    if( m_fireModelChangedJob != null )
      m_fireModelChangedJob.cancel();

// if( Arrays.isEmpty( columns ) )
// Collections.addAll( m_stack, getColumns() );
// else
// Collections.addAll( m_stack, columns );

    m_stackEvent |= event.getEvent();

    m_fireModelChangedJob = new Job( "firing zml model changes" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        if( monitor.isCanceled() )
          return Status.CANCEL_STATUS;

        final IZmlModelColumn[] changed = m_stack.toArray( new IZmlModelColumn[] {} );
        m_stack.clear();

        final int e = m_stackEvent;
        m_stackEvent = 0;

        final IZmlColumnModelListener[] listeners = m_listeners.toArray( new IZmlColumnModelListener[] {} );
        for( final IZmlColumnModelListener listener : listeners )
        {
          listener.modelChanged( new ZmlModelColumnChangeType( e ) );
        }

        return Status.OK_STATUS;
      }
    };

    m_fireModelChangedJob.setUser( false );
    m_fireModelChangedJob.setSystem( true );
    m_fireModelChangedJob.setRule( MUTEX_FIRE_MODEL_CHANGED );

    m_fireModelChangedJob.schedule( 150 );
  }

  @Override
  public IZmlModelColumn getColumn( final String id )
  {
    synchronized( this )
    {
      final IZmlModelColumn[] columns = m_columns.values().toArray( new IZmlModelColumn[] {} );
      for( final IZmlModelColumn column : columns )
      {
        if( column.getIdentifier().equals( id ) )
          return column;
      }

      return null;
    }
  }

  @Override
  @Deprecated
  public ZmlModelColumn[] getAvailableColumns( )
  {
    final Set<ZmlModelColumn> active = new LinkedHashSet<ZmlModelColumn>();

    final ZmlModelColumn[] columns = getColumns();
    for( final ZmlModelColumn column : columns )
    {
      if( column.isActive() )
        active.add( column );
    }

    return active.toArray( new ZmlModelColumn[] {} );

  }

  @Override
  public ZmlModelColumn[] getColumns( )
  {
    return m_columns.values().toArray( new ZmlModelColumn[] {} );
  }

  @Override
  public IZmlModelRow getRow( final Date index )
  {
    synchronized( this )
    {
      if( m_rows.isEmpty() )
        getRows();
    }

    return m_rows.get( index );
  }

  @Override
  public IZmlModelRow getRowAt( final int index )
  {
    final IZmlModelRow[] rows = getRows();
    if( index >= rows.length )
      return null;

    return rows[index];
  }

  @Override
  public IZmlModelRow[] getRows( )
  {
    synchronized( this )
    {
      if( m_rows.isEmpty() )
      {
        final ZmlRowBuilder builder = new ZmlRowBuilder( this );
        m_rows = builder.execute();
      }

      return m_rows.values().toArray( new IZmlModelRow[] {} );
    }
  }

  @Override
  public ZmlTableType getTableType( )
  {
    return m_type;
  }

  @Override
  public void modelColumnChangedEvent( final IZmlModelColumn column, final ZmlModelColumnChangeType event )
  {
    if( event.doForceChange() )
      m_rows.clear();

    fireModelChanged( event );
  }

  public URL getContext( )
  {
    return m_context;
  }

  public void setIgnoreTypes( final String[] ignoreTypes )
  {
    if( ArrayUtils.isEquals( m_ignoreTypes, ignoreTypes ) )
      return;

    KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "ZmlTableModel - Setting ignore types\n" );
    m_ignoreTypes = ignoreTypes;

    fireModelChanged( new ZmlModelColumnChangeType( IGNORE_TYPES_CHANGED ) );
  }

  @Override
  public String[] getIgnoreTypes( )
  {
    return m_ignoreTypes;
  }

  public ZmlModelColumnLoader getLoader( )
  {
    return m_loader;
  }

  @Override
  public AbstractColumnType getColumnType( final String id )
  {
    return m_columnTypeMap.get( id );
  }
}