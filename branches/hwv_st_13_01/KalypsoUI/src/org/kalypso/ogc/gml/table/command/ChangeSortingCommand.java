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
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.table.LayerTableSorter;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author gernot
 */
public class ChangeSortingCommand implements ICommand
{
  private final LayerTableViewer m_viewer;

  private final LayerTableSorter m_sorter;

  private final GMLXPath m_oldPropertyPath;

  private final GMLXPath m_newPropertyPath;

  private final boolean m_oldInverse;

  private final boolean m_newInverse;

  public ChangeSortingCommand( final LayerTableViewer viewer, final TableColumn tableColumn )
  {
    m_viewer = viewer;

    final GMLXPath propertyPath = (GMLXPath) tableColumn.getData( LayerTableViewer.COLUMN_PROP_PATH );

    m_sorter = (LayerTableSorter)m_viewer.getSorter();
    // TODO: check if sorting is supported at all

    m_oldPropertyPath = m_sorter.getPropertyPath();
    m_oldInverse = m_sorter.isInverse();

    if( m_oldPropertyPath != null && m_oldPropertyPath.equals( propertyPath ) )
    {
      // falls bereits invers, ausschalten
      if( m_oldInverse )
      {
        m_newPropertyPath = null;
        m_newInverse = false;
      }
      else
      {
        m_newInverse = true;
        m_newPropertyPath = m_oldPropertyPath;
      }
    }
    else
    {
      m_newInverse = false;
      m_newPropertyPath = propertyPath;
    }
  }

  /**
   * @see org.kalypso.commons.command.ICommand#isUndoable()
   */
  @Override
  public boolean isUndoable()
  {
    return true;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#process()
   */
  @Override
  public void process() throws Exception
  {
    changeSorter( m_newInverse, m_newPropertyPath );
  }

  private void changeSorter( final boolean bInverse, final GMLXPath propertyPath )
  {
    m_sorter.setInverse( bInverse );
    m_sorter.setPropertyPath( propertyPath );

    final LayerTableViewer viewer = m_viewer;
    viewer.getControl().getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run()
      {
        viewer.refresh();
      }
    } );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  @Override
  public void redo() throws Exception
  {
    process();
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  @Override
  public void undo() throws Exception
  {
    changeSorter( m_oldInverse, m_oldPropertyPath );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  @Override
  public String getDescription()
  {
    return (m_newInverse ? Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.0" ) : Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.1" )) + Messages.getString( "org.kalypso.ogc.gml.table.command.ChangeSortingCommand.2" ) + m_newPropertyPath; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}