/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.CompositeCommand;
import org.kalypso.ogc.gml.command.MoveThemeUpCommand;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.ChangeSelectionRunnable;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * @author Gernot Belger
 */
public class MoveThemeUpHandler extends AbstractHandler
{
  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final Display display = shell == null ? null : shell.getDisplay();
    final ISelection selection = (ISelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );
    final GisMapOutlinePage mapOutline = MapHandlerUtils.getMapOutline( context );

    /* Order the selection as the themes are ordered in the outline. */
    final IKalypsoTheme[] selectedThemesInOrder = MapHandlerUtils.getSelectedThemesInOrder( selection );
    if( selectedThemesInOrder.length == 0 )
      return null;

    /* Do nothing if we have selected the first theme */
    if( selectedThemesInOrder[0].getMapModell().getAllThemes()[0] == selectedThemesInOrder[0] )
      return null;

    final CompositeCommand compositeCommand = new CompositeCommand( Messages.getString( "org.kalypso.ogc.gml.outline.handler.MoveThemeUpHandler.0" ) ); //$NON-NLS-1$
    for( final IKalypsoTheme kalypsoTheme : selectedThemesInOrder )
    {
      final IMapModell themeMapModell = kalypsoTheme.getMapModell();
      compositeCommand.addCommand( new MoveThemeUpCommand( themeMapModell, kalypsoTheme ) );
    }

    /* (Re-)select moved themes */
    final List<IThemeNode> selectedNodesInOrder = new ArrayList<>();
    for( final IKalypsoTheme theme : selectedThemesInOrder )
    {
      final IThemeNode node = mapOutline.findNode( theme );
      if( node != null )
        selectedNodesInOrder.add( node );
    }

    final StructuredSelection newSelection = new StructuredSelection( selectedNodesInOrder );
    MapHandlerUtils.postCommandChecked( context, compositeCommand, new ChangeSelectionRunnable( mapOutline, newSelection, display ) );

    return null;
  }

}
