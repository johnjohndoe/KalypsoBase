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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ogc.sensor.view.ObservationViewerDialog;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree_impl.gml.schema.SpecialPropertyMapper;

/**
 * @author Gernot Belger
 */
public class ClipboardExportAction extends AbstractObservationAction
{
  private final Clipboard m_clipboard;

  public ClipboardExportAction( final ObservationViewerDialog dialog, final Clipboard clipboard )
  {
    super( dialog );
    m_clipboard = clipboard;
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#getLabel()
   */
  @Override
  protected String getLabel( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.10" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#getTooltip()
   */
  @Override
  protected String getTooltip( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.11" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#run()
   */
  @Override
  protected IStatus run( )
  {
    final Object input = getDialog().getInput();
    if( !(input instanceof IObservation) )
      return Status.OK_STATUS;

    try
    {
      final String content = createClipboardStringFrom( (IObservation) input, null );
      m_clipboard.setContents( new Object[] { content }, new Transfer[] { TextTransfer.getInstance() } );
    }
    catch( final SensorException e )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Failed to export table to clipboard", e );
    }

    return Status.OK_STATUS;
  }

  private static String createClipboardStringFrom( final IObservation observation, final IRequest request ) throws SensorException
  {
    // FIXME: we should not export the observation but the actual table, like it is currently visible to the user

    // vice versa, we should import in the same way; everything else is confusing and cannot be handled by the user.

    final StringBuffer result = new StringBuffer();
    final ITupleModel values = observation.getValues( request );
    final IAxis[] axes = values.getAxes();
    final int count = values.size();
    // actually just the first key axis is relevant in our case
    final IAxis[] keyAxes = ObservationUtilities.findAxesByKey( axes );
    final List<IAxis> list = new ArrayList<IAxis>();
    list.add( keyAxes[0] );
    for( final IAxis axe : axes )
    {
      if( axe != keyAxes[0] )
        list.add( axe );
    }

    final IAxis[] sortedAxes = list.toArray( new IAxis[list.size()] );
    for( int row = 0; row < count; row++ )
    {
      for( int col = 0; col < sortedAxes.length; col++ )
      {
        final Object value = values.get( row, sortedAxes[col] );
        String stringValue;
        try
        {
          // FIXME: evil: everything should be exported as it appears in the table!
          if( value instanceof Number )
            stringValue = TimeseriesUtils.getNumberFormatFor( sortedAxes[col].getType() ).format( value );
          else
            stringValue = (String) SpecialPropertyMapper.cast( value, String.class, true );
          result.append( stringValue != null ? stringValue : " " ); //$NON-NLS-1$
        }
        catch( final Exception e )
        {
          result.append( Messages.getString( "org.kalypso.ogc.sensor.zml.ZmlFactory.34" ) ); //$NON-NLS-1$
          // ignore
        }
        if( col + 1 == sortedAxes.length )
          result.append( "\r\n" ); //$NON-NLS-1$
        else
          result.append( "\t" ); //$NON-NLS-1$
      }
    }
    return result.toString();
  }
}
