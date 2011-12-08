/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.util.Arrays;
import java.util.TimeZone;

import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.core.KalypsoCoreExtensions;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.ogc.sensor.adapter.INativeObservationAdapter;

/**
 * @author Gernot Belger
 */
public class ImportObservationData extends AbstractModelObject
{
  static final String PROPERTY_TIMEZONE = "timezone";

  static final String PROPERTY_ADAPTER = "adapter";

  private String m_timezone = KalypsoCorePlugin.getDefault().getTimeZone().getID();

  private final INativeObservationAdapter[] m_adapters = KalypsoCoreExtensions.createNativeAdapters();

  private INativeObservationAdapter m_adapter = null;

  private final FileAndHistoryData m_sourceFileData = new FileAndHistoryData( "sourceFile" ); //$NON-NLS-1$

  public ImportObservationData( )
  {
    if( m_adapters.length > 0 )
      m_adapter = m_adapters[0];
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
}
