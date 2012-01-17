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

import org.eclipse.core.resources.IMarker;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

/**
 * @author kimwerner
 */
public interface IProfil extends IObservation<TupleResult>
{
  void addPoint( int index, IRecord point );

  /**
   * @return true
   *         <p>
   *         adds a new Record at the end of this Observation and copies the values of the Components existing in both
   *         records
   */
  boolean addPoint( IProfileRecord point );

  /**
   * @param pointProperty
   */
  void addPointProperty( IComponent pointProperty );

  void addPointProperty( IComponent pointProperty, Object defaultValue );

  IComponent getPointPropertyFor( String propertyID );

  void addPointProperty( IComponent pointProperty, IComponent initialValues );

  /**
   * remove the current ProfileObject and adds the given ProfileObject
   * 
   * @return the oldObject
   * @param building
   *          must not be null, in this case use removeProfileObject()
   */
  IProfileObject[] addProfileObjects( IProfileObject... profileObjects );

  void addProfilListener( IProfilListener pl );

  boolean isPointMarker( String propertyID );

  void fireProfilChanged( ProfilChangeHint hint, IProfilChange[] changes );

  void removeProfilListener( IProfilListener pl );

  /**
   * @return a valid profilPoint, addable to this profile
   * @see #addPoint(IRecord)
   */
  IProfileRecord createProfilPoint( );

  IProfilPointMarker createPointMarker( String markerID, IProfileRecord point );

  IRangeSelection getSelection( );

  /**
   * @return something stored in the profile as Strings
   */
  String getComment( );

  IProfileRecord[] getMarkedPoints( );

  /**
   * Gets all PointMarker of the given type in this profile.
   */
  IProfilPointMarker[] getPointMarkerFor( IComponent pointMarker );

  /**
   * Gets all PointMarker of the given type in this profile.
   */
  IProfilPointMarker[] getPointMarkerFor( String pointMarkerID );

  /**
   * Gets all markers for this record.
   */
  IProfilPointMarker[] getPointMarkerFor( IProfileRecord record );

  /**
   * @return all Marker-Types stored in This profile, NOT all available Marker-Types registered for this
   *         {@link #getType()}
   * @see org.kalypso.model.wspm.core.profil.IProfilPointMarkerProvider
   */
  IComponent[] getPointMarkerTypes( );

  IProfileRecord getPoint( int index );

  /**
   * @return include both , startPoint and endPoint
   */
  IProfileRecord[] getPoints( int startPoint, int endPoint );

  /**
   * @return all PointProperties used by this profile
   */
  IComponent[] getPointProperties( );

  /**
   * @return Points of profile
   */
  IProfileRecord[] getPoints( );

  /**
   * @return the current building(Tuhh) or other kind of ProfileObject, maybe null
   */
  IProfileObject[] getProfileObjects( );

  /**
   * @return the current building(Tuhh) or other kind of ProfileObject, maybe null
   */
  <T extends IProfileObject> T[] getProfileObjects( Class<T> clazz );

  /**
   * FIXME: make usable again:<br>
   * - use String/String instead of object/object<br/>
   * - make sure everything gets serialized automatically (feature <-> profile)
   * 
   * @param key
   * @return the value from internal HashMap<Object,Object>
   * @deprecated caution: additional properties will not be serialized to profile features
   */
  @Deprecated
  Object getProperty( Object key );

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

  boolean removePoint( IProfileRecord point );

  boolean removePoints( IProfileRecord[] points );

  /*
   * obsolete - point markers will be automatically set by their own setValue() implementation (value will be directly
   * added to observation, and so the point marker is registered)
   */
  Object removePointMarker( IProfilPointMarker devider );

  /**
   * @param pointProperty
   *          to remove
   * @return false if the pointProperty is not used in this profile
   */
  boolean removePointProperty( IComponent pointProperty );

  boolean removeProfileObject( IProfileObject profileObject );

  /**
   * @param key
   *          removes the key and its value from the profiles internal HashMap<Object,Object>
   */
  Object removeProperty( Object key );

  void setComment( String comment );

  /**
   * @param key
   * @param value
   *          saves any (key,value-Object) in the profiles internal HashMap
   */
  void setProperty( Object key, Object value );

  void setStation( double station );

  /**
   * Returns the current problem markers, if any, attached to this profile.
   */
  MarkerIndex getProblemMarker( );

  /**
   * Sets the current problem markers of this profile.<br>
   * A profile event is fired upon this action.
   */
  void setProblemMarker( IMarker[] markers );

  /**
   * @return source of the profile
   */
  Object getSource( );

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

  int indexOfProperty( IComponent pointProperty );

  int indexOfProperty( String id );

  int indexOf( IProfileRecord record );

}