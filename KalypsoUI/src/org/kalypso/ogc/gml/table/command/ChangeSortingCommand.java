/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.table.command;

import org.eclipse.swt.widgets.TableColumn;
import org.kalypso.commons.command.ICommand;
import org.kalypso.ogc.gml.table.IColumnDescriptor;
import org.kalypso.ogc.gml.table.LayerTableSorter;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author gernot
 */
public class ChangeSortingCommand implements ICommand
{
  private final LayerTableViewer m_viewer;

  private final LayerTableSorter m_sorter;

  private final IColumnDescriptor m_oldSortColumn;

  private final IColumnDescriptor m_newSortColumn;

  private final boolean m_oldInverse;

  private final boolean m_newInverse;

  public ChangeSortingCommand( final LayerTableViewer viewer, final TableColumn tableColumn )
  {
    m_viewer = viewer;

    final IColumnDescriptor column = LayerTableViewer.getDescriptor( tableColumn );

    m_sorter = (LayerTableSorter)m_viewer.getSorter();
    // TODO: check if sorting is supported at all

    m_oldSortColumn = m_sorter.getColumn();
    m_oldInverse = m_sorter.isInverse();

    if( m_oldSortColumn != null && m_oldSortColumn.equals( column ) )
    {
      // falls bereits invers, ausschalten
      if( m_oldInverse )
      {
        m_newSortColumn = null;
        m_newInverse = false;
      }
      else
      {
        m_newInverse = true;
        m_newSortColumn = m_oldSortColumn;
      }
    }
    else
    {
      m_newInverse = false;
      m_newSortColumn = column;
    }
  }

  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  @Override
  public void process( ) throws Exception
  {
    changeSorter( m_newInverse, m_newSortColumn );
  }

  private void changeSorter( final boolean bInverse, final IColumnDescriptor column )
  {
    m_sorter.setInverse( bInverse );
    m_sorter.setColumn( column );

    final LayerTableViewer viewer = m_viewer;
    viewer.getControl().getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        viewer.refresh();
      }
    } );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  @Override
  public void redo( ) throws Exception
  {
    process();
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  @Override
  public void undo( ) throws Exception
  {
    changeSorter( m_oldInverse, m_oldSortColumn );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return (m_newInverse ? Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.0" ) : Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.1" )) + Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.2" ) + m_newSortColumn; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}