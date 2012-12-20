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
package org.kalypso.core.status;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TreeAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

/**
 * @author Gernot Belger
 */
public class StatusTreeViewer extends StatusViewer
{
  private final TreeViewer m_treeViewer;

  public StatusTreeViewer( final Composite parent, final int style )
  {
    m_treeViewer = new TreeViewer( parent, style | SWT.V_SCROLL | SWT.H_SCROLL );

    final Tree tree = m_treeViewer.getTree();
    tree.setHeaderVisible( true );
    tree.setLinesVisible( false );

    addSeverityColumn( m_treeViewer );
    addMessageColumn( m_treeViewer );

    m_treeViewer.setContentProvider( new StatusTreeContentProvider() );

    hookListener();
  }

  @Override
  protected void hookListener( )
  {
    super.hookListener();

    final Tree tree = m_treeViewer.getTree();
    tree.addTreeListener( new TreeAdapter()
    {
      @Override
      public void treeCollapsed( final org.eclipse.swt.events.TreeEvent e )
      {
        updateColumnSizes();
      }

      @Override
      public void treeExpanded( final org.eclipse.swt.events.TreeEvent e )
      {
        updateColumnSizes();
      }
    } );
  }

  @Override
  public TreeViewer getViewer( )
  {
    return m_treeViewer;
  }
}