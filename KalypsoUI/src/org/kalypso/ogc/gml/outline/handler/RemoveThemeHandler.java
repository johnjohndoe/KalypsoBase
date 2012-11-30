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
package org.kalypso.ogc.gml.outline.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.CompositeCommand;
import org.kalypso.ogc.gml.command.RemoveThemeCommand;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class RemoveThemeHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();
    final ISelection selection = (ISelection)context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
    final IKalypsoTheme[] selectedThemes = MapHandlerUtils.getSelectedThemes( selection );

    /* Ask user */
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final String commandName = HandlerUtils.getCommandName( event );

    final String message = String.format( Messages.getString( "RemoveThemeHandler.0" ) ); //$NON-NLS-1$
    if( !MessageDialog.openConfirm( shell, commandName, message ) )
      return null;

    /* Remove the themes */
    final CompositeCommand compositeCommand = new CompositeCommand( Messages.getString( "org.kalypso.ogc.gml.outline.handler.RemoveThemeHandler.0" ) ); //$NON-NLS-1$
    for( final IKalypsoTheme theme : selectedThemes )
      compositeCommand.addCommand( new RemoveThemeCommand( theme.getMapModell(), theme ) );

    MapHandlerUtils.postCommandChecked( context, compositeCommand, null );

    return null;
  }

}
