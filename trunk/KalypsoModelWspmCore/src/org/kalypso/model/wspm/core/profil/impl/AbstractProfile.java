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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IMarker;
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
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileListener;
import org.kalypso.model.wspm.core.profil.IProfileMetadata;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.IProfileObjectListener;
import org.kalypso.model.wspm.core.profil.IProfilePointMarker;
import org.kalypso.model.wspm.core.profil.IProfilePointPropertyProvider;
import org.kalypso.model.wspm.core.profil.IProfileTransaction;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.MarkerIndex;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.impl.marker.PointMarker;
import org.kalypso.model.wspm.core.profil.visitors.ProfileVisitors;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;
import org.kalypso.model.wspm.core.profil.wrappers.ProfileRecordFactory;
import org.kalypso.observation.IObservationVisitor;
import org.kalypso.observation.phenomenon.IPhenomenon;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.ITupleResultChangedListener;
import org.kalypso.observation.result.TupleResult;

/**
 * @author Kim Werner
 * @author Dirk Kuch
 */
public abstract class AbstractProfile extends ProfileMetadataObserver implements IProfile
{
  private final List<IProfileObject> m_profileObjects = new ArrayList<>();

  private final String m_type;

  private double m_station;

  private IPhenomenon m_phenomenon;

  private final TupleResult m_result;

  private String m_name;

  private String m_description;

  private String m_srsName;

