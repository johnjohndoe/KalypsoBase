/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.observation.result;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.observation.result.ITupleResultChangedListener.TYPE;
import org.kalypso.observation.result.ITupleResultChangedListener.ValueChange;

/**
 * @author Marc Schlienger
 */
public class TupleResult implements List<IRecord>
{
  private final List<IRecord> m_records = new ArrayList<>();

  private final List<IComponent> m_components = new ArrayList<>();

  /** This tuple result gets sorted by these components. */
  // TODO: this is wrong: instead we need a list of the indices. BUT: the schema does not support that...
  private final Set<IComponent> m_sortComponents = new LinkedHashSet<>();

  private final Set<ITupleResultChangedListener> m_listeners = new HashSet<>();

  private IComponent m_ordinalNumberComponent = null;

  private int m_ordinalNumberComponentIndex = -1;

  /** Internal sort state. Initially <code>true</code> as the empty list is sorted. */
  private boolean m_isSorted = true;

  private IInterpolationHandler m_interpolationHandler = null;

  private final Comparator<IRecord> m_sortComparator = new Comparator<IRecord>()
  {
    @Override
    public int compare( final IRecord o1, final IRecord o2 )
    {
      final IComponent[] sortComponents = getSortComponents();
      for( final IComponent component : sortComponents )
      {
        final int index = indexOfComponent( component );
        if( index == -1 )
          continue;

        final Object v1 = o1.getValue( index );
        final Object v2 = o2.getValue( index );

        final int result = component.compare( v1, v2 );

        /* obj differs? no further sorting needed! */
        if( result != 0 )
          return result;
      }

      /* return equals */
      return 0;
    }
  };

  /** Factory that must be used to create all records */
  private final IRecordFactory m_factory;

  public TupleResult( )
  {
    this( new IComponent[] {} );
  }

  public TupleResult( final IRecordFactory factory )
  {
    this( factory, new IComponent[] {} );
  }

  public TupleResult( final IComponent[] comps )
  {
    this( null, comps );
  }

  public TupleResult( final IRecordFactory factory, final IComponent[] comps )
  {
    if( factory == null )
      m_factory = new RecordFactory();
    else
      m_factory = factory;

    for( final IComponent element : comps )
      addComponent( element );
  }

  public void setSortComponents( final IComponent[] comps )
  {
    m_sortComponents.clear();
    for( final IComponent component : comps )
      m_sortComponents.add( component );

    m_isSorted = false;

    // TODO: check: should be rather fireSortComponentChanged(TYPE.SORT) ???
    fireRecordsChanged( null, TYPE.CHANGED );
  }

  public void setOrdinalNumberComponent( final IComponent component )
  {
    m_ordinalNumberComponent = component;
    if( m_ordinalNumberComponent != null )
      for( int i = 0; i < m_components.size(); i++ )
        if( m_ordinalNumberComponent.equals( m_components.get( i ) ) )
        {
          m_ordinalNumberComponentIndex = i;
          break;
        }
  }

  private void renumber( )
  {
    if( m_ordinalNumberComponent != null )
    {
      int i = 1;
      /*
       * setting of ordinal number doesn't need to fire event on each value.
       * one event is fired after every call of renumber function.
       * */
      for( final IRecord record : m_records )
        record.setValue( m_ordinalNumberComponentIndex, new BigInteger( new Integer( i++ ).toString() ), true );
    }
  }

  public IComponent[] getSortComponents( )
  {
    return m_sortComponents.toArray( new IComponent[m_sortComponents.size()] );
  }

  public IComponent getOrdinalNumberComponent( )
  {
    return m_ordinalNumberComponent;
  }

  /**
   * Invalidates the sort state.<br>
   * The next access to any data results in sorting.<br>
   * Not intended to be called by client other than {@link Record}.
   */
  /* default */boolean invalidateSort( final int index )
  {
    final IComponent component = m_components.get( index );
    if( m_sortComponents.contains( component ) )
    {
      m_isSorted = false;
      return true;
    }

    return false;
  }

  /**
   * Sorts this tuple result by its sort components.<br>
   * This method should only be called by the {@link Record} class or this.
   */
  /* default */void sort( )
  {
    if( m_isSorted )
      return;

    if( m_sortComponents.size() == 0 )
      return;

    Collections.sort( m_records, m_sortComparator );

    m_isSorted = true;

    renumber();
    
    fireRecordsChanged( m_records.toArray( new IRecord[m_records.size()] ), TYPE.CHANGED );
  }

  @Override
  public String toString( )
  {
    return "TupleResult: " + getComponents(); //$NON-NLS-1$
  }

  public void setInterpolationHandler( final IInterpolationHandler handler )
  {
    m_interpolationHandler = handler;
  }

  public IInterpolationHandler getInterpolationHandler( )
  {
    return m_interpolationHandler;
  }

  public boolean doInterpolation( final IRecord record, final int index, final double distance )
  {
    return m_interpolationHandler == null ? false : m_interpolationHandler.doInterpolation( this, record, index, distance );
  }

