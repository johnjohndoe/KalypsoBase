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
package org.kalypso.model.wspm.core.profil.impl;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.MarkerIndex;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.impl.marker.PointMarker;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecord;
import org.kalypso.observation.IObservationVisitor;
import org.kalypso.observation.phenomenon.IPhenomenon;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.TupleResult;

/**
 * @author Kim Werner
 * @author Dirk Kuch
 */
public abstract class AbstractProfil implements IProfil
{
  private final List<IProfileObject> m_profileObjects = new ArrayList<IProfileObject>();

  private final String m_type;

  private double m_station;

  private IPhenomenon m_phenomenon;

  private TupleResult m_result;

  private String m_name;

  private String m_description;

  private final Set<IProfilListener> m_listeners = new LinkedHashSet<IProfilListener>( 10 );

  private final Map<Object, Object> m_additionalProfileSettings = new HashMap<Object, Object>();

  private final ITupleResultChangedListener m_tupleResultListener = new ProfilTupleResultChangeListener( this );

  private final ITupleResultChangedListener m_objectTupleListener = new ProfilObjectListener( this );

  private MarkerIndex m_markerIndex;

  private final IProfileFeature m_source;

  private final RangeSelection m_selection;

  private Object m_transactionLock;

  private int m_transactionHint;

  public AbstractProfil( final String type, final TupleResult result, final IProfileFeature source )
  {
    m_type = type;
    m_source = source;
    setResult( result );

    m_selection = new RangeSelection( this );
  }

  @Override
  public final IRangeSelection getSelection( )
  {
    return m_selection;
  }

  @Override
  public IProfileFeature getSource( )
  {
    return m_source;
  }

  @Override
  public void addPoint( final int index, final IProfileRecord point )
  {
    getResult().add( index, point.getRecord() );
  }

  @Override
  public boolean addPoint( final IProfileRecord point )
  {
    return getResult().add( point.getRecord() );
  }

  @Override
  public void addPointProperty( final IComponent pointProperty )
  {
    if( pointProperty == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.model.wspm.core.profil.impl.AbstractProfil.0" ) ); //$NON-NLS-1$

    final IComponent[] pointProperties = getPointProperties();
    if( ArrayUtils.contains( pointProperties, pointProperty ) )
      return;

    getResult().addComponent( pointProperty );
  }

  @Override
  public void addPointProperty( final IComponent pointProperty, final Object defaultValue )
  {
    if( pointProperty == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.model.wspm.core.profil.impl.AbstractProfil.0" ) ); //$NON-NLS-1$

    final IComponent[] pointProperties = getPointProperties();
    if( ArrayUtils.contains( pointProperties, pointProperty ) )
      return;

    getResult().addComponent( pointProperty, defaultValue );
  }

  @Override
  public void addPointProperty( final IComponent pointProperty, final IComponent initialValues )
  {
    if( pointProperty == null )
      throw new IllegalStateException( Messages.getString( "org.kalypso.model.wspm.core.profil.impl.AbstractProfil.0" ) ); //$NON-NLS-1$

    final IComponent[] pointProperties = getPointProperties();
    if( ArrayUtils.contains( pointProperties, pointProperty ) )
      return;

    getResult().addComponent( pointProperty, initialValues );
  }

  @Override
  public IProfileObject[] addProfileObjects( final IProfileObject... profileObjects )
  {
    for( final IProfileObject object : profileObjects )
    {
      // TODO: what if objects have additional state ?
      object.getObservation().getResult().addChangeListener( m_objectTupleListener );
      m_profileObjects.add( object );
    }

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.OBJECT_CHANGED ) );

    return m_profileObjects.toArray( new IProfileObject[] {} );
  }

  @Override
  public void addProfilListener( final IProfilListener pl )
  {
    if( Objects.isNull( pl ) )
      return;

    m_listeners.add( pl );
  }

  @Override
  public IProfileRecord createProfilPoint( )
  {
    final IRecord record = m_result.createRecord();

    return new ProfileRecord( this, record );
  }

