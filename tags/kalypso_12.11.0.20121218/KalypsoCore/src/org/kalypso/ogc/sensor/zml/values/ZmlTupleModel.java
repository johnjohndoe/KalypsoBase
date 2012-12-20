/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.zml.values;

import java.util.Map;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.event.IObservationChangeEvent;
import org.kalypso.ogc.sensor.impl.AbstractTupleModel;

/**
 * A specific TuppleModel that can deal with values coming from Zml-Files.
 *
 * @author schlienger
 */
public class ZmlTupleModel extends AbstractTupleModel
{
  private final Map<IAxis, IZmlValues> m_valuesMap;

  /**
   * Constructor
   *
   * @param valuesMap
   */
  public ZmlTupleModel( final Map<IAxis, IZmlValues> valuesMap )
  {
    super( valuesMap.keySet().toArray( new IAxis[0] ) );

    m_valuesMap = valuesMap;
  }

  @Override
  public int size( ) throws SensorException
  {
    if( m_valuesMap.size() == 0 )
      return 0;

    return m_valuesMap.values().iterator().next().getCount();
  }

  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    if( m_valuesMap.size() == 0 )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.zml.values.ZmlTuppleModel.0" ) ); //$NON-NLS-1$

    final IZmlValues values = m_valuesMap.get( axis );
    if( values == null )
      return -1;

    return values.indexOf( element );
  }

  @Override
  public Object get( final int index, final IAxis axis ) throws SensorException
  {
    if( m_valuesMap.size() == 0 )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.zml.values.ZmlTuppleModel.1" ) ); //$NON-NLS-1$

    final IZmlValues values = m_valuesMap.get( axis );

    if( values == null )
      throw new SensorException( String.format( "Unknwon axis: %s", axis.getName() ) ); //$NON-NLS-1$
    // return new Double( 0 );

    return values.getElement( index );
  }

  @Override
  public void set( final int index, final IAxis axis, final Object element ) throws SensorException
  {
    if( m_valuesMap.size() == 0 )
      throw new IllegalStateException( Messages.getString( "org.kalypso.ogc.sensor.zml.values.ZmlTuppleModel.2" ) ); //$NON-NLS-1$

    m_valuesMap.get( axis ).setElement( index, element );

    fireModelChanged( IObservationChangeEvent.VALUE_CHANGED );
  }
}