  @Override
  public void add( final int index, final IRecord element )
  {
    checkRecord( element );

    m_records.add( index, element );
    fireRecordsChanged( new IRecord[] { element }, TYPE.ADDED );
  }

  @Override
  public boolean add( final IRecord o )
  {
    checkRecord( o );

    final boolean result = m_records.add( o );

    fireRecordsChanged( new IRecord[] { o }, TYPE.ADDED );

    return result;
  }

  @Override
  public boolean addAll( final Collection< ? extends IRecord> c )
  {
    return addAll( m_records.size(), c );
  }

  @Override
  public boolean addAll( final int index, final Collection< ? extends IRecord> c )
  {
    checkRecords( c );

    final boolean result = m_records.addAll( index, c );
    
    m_isSorted = false;
    sort();

    fireRecordsChanged( c.toArray( new IRecord[c.size()] ), TYPE.ADDED );
    
    
    return result;
  }

  @Override
  public void clear( )
  {
    final IRecord[] oldRecords = m_records.toArray( new IRecord[m_records.size()] );

    m_records.clear();
    m_isSorted = true; // empty list is sorted

    fireRecordsChanged( oldRecords, TYPE.REMOVED );
  }

  @Override
  public boolean contains( final Object o )
  {
    return m_records.contains( o );
  }

  /**
   * @see java.util.List#containsAll(java.util.Collection)
   */
  @Override
  public boolean containsAll( final Collection< ? > c )
  {
    return m_records.containsAll( c );
  }

  @Override
  public IRecord get( final int index )
  {
    sort();

    return m_records.get( index );
  }

  @Override
  public boolean isEmpty( )
  {
    return m_records.isEmpty();
  }

  /**
   * IMPORTANT: removing via this iterator does not inform the listeners.
   */
  @Override
  public Iterator<IRecord> iterator( )
  {
    sort();

    return m_records.iterator();
  }

  @Override
  public int lastIndexOf( final Object o )
  {
    sort();

    return m_records.lastIndexOf( o );
  }

  /**
   * IMPORTANT: removing via this iterator does not inform the listeners.
   */
  @Override
  public ListIterator<IRecord> listIterator( )
  {
    sort();

    return m_records.listIterator();
  }

  /**
   * IMPORTANT: removing via this iterator does not inform the listeners.
   */
  @Override
  public ListIterator<IRecord> listIterator( final int index )
  {
    sort();

    return m_records.listIterator( index );
  }

  @Override
  public IRecord remove( final int index )
  {
    sort();

    final IRecord result = m_records.remove( index );
    fireRecordsChanged( new IRecord[] { result }, TYPE.REMOVED );

    return result;
  }

  @Override
  public boolean remove( final Object o )
  {
    final boolean result = m_records.remove( o );

    if( result )
    {
      fireRecordsChanged( new IRecord[] { (IRecord)o }, TYPE.REMOVED );
    }

    return result;
  }

  @Override
  public boolean removeAll( final Collection< ? > c )
  {
    final boolean removeAll = m_records.removeAll( c );

    final Object[] objects = c.toArray();
    final IRecord[] removedRecords = new IRecord[objects.length];
    for( int i = 0; i < objects.length; i++ )
      removedRecords[i] = (IRecord)objects[i];

    fireRecordsChanged( removedRecords, TYPE.REMOVED );
    
    //force renumber of not removed
    fireRecordsChanged( m_records.toArray( new IRecord[m_records.size()] ), TYPE.CHANGED );
    
    return removeAll;
  }

  @Override
  public boolean retainAll( final Collection< ? > c )
  {
    // TODO check if this works? Hmm...

    fireRecordsChanged( null, TYPE.REMOVED );

    return m_records.retainAll( c );
  }

  @Override
  public IRecord set( final int index, final IRecord element )
  {
    final IRecord result = m_records.set( index, element );

    m_isSorted = false;

    fireRecordsChanged( new IRecord[] { element }, TYPE.CHANGED );

    return result;
  }

  @Override
  public int size( )
  {
    return m_records.size();
  }

  @Override
  public List<IRecord> subList( final int fromIndex, final int toIndex )
  {
    // TODO: problem:
    // - listeners do not get informed
    // - sorting is not maintained
    // TODO: implement a special sub-list

    sort();

    return m_records.subList( fromIndex, toIndex );
  }

  @Override
  public Object[] toArray( )
  {
    sort();

    return m_records.toArray();
  }

  @Override
  public <T> T[] toArray( final T[] a )
  {
    sort();

    return m_records.toArray( a );
  }

  private void checkRecord( final IRecord record )
  {
    final TupleResult owner = record.getOwner();

    Assert.isNotNull( owner );
    Assert.isTrue( owner == this );
    // FIXME: check still makes sense!
    // Assert.isTrue( r.getCount() == m_components.size() );
  }

  private void checkRecords( final Collection< ? extends IRecord> c )
  {
    for( final IRecord record : c )
      checkRecord( record );
  }

  //
  // COMPONENTS
  //

  public IComponent[] getComponents( )
  {
    return m_components.toArray( new IComponent[] {} );
  }

