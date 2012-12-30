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
package org.kalypso.contribs.eclipse.jface.viewers.tree;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.contribs.eclipse.jface.viewers.table.ColumnsResizeControlListener;

/**
 * Reuseable action that expands all tree items of a {@link TreeViewer}.
 * 
 * @author Gernot Belger
 */
public class ExpandAllTreeItemsAction extends Action
{
  private final ITreeViewerProvider m_provider;

  public ExpandAllTreeItemsAction( final TreeViewer viewer )
  {
    this( new DefaultTreeViewerProvider( viewer ) );
  }

  public ExpandAllTreeItemsAction( final ITreeViewerProvider provider )
  {
    m_provider = provider;

    // TRICKY: get messages and image from eclipse code, so it is sure is translated etc.
    try
    {
      final IServiceLocator locator = PlatformUI.getWorkbench();
      final ICommandService cs = (ICommandService)locator.getService( ICommandService.class );
      final Command cmd = cs.getCommand( IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL );
      setText( cmd.getName() );
      setToolTipText( cmd.getDescription() );

      final ICommandImageService cis = (ICommandImageService)locator.getService( ICommandImageService.class );
      final ImageDescriptor collapseAllImage = cis.getImageDescriptor( IWorkbenchCommandConstants.NAVIGATE_EXPAND_ALL, ICommandImageService.TYPE_DEFAULT, ICommandImageService.IMAGE_STYLE_TOOLBAR );
      setImageDescriptor( collapseAllImage );
    }
    catch( final NotDefinedException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final TreeViewer viewer = m_provider.getTreeViewer();
    if( viewer == null )
      return;

    viewer.expandAll();

    ColumnsResizeControlListener.refreshColumnsWidth( viewer.getTree() );
  }
}