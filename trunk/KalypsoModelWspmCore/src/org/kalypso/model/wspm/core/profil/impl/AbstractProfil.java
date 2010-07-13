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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.IProfilPointMarker;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.MarkerIndex;
import org.kalypso.model.wspm.core.profil.changes.ActiveObjectEdit;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.impl.marker.PointMarker;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.phenomenon.IPhenomenon;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.TupleResult;

/**
 * Basisprofil with Events
 * 
 * @author kimwerner
 */
public abstract class AbstractProfil implements IProfil
{
  private final List<IProfileObject> m_profileObjects = new ArrayList<IProfileObject>();

  private final String m_type;

  private double m_station;

  private IPhenomenon m_phenomenon;

  private IRecord m_activePoint;

  private IComponent m_activePointProperty;

  private TupleResult m_result;

  private String m_name;

  private String m_description;

  private final List<IProfilListener> m_listeners = new ArrayList<IProfilListener>( 10 );

  private final Map<Object, Object> m_additionalProfileSettings = new HashMap<Object, Object>();

  private final ITupleResultChangedListener m_tupleResultListener = new ProfilTupleResultChangeListener( this );

  private MarkerIndex m_markerIndex;

  public AbstractProfil( final String type, final TupleResult result )
  {
    m_type = type;
    setResult( result );
  }

  @Override
  public void addPoint( final int index, final IRecord point )
  {
    getResult().add( index, point );
  }

