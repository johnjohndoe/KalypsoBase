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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.TupleModelDataSet;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.transaction.ITupleModelCommand;
import org.kalypso.ogc.sensor.transaction.ITupleModelTransaction;
import org.kalypso.ogc.sensor.visitor.ITupleModelValueContainer;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;

/**
 * Provides common functionnality:
 * <ul>
 * <li>getPositionFor( IAxis )</li>
 * <li>mapAxisToPos( IAxis, int )</li>
 * <li>getRangeFor( IAxis )</li>
 * </ul>
 *
 * @author schlienger
 */
public abstract class AbstractTupleModel implements ITupleModel
{
  /**
   * maps an axis to its position in this tuple model.
   */
  private final Map<IAxis, Integer> m_axes2pos = new LinkedHashMap<>();

  private final IAxis[] m_axes;

  Set<ITupleModelChangeListener> m_listeners = Collections.synchronizedSet( new LinkedHashSet<ITupleModelChangeListener>() );

  private ITupleModelTransaction m_transactionLock;

  private int m_event = 0;

  public AbstractTupleModel( final IAxis[] axes )
  {
    for( int ia = 0; ia < axes.length; ia++ )
    {
      mapAxisToPos( axes[ia], ia );
    }

    m_axes = axes;
  }

  @Override
  public void accept( final ITupleModelVisitor visitor, final int direction ) throws SensorException
  {
    if( direction >= 0 )
    {
      for( int index = 0; index < size(); index++ )
      {
        try
        {
          doVisit( visitor, index );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }
    else
    {
      for( int index = size() - 1; index >= 0; index-- )
      {
        try
        {
          doVisit( visitor, index );
        }
        catch( final CancelVisitorException e )
        {
          return;
        }
      }
    }

  }

  private void doVisit( final ITupleModelVisitor visitor, final int index ) throws SensorException, CancelVisitorException
  {
    visitor.visit( new ITupleModelValueContainer()
    {
      @Override
      public int getIndex( )
      {
        return index;
      }

      @Override
      public Object get( final IAxis axis ) throws SensorException
      {
        return AbstractTupleModel.this.get( index, axis );
      }

      @Override
      public Object getPrevious( final IAxis axis ) throws SensorException
      {
        if( index > 0 )
          return AbstractTupleModel.this.get( index - 1, axis );

        return null;
      }

      @Override
      public Object getNext( final IAxis axis ) throws SensorException
      {
        if( index + 1 < AbstractTupleModel.this.size() )
          return AbstractTupleModel.this.get( index + 1, axis );

        return null;
      }

      @Override
      public boolean hasAxis( final String... types )
      {
        for( final String type : types )
        {
          if( AxisUtils.findAxis( AbstractTupleModel.this.getAxes(), type ) == null )
            return false;
        }

        return true;
      }

      @Override
      public IAxis[] getAxes( )
      {
        return AbstractTupleModel.this.getAxes();
      }

      @Override
      public void set( final IAxis axis, final Object value ) throws SensorException
      {
        AbstractTupleModel.this.set( index, axis, value );
      }

      @Override
      public TupleModelDataSet getDataSetFor( final MetadataList metadata, final String valueAxis ) throws SensorException
      {
        return TupleModelDataSet.toDataSet( this, metadata, valueAxis );
      }

    } );

  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getAxisList()
   */
  @Override
  public IAxis[] getAxes( )
  {
    return m_axes;
  }

  /**
   * @see org.kalypso.ogc.sensor.ITuppleModel#getPositionFor(org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public int getPosition( final IAxis axis ) throws SensorException
  {
    if( !m_axes2pos.containsKey( axis ) )
      throw new SensorException( Messages.getString( "org.kalypso.ogc.sensor.impl.AbstractTuppleModel.0" ) + axis ); //$NON-NLS-1$

    return m_axes2pos.get( axis ).intValue();
  }

  /**
   * Maps an axis to its position in this model
   */
  protected void mapAxisToPos( final IAxis axis, final int pos )
  {
    m_axes2pos.put( axis, new Integer( pos ) );
  }

  /**
   * Clears the maps
   */
  protected void clearAxesPositions( )
  {
    m_axes2pos.clear();
  }

  /**
   * This needs refactoring... which might be undertaken once the whole IObservation and ITuppleModel stuff is
   * refactored.
   * <p>
   * The assuption here that the order is already ok is tricky. And the case where the axis denotes numbers is not so
   * nice, even not really performant.
   *
   * @see org.kalypso.ogc.sensor.ITuppleModel#getRangeFor(org.kalypso.ogc.sensor.IAxis)
   */
  @Override
  public IAxisRange getRange( final IAxis axis ) throws SensorException
  {
    if( size() == 0 )
      return null;

    // for numbers we need to step through all the
    // rows in order to find the range
    if( Number.class.isAssignableFrom( axis.getDataClass() ) )
    {
      Number lower = new Double( Double.MAX_VALUE );
      Number upper = new Double( -Double.MAX_VALUE );

      final DoubleComparator dc = new DoubleComparator( 0.000001 );
      for( int i = 0; i < size(); i++ )
      {
        final Number value = (Number) get( i, axis );
        if( value == null )
        {
          System.out.println( String.format( "AbstractTupleModel.getRange() - found invalid NULL value - index: %d", i ) ); //$NON-NLS-1$
          continue;
        }

        if( dc.compare( value, lower ) < 0 )
          lower = value;

        if( dc.compare( value, upper ) > 0 )
          upper = value;
      }

      return new DefaultAxisRange( lower, upper );
    }

    // else we assume that the order is already correct
    // and simply take the first and the last element
    final Object begin = get( 0, axis );
    final Object end = get( size() - 1, axis );

    return new DefaultAxisRange( begin, end );
  }

  @Override
  public boolean isEmpty( ) throws SensorException
  {
    return size() == 0;
  }

  @Override
  public void addChangeListener( final ITupleModelChangeListener listener )
  {
    m_listeners.add( listener );
  }

  protected void fireModelChanged( final int changeType )
  {
    if( changeType == 0 )
      return;

    if( Objects.isNotNull( m_transactionLock ) )
    {
      m_event |= changeType;
      return;
    }

    final ITupleModelChangeListener[] listeners = m_listeners.toArray( new ITupleModelChangeListener[] {} );
    for( final ITupleModelChangeListener listener : listeners )
    {
      listener.modelChangedEvent( new ObservationChangeType( changeType ) );
    }
  }

  @Override
  public synchronized IStatus execute( final ITupleModelTransaction transaction )
  {
    try
    {
      Assert.isTrue( Objects.equal( transaction.getModel(), this ) );

      start( transaction );

      final Set<IStatus> stati = new LinkedHashSet<>();
      for( final ITupleModelCommand command : transaction.getCommands() )
      {
        try
        {
          stati.add( command.execute( this, transaction.getMetadata() ) );
        }
        catch( final Throwable t )
        {
          stati.add( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), Messages.getString("AbstractTupleModel.0") ) ); //$NON-NLS-1$
        }
      }

      return StatusUtilities.createStatus( stati, Messages.getString("AbstractTupleModel.1") ); //$NON-NLS-1$
    }
    finally
    {
      stop( transaction );
    }
  }

  private void start( final ITupleModelTransaction transaction )
  {
    Assert.isTrue( Objects.isNull( m_transactionLock ) );

    m_transactionLock = transaction;
  }

  private void stop( final ITupleModelTransaction transaction )
  {
    Assert.isTrue( Objects.equal( m_transactionLock, transaction ) );
    final int event = m_event;

    m_transactionLock = null;
    m_event = 0;

    // because of synchronized block execute(transaction)
    new Job( Messages.getString("AbstractTupleModel.2") ) //$NON-NLS-1$
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        fireModelChanged( event );

        return Status.OK_STATUS;
      }
    }.schedule();

  }
}