  private final Set<IProfileListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<IProfileListener>( 10 ) );

  private final ITupleResultChangedListener m_tupleResultListener = new ProfileTupleResultChangeListener( this );

  private final IProfileObjectListener m_profileObjectListener = new ProfileObjectListener( this );

  private MarkerIndex m_markerIndex;

  private final IProfileFeature m_source;

  private final RangeSelection m_selection;

  private final Set<Object> m_transactionLock = new HashSet<>();

  private int m_transactionHint;

  private final IProfileMetadata m_metadata;

  public AbstractProfile( final String type, final IProfileFeature source )
  {
    m_type = type;
    m_source = source;

    m_selection = new RangeSelection( this );

    // FIXME: we need another hint for the metadata
    m_metadata = new ProfileMetadata( this, ProfileChangeHint.PROFILE_PROPERTY_CHANGED );

    m_result = new TupleResult( new ProfileRecordFactory( this ) );

    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( m_type );
    provider.checkComponents( m_result );

    m_result.addChangeListener( m_tupleResultListener );
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
    getResult().add( index, point );
  }

  @Override
  public boolean addPoint( final IProfileRecord point )
  {
    return getResult().add( point );
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
      object.addProfileObjectListener( m_profileObjectListener );
      m_profileObjects.add( object );
    }

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.OBJECT_CHANGED ) );

    return m_profileObjects.toArray( new IProfileObject[] {} );
  }

  @Override
  public void addProfilListener( final IProfileListener pl )
  {
    if( Objects.isNull( pl ) )
      return;

    m_listeners.add( pl );
  }

  @Override
  public IProfileRecord createProfilPoint( )
  {
    return (IProfileRecord)m_result.createRecord();
  }

  private void fireProblemMarkerChanged( )
  {
    if( !m_transactionLock.isEmpty() )
      return;
    // FIXME: check: fireProblemMarkerChanged after transaction?

    final IProfileListener[] listeners = m_listeners.toArray( new IProfileListener[m_listeners.size()] );
    for( final IProfileListener listener : listeners )
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

  @Override
  void fireProfilChanged( final ProfileChangeHint hint )
  {
    // TODO: instead of ProfileOperation, we could combine the hints ourselfs during transaction mode
    if( !m_transactionLock.isEmpty() )
    {
      m_transactionHint |= hint.getEvent();
      return;
    }

    final IProfileListener[] listeners = m_listeners.toArray( new IProfileListener[m_listeners.size()] );
    for( final IProfileListener listener : listeners )
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
    final ArrayList<IProfileRecord> records = new ArrayList<>();

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
  public IProfilePointMarker[] getPointMarkerFor( final IComponent markerColumn )
  {
    if( Objects.isNull( markerColumn ) )
      return new IProfilePointMarker[] {};

    final int index = getResult().indexOfComponent( markerColumn );
    if( index < 0 )
      return new IProfilePointMarker[] {};

    final List<IProfilePointMarker> markers = new ArrayList<>();

    final IProfileRecord[] points = getPoints();
    for( final IProfileRecord point : points )
    {
      final Object value = point.getValue( index );
      if( Objects.isNotNull( value ) )
      {
        markers.add( new PointMarker( markerColumn, point ) );
      }
    }
    return markers.toArray( new IProfilePointMarker[] {} );
  }

  @Override
  public IProfilePointMarker[] getPointMarkerFor( final IProfileRecord record )
  {
    final ArrayList<IProfilePointMarker> pointMarkers = new ArrayList<>();
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
  public IProfilePointMarker[] getPointMarkerFor( final String pointMarkerID )
  {
    final IComponent cmp = hasPointProperty( pointMarkerID );
    if( cmp == null )
      return new PointMarker[] {};
    return getPointMarkerFor( cmp );
  }

  @Override
  public IComponent[] getPointMarkerTypes( )
  {
    final List<IComponent> marker = new ArrayList<>();

    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
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
    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
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
   * @return a pointProperty from PointPropertyProvider, see {@code IProfilPointPropertyProvider#getPointProperty(String)}
   *         <p>
   *         you must check {@link #hasPointProperty(IComponent)}, if false you must call {@link #addPointProperty(IComponent)}
   */
  @Override
  public IComponent getPointPropertyFor( final String propertyID )
  {
    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( getType() );
    return provider == null ? null : provider.getPointProperty( propertyID );
  }

  @Override
  public IProfileRecord[] getPoints( )
  {
    final TupleResult result = getResult();

    return result.toArray( new IProfileRecord[result.size()] );
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

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends IProfileObject> T[] getProfileObjects( final Class<T> clazz )
  {
    final List<T> objects = new ArrayList<>();

    for( final IProfileObject object : m_profileObjects )
    {
      if( clazz.isInstance( object ) )
      {
        objects.add( (T)object );
      }
    }

    return objects.toArray( (T[])Array.newInstance( clazz, objects.size() ) );
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
    return getResult().remove( point );
  }

  @Override
  public boolean removePoints( final IProfileRecord[] points )
  {
    return getResult().removeAll( Arrays.asList( points ) );
  }

  @Override
  public Object removePointMarker( final IProfilePointMarker marker )
  {
    final Object oldValue = marker.getValue();

    final IComponent id = marker.getComponent();
    final Object defaultValue = id.getDefaultValue();
    marker.setValue( defaultValue );

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.MARKER_MOVED ) );

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
    profileObject.removeProfileObjectListener( m_profileObjectListener );

    final boolean removed = m_profileObjects.remove( profileObject );

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.OBJECT_CHANGED ) );

    return removed;
  }

  @Override
  public void removeProfilListener( final IProfileListener pl )
  {
    m_listeners.remove( pl );
  }

  @Override
  public void setComment( final String comment )
  {
    setDescription( comment );

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setDescription( final String desc )
  {
    m_description = desc;

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setName( final String name )
  {
    m_name = name;

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setPhenomenon( final IPhenomenon phenomenon )
  {
    m_phenomenon = phenomenon;

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public void setProblemMarker( final IMarker[] markers )
  {
    m_markerIndex = new MarkerIndex( this, markers );

    fireProblemMarkerChanged();
  }

//  @Override
//  public void setResult( final TupleResult result )
//  {
//    Assert.isNotNull( result );
//
//    if( m_result != null )
//    {
//      m_result.removeChangeListener( m_tupleResultListener );
//    }
//
//    m_result = result;
//
//    final IProfilePointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( m_type );
//    provider.checkComponents( result );
//
//    m_result.addChangeListener( m_tupleResultListener );
//  }

  @Override
  public void setStation( final double station )
  {
    m_station = station;

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
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
      return getResult().indexOf( record );

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

    return (IProfileRecord)getResult().get( 0 );
  }

  @Override
  public IProfileRecord getLastPoint( )
  {
    final TupleResult result = getResult();
    if( result.isEmpty() )
      return null;

    return (IProfileRecord)result.get( result.size() - 1 );
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
  public IProfilePointMarker[] getPointMarkers( )
  {
    final Set<IProfilePointMarker> markers = new LinkedHashSet<>();

    final IComponent[] types = getPointMarkerTypes();
    for( final IComponent type : types )
    {
      Collections.addAll( markers, getPointMarkerFor( type ) );
    }

    return markers.toArray( new IProfilePointMarker[] {} );
  }

  @Override
  public synchronized void startTransaction( final Object lock )
  {
    m_transactionLock.add( lock );
  }

  @Override
  public synchronized void stopTransaction( final Object lock )
  {
    final int hint = m_transactionHint;

    m_transactionLock.remove( lock );
    m_transactionHint = 0;

    fireProfilChanged( new ProfileChangeHint( hint ) );
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
      final String msg = String.format( "Performing profile transaction on profile %.3f km failed", getStation() ); //$NON-NLS-1$

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
    return m_srsName;
  }

  @Override
  public void setSrsName( final String srsName )
  {
    m_srsName = srsName;

    fireProfilChanged( new ProfileChangeHint( ProfileChangeHint.PROFILE_PROPERTY_CHANGED ) );
  }

  @Override
  public IProfileMetadata getMetadata( )
  {
    return m_metadata;
  }
}