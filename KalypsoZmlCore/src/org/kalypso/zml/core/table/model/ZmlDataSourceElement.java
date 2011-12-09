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
package org.kalypso.zml.core.table.model;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Set;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.PooledObsProvider;
import org.kalypso.zml.core.table.IZmlTableElement;
import org.kalypso.zml.core.table.model.memento.IZmlMemento;
import org.kalypso.zml.core.table.model.memento.LabeledObsProviderDelegate;

/**
 * @author Dirk Kuch
 */
public class ZmlDataSourceElement implements IZmlTableElement
{
  private PooledObsProvider m_provider;

  private final String m_identifier;

  private final String m_href;

  private final URL m_context;

  private final String m_labeling;

  private String m_label;

  private final IZmlMemento m_memento;

  public ZmlDataSourceElement( final String identifier, final String href, final URL context, final String labeling, final IZmlMemento memento )
  {
    m_identifier = identifier;
    m_href = href;
    m_context = context;
    m_labeling = labeling;
    m_memento = memento;
  }

  @Override
  public void dispose( )
  {
    m_provider.dispose();
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  @Override
  public IObsProvider getObsProvider( )
  {
    final PoolableObjectType type = new PoolableObjectType( "zml", m_href, m_context, true ); //$NON-NLS-1$

    synchronized( this )
    {
      if( Objects.isNotNull( m_provider ) )
        return m_provider;

// KalypsoZmlCoreDebug.DEBUG_TABLE_MODEL_INIT.printf( "Creating new pooled obs provider - %s\n", type );

      m_provider = new PooledObsProvider( type );
    }

    m_memento.register( type, new LabeledObsProviderDelegate( m_provider, m_labeling ) );

    return m_provider;
  }

  @Override
  public String getTitle( final IAxis axis )
  {
    if( Strings.isNotEmpty( m_label ) )
      return m_label;

    final IObservation observation = getObsProvider().getObservation();
    final MetadataList metadata = observation.getMetadataList();

    final String[] properties = findProperties( m_labeling );
    m_label = m_labeling;

    for( final String property : properties )
    {
      final String value = metadata.getProperty( property );

      m_label = m_label.replaceAll( "%" + property + "%", value ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    return m_label;
  }

  private String[] findProperties( final String labeling )
  {
    final Set<String> properties = new LinkedHashSet<String>();

    int ptr = 0;
    while( ptr >= 0 )
    {
      final int index = labeling.indexOf( '%', ptr ) + 1; //$NON-NLS-1$
      if( index <= 0 )
        break;

      final int index2 = labeling.substring( index ).indexOf( '%' ) + 1; //$NON-NLS-1$
      properties.add( labeling.substring( index, index2 ) );

      ptr = index2 + 1;
    }

    return properties.toArray( new String[] {} );
  }

  @Override
  public String getTitleTokenzizer( )
  {
    return "";
  }

}