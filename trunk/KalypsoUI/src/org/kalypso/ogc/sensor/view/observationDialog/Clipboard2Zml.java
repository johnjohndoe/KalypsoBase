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
package org.kalypso.ogc.sensor.view.observationDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree_impl.gml.schema.SpecialPropertyMapper;

/**
 * Converts a clipbard content to an zml-observation.
 * 
 * @author Gernot Belger
 */
public class Clipboard2Zml
{
  private final List<Object[]> m_collector = new ArrayList<Object[]>();

  private final IAxis[] m_axis;

  public Clipboard2Zml( final IAxis[] axis )
  {
    m_axis = axis;
  }

  public ITupleModel convert( final String content ) throws CoreException
  {
    final String[] rows = content.split( "\\n" ); //$NON-NLS-1$
    for( final String row : rows )
    {
      try
      {
        final Object[] rowValues = convertRow( row );

        if( rowValues != null && rowValues[0] != null )
          m_collector.add( rowValues );
      }
      catch( final Exception e )
      {
        final String message = String.format( "Failed to parse row: %s", row );
        final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), message, e );
        throw new CoreException( status );
      }
    }

    final Object[][] values = m_collector.toArray( new Object[m_collector.size()][] );
    return new SimpleTupleModel( m_axis, values );
  }

  private Object[] convertRow( final String row ) throws Exception
  {
    final String[] cells = row.split( "\\t" ); //$NON-NLS-1$

    final Object[] rowValues = new Object[m_axis.length];
    for( int ax = 0; ax < m_axis.length; ax++ )
    {
      if( ax < cells.length )
        rowValues[ax] = parseValue( cells[ax], m_axis[ax] );
    }

    return rowValues;
  }

  private Object parseValue( final String stringValue, final IAxis axis ) throws Exception
  {
    final Class< ? > dataClass = axis.getDataClass();

    if( Number.class.isAssignableFrom( dataClass ) )
    {
      final String type = axis.getType();
      final NumberFormat numberFormat = TimeseriesUtils.getNumberFormatFor( type );
      final Object parsedValue = numberFormat.parseObject( stringValue );
      if( dataClass.isInstance( parsedValue ) )
        return parsedValue;

      return SpecialPropertyMapper.cast( parsedValue, dataClass, false );
    }
    else
      return SpecialPropertyMapper.cast( stringValue, dataClass, false );
  }

}