  private void fireProblemMarkerChanged( )
  {
    if( m_transactionLock != null )
      return;
    // FIXME: check: fireProblemMarkerChanged after transaction?

    final IProfilListener[] listeners = m_listeners.toArray( new IProfilListener[m_listeners.size()] );
    for( final IProfilListener listener : listeners )
    {
      try
      {
        listener.onProblemMarkerChanged( this );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  void fireProfilChanged( final ProfilChangeHint hint )
  {
    // TODO: instead of ProfileOperation, we could combine the hints ourselfs during transaction mode
    if( m_transactionLock != null )
    {
      m_transactionHint |= hint.getEvent();
      return;
    }

    final IProfilListener[] listeners = m_listeners.toArray( new IProfilListener[m_listeners.size()] );
    for( final IProfilListener listener : listeners )
    {
      try
      {
        listener.onProfilChanged( hint );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  @Override
  public String getComment( )
  {
    final String description = getDescription();
    if( Strings.isEmpty( description ) )
      return ""; //$NON-NLS-1$

    return description;
  }

  @Override
  public String getDescription( )
  {
    return m_description;
  }

  @Override
  public IProfileRecord[] getMarkedPoints( )
  {
    final ArrayList<IProfileRecord> records = new ArrayList<IProfileRecord>();

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      if( getPointMarkerFor( point ).length > 0 )
      {
        records.add( point );
      }
    }

    return records.toArray( new IProfileRecord[] {} );
  }

  @Override
  public String getName( )
  {
    return m_name;
  }

  @Override
  public IPhenomenon getPhenomenon( )
  {
    return m_phenomenon;
  }

  @Override
  public IProfileRecord getPoint( final int index )
  {
    return getPoints()[index];
  }

  @Override
  public IProfilPointMarker[] getPointMarkerFor( final IComponent markerColumn )
  {
    if( Objects.isNull( markerColumn ) )
      return new IProfilPointMarker[] {};

    final int index = getResult().indexOfComponent( markerColumn );
    if( index < 0 )
      return new IProfilPointMarker[] {};

    final List<IProfilPointMarker> markers = new ArrayList<IProfilPointMarker>();

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      final Object value = point.getValue( index );
      if( Objects.isNotNull( value ) )
      {
        markers.add( new PointMarker( markerColumn, point ) );
      }
    }
    return markers.toArray( new IProfilPointMarker[] {} );
  }

  @Override
  public IProfilPointMarker[] getPointMarkerFor( final IProfileRecord record )
  {
    final ArrayList<IProfilPointMarker> pointMarkers = new ArrayList<IProfilPointMarker>();
    final IComponent[] markers = getPointMarkerTypes();
    for( final IComponent marker : markers )
    {
      final int index = getResult().indexOfComponent( marker );
      if( record.getValue( index ) != null )
      {
        pointMarkers.add( new PointMarker( marker, record ) );
      }
    }
    return pointMarkers.toArray( new PointMarker[] {} );
  }

  @Override
  public IProfilPointMarker[] getPointMarkerFor( final String pointMarkerID )
  {
    final IComponent cmp = hasPointProperty( pointMarkerID );
    if( cmp == null )
      return new PointMarker[] {};
    return getPointMarkerFor( cmp );
  }

  @Override
  public IComponent[] getPointMarkerTypes( )
  {
    final List<IComponent> marker = new ArrayList<IComponent>();

    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
    final IComponent[] properties = getPointProperties();

    for( final IComponent component : properties )
    {
      if( provider.isMarker( component.getId() ) )
        marker.add( component );
    }

    return marker.toArray( new IComponent[] {} );
  }

  @Override
  public boolean isPointMarker( final String propertyID )
  {
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
    return provider == null ? false : provider.isMarker( propertyID );
  }

  @Override
  public IComponent[] getPointProperties( )
  {
    final TupleResult result = getResult();
    return result.getComponents();
  }

  /**
   * CREATES A NEW POINT PROPERTY.
   * 
   * @return a pointProperty from PointPropertyProvider, see
   *         {@code IProfilPointPropertyProvider#getPointProperty(String)}
   *         <p>
   *         you must check {@link #hasPointProperty(IComponent)}, if false you must call
   *         {@link #addPointProperty(IComponent)}
   */
  @Override
  public IComponent getPointPropertyFor( final String propertyID )
  {
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
    return provider == null ? null : provider.getPointProperty( propertyID );
  }

  @Override
  public IProfileRecord[] getPoints( )
  {
    final Set<IProfileRecord> collection = new LinkedHashSet<>();
    final IRecord[] records = getResult().toArray( new IRecord[] {} );
    for( final IRecord record : records )
    {
      collection.add( new ProfileRecord( this, record ) );
    }

    return collection.toArray( new IProfileRecord[] {} );
  }

  @Override
  public IProfileRecord[] getPoints( final int startPoint, final int endPoint )
  {
    return ProfileVisitors.findPointsBetween( this, startPoint, endPoint );
  }

  @Override
  public MarkerIndex getProblemMarker( )
  {
    return m_markerIndex;
  }

  @Override
  public IProfileObject[] getProfileObjects( )
  {
    return m_profileObjects.toArray( new IProfileObject[] {} );
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends IProfileObject> T[] getProfileObjects( final Class<T> clazz )
  {
    // TODO visitor
    final List<T> objects = new ArrayList<T>();
    for( final IProfileObject object : m_profileObjects )
    {
      if( clazz.isInstance( object ) )
      {
        objects.add( (T) object );
      }
    }

    return objects.toArray( (T[]) Array.newInstance( clazz, objects.size() ) );
  }

  /**
   * @deprecated caution: additional properties will not be serialized to profile features
   * @see org.kalypso.model.wspm.core.profil.IProfil#getProperty(java.lang.Object)
   */
  @Deprecated
  @Override
  public Object getProperty( final Object key )
  {
    return m_additionalProfileSettings.get( key );
  }

  @Override
  public TupleResult getResult( )
  {
    return m_result;
  }

  @Override
  public double getStation( )
  {
    return m_station;
  }

  @Override
  public String getType( )
  {
    return m_type;
  }

  @Override
  public boolean removePoint( final IProfileRecord point )
  {
    return getResult().remove( point.getRecord() );
  }

  @Override
  public boolean removePoints( final IProfileRecord[] points )
  {
    boolean state = true;
    for( final IProfileRecord point : points )
    {
      if( !getResult().remove( point.getRecord() ) )
        state = false;
    }

    return state;
  }

  @Override
  public Object removePointMarker( final IProfilPointMarker marker )
  {
    final Object oldValue = marker.getValue();

    final IComponent id = marker.getComponent();
    final Object defaultValue = id.getDefaultValue();
    marker.setValue( defaultValue );

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.MARKER_MOVED ) );

    return oldValue;
  }

  @Override
  public boolean removePointProperty( final IComponent pointProperty )
  {
    final int index = getResult().indexOfComponent( pointProperty );
    if( index < 0 )
      return false;
    return getResult().removeComponent( index );
  }

  @Override
  public boolean removeProfileObject( final IProfileObject profileObject )
  {
    profileObject.getObservation().getResult().removeChangeListener( m_objectTupleListener );

    final boolean removed = m_profileObjects.remove( profileObject );

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.OBJECT_CHANGED ) );

    return removed;
  }

  @Override
  public void removeProfilListener( final IProfilListener pl )
  {
    m_listeners.remove( pl );
  }

  @Override
  public Object removeProperty( final Object key )
  {
    final Object old = m_additionalProfileSettings.get( key );
    m_additionalProfileSettings.remove( key );

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );

    return old;
  }

