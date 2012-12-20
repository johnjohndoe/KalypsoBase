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
import org.kalypso.ogc.gml.typehandler.ZmlInlineTypeHandler;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.impl.SimpleObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ClipboardImportAction extends AbstractObservationAction
{
  private final ZmlInlineTypeHandler m_typeHandler;

  public ClipboardImportAction( final ZmlInlineTypeHandler typeHandler )
  {
    m_typeHandler = typeHandler;
  }

  @Override
  protected String getLabel( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.8" ); //$NON-NLS-1$ 
  }

  @Override
  protected String getTooltip( )
  {
    return Messages.getString( "org.kalypso.ogc.sensor.view.ObservationViewerDialog.9" ); //$NON-NLS-1$
  }

  @Override
  protected IStatus execute( )
  {
    final ObservationViewer viewer = getViewer();
    final Clipboard clipboard = viewer.getClipboard();
    final Object content = clipboard.getContents( TextTransfer.getInstance() );
    if( content == null || !(content instanceof String) )
      return new Status( IStatus.WARNING, KalypsoGisPlugin.getId(), Messages.getString( "ClipboardImportAction.0" ) ); //$NON-NLS-1$

    try
    {
      final Object inputObs = viewer.getInput();
      final String name = inputObs instanceof IObservation ? ((IObservation)inputObs).getName() : ""; //$NON-NLS-1$

      final IAxis[] axis = m_typeHandler.createAxes();

      final Clipboard2Zml clipboard2Zml = new Clipboard2Zml( axis );

      final ITupleModel model = clipboard2Zml.convert( (String)content );

      final IObservation obs = new SimpleObservation( null, name, new MetadataList(), model );

      viewer.setInput( obs, viewer.getShow() );

      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
  }
}