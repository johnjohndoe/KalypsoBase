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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.timeseries.TimeserieUtils;
import org.kalypso.ogc.sensor.view.ObservationViewerDialog;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author Gernot Belger
 */
public class ClipboardImportAction extends AbstractObservationAction
{
  private final Clipboard m_clipboard;

  public ClipboardImportAction( final ObservationViewerDialog dialog, final Clipboard clipboard )
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
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.8" ); //$NON-NLS-1$ 
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#getTooltip()
   */
  @Override
  protected String getTooltip( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.9" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.sensor.view.observationDialog.AbstractObservationAction#run()
   */
  @Override
  protected IStatus run( )
  {
    final Object content = m_clipboard.getContents( TextTransfer.getInstance() );
    if( content == null || !(content instanceof String) )
    {
      // TODO messagebox
      return new Status( IStatus.WARNING, KalypsoGisPlugin.getId(), "Clipboard content is not of type text." );
    }

    try
    {
      final Object inputObs = getDialog().getInput();
      final String name = inputObs instanceof IObservation ? ((IObservation) inputObs).getName() : ""; //$NON-NLS-1$

      final String[] axisTypes = getDialog().getAxisTypes();
      final IAxis[] axis = TimeserieUtils.createDefaultAxes( axisTypes, true );

      final Clipboard2Zml clipboard2Zml = new Clipboard2Zml( axis );

      final ITupleModel model = clipboard2Zml.convert( (String) content );

      final IObservation obs = new SimpleObservation( null, name, new MetadataList(), model );

      getDialog().setInput( obs );

      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
  }
}
