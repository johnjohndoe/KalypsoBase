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
package org.kalypso.ogc.sensor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;

/**
 * Simple implementation of the {@link ITuppleModel} interface.
 * 
 * @author Marc Schlienger
 */
public class SimpleTupleModel extends AbstractTupleModel
{
  /**
   * An empty tuple model.
   */
  public static final ITupleModel EMPTY_TUPPLEMODEL = new SimpleTupleModel( new IAxis[0] );

  /**
   * The list of tuples.
   */
  private List<Object[]> m_tuples;

  /**
   * The constructor. The model will contain no data.
   * 
   * @param axes
   *          A list of axes.
   */
  public SimpleTupleModel( final List<IAxis> axes )
  {
    this( axes.toArray( new IAxis[axes.size()] ) );
  }

  /**
   * The constructor. The model will contain no data.
   * 
   * @param axes
   *          An array of axes.
   */
  public SimpleTupleModel( final IAxis[] axes )
  {
    this( axes, new Object[0][axes.length] );
  }

  /**
   * The constructor. The model will contain a copy of the data from the given model.
   * 
   * @param copyTupples
   *          A model. Its data will be copied.
   */
  public SimpleTupleModel( final ITupleModel copyTupples ) throws SensorException
  {
    super( copyTupples.getAxisList() );

    // TODO this leads to unsaved changes when a value is set because the underlying (real) model isn't changed, just
    // the copy of it (see setFrom and the calling constructors in SimpleTuppleModel).
    setFrom( copyTupples );
  }

  /**
   * The constructor. The model will contain a copy of the data from the given model.
   * 
   * @param copyTupples
   *          A model. Its data will be copied.
   * @param dra
   *          The date range is used to limit the values that are returned by the given model.
   */
  public SimpleTupleModel( final ITupleModel copyTupples, final DateRange dra ) throws SensorException
  {
    super( copyTupples.getAxisList() );

    // TODO this leads to unsaved changes when a value is set because the underlying (real) model isn't changed, just
    // the copy of it (see setFrom and the calling constructors in SimpleTuppleModel).
    setFrom( copyTupples, dra );
  }

  /**
   * The constructor. The model will contain the given data.
   * 
   * @param axes
   *          An array of axes.
   * @param values
   *          The values.
   */
  public SimpleTupleModel( final IAxis[] axes, final Object[][] values )
  {
    super( axes );

    m_tuples = new ArrayList<Object[]>();
    for( int i = 0; i < values.length; i++ )
    {
      m_tuples.add( values[i] );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getCount()
   */
  @Override
  public int getCount( )
  {
    return m_tuples.size();
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getElement(int, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public Object getElement( final int index, final IAxis axis ) throws SensorException
  {
    final Object[] row = m_tuples.get( index );
    final int columnIndex = getPositionFor( axis );

    return row[columnIndex];
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#setElement(int, java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public void setElement( final int index, final Object element, final IAxis axis ) throws SensorException
  {
    // TODO For debug purposes! Once problem with "null" is solved remove?
    if( element == null )
      Logger.getLogger( SimpleTupleModel.class.getName() ).warning( Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleTuppleModel.0" ) + index + Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleTuppleModel.1" ) + axis ); //$NON-NLS-1$ //$NON-NLS-2$

    final Object[] row = m_tuples.get( index );
    final int columnIndex = getPositionFor( axis );
    row[columnIndex] = element;
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#indexOf(java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    if( element == null )
      return -1;

    final int columnIndex = getPositionFor( axis );
    for( int i = 0; i < m_tuples.size(); i++ )
    {
      final Object[] row = m_tuples.get( i );
      final Object columnElement = row[columnIndex];
      if( element.equals( columnElement ) )
        return i;
    }

    return -1;
  }

  /**
   * This function adds a tupple at the end of the model.
   * 
   * @param tupple
   *          The 'row' to be added.
   */
  public void addTuple( final Object[] tupple )
  {
    m_tuples.add( tupple );
  }

  /**
   * This function adds a tuple at the end of the model
   * 
   * @param tupple
   *          The 'row' to be added.
   */
  public void addTupple( final Vector<Object> tupple )
  {
    addTuple( tupple.toArray( new Object[] {} ) );
  }

  /**
   * This function sets the data from the given model.
   * 
   * @param copyTupples
   *          The model, which data will be copied.
   */
  private void setFrom( final ITupleModel copyTupples ) throws SensorException
  {
    final IAxis[] axes = getAxisList();

    m_tuples = new ArrayList<Object[]>();

    clearAxesPositions();
    for( int ia = 0; ia < axes.length; ia++ )
    {
      mapAxisToPos( axes[ia], ia );
    }

    for( int ix = 0; ix < copyTupples.getCount(); ix++ )
    {
      final Object[] row = new Object[axes.length];
      for( int ia = 0; ia < axes.length; ia++ )
      {
        row[ia] = copyTupples.getElement( ix, axes[ia] );
      }

      m_tuples.add( row );
    }
  }

  /**
   * This function sets the data from the given model.
   * 
   * @param copyTupples
   *          The model, which data will be copied.
   * @param dra
   *          The date range is used to limit the values that are returned by the given model.
   */
  private void setFrom( final ITupleModel copyTupples, final DateRange dra ) throws SensorException
  {
    final IAxis[] axes = getAxisList();

    final IAxis dateAxis = ObservationUtilities.findAxisByClassNoEx( axes, Date.class );
    if( dra == null || dateAxis == null )
    {
      setFrom( copyTupples );
      return;
    }

    m_tuples = new ArrayList<Object[]>();

    clearAxesPositions();
    for( int ia = 0; ia < axes.length; ia++ )
    {
      mapAxisToPos( axes[ia], ia );
    }

    for( int ix = 0; ix < copyTupples.getCount(); ix++ )
    {
      final Date d = (Date) copyTupples.getElement( ix, dateAxis );
      if( d.compareTo( dra.getFrom() ) >= 0 && d.compareTo( dra.getTo() ) <= 0 )
      {
        final Object[] row = new Object[axes.length];
        for( int ia = 0; ia < axes.length; ia++ )
        {
          row[ia] = copyTupples.getElement( ix, axes[ia] );
        }

        m_tuples.add( row );
      }
    }
  }
}