  @Override
  public void setComment( final String comment )
  {
    setDescription( comment );

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setDescription( final String desc )
  {
    m_description = desc;

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setName( final String name )
  {
    m_name = name;

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setPhenomenon( final IPhenomenon phenomenon )
  {
    m_phenomenon = phenomenon;

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setProblemMarker( final IMarker[] markers )
  {
    m_markerIndex = new MarkerIndex( this, markers );

    fireProblemMarkerChanged();
  }

  /**
   * @deprecated caution: additional properties will not be serialized to profile features
   */
  @Deprecated
  @Override
  public void setProperty( final Object key, final Object value )
  {
    m_additionalProfileSettings.put( key, value );

    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setObjectChanged();

    fireProfilChanged( hint );
  }

  @Override
  public void setResult( final TupleResult result )
  {
    Assert.isNotNull( result );

    if( m_result != null )
    {
      m_result.removeChangeListener( m_tupleResultListener );
    }

    m_result = result;

    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( m_type );
    provider.checkComponents( result );

    m_result.addChangeListener( m_tupleResultListener );
  }

  @Override
  public void setStation( final double station )
  {
    m_station = station;

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public String toString( )
  {
    final Double station = getStation();
    if( station != null )
      return String.format( Messages.getString( "AbstractProfil_0" ), station ); //$NON-NLS-1$

    return super.toString();
  }

  @Override
  public void accept( final IObservationVisitor visitor )
  {
    throw new UnsupportedOperationException();
  }

  @Override
  public int indexOfProperty( final IComponent pointProperty )
  {
    return getResult().indexOfComponent( pointProperty );
  }

  @Override
  public int indexOfProperty( final String id )
  {
    final IComponent component = hasPointProperty( id );
    if( Objects.isNull( component ) )
      return -1;

    return getResult().indexOfComponent( component );
  }

  @Override
  public int indexOf( final IProfileRecord record )
  {
    final int index = record.getIndex();
    if( index == -1 ) // fall back - this should never happen
      return getResult().indexOf( record.getRecord() );

    return index;
  }

  @Override
  public boolean hasPointProperty( final IComponent component )
  {
    if( Objects.isNull( component ) )
      return false;

    return getResult().hasComponent( component );
  }

  @Override
  public IComponent hasPointProperty( final String identifier )
  {
    final IComponent[] components = getResult().getComponents();
    for( final IComponent component : components )
    {
      if( StringUtils.equals( component.getId(), identifier ) )
        return component;
    }

    return null;
  }

  @Override
  public IProfileRecord getFirstPoint( )
  {
    if( getResult().isEmpty() )
      return null;

    return new ProfileRecord( this, getResult().get( 0 ) );
  }

  @Override
  public IProfileRecord getLastPoint( )
  {
    final TupleResult result = getResult();
    if( result.isEmpty() )
      return null;

    return new ProfileRecord( this, result.get( result.size() - 1 ) );
  }

  @Override
  public void accept( final IProfileRecordVisitor visitor, final int direction )
  {
    doAccept( visitor, getPoints(), direction );
  }

  @Override
  public void accept( final IProfileRecordVisitor visitor, final Double p1, final Double pn, final boolean includeVertexPoints, final int direction )
  {
    accept( visitor, Range.between( p1, pn ), includeVertexPoints, direction );
  }

  @Override
  public void accept( final IProfileRecordVisitor visitor, final Range<Double> range, final boolean includeVertexPoints, final int direction )
  {
    doAccept( visitor, ProfileVisitors.findPointsBetween( this, range, includeVertexPoints ), direction );
  }

  private void doAccept( final IProfileRecordVisitor visitor, final IProfileRecord[] points, final int direction )
  {
    if( visitor.isWriter() )
      startTransaction( visitor );

    try
    {
      if( direction >= 0 )
      {
        for( final IProfileRecord point : points )
        {
          visitor.visit( point, 1 );
        }
      }
      else
      {
        for( int index = ArrayUtils.getLength( points ) - 1; index > 0; index-- )
        {
          final IProfileRecord point = points[index];
          visitor.visit( point, direction );
        }
      }
    }
    catch( final CancelVisitorException ex )
    {
      return;
    }
    finally
    {
      if( visitor.isWriter() )
        stopTransaction( visitor );
    }
  }

  @Override
  public IProfileRecord findNextPoint( final double breite )
  {
    return ProfileVisitors.findNextPoint( this, breite );
  }

  @Override
  public IProfileRecord findPreviousPoint( final double breite )
  {
    return ProfileVisitors.findPreviousPoint( this, breite );
  }

  @Override
  public IProfilPointMarker[] getPointMarkers( )
  {
    final Set<IProfilPointMarker> markers = new LinkedHashSet<>();

    final IComponent[] types = getPointMarkerTypes();
    for( final IComponent type : types )
    {
      Collections.addAll( markers, getPointMarkerFor( type ) );
    }

    return markers.toArray( new IProfilPointMarker[] {} );
  }

  @Override
  public synchronized void startTransaction( final Object lock )
  {
    if( m_transactionLock != null )
      throw new IllegalStateException();

    m_transactionLock = lock;
    m_transactionHint = 0;
  }

  @Override
  public synchronized void stopTransaction( final Object lock )
  {
    if( m_transactionLock == null )
      throw new IllegalStateException();

    final int hint = m_transactionHint;

    m_transactionLock = null;
    m_transactionHint = 0;

    fireProfilChanged( new ProfilChangeHint( hint ) );
  }

  @Override
  public IStatus doTransaction( final IProfileTransaction transaction )
  {
    startTransaction( transaction );
    try
    {
      return transaction.execute( this );
    }
    catch( final Throwable t )
    {
      final String msg = String.format( "Performing profile transaction on profile %.3f km failed", getStation() );

      return new Status( IStatus.ERROR, KalypsoModelWspmCorePlugin.getID(), msg, t );
    }
    finally
    {
      stopTransaction( transaction );
    }

  }

  @Override
  public String getSrsName( )
  {
    final IProfileFeature source = getSource();
    if( Objects.isNotNull( source ) )
      return source.getSrsName();

    return null;
  }

  @Override
  public void setSrsName( final String srsName )
  {
    // FIXME: check: there is also a profile object SRSNAME
    final IProfileFeature source = getSource();
    if( Objects.isNotNull( source ) )
      source.setSrsName( srsName );

    fireProfilChanged( new ProfilChangeHint( ProfilChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }
}