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
package org.kalypso.zml.ui.chart.layer.visitor;

import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.menus.CommandContributionItem;
import org.kalypso.chart.ui.editor.commandhandler.visibility.ChangeVisibilityCommandHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;

/**
 * a layer provider can define different "content.xxx" values. this values are used to en-/disable visibility of an
 * IChartLayer by the ChangeVisibilityCommand.<br />
 * <br />
 * visibility must be updated with each selection update of a chart diagram.
 * 
 * @author Dirk Kuch
 */
public class SetContentLayerVisibilityVisitor implements IChartLayerVisitor
{

  private final IToolBarManager m_manager;

  public SetContentLayerVisibilityVisitor( final IToolBarManager manager )
  {
    m_manager = manager;
  }

  /**
   * @see org.kalypso.zml.core.diagram.base.AbstractExternalChartModelVisitor#accept(de.openali.odysseus.chart.framework.model.layer.IChartLayer)
   */
  @Override
  public void visit( final IChartLayer layer )
  {
    if( !layer.isVisible() )
      return;

    if( layer instanceof IZmlLayer )
    {
      layer.setVisible( isVisible( layer ) );
    }

    layer.getLayerManager().accept( this );
  }

  private boolean isVisible( final IChartLayer layer )
  {
    final ILayerProvider provider = layer.getProvider();
    if( provider == null )
      return true;

    final IParameterContainer container = provider.getParameterContainer();
    if( container == null )
      return true;

    /**
     * a layer can define different content keys. check if content is enabled (-> check tool bar button state)
     */
    final String[] contentKeys = container.findAllKeys( "content." );
    for( final String contentKey : contentKeys )
    {
      final CommandContributionItem item = findItem( contentKey );
      if( item == null )
        return true;

      final ParameterizedCommand command = item.getCommand();
      final Command cmd = command.getCommand();
      final IHandler handler = cmd.getHandler();

      // FIXME ask gernot

      return true;
    }

    return true;
  }

  private CommandContributionItem findItem( final String contentKey )
  {
    final IContributionItem[] items = m_manager.getItems();
    for( final IContributionItem item : items )
    {
      if( !(item instanceof CommandContributionItem) )
        continue;

      if( !ChangeVisibilityCommandHandler.ID.equals( item.getId() ) )
        continue;

      final CommandContributionItem cmdItem = (CommandContributionItem) item;

      final ParameterizedCommand command = cmdItem.getCommand();
      final Map parameters = command.getParameterMap();
      final String parameter = (String) parameters.get( ChangeVisibilityCommandHandler.LAYER_PARAMETER );

      if( contentKey.equals( parameter ) )
        return cmdItem;
    }

    return null;
  }
}
