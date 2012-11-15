/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.core.profil;

import org.apache.commons.lang3.Range;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecordVisitor;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 * @author Dirk Kuch
 */
public interface IProfile extends IObservation<TupleResult>
{
  void accept( final IProfileRecordVisitor visitor, final int direction );

  void accept( final IProfileRecordVisitor visitor, Double p1, Double pn, final boolean includeVertexPoints, final int direction );

  void accept( final IProfileRecordVisitor visitor, final Range<Double> range, final boolean includeVertexPoints, final int direction );

  IProfileRecord getFirstPoint( );

  IProfileRecord getLastPoint( );

  void addPoint( int index, IProfileRecord point );

  /**
   * @return true
   *         <p>
   *         adds a new Record at the end of this Observation and copies the values of the Components existing in both records
   */
  boolean addPoint( IProfileRecord point );

  /**
   * @param pointProperty
   */
  void addPointProperty( IComponent pointProperty );

  void addPointProperty( IComponent pointProperty, IComponent initialValues );

  void addPointProperty( IComponent pointProperty, Object defaultValue );

  IProfileObject[] addProfileObjects( IProfileObject... profileObjects );

  void addProfilListener( IProfileListener listener );

  IProfilePointMarker createPointMarker( String markerID, IProfileRecord point );

  /**
   * @return a valid profilPoint, addable to this profile
   * @see #addPoint(IRecord)
   */
  IProfileRecord createProfilPoint( );

  /**
   * @return something stored in the profile as Strings
   */
  String getComment( );

  IProfileRecord[] getMarkedPoints( );

  IProfileRecord getPoint( int index );

  /**
   * Gets all PointMarker of the given type in this profile.
   */
  IProfilePointMarker[] getPointMarkerFor( IComponent pointMarker );

  /**
   * Gets all markers for this record.
   */
  IProfilePointMarker[] getPointMarkerFor( IProfileRecord record );

  /**
   * Gets all PointMarker of the given type in this profile.
   */
  IProfilePointMarker[] getPointMarkerFor( String pointMarkerID );

  /**
   * @return all Marker-Types stored in This profile, NOT all available Marker-Types registered for this {@link #getType()}
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarkerProvider
   */
  IComponent[] getPointMarkerTypes( );

  /**
   * @return all PointProperties used by this profile
   */
  IComponent[] getPointProperties( );

  IComponent getPointPropertyFor( String propertyID );

  /**
   * @return Points of profile
   */
  IProfileRecord[] getPoints( );

  /**
   * @return include both , startPoint and endPoint
   */
  IProfileRecord[] getPoints( int startPoint, int endPoint );

  /**
   * Returns the current problem markers, if any, attached to this profile.
   */
  MarkerIndex getProblemMarker( );

  /**
   * @return the current building(Tuhh) or other kind of ProfileObject, maybe null
   */
  IProfileObject[] getProfileObjects( );

  /**
   * @return the current building(Tuhh) or other kind of ProfileObject, maybe null
   */
  <T extends IProfileObject> T[] getProfileObjects( Class<T> clazz );

  IRangeSelection getSelection( );

  /**
   * @return source of the profile
   */
  IProfileFeature getSource( );

  double getStation( );

  /**
   * Returns the type of the profile.
   * <p>
   * The type controls the following behaviour:
   * <ul>
   * <li>Visualisation (which layers are used)</li>
   * <li>Serialization</li>
   * <li>Validation (which rules are applied)</li>
   * </ul>
   */
  String getType( );

  /**
   * @return true if the profile contains the property
   * @see org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider to get addable properties
   */
  boolean hasPointProperty( IComponent property );

  /**
   * @return the FIRST component with the given Id, if the profile contains the property otherwise null
   * @note the Id maybe NOT unique in the profiles TupleResult
   * @see #hasPointProperty(IComponent)
   * @see org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider to get addable properties
   */
  IComponent hasPointProperty( String propertyId );

  int indexOf( IProfileRecord record );

  int indexOfProperty( IComponent pointProperty );

  int indexOfProperty( String id );

  boolean isPointMarker( String propertyID );

  boolean removePoint( IProfileRecord point );

  /*
   * obsolete - point markers will be automatically set by their own setValue() implementation (value will be directly
   * added to observation, and so the point marker is registered)
   */
  Object removePointMarker( IProfilePointMarker devider );

  /**
   * @param pointProperty
   *          to remove
   * @return false if the pointProperty is not used in this profile
   */
  boolean removePointProperty( IComponent pointProperty );

  boolean removePoints( IProfileRecord[] points );

  boolean removeProfileObject( IProfileObject profileObject );

  void removeProfilListener( IProfileListener pl );

  void setComment( String comment );

  /**
   * Sets the current problem markers of this profile.<br>
   * A profile event is fired upon this action.
   */
  void setProblemMarker( IMarker[] markers );

  void setStation( double station );

  IProfileRecord findNextPoint( double breite );

  IProfileRecord findPreviousPoint( double breite );

  IProfilePointMarker[] getPointMarkers( );

  /**
   * Locks all change events until {@link #stopTransaction(Object, ProfilChangeHint)} is called.
   * 
   * @deprecated use {@link IProfil#doTransaction(IProfileTransaction)}
   */
  @Deprecated
  void startTransaction( Object lock );

  /**
   * Unlocks change events and fire one big event with the given hint.
   * 
   * @deprecated use {@link IProfil#doTransaction(IProfileTransaction)}
   */
  @Deprecated
  void stopTransaction( Object lock );

  String getSrsName( );

  void setSrsName( String srsName );

  IStatus doTransaction( IProfileTransaction transaction );

  IProfileMetadata getMetadata( );
}