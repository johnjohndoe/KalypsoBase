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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.model.wspm.core.profil.IProfileMetadata;
import org.kalypso.model.wspm.core.profil.IProfileObject;
import org.kalypso.model.wspm.core.profil.IProfileObjectListener;
import org.kalypso.model.wspm.core.profil.IProfileObjectRecord;
import org.kalypso.model.wspm.core.profil.IProfileObjectRecords;
import org.kalypso.model.wspm.core.profil.changes.ProfileChangeHint;
import org.kalypso.model.wspm.core.profil.util.ProfileUtil;
import org.kalypso.observation.result.IComponent;

/**
 * @author Holger Albert
 */
public abstract class AbstractProfileObject extends ProfileMetadataObserver implements IProfileObject
{
  private final List<IProfileObjectListener> m_listener = new ArrayList<>();

  private String m_description = null;

  private final String m_typeLabel = createTypeLabel();

  private final IProfileObjectRecords m_records = new ProfileObjectRecords( this );

  // FIXME: we need another hint
  private final IProfileMetadata m_metadata = new ProfileMetadata( this, ProfileChangeHint.PROFILE_PROPERTY_CHANGED );

  protected AbstractProfileObject( )
  {
  }

  @Override
  public String getDescription( )
  {
    return m_description;
  }

  @Override
  public void setDescription( final String description )
  {
    m_description = description;
  }

  @Override
  public String getTypeLabel( )
  {
    return m_typeLabel;
  }

  @Override
  public String[] getProperties( )
  {
    return new String[] {};
  }

  @Override
  public String getPropertyLabel( final String property )
  {
    return null;
  }

  @Override
  public IProfileObjectRecords getRecords( )
  {
    return m_records;
  }

  @Override
  public IProfileMetadata getMetadata( )
  {
    return m_metadata;
  }

  @Override
  public String getValue( final String key, final String defaultValue )
  {
    final String value = m_metadata.getMetadata( key );
    if( value == null )
      return defaultValue;

    return value;
  }

  @Override
  public void setValue( final String key, final String value )
  {
    m_metadata.setMetadata( key, value );
  }

  @Override
  public String removeValue( final String key )
  {
    return m_metadata.removeMetadata( key );
  }

  @Override
  public void addProfileObjectListener( final IProfileObjectListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  @Override
  public void removeProfileObjectListener( final IProfileObjectListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  @Override
  void fireProfilChanged( final ProfileChangeHint hint )
  {
    fireProfileObjectMetadataChanged();
  }

  protected String createTypeLabel( )
  {
    try
    {
      // FIXME Children should provide the type description...
      final String buildingId = getType();
      final IComponent buildingComponent = ProfileUtil.getFeatureComponent( buildingId );
      return buildingComponent == null ? buildingId : buildingComponent.getName();
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
      return "Generisch"; //$NON-NLS-1$
    }
  }

  protected Integer getIntegerValue( final String key, final Integer defaultValue )
  {
    final String value = m_metadata.getMetadata( key );
    if( value == null )
      return defaultValue;

    final Integer integerValue = NumberUtils.parseQuietInteger( value );
    if( integerValue == null )
      return defaultValue;

    return integerValue;
  }

  protected void setIntegerValue( final String key, final Integer value )
  {
    if( value == null )
    {
      m_metadata.setMetadata( key, null );
      return;
    }

    m_metadata.setMetadata( key, String.format( Locale.PRC, "%d", value.intValue() ) ); //$NON-NLS-1$
  }

  protected Double getDoubleValue( final String key, final Double defaultValue )
  {
    final String value = m_metadata.getMetadata( key );
    if( value == null )
      return defaultValue;

    final double doubleValue = NumberUtils.parseQuietDouble( value );
    if( Double.isNaN( doubleValue ) )
      return defaultValue;

    return new Double( doubleValue );
  }

  protected void setDoubleValue( final String key, final Double value )
  {
    if( value == null )
    {
      m_metadata.setMetadata( key, null );
      return;
    }

    m_metadata.setMetadata( key, String.format( Locale.PRC, "%f", value.doubleValue() ) ); //$NON-NLS-1$
  }

  protected BigDecimal getBigDecimalValue( final String key, final BigDecimal defaultValue )
  {
    final String value = m_metadata.getMetadata( key );
    if( value == null )
      return defaultValue;

    try
    {
      return NumberUtils.parseBigDecimal( value );
    }
    catch( final NumberFormatException e )
    {
      // Ignore
      return defaultValue;
    }
  }

  protected void setBigDecimalValue( final String key, final BigDecimal value )
  {
    if( value == null )
    {
      m_metadata.setMetadata( key, null );
      return;
    }

    m_metadata.setMetadata( key, String.format( Locale.PRC, "%f", value.doubleValue() ) ); //$NON-NLS-1$
  }

  @SuppressWarnings( "unused" )
  protected void fireProfileObjectRecordChanged( final IProfileObjectRecord changedRecord )
  {
    for( final IProfileObjectListener listener : m_listener )
      listener.profileObjectChanged( this );
  }

  protected void fireProfileObjectRecordsChanged( )
  {
    for( final IProfileObjectListener listener : m_listener )
      listener.profileObjectChanged( this );
  }

  private void fireProfileObjectMetadataChanged( )
  {
    for( final IProfileObjectListener listener : m_listener )
      listener.profileObjectChanged( this );
  }
}