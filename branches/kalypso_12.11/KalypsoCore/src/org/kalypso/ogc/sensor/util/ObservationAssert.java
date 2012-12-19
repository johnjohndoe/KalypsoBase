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
package org.kalypso.ogc.sensor.util;

import junit.framework.Assert;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;

/**
 * Helper that compares {@link IObservation}'s.
 * 
 * @author Gernot Belger
 */
public class ObservationAssert extends Assert
{
  private final IObservation m_expected;

  private final IObservation m_actual;

  private boolean m_compareName = true;

  private boolean m_compareHref = true;

  private boolean m_compareAxes = true;

  private boolean m_compareMetadata = true;

  private boolean m_compareValues = true;

  public ObservationAssert( final IObservation expected, final IObservation actual )
  {
    m_expected = expected;
    m_actual = actual;
  }

  public void ignoreName( )
  {
    m_compareName = false;
  }

  public void ignoreHref( )
  {
    m_compareHref = false;
  }

  public void ignoreAxes( )
  {
    m_compareAxes = false;
  }

  public void ignoreMetadata( )
  {
    m_compareMetadata = false;
  }

  public void ignoreValues( )
  {
    m_compareValues = false;
  }

  public void assertEquals( ) throws SensorException
  {
    if( m_compareName )
      assertNameEqual();

    if( m_compareHref )
      assertHrefEqual();

    if( m_compareAxes )
      assertAxesEqual();

    if( m_compareValues )
      assertValuesEqual();

    if( m_compareMetadata )
      assertMetadataEqual();
  }

  private void assertNameEqual( )
  {
    assertEquals( m_expected.getName(), m_actual.getName() );
  }

  private void assertHrefEqual( )
  {
    assertEquals( m_expected.getHref(), m_actual.getHref() );
  }

  private void assertAxesEqual( )
  {
    final IAxis[] expectedAxes = m_expected.getAxes();
    final IAxis[] actualAxes = m_actual.getAxes();

    assertEquals( expectedAxes.length, actualAxes.length );

    for( int i = 0; i < actualAxes.length; i++ )
    {
      final IAxis expectedAxis = expectedAxes[i];

      final String axisName = expectedAxis.getName();
      final IAxis actualAxis = ObservationUtilities.findAxisByName( actualAxes, axisName );
      assertNotNull( String.format( Messages.getString("ObservationAssert_0"), axisName ), actualAxes ); //$NON-NLS-1$

      assertAxisEqual( expectedAxis, actualAxis );
    }
  }

  private void assertAxisEqual( final IAxis expectedAxis, final IAxis actualAxis )
  {
    assertEquals( expectedAxis.getDataClass(), actualAxis.getDataClass() );
    assertEquals( expectedAxis.getName(), actualAxis.getName() );
    assertEquals( expectedAxis.getType(), actualAxis.getType() );
    assertEquals( expectedAxis.getUnit(), actualAxis.getUnit() );
  }

  private void assertMetadataEqual( )
  {
    final MetadataList expectedMetadata = m_expected.getMetadataList();
    final MetadataList actualMetadata = m_actual.getMetadataList();

    assertEquals( expectedMetadata, actualMetadata );
  }

  private void assertValuesEqual( ) throws SensorException
  {
    final ITupleModel expectedValues = m_expected.getValues( null );
    final ITupleModel actualValues = m_actual.getValues( null );

    final int expectedSize = expectedValues.size();
    final int actualSize = actualValues.size();
    assertEquals( Messages.getString("ObservationAssert_1"), expectedSize, actualSize ); //$NON-NLS-1$

    /* Axes should already have been compared, so they are the same */
    final IAxis[] expectedAxes = m_expected.getAxes();
    final IAxis[] actualAxes = m_actual.getAxes();

    final IAxis keyAxis = AxisUtils.findDateAxis( expectedAxes );

    for( int i = 0; i < expectedSize; i++ )
    {
      final Object key = expectedValues.get( i, keyAxis );
      final String msg = String.format( Messages.getString("ObservationAssert_2"), i, key ); //$NON-NLS-1$

      for( final IAxis expectedAxis : expectedAxes )
      {
        /* Find actualAxis by name, we cannot assume they are in the same order */
        final String axisName = expectedAxis.getName();
        final IAxis actualAxis = ObservationUtilities.findAxisByName( actualAxes, axisName );

        final Object expectedValue = expectedValues.get( i, expectedAxis );
        final Object actualValue = actualValues.get( i, actualAxis );

        final String axisMsg = String.format( Messages.getString("ObservationAssert_3"), expectedAxis ); //$NON-NLS-1$
        assertEquals( msg + axisMsg, expectedValue, actualValue );
      }
    }
  }
}