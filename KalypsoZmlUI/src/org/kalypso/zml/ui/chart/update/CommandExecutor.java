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
package org.kalypso.zml.ui.chart.update;

import java.util.Map;

import jregex.Pattern;
import jregex.RETokenizer;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IEvaluationService;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.net.QueryUtilities;
import org.kalypso.zml.ui.KalypsoZmlUI;

/**
 * @author Dirk Kuch
 */
public class CommandExecutor implements Runnable
{
  private final ICommandExecutorTrigger m_trigger;

  private final boolean m_firstRun;

  public CommandExecutor( final ICommandExecutorTrigger trigger, final boolean firstRun )
  {
    m_trigger = trigger;
    m_firstRun = firstRun;
  }

  /**
   * @see java.lang.Runnable#run()
   */
  @Override
  public void run( )
  {
    final ICommandService commandService = m_trigger.getCommandService();
    final IEvaluationService evaluationService = m_trigger.getEvaluatonService();

    final IEvaluationContext context = evaluationService.getCurrentState();

    for( final String commandUri : m_trigger.getCommands() )
    {
      try
      {
        final String id = getCommandId( commandUri );

        if( m_firstRun )
        {
          final Map<String, String> parameters = QueryUtilities.parse( commandUri );

          final Command command = commandService.getCommand( id );
          final IHandler handler = command.getHandler();

          final ExecutionEvent event = new ExecutionEvent( command, parameters, this, context );
          handler.execute( event );
        }

        /** don't set parameters, otherwise the updateElement function of the command handler will not called */
        commandService.refreshElements( id, null );
      }
      catch( final ExecutionException e )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
    }
  }

  private String getCommandId( final String commandUri )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( "\\?.*" ), commandUri ); //$NON-NLS-1$

    return tokenizer.nextToken();
  }

}
