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
import org.kalypso.ogc.gml.table.LayerTableViewer;

/**
 * Kommando zum �ndern der Spaltenbreite. Es wird davon ausgegangen, dass die Breite der Spalte (d.h. des Widgets)
 * bereits gesetzt wurde.
 * 
 * @author Belger
 */
public class SetColumnWidthCommand implements ICommand
{
  private final TableColumn m_tableColumn;

  private final int m_oldWidth;

  private final int m_newWidth;

  public SetColumnWidthCommand( final TableColumn tableColumn, final int width )
  {
    m_tableColumn = tableColumn;

    m_newWidth = width;
    m_oldWidth = ( (Integer)tableColumn.getData( LayerTableViewer.COLUMN_PROP_WIDTH ) ).intValue();
  }

  /**
   * @see org.kalypso.commons.command.ICommand#isUndoable()
   */
  public boolean isUndoable()
  {
    return true;
  }

  /**
   * @see org.kalypso.commons.command.ICommand#process()
   */
  public void process() throws Exception
  {
    setWidth( m_newWidth, false );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#redo()
   */
  public void redo() throws Exception
  {
    setWidth( m_newWidth, true );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#undo()
   */
  public void undo() throws Exception
  {
    setWidth( m_oldWidth, true );
  }

  /**
   * @see org.kalypso.commons.command.ICommand#getDescription()
   */
  public String getDescription()
  {
    return Messages.get("org.kalypso.ogc.gml.table.celleditors.SetColumnWidthCommand.0"); //$NON-NLS-1$
  }

  private void setWidth( final int width, final boolean bSetControlWidth )
  {
    final TableColumn tableColumn = m_tableColumn;
    if( !tableColumn.isDisposed() )
    {
      m_tableColumn.getDisplay().asyncExec( new Runnable()
      {
        public void run()
        {
          tableColumn.setData( LayerTableViewer.COLUMN_PROP_WIDTH, new Integer( width ) );
          if( bSetControlWidth )
            tableColumn.setWidth( width );
        }
      } );
    }
  }

}