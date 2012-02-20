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
package org.kalypso.model.wspm.core.profil.wrappers;

import java.util.Comparator;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.kalypso.commons.java.lang.Doubles;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.jts.JTSConverter;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IRangeSelection;
import org.kalypso.model.wspm.core.profil.util.ProfilUtil;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Dirk Kuch
 */
public class ProfileRecord extends AbstractRecordWrapper implements IProfileRecord
{

  public static final Comparator<IProfileRecord> COMPARATOR = new Comparator<IProfileRecord>()
  {

    @Override
    public int compare( final IProfileRecord o1, final IProfileRecord o2 )
    {
      final double b1 = o1.getBreite();
      final double b2 = o2.getBreite();

      return Double.valueOf( b1 ).compareTo( Double.valueOf( b2 ) );
    }
  };

  private IProfil m_profile;

  public ProfileRecord( final IProfil parent, final IRecord record )
  {
    super( record );

    setProfile( parent );
  }

  @Override
  public boolean equals( final Object object )
  {
    if( object instanceof IProfileRecord )
    {
      final IProfileRecord other = (IProfileRecord) object;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( hashCode(), other.hashCode() );

      return builder.isEquals();
    }

    return super.equals( object );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getRecord() );

    return builder.toHashCode();
  }

  @Override
  public Double getHoehe( )
  {
    final double value = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOEHE, this );
    if( Double.isNaN( value ) )
      return null;

    return value;
  }

  @Override
  public Double getBreite( )
  {
    final double value = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_BREITE, this );
    if( Double.isNaN( value ) )
      return null;

    return value;
  }

  @Override
  public Double getHochwert( )
  {
    final double value = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_HOCHWERT, this );
    if( Double.isNaN( value ) )
      return null;

    return value;
  }

  @Override
  public Double getRechtswert( )
  {
    final double value = ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT, this );
    if( Double.isNaN( value ) )
      return null;

    return value;
  }

  @Override
  public void setBreite( final Double width )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BREITE );
    getRecord().setValue( index, width );
  }

  @Override
  public void setHoehe( final Double hoehe )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_HOEHE );
    getRecord().setValue( index, hoehe );
  }

  @Override
  public String toString( )
  {
    return String.format( "[%.2f, %.2f]", getBreite(), getHoehe() ); //$NON-NLS-1$
  }

  @Override
  public void setKsValue( final Double ksValue )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS );
    if( index < 0 )
      return;

    getRecord().setValue( index, ksValue );
  }

  @Override
  public void setKstValue( final Double kstValue )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST );
    if( index < 0 )
      return;

    getRecord().setValue( index, kstValue );
  }

  /**
   * Returns the geo coordinate (hochwert, rechtswert) of this point. The returned coordinate is in the coordinate
   * system of the profile.
   */
  @Override
  public Coordinate getCoordinate( )
  {
    final Double x = getRechtswert();
    final Double y = getHochwert();
    final Double z = getHoehe();

    if( Doubles.isNaN( x, y ) )
      return null;

    if( z == null )
      return new Coordinate( x, y, Double.NaN );

    return new Coordinate( x, y, z );
  }

  @Override
  public Double getKsValue( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KS, this );
  }

  @Override
  public Double getKstValue( )
  {
    return ProfilUtil.getDoubleValueFor( IWspmPointProperties.POINT_PROPERTY_RAUHEIT_KST, this );
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public void setBewuchsAx( final Double bewuchsAx )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
    if( index < 0 )
      return;

    getRecord().setValue( index, bewuchsAx );
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public void setBewuchsAy( final Double bewuchsAy )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
    if( index < 0 )
      return;

    getRecord().setValue( index, bewuchsAy );
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public void setBewuchsDp( final Double bewuchsDp )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );
    if( index < 0 )
      return;

    getRecord().setValue( index, bewuchsDp );
  }

  @Override
  public void setRechtswert( final double x )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_RECHTSWERT );
    if( index < 0 )
      return;
    getRecord().setValue( index, x );
  }

  @Override
  public void setHochwert( final double y )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_HOCHWERT );
    if( index < 0 )
      return;

    getRecord().setValue( index, y );
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public Double getBewuchsAx( )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AX );
    if( index < 0 )
      return null;

    final Object value = getRecord().getValue( index );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return null;
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public Double getBewuchsAy( )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_AY );
    if( index < 0 )
      return null;

    final Object value = getRecord().getValue( index );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return null;
  }

  /**
   * FIXME use BigDecimal
   */
  @Override
  public Double getBewuchsDp( )
  {
    final int index = findComponent( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_DP );
    if( index < 0 )
      return null;

    final Object value = getRecord().getValue( index );
    if( value instanceof Number )
      return ((Number) value).doubleValue();

    return null;
  }

  @Override
  public Point toPoint( )
  {
    return JTSConverter.toPoint( new Coordinate( getRechtswert(), getHochwert() ) );
  }

  @Override
  public IProfil getProfile( )
  {
    return m_profile;
  }

  @Override
  public IProfileRecord cloneRecord( )
  {
    return new ProfileRecord( getProfile(), getRecord().cloneRecord() );
  }

  public void setProfile( final IProfil profile )
  {
    m_profile = profile;
  }

  @Override
  public int getIndex( )
  {
    return getRecord().getIndex();
  }

  @Override
  public int indexOfProperty( final IComponent pointProperty )
  {
    return getProfile().indexOfProperty( pointProperty );
  }

  @Override
  public int indexOfProperty( final String id )
  {
    return getProfile().indexOfProperty( id );
  }

  @Override
  public boolean hasPointProperty( final IComponent component )
  {
    return getProfile().hasPointProperty( component );
  }

  @Override
  public IComponent hasPointProperty( final String identifier )
  {
    return getProfile().hasPointProperty( identifier );
  }

  @Override
  public IProfileRecord getNextPoint( )
  {
    final TupleResult result = getOwner();
    if( Objects.isNull( result ) )
      return getProfile().findNextPoint( getBreite() );

    final int index = getIndex();
    if( result.size() - 1 > index )
      return new ProfileRecord( getProfile(), result.get( index + 1 ) );

    return getProfile().findNextPoint( getBreite() );
  }

  @Override
  public IProfileRecord getPreviousPoint( )
  {
    final TupleResult result = getOwner();
    if( Objects.isNull( result ) )
      return getProfile().findPreviousPoint( getBreite() );

    final int index = getIndex();
    if( index == 0 )
      return null;
    if( result.size() >= index )
      return new ProfileRecord( getProfile(), result.get( index - 1 ) );

    return getProfile().findPreviousPoint( getBreite() );
  }

  @Override
  public Range<Double> getBreiteAsRange( )
  {
    return Range.is( getBreite() );
  }

  @Override
  public boolean isSelected( )
  {
    final IProfil profile = getProfile();
    if( Objects.isNull( profile ) )
      return false;

    final IRangeSelection selection = profile.getSelection();
    final Range<Double> range = selection.getRange();
    if( Objects.isNull( range ) )
      return false;

    return range.contains( getBreite() );
  }
}
