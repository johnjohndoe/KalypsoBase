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
package org.kalypso.ogc.sensor.filter.filters;

import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.filter.filters.valuecomp.IValueComp;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * ValueFilter
 * 
 * @author schlienger
 */
public class ValueFilter extends AbstractObservationFilter
{
  private final Map<IAxis, IValueComp> m_axisMap = new Hashtable<IAxis, IValueComp>();

  @Override
  public void initFilter( final Object conf, final IObservation obs, final URL context ) throws SensorException
  {
    super.initFilter( conf, obs, context );

    m_axisMap.clear();

    final Iterator<IValueComp> it = ((List<IValueComp>) conf).iterator();
    while( it.hasNext() )
    {
      final IValueComp vc = it.next();

      m_axisMap.put( vc.getAxis(), vc );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest args ) throws SensorException
  {
    final ITupleModel values = super.getValues( args );
    final IAxis[] axes = values.getAxisList();

    final SimpleTupleModel newValues = new SimpleTupleModel( axes );

    for( int i = 0; i < values.getCount(); i++ )
    {
      final Vector<Object> tupple = new Vector<Object>( axes.length );

      boolean add = true;

      for( final IAxis axe : axes )
      {
        final IValueComp comp = m_axisMap.get( axe );

        final Object elt = values.getElement( i, axe );

        if( comp == null || comp.validates( elt ) )
          tupple.add( newValues.getPositionFor( axe ), elt );
        else
        {
          add = false;
          continue;
        }
      }

      if( add )
        newValues.addTupple( tupple );
    }

    return newValues;
  }
}