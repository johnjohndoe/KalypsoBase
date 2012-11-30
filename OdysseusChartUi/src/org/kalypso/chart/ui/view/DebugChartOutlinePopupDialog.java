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
package org.kalypso.chart.ui.view;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.viewers.ArrayTreeContentProvider;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartHandler;
import de.openali.odysseus.chart.framework.view.IChartHandlerManager;

/**
 * @author Dirk Kuch
 */
public class DebugChartOutlinePopupDialog extends ChartOutlinePopupDialog
{

  public DebugChartOutlinePopupDialog( final Shell parentShell, final IChartComposite chartComposite )
  {
    super( parentShell, chartComposite );
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    final Composite body = (Composite) super.createDialogArea( parent );

    addChartInfo( body );

    return body;
  }

  private void addChartInfo( final Composite body )
  {
    new Label( body, SWT.NULL ).setText( "Debug" ); //$NON-NLS-1$

    final TreeViewer viewer = new TreeViewer( body );
    viewer.getTree().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
    viewer.setContentProvider( new ArrayTreeContentProvider() );
    viewer.setLabelProvider( new DebugChartLabelProvider() );

    viewer.setInput( toDebugInput( getChartComposite() ) );

  }

  private Object[] toDebugInput( final IChartComposite composite )
  {
    final Set<Object> input = new LinkedHashSet<>();

    input.add( "Chart Handlers" ); //$NON-NLS-1$
    final IChartHandlerManager handler = composite.getPlotHandler();
    final IChartHandler[] handlers = handler.getActiveHandlers();
    if( ArrayUtils.isNotEmpty( handlers ) )
      Collections.addAll( input, handlers );
    else
      input.add( " - none" ); //$NON-NLS-1$

    return input.toArray();
  }
}