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
package org.kalypso.ui.wizard.sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;

/**
 * @author Gernot Belger
 */
public class ImportObservationData extends AbstractModelObject
{
  static final String PROPERTY_TIMEZONE = "timezone";

  static final String PROPERTY_ADAPTER = "adapter";

  public static final String PROPERTY_PARAMETER_AXIS = "parameterAxis";

  private String m_timezone = KalypsoCorePlugin.getDefault().getTimeZone().getID();

  private final INativeObservationAdapter[] m_adapters;

  private INativeObservationAdapter m_adapter = null;

  private final FileAndHistoryData m_sourceFileData = new FileAndHistoryData( "sourceFile" ); //$NON-NLS-1$

  private final IAxis[] m_allowedParameterAxes;

  private IAxis m_parameterAxis;

  public ImportObservationData( final IAxis[] allowedParameterAxes )
  {
    final INativeObservationAdapter[] adapters = KalypsoCoreExtensions.createNativeAdapters();

    /* Build minimal subset of allowed axis and available adapters */
    final Set<String> minimalTypes = findMinimalTypeSet( allowedParameterAxes, adapters );

    m_adapters = filterAdapters( adapters, minimalTypes );
    m_allowedParameterAxes = filterAxes( allowedParameterAxes, minimalTypes );

    if( m_adapters.length > 0 )
      m_adapter = m_adapters[0];

    if( m_allowedParameterAxes.length > 0 )
      m_parameterAxis = m_allowedParameterAxes[0];
  }

  private INativeObservationAdapter[] filterAdapters( final INativeObservationAdapter[] adapters, final Set<String> minimalTypes )
  {
    final Collection<INativeObservationAdapter> filteredAdapters = new ArrayList<>();

    for( final INativeObservationAdapter adapter : adapters )
    {
      if( minimalTypes.contains( adapter.getAxisTypeValue() ) )
        filteredAdapters.add( adapter );
    }

    return filteredAdapters.toArray( new INativeObservationAdapter[filteredAdapters.size()] );
  }

  private IAxis[] filterAxes( final IAxis[] allowedParameterAxes, final Set<String> minimalTypes )
  {
    final Collection<IAxis> filteredAxes = new ArrayList<>();

    for( final IAxis axis : allowedParameterAxes )
    {
      if( minimalTypes.contains( axis.getType() ) )
        filteredAxes.add( axis );
    }

    return filteredAxes.toArray( new IAxis[filteredAxes.size()] );
  }

  private Set<String> findMinimalTypeSet( final IAxis[] allowedParameterAxes, final INativeObservationAdapter[] adapters )
  {
    final Set<String> axesTypes = new HashSet<>();
    for( final IAxis axis : allowedParameterAxes )
      axesTypes.add( axis.getType() );

    final Set<String> adapterTypes = new HashSet<>();
    for( final INativeObservationAdapter adapter : adapters )
      adapterTypes.add( adapter.getAxisTypeValue() );

    axesTypes.retainAll( adapterTypes );

    return Collections.unmodifiableSet( axesTypes );
  }

  public FileAndHistoryData getSourceFileData( )
  {
    return m_sourceFileData;
  }

  public String[] getAllTimezones( )
  {
    final String[] tz = TimeZone.getAvailableIDs();
    Arrays.sort( tz );
    return tz;
  }

  public TimeZone getTimezoneParsed( )
  {
    return TimeZone.getTimeZone( m_timezone );
  }

  public String getTimezone( )
  {
    return m_timezone;
  }

  public void setTimezone( final String timezone )
  {
    final String oldValue = m_timezone;

    m_timezone = timezone;

    firePropertyChange( PROPERTY_TIMEZONE, oldValue, timezone );
  }

  public INativeObservationAdapter[] getObservationAdapters( )
  {
    return m_adapters;
  }

  public INativeObservationAdapter getAdapter( )
  {
    return m_adapter;
  }

  public void setAdapter( final INativeObservationAdapter adapter )
  {
    final INativeObservationAdapter oldValue = m_adapter;

    m_adapter = adapter;

    firePropertyChange( PROPERTY_ADAPTER, oldValue, adapter );
  }

  public IAxis[] getAllowedParameterAxes( )
  {
    return m_allowedParameterAxes;
  }

  public IAxis getParameterAxis( )
  {
    return m_parameterAxis;
  }

  public void setParameterAxis( final IAxis parameterAxis )
  {
    final IAxis oldValue = m_parameterAxis;

    m_parameterAxis = parameterAxis;

    firePropertyChange( PROPERTY_PARAMETER_AXIS, oldValue, parameterAxis );
  }
}