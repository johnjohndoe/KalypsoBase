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
package org.kalypso.zml.ui.chart.layer.commands;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.chart.ui.editor.commandhandler.utils.CommandHandlerUtils;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.ui.chart.layer.visitor.UpdateFilterVisitor;

import de.openali.odysseus.chart.framework.OdysseusChartExtensions;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class SetZmlFilterCommandHandler extends AbstractHandler implements IElementUpdater
{
  private static final String DISABLE_COMMAND = "disableCommand"; //$NON-NLS-1$

  private static final String ENABLE_COMMAND = "enableCommand"; //$NON-NLS-1$

  public static final String ID = "org.kalypso.chart.ui.commands.change.visibility"; //$NON-NLS-1$

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( chart == null )
      return Status.CANCEL_STATUS;

    final IChartModel model = chart.getChartModel();
    final ILayerManager layerManager = model.getLayerManager();
    final boolean enabled = CommandHandlerUtils.isEnabled( event );

    final IChartLayerFilter[] add = new IChartLayerFilter[] { OdysseusChartExtensions.createFilter( getFilter( enabled, event ) ) };
    final IChartLayerFilter[] remove = new IChartLayerFilter[] { OdysseusChartExtensions.createFilter( getFilter( !enabled, event ) ) };
    layerManager.accept( new UpdateFilterVisitor( add, remove ) );

    return Status.OK_STATUS;
  }

  private String getFilter( final boolean enabled, final ExecutionEvent event )
  {
    if( enabled )
      return event.getParameter( ENABLE_COMMAND );

    return event.getParameter( DISABLE_COMMAND );
  }

  @Override
  public void updateElement( final UIElement element, final Map parameters )
  {
    final IChartModel model = ChartHandlerUtilities.getModel( element );
    if( Objects.isNull( model ) )
      element.setChecked( false );
    else
    {

      final String filterEnabled = (String) parameters.get( ENABLE_COMMAND );
      final String filterDisabled = (String) parameters.get( DISABLE_COMMAND );

      final ILayerManager layerManager = model.getLayerManager();

      final ActiveFilterVisitor visitor = new ActiveFilterVisitor( filterEnabled, filterDisabled );
      layerManager.accept( visitor );

      element.setChecked( visitor.isEnabled() );
    }
  }
}
