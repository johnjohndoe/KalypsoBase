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
package org.kalypso.zml.ui.imports;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.impl.SimpleTupleModel;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.timeseries.wq.WQTuppleModel;

/**
 * @author Gernot Belger
 */
public class TargetObservation
{
  private final ITupleModel m_targetModel;

  private final int m_countTarget;

  private final IObservation m_targetObservation;

  private ITupleModel m_newTuppelModel;

  public TargetObservation( final IObservation targetObservation, final ITupleModel targetModel, final int countTarget )
  {
    m_targetObservation = targetObservation;
    m_targetModel = targetModel;
    m_countTarget = countTarget;
  }

  private ITupleModel createNewTuppleModel( final int countSrc, final IAxis[] axesNew )
  {
    if( m_targetModel == null )
    {
      final Object[][] newValues = new Object[countSrc + m_countTarget][axesNew.length];
      return new SimpleTupleModel( axesNew, newValues );
    }

    if( m_targetModel instanceof WQTuppleModel )
    {
      final WQTuppleModel wq = (WQTuppleModel) m_targetModel;
      final Object[][] newValues = new Object[countSrc + m_countTarget][axesNew.length - 1];
      final ITupleModel model = new SimpleTupleModel( axesNew, newValues );

      return new WQTuppleModel( model, wq.getMetadata(), axesNew, wq.getSourceAxes(), wq.getTargetAxes(), wq.getConverter() );
    }

    final Object[][] newValues = new Object[countSrc + m_countTarget][axesNew.length];
    return new SimpleTupleModel( axesNew, newValues );
  }

  public void fillSourceData( final int countSrc, final ITupleModel srcModel, final IAxis[] axesSrc, final IAxis[] axesNew ) throws SensorException
  {
    m_newTuppelModel = createNewTuppleModel( countSrc, axesNew );

    // fill from source
    for( int i = 0; i < countSrc; i++ )
    {
      for( int a = 0; a < axesNew.length; a++ )
      {
        final Object newValue = findSrcValue( srcModel, axesSrc, axesNew, i, a );
        m_newTuppelModel.set( i, axesNew[a], newValue );
      }
    }

    // append from existing target
    if( m_targetModel != null )
    {
      for( int i = 0; i < m_countTarget; i++ )
      {
        for( final IAxis element : axesNew )
          m_newTuppelModel.set( countSrc + i, element, m_targetModel.get( i, element ) );
      }
    }
  }

  private Object findSrcValue( final ITupleModel srcModel, final IAxis[] axesSrc, final IAxis[] axesNew, final int i, final int a ) throws SensorException
  {
    if( axesSrc[a] == null )
    {
      if( KalypsoStatusUtils.isStatusAxis( axesNew[a] ) )
        return new Integer( KalypsoStati.BIT_USER_MODIFIED );
      else
        return null;
    }

    return srcModel.get( i, axesSrc[a] );
  }

  public Map< ? extends Object, ? extends Object> getMetadataList( )
  {
    if( m_targetObservation != null )
      return m_targetObservation.getMetadataList();

    return new MetadataList();
  }

  public IObservation createNewObservation( final String name, final MetadataList metadata )
  {
    final String href = StringUtils.EMPTY;
    return new SimpleObservation( href, name, metadata, m_newTuppelModel );
  }
}