  @Override
  public boolean addPoint( final IRecord point )
  {
    return getResult().add( point );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#addPointProperty(org.kalypso.model.wspm.core.profil.POINT_PROPERTY)
   */
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

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#setProfileObject(org.kalypso.model.wspm.core.profil.IProfileObject[])
   */
  @Override
  public IProfileObject[] addProfileObjects( final IProfileObject... profileObjects )
  {
    Collections.addAll( m_profileObjects, profileObjects );

    return m_profileObjects.toArray( new IProfileObject[] {} );
  }

  @Override
  public void addProfilListener( final IProfilListener pl )
  {
    m_listeners.add( pl );
  }

  /**
   * @see 
   *      org.kalypso.model.wspm.core.profil.IProfil#createProfileObjects(org.kalypso.observation.IObservation<org.kalypso
   *      .observation.result.TupleResult>[]) override this method if you have got the
   *      org.kalypso.model.wspm.core.profil.IProfileObjectProvider for your m_type
   */
  @Override
  public void createProfileObjects( final IObservation<TupleResult>[] profileObjects )
  {
    throw new NotImplementedException();

  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#createProfilPoint()
   */
  @Override
  public IRecord createProfilPoint( )
  {
    return m_result.createRecord();
  }

  private void fireProblemMarkerChanged( )
  {
    final IProfilListener[] listeners = m_listeners.toArray( new IProfilListener[m_listeners.size()] );
    for( final IProfilListener l : listeners )
    {
      try
      {
        l.onProblemMarkerChanged( this );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  @Override
  public void fireProfilChanged( final ProfilChangeHint hint, final IProfilChange[] changes )
  {
    if( ArrayUtils.isEmpty( changes ) )
      return;

    final IProfilListener[] listeners = m_listeners.toArray( new IProfilListener[m_listeners.size()] );
    for( final IProfilListener l : listeners )
    {
      try
      {
        l.onProfilChanged( hint, changes );
      }
      catch( final Throwable e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmCorePlugin.getDefault().getLog().log( status );
      }
    }
  }

  /**
   * @return Returns the activePoint.
   */
  @Override
  public IRecord getActivePoint( )
  {
    if( m_result.isEmpty() )
      return null;
    else if( m_activePoint == null )
      return m_result.get( 0 );
    else
      return m_activePoint;
  }

  @Override
  public IComponent getActiveProperty( )
  {
    return m_activePointProperty;
  }

  @Override
  public String getComment( )
  {
    final String description = getDescription();
    if( description == null )
      return ""; //$NON-NLS-1$

    return description;
  }

  /**
   * @see org.kalypso.observation.IObservation#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return m_description;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getMarkedPoints()
   */
  @Override
  public IRecord[] getMarkedPoints( )
  {
    final ArrayList<IRecord> records = new ArrayList<IRecord>();

    for( final IRecord record : getResult() )
      if( getPointMarkerFor( record ).length > 0 )
        records.add( record );
    return records.toArray( new IRecord[] {} );
  }

  @Override
  public String getName( )
  {
    return m_name;
  }

  /**
   * @see org.kalypso.observation.IObservation#getPhenomenon()
   */
  @Override
  public IPhenomenon getPhenomenon( )
  {
    return m_phenomenon;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPoint(int)
   */
  @Override
  public IRecord getPoint( final int index )
  {

    return getResult().get( index );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPointMarkerFor(org.kalypso.observation.result.IComponent)
   */
  @Override
  public IProfilPointMarker[] getPointMarkerFor( final IComponent markerColumn )
  {

    if( markerColumn == null )
      return new IProfilPointMarker[] {};
    final int index = getResult().indexOfComponent( markerColumn );
    if( index < 0 )
      return new IProfilPointMarker[] {};

    final List<IProfilPointMarker> markers = new ArrayList<IProfilPointMarker>();
    for( final IRecord record : getResult() )
    {
      final Object value = record.getValue( index );
      if( value != null )
        markers.add( new PointMarker( markerColumn, record ) );
    }
    return markers.toArray( new IProfilPointMarker[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPointMarkerFor(org.kalypso.observation.result.IRecord)
   */
  @Override
  public IProfilPointMarker[] getPointMarkerFor( final IRecord record )
  {
    final ArrayList<IProfilPointMarker> pointMarkers = new ArrayList<IProfilPointMarker>();
    final IComponent[] markers = getPointMarkerTypes();
    for( final IComponent marker : markers )
    {
      final int index = getResult().indexOfComponent( marker );
      if( record.getValue( index ) != null )
        pointMarkers.add( new PointMarker( marker, record ) );
    }
    return pointMarkers.toArray( new PointMarker[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPointMarkerFor(java.lang.String)
   */
  @Override
  public IProfilPointMarker[] getPointMarkerFor( final String pointMarkerID )
  {
    final IComponent cmp = hasPointProperty( pointMarkerID );
    if( cmp == null )
      return new PointMarker[] {};
    return getPointMarkerFor( cmp );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPointMarkerTypes()
   */
  @Override
  public IComponent[] getPointMarkerTypes( )
  {
    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
    final List<IComponent> marker = new ArrayList<IComponent>();
    final IComponent[] properties = getPointProperties();

    for( final IComponent component : properties )
      if( provider.isMarker( component.getId() ) )
        marker.add( component );
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

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getRecordPoints()
   */
  @Override
  public IRecord[] getPoints( )
  {
    return getResult().toArray( new IRecord[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getPoints(int, int)
   */
  @Override
  public IRecord[] getPoints( final int startPoint, final int endPoint )
  {
    final int size = endPoint - startPoint + 1;
    final IRecord[] subList = new IRecord[size];
    for( int i = 0; i < size; i++ )
    {
      subList[i] = getResult().get( startPoint + i );
    }
    return subList;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getProblemMarker()
   */
  @Override
  public MarkerIndex getProblemMarker( )
  {
    return m_markerIndex;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getProfileObject()
   */
  @Override
  public IProfileObject[] getProfileObjects( )
  {
    return m_profileObjects.toArray( new IProfileObject[] {} );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getProfileObject()
   */
  @Override
  public <T extends IProfileObject> T[] getProfileObjects( final Class<T> clazz )
  {
    final List<T> objects = new ArrayList<T>();
    for( final IProfileObject object : m_profileObjects )
    {
      if( clazz.isInstance( object ) )
        objects.add( (T) object );
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

  /**
   * @see org.kalypso.observation.IObservation#getResult()
   */
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

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#getType()
   */
  @Override
  public String getType( )
  {
    return m_type;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#hasPointProperty(org.kalypso.model.wspm.core.profil.IComponent)
   */
  @Override
  public boolean hasPointProperty( final IComponent property )
  {
    return property == null ? false : getResult().hasComponent( property );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#hasPointProperty(org.kalypso.model.wspm.core.profil.IComponent)
   */
  @Override
  public IComponent hasPointProperty( final String propertyId )
  {
    for( final IComponent component : getResult().getComponents() )
      if( component.getId().equals( propertyId ) )
        return component;
    return null;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#indexOfPoint(org.kalypso.observation.result.IRecord)
   */
  @Override
  public int indexOfPoint( final IRecord point )
  {
    return getResult().indexOf( point );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#indexOfProperty(org.kalypso.observation.result.IComponent)
   */
  @Override
  public int indexOfProperty( final IComponent pointProperty )
  {
    return getResult().indexOfComponent( pointProperty );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#indexOfProperty(java.lang.String)
   */
  @Override
  public int indexOfProperty( final String id )
  {
    final IComponent comp = hasPointProperty( id );
    if( comp != null )
      return getResult().indexOfComponent( comp );
    return -1;
  }

  /**
   * @see org.kalypso.model.wspm.core.profilinterface.IProfil#removePoint(org.kalypso.model.wspm.core.profilinterface.IPoint)
   */
  @Override
  public boolean removePoint( final IRecord point )
  {
    return getResult().remove( point );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#removePointMarker(org.kalypso.model.wspm.core.profil.IProfilPointMarker)
   */
  @Override
  public Object removePointMarker( final IProfilPointMarker marker )
  {
    final Object oldValue = marker.getValue();
    marker.setValue( null );

    return oldValue;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#removePointProperty(org.kalypso.model.wspm.core.profil.POINT_PROPERTY)
   */
  @Override
  public boolean removePointProperty( final IComponent pointProperty )
  {
    final int index = getResult().indexOfComponent( pointProperty );
    if( index < 0 )
      return false;
    return getResult().removeComponent( index );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#removeProfileObject(org.kalypso.model.wspm.core.profil.IProfileObject)
   */
  @Override
  public boolean removeProfileObject( final IProfileObject profileObject )
  {
    return m_profileObjects.remove( profileObject );
  }

  @Override
  public void removeProfilListener( final IProfilListener pl )
  {
    m_listeners.remove( pl );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#removeProperty(java.lang.Object)
   */
  @Override
  public Object removeProperty( final Object key )
  {
    final Object old = m_additionalProfileSettings.get( key );
    m_additionalProfileSettings.remove( key );
    return old;
  }

  @Override
  public void setActivePoint( final IRecord point )
  {
    m_activePoint = point;
    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setActivePointChanged();
    fireProfilChanged( hint, new IProfilChange[] { new ActiveObjectEdit( this, point, m_activePointProperty ) } );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#setActiveproperty(org.kalypso.model.wspm.core.profil.IComponent)
   */
  @Override
  public void setActivePointProperty( final IComponent pointProperty )
  {
    m_activePointProperty = pointProperty;
    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setActivePropertyChanged( true );
    fireProfilChanged( hint, new IProfilChange[] { new ActiveObjectEdit( this, m_activePoint, m_activePointProperty ) } );
  }

  @Override
  public void setComment( final String comment )
  {
    setDescription( comment );
  }

  /**
   * @see org.kalypso.observation.IObservation#setDescription(java.lang.String)
   */
  @Override
  public void setDescription( final String desc )
  {
    m_description = desc;
  }

  @Override
  public void setName( final String name )
  {
    m_name = name;
  }

  /**
   * @see org.kalypso.observation.IObservation#setPhenomenon(org.kalypso.observation.phenomenon.IPhenomenon)
   */
  @Override
  public void setPhenomenon( final IPhenomenon phenomenon )
  {
    m_phenomenon = phenomenon;
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#setProblemMarker(org.eclipse.core.resources.IMarker[])
   */
  @Override
  public void setProblemMarker( final IMarker[] markers )
  {
    m_markerIndex = new MarkerIndex( this, markers );

    fireProblemMarkerChanged();
  }

  /**
   * @deprecated caution: additional properties will not be serialized to profile features
   * @see org.kalypso.model.wspm.core.profil.IProfil#setProperty(java.lang.Object, java.lang.Object)
   */
  @Deprecated
  @Override
  public void setProperty( final Object key, final Object value )
  {
    m_additionalProfileSettings.put( key, value );

// // TODO implement some meaningful change event
// final ProfilChangeHint hint = new ProfilChangeHint();
// hint.setObjectChanged();
//
// fireProfilChanged( hint, new IProfilChange[] { new EmptyChange() } );
  }

  /**
   * @see org.kalypso.observation.IObservation#setResult(java.lang.Object)
   */
  @Override
  public void setResult( final TupleResult result )
  {
    Assert.isNotNull( result );

    if( m_result != null )
      m_result.removeChangeListener( m_tupleResultListener );

    m_result = result;

    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( m_type );
    provider.checkComponents( result );

    m_result.addChangeListener( m_tupleResultListener );
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfil#setStation(double)
   */
  @Override
  public void setStation( final double station )
  {
    m_station = station;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    final Double station = getStation();
    if( station != null )
      return String.format( "Profile %.3f km", station );

    return super.toString();
  }
}