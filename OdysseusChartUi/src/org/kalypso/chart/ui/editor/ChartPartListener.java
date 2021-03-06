/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.chart.ui.editor;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.chart.ui.editor.commandhandler.ChartSourceProvider;
import org.kalypso.contribs.eclipse.ui.partlistener.PartAdapter2;

import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * Part listener for all chart view parts. Handles activation/decativation o fthe chart source.
 *
 * @author burtscher1
 */
public class ChartPartListener extends PartAdapter2
{
  private final IWorkbenchPart m_chartPart;

  private ChartSourceProvider m_sourceProvider = null;

  private final IServiceLocator m_locator;

  public ChartPartListener( final IWorkbenchPart chartPart, final IServiceLocator locator )
  {
    Assert.isNotNull( locator );
    Assert.isNotNull( chartPart );

    m_chartPart = chartPart;
    m_locator = locator;
  }

  public void dispose( )
  {
    destroySource();
  }

  @Override
  public void partActivated( final IWorkbenchPartReference partRef )
  {
    final IWorkbenchPart part = partRef.getPart( false );
    if( part == m_chartPart && m_sourceProvider != null )
      m_sourceProvider.fireSourceChanged();
  }

  private void activateSource( final IChartComposite chart ) throws Throwable
  {
    destroySource();

    if( chart != null )
    {
      m_sourceProvider = new ChartSourceProvider( m_locator, chart );
      // Execute event now????
      activateDefaultHandler( chart );
    }
  }

  private void activateDefaultHandler( final IChartComposite chartComposite ) throws Throwable
  {
    if( chartComposite.getPlotHandler().getActiveHandlers().length > 0 )
      return;
    final IHandlerService hs = (IHandlerService) ((WorkbenchPart) m_chartPart).getSite().getService( IHandlerService.class );
    final Event event = new Event();
    hs.executeCommand( "org.kalypso.chart.ui.commands.zoom_pan_maximize", event ); //$NON-NLS-1$
  }

  private void destroySource( )
  {
    if( m_sourceProvider != null )
    {
      m_sourceProvider.dispose();
      m_sourceProvider = null;
    }
  }

  public void setChart( final IChartComposite chartComposite )
  {
    try
    {
      if( m_sourceProvider == null )
        activateSource( chartComposite );
      else
        m_sourceProvider.setChart( chartComposite );
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
    }
  }
}