  /**
   * Adds a component to this tuple result. Does nothing if an equal component was already added.
   */
  public final boolean addComponent( final IComponent comp )
  {
    final boolean added = m_components.add( comp );

    for( final IRecord record : this )
    {
      final Record r = (Record)record;
      r.set( m_components.size() - 1, comp.getDefaultValue() );
    }

    if( m_sortComponents.contains( comp ) )
      m_isSorted = false;

    fireComponentsChanged( new IComponent[] { comp }, TYPE.ADDED );
    return added;
  }

  /**
   * Adds a component to this tuple result. Does nothing if an equal component was already added.
   * <p>
   * Copies the values from the given component if this tupleResult contains the component and the QName Types are equal
   */
  public final boolean addComponent( final IComponent comp, final IComponent cloneFrom )
  {
    final boolean added = m_components.add( comp );
    int index = -1;
    if( hasComponent( cloneFrom ) && cloneFrom.getValueTypeName().equals( comp.getValueTypeName() ) )
      index = indexOfComponent( cloneFrom );

    for( final IRecord record : this )
    {
      final Record r = (Record)record;
      if( index > -1 )
        r.set( m_components.size() - 1, record.getValue( index ) );
      else
        r.set( m_components.size() - 1, comp.getDefaultValue() );
    }

    if( m_sortComponents.contains( comp ) )
      m_isSorted = false;

    fireComponentsChanged( new IComponent[] { comp }, TYPE.ADDED );
    return added;
  }

  /**
   * Adds a component to this tuple result. Does nothing if an equal component was already added.
   * <p>
   * Set the value for all records of this tupleResult, wrong type will raise an Exception later
   */
  public final boolean addComponent( final IComponent comp, final Object defaultValue )
  {
    final boolean added = m_components.add( comp );

    for( final IRecord record : this )
    {
      final Record r = (Record)record;
      r.set( m_components.size() - 1, defaultValue );
    }

    if( m_sortComponents.contains( comp ) )
      m_isSorted = false;

    fireComponentsChanged( new IComponent[] { comp }, TYPE.ADDED );
    return added;
  }

  public boolean removeComponent( final int index )
  {
    for( final IRecord record : m_records )
      ((Record)record).remove( index );

    final IComponent comp = m_components.remove( index );
    fireComponentsChanged( new IComponent[] { comp }, TYPE.REMOVED );
    return true;
  }

  /**
   * This method creates, but DOES NOT adds a record.<br/>
   * This allows to modify record before they are added to the result. This is necessary in order to avoid many change
   * events when new records are created.
   */
  public IRecord createRecord( )
  {
    return m_factory.createRecord( this, getComponents() );
  }

  public boolean hasComponent( final IComponent comp )
  {
    return m_components.contains( comp );
  }

  /**
   * Add a listener to the list of listeners which will be informed of changes to tuples. Has no effect if the same
   * listener is already registered.
   */
  public void addChangeListener( final ITupleResultChangedListener l )
  {
    m_listeners.add( l );
  }

  public void removeChangeListener( final ITupleResultChangedListener l )
  {
    m_listeners.remove( l );
  }

  /* default */void fireValuesChanged( final ValueChange[] changes )
  {
    final ITupleResultChangedListener[] listeners = m_listeners.toArray( new ITupleResultChangedListener[m_listeners.size()] );
    for( final ITupleResultChangedListener l : listeners )
    {
      try
      {
        l.valuesChanged( changes );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  /* default */void fireRecordsChanged( final IRecord[] records, final TYPE type )
  {
    // FIXME: major performance hotspot for obsrvation with the ugly 'ordinal component'
    renumber();

    final ITupleResultChangedListener[] listeners = m_listeners.toArray( new ITupleResultChangedListener[m_listeners.size()] );
    for( final ITupleResultChangedListener l : listeners )
    {
      try
      {
        l.recordsChanged( records, type );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  private void fireComponentsChanged( final IComponent[] components, final TYPE type )
  {
    final ITupleResultChangedListener[] listeners = m_listeners.toArray( new ITupleResultChangedListener[m_listeners.size()] );
    for( final ITupleResultChangedListener l : listeners )
    {
      try
      {
        l.componentsChanged( components, type );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  public int indexOfComponent( final IComponent comp )
  {
    return m_components.indexOf( comp );
  }

  public IComponent getComponent( final int index )
  {
    return m_components.get( index );
  }

  @Override
  public int indexOf( final Object o )
  {
    if( o == null )
      return -1;

    if( o instanceof IRecord )
    {
      sort();
      return m_records.indexOf( o );
    }
    else
      throw new IllegalArgumentException( o.toString() );
  }

  /**
   * @return -1, if no such component exists. Returns the index of the first component with a given id.
   */
  public int indexOfComponent( final String componentId )
  {
    for( int i = 0; i < m_components.size(); i++ )
    {
      final IComponent comp = m_components.get( i );
      if( comp.getId().equals( componentId ) )
        return i;
    }

    return -1;
  }

  IRecordFactory getFactory( )
  {
    return m_factory;
  }
}