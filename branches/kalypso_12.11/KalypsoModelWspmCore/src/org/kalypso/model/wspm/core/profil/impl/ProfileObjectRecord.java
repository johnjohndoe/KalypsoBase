/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.core.profil.impl;

import org.kalypso.model.wspm.core.profil.IProfileObjectRecord;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * @author Holger Albert
 */
class ProfileObjectRecord implements IProfileObjectRecord
{
  private final ProfileObjectRecords m_parent;

  private String m_id;

  private String m_comment;

  private Double m_breite;

  private Double m_hoehe;

  private Double m_rechtswert;

  private Double m_hochwert;

  private String m_code;

  public ProfileObjectRecord( final ProfileObjectRecords parent )
  {
    this( parent, null, null, 0.0, 0.0, 0.0, 0.0, null );
  }

  public ProfileObjectRecord( final ProfileObjectRecords parent, final String id, final String comment, final Double breite, final Double hoehe, final Double rechtswert, final Double hochwert, final String code )
  {
    m_parent = parent;
    m_id = id;
    m_comment = comment;
    m_breite = breite;
    m_hoehe = hoehe;
    m_rechtswert = rechtswert;
    m_hochwert = hochwert;
    m_code = code;
  }

  @Override
  public String getId( )
  {
    return m_id;
  }

  @Override
  public void setId( final String id )
  {
    m_id = id;
    fireProfileObjectRecordChanged();
  }

  @Override
  public String getComment( )
  {
    return m_comment;
  }

  @Override
  public void setComment( final String comment )
  {
    m_comment = comment;
    fireProfileObjectRecordChanged();
  }

  @Override
  public Double getBreite( )
  {
    return m_breite;
  }

  @Override
  public void setBreite( final Double breite )
  {
    m_breite = breite;
    fireProfileObjectRecordChanged();
  }

  @Override
  public Double getHoehe( )
  {
    return m_hoehe;
  }

  @Override
  public void setHoehe( final Double hoehe )
  {
    m_hoehe = hoehe;
    fireProfileObjectRecordChanged();
  }

  @Override
  public Double getRechtswert( )
  {
    return m_rechtswert;
  }

  @Override
  public void setRechtswert( final Double rechtswert )
  {
    m_rechtswert = rechtswert;
    fireProfileObjectRecordChanged();
  }

  @Override
  public Double getHochwert( )
  {
    return m_hochwert;
  }

  @Override
  public void setHochwert( final Double hochwert )
  {
    m_hochwert = hochwert;
    fireProfileObjectRecordChanged();
  }

  @Override
  public String getCode( )
  {
    return m_code;
  }

  @Override
  public void setCode( final String code )
  {
    m_code = code;

    fireProfileObjectRecordChanged();
  }

  @Override
  public Coordinate getWidthHeightLocation( )
  {
    final Double breite = getBreite();
    final Double hoehe = getHoehe();
    if( breite != null && hoehe != null )
      return new Coordinate( breite.doubleValue(), hoehe.doubleValue() );

    return null;
  }

  @Override
  public Coordinate getGeoLocation( )
  {
    final Double rechtswert = getRechtswert();
    final Double hochwert = getHochwert();
    if( rechtswert != null && hochwert != null )
      return new Coordinate( rechtswert.doubleValue(), hochwert.doubleValue() );

    return null;
  }

  private void fireProfileObjectRecordChanged( )
  {
    if( m_parent == null )
      return;

    m_parent.fireProfileObjectRecordChanged( this );
  }
}