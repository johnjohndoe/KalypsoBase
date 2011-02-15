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

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.IObservationListener;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.event.ObservationEventAdapter;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * Default implementation of the <code>IObservation</code> interface.
 * 
 * @author schlienger
 */
public class SimpleObservation implements IObservation
{
  private String m_name;

  private final MetadataList m_metadata;

  private ITupleModel m_model = null;

  private final ObservationEventAdapter m_evtPrv = new ObservationEventAdapter( this );

  private String m_href;

  public SimpleObservation( )
  {
    this( "", "", new MetadataList(), new IAxis[0] ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public SimpleObservation( final IAxis[] axes )
  {
    this( "", "", new MetadataList(), axes ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public SimpleObservation( final String href, final String name, final MetadataList metadata, final IAxis[] axes )
  {
    this( href, name, metadata, new SimpleTupleModel( axes ) );
  }

  public SimpleObservation( final String href, final String name, final MetadataList metadata, final ITupleModel model )
  {
    m_href = href;
    m_name = name;
    m_metadata = metadata;
    m_model = model;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getName()
   */
  @Override
  public String getName( )
  {
    return m_name;
  }

  public void setName( final String name )
  {
    m_name = name;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getMetadataList()
   */
  @Override
  public MetadataList getMetadataList( )
  {
    return m_metadata;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getAxisList()
   */
  @Override
  public IAxis[] getAxisList( )
  {
    return m_model.getAxes();
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getValues(org.kalypso.ogc.sensor.request.IRequest)
   */
  @Override
  public ITupleModel getValues( final IRequest request ) throws SensorException
  {
    if( m_model == null )
      throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleObservation.6" ) ); //$NON-NLS-1$

    // TODO this leads to unsaved changes when a value is set because the underlying
    // (real) model isn't changed, just the copy of it (see setFrom and the calling
    // constructors in SimpleTuppleModel).
    if( (request != null) && (request.getDateRange() != null) )
      return new SimpleTupleModel( m_model, request.getDateRange() );

    return m_model;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#setValues(org.kalypso.ogc.sensor.ITuppleModel)
   */
  @Override
  public void setValues( final ITupleModel values ) throws SensorException
  {
    if( values == null )
    {
      m_model = new SimpleTupleModel( m_model.getAxes() );
      return;
    }

    if( m_model == null )
    {
      m_model = values;
      return;
    }

    final IAxis[] otherAxes = values.getAxes();
    final Map<IAxis, IAxis> map = new HashMap<IAxis, IAxis>( getAxisList().length );

    for( final IAxis axis : getAxisList() )
    {
      try
      {
        final IAxis other = ObservationUtilities.findAxisByName( otherAxes, axis.getName() );
        map.put( axis, other );
      }
      catch( final NoSuchElementException e )
      {
        throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleObservation.7" ) + toString(), e ); //$NON-NLS-1$
      }
    }

    final IAxis[] keys = ObservationUtilities.findAxesByKey( getAxisList() );

    for( int i = 0; i < values.size(); i++ )
    {
      // check presence of values if associated axes are keys
      int ixPresent = -1;

      for( final IAxis key : keys )
      {
        final Object obj = values.get( i, map.get( key ) );
        final int ix = m_model.indexOf( obj, key );

        if( (ix >= 0) && (ixPresent != -1) )
        {
          if( ixPresent != ix )
            break;
        }
        else
          ixPresent = ix;
      }

      // replace if values of keys already exist
      if( ixPresent != -1 )
      {
        final Set<IAxis> kset = map.keySet();

        for( final IAxis myA : kset )
        {
          final IAxis oA = map.get( myA );

          final Object obj = values.get( i, oA );
          m_model.set( ixPresent, myA, obj );
        }
      }
      else
      {
        final Set<IAxis> kset = map.keySet();

        final Object[] tupple = new Object[kset.size()];

        final SimpleTupleModel stm = prepareForAdding();

        for( final IAxis myA : kset )
        {
          final Object obj = values.get( i, map.get( myA ) );
          tupple[stm.getPosition( myA )] = obj;
        }

        stm.addTuple( tupple );
      }
    }

    m_evtPrv.fireChangedEvent( null );
  }

  /**
   * Helper: since we are adding tupples to our model, we need a way to be sure that this is possible. For now, we
   * simply copy the existing values in a SimpleTuppleModel which finally allows to add tupples as desired.
   * 
   * @return a SimpleTuppleModel
   * @throws SensorException
   */
  private SimpleTupleModel prepareForAdding( ) throws SensorException
  {
    // since we are adding
    if( !(m_model instanceof SimpleTupleModel) )
      m_model = new SimpleTupleModel( m_model );

    return (SimpleTupleModel) m_model;
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#addListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void addListener( final IObservationListener listener )
  {
    m_evtPrv.addListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#removeListener(org.kalypso.ogc.sensor.IObservationListener)
   */
  @Override
  public void removeListener( final IObservationListener listener )
  {
    m_evtPrv.removeListener( listener );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservationEventProvider#fireChangedEvent(java.lang.Object)
   */
  @Override
  public void fireChangedEvent( final Object source )
  {
    m_evtPrv.fireChangedEvent( source );
  }

  /**
   * @see org.kalypso.ogc.sensor.IObservation#getHref()
   */
  @Override
  public String getHref( )
  {
    return m_href;
  }

  /**
   * Sets the href
   * 
   * @param href
   *          localisation of the observation when it comes from a zml file for instance.
   */
  public void setHref( final String href )
  {
    m_href = href;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleObservation.8" ) + m_name + Messages.getString( "org.kalypso.ogc.sensor.impl.SimpleObservation.10" ) + m_href; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

}