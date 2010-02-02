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
import org.kalypso.ogc.sensor.ITuppleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;

/**
 * Simple implementation of the {@link ITuppleModel} interface.
 * 
 * @author Marc Schlienger
 */
public class SimpleTuppleModel extends AbstractTuppleModel
{
  /**
   * An empty tupple model.
   */
  public static final ITuppleModel EMPTY_TUPPLEMODEL = new SimpleTuppleModel( new IAxis[0] );

  /**
   * The list of tupples.
   */
  private List<Object[]> m_tupples;

  /**
   * The constructor. The model will contain no data.
   * 
   * @param axes
   *          A list of axes.
   */
  public SimpleTuppleModel( final List<IAxis> axes )
  {
    this( axes.toArray( new IAxis[axes.size()] ) );
  }

  /**
   * The constructor. The model will contain no data.
   * 
   * @param axes
   *          An array of axes.
   */
  public SimpleTuppleModel( final IAxis[] axes )
  {
    this( axes, new Object[0][axes.length] );
  }

  /**
   * The constructor. The model will contain a copy of the data from the given model.
   * 
   * @param copyTupples
   *          A model. Its data will be copied.
   */
  public SimpleTuppleModel( final ITuppleModel copyTupples ) throws SensorException
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
  public SimpleTuppleModel( final ITuppleModel copyTupples, final DateRange dra ) throws SensorException
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
  public SimpleTuppleModel( final IAxis[] axes, final Object[][] values )
  {
    super( axes );

    m_tupples = new ArrayList<Object[]>();
    for( int i = 0; i < values.length; i++ )
      m_tupples.add( values[i] );
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getCount()
   */
  public int getCount( )
  {
    return m_tupples.size();
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getElement(int, org.kalypso.ogc.sensor.IAxis)
   */
  public Object getElement( final int index, final IAxis axis ) throws SensorException
  {
    Object[] row = m_tupples.get( index );
    int columnIndex = getPositionFor( axis );
    return row[columnIndex];
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#setElement(int, java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  public void setElement( final int index, final Object element, final IAxis axis ) throws SensorException
  {
    // TODO For debug purposes! Once problem with "null" is solved remove?
    if( element == null )
      Logger.getLogger( SimpleTuppleModel.class.getName() ).warning( Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleTuppleModel.0" ) + index + Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleTuppleModel.1" ) + axis ); //$NON-NLS-1$ //$NON-NLS-2$

    Object[] row = m_tupples.get( index );
    int columnIndex = getPositionFor( axis );
    row[columnIndex] = element;
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#indexOf(java.lang.Object, org.kalypso.ogc.sensor.IAxis)
   */
  public int indexOf( final Object element, final IAxis axis ) throws SensorException
  {
    if( element == null )
      return -1;

    int columnIndex = getPositionFor( axis );
    for( int i = 0; i < m_tupples.size(); i++ )
    {
      Object[] row = m_tupples.get( i );
      Object columnElement = row[columnIndex];
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
  public void addTupple( final Object[] tupple )
  {
    m_tupples.add( tupple );
  }

  /**
   * This function adds a tupple at the end of the model
   * 
   * @param tupple
   *          The 'row' to be added.
   */
  public void addTupple( final Vector<Object> tupple )
  {
    addTupple( tupple.toArray( new Object[] {} ) );
  }

  /**
   * This function sets the data from the given model.
   * 
   * @param copyTupples
   *          The model, which data will be copied.
   */
  private void setFrom( final ITuppleModel copyTupples ) throws SensorException
  {
    IAxis[] axes = getAxisList();

    m_tupples = new ArrayList<Object[]>();

    clearAxesPositions();
    for( int ia = 0; ia < axes.length; ia++ )
      mapAxisToPos( axes[ia], ia );

    for( int ix = 0; ix < copyTupples.getCount(); ix++ )
    {
      Object[] row = new Object[axes.length];
      for( int ia = 0; ia < axes.length; ia++ )
        row[ia] = copyTupples.getElement( ix, axes[ia] );

      m_tupples.add( row );
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
  private void setFrom( final ITuppleModel copyTupples, final DateRange dra ) throws SensorException
  {
    IAxis[] axes = getAxisList();

    final IAxis dateAxis = ObservationUtilities.findAxisByClassNoEx( axes, Date.class );
    if( dra == null || dateAxis == null )
    {
      setFrom( copyTupples );
      return;
    }

    m_tupples = new ArrayList<Object[]>();

    clearAxesPositions();
    for( int ia = 0; ia < axes.length; ia++ )
      mapAxisToPos( axes[ia], ia );

    for( int ix = 0; ix < copyTupples.getCount(); ix++ )
    {
      final Date d = (Date) copyTupples.getElement( ix, dateAxis );
      if( d.compareTo( dra.getFrom() ) >= 0 && d.compareTo( dra.getTo() ) <= 0 )
      {
        Object[] row = new Object[axes.length];
        for( int ia = 0; ia < axes.length; ia++ )
          row[ia] = copyTupples.getElement( ix, axes[ia] );

        m_tupples.add( row );
      }
    }
  }
}