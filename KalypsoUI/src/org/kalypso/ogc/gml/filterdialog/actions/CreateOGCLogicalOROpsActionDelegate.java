/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.gml.filterdialog.actions;

import java.util.ArrayList;

import org.eclipse.jface.action.IAction;
import org.kalypso.ogc.gml.filterdialog.dialog.TreeSelection;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree_impl.filterencoding.ComplexFilter;
import org.kalypsodeegree_impl.filterencoding.LogicalOperation;
import org.kalypsodeegree_impl.filterencoding.OperationDefines;

/**
 * @author kuepfer
 */
public class CreateOGCLogicalOROpsActionDelegate extends AbstractCreateOperationActionDelegate
{

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run( IAction action )
  {
    if( m_selection != null && action.isEnabled() )
    {
      if( m_selection instanceof TreeSelection )
      {
        Object firstElement = m_selection.getFirstElement();
        if( firstElement instanceof ComplexFilter )
        {
          ComplexFilter filter = (ComplexFilter)firstElement;
          filter.setOperation( new LogicalOperation( OperationDefines.OR, new ArrayList<Operation>() ) );
        }
        if( firstElement instanceof LogicalOperation )
        {
          final LogicalOperation operation = (LogicalOperation)firstElement;
          //add new Logical Operation
          ArrayList<Operation> arguments = operation.getArguments();
          if( arguments == null )
            arguments = new ArrayList<Operation>();
          arguments.add( new LogicalOperation( OperationDefines.OR, new ArrayList<Operation>() ) );
        }
        ( (TreeSelection)m_selection ).structureChanged();
      }
    }
  }

}
