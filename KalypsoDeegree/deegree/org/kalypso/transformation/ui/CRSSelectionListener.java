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
package org.kalypso.transformation.ui;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.kalypso.transformation.crs.ICoordinateSystem;

/**
 * This class is a listener for notifying, if a selection of a crs has changed.
 * 
 * @author Holger Albert
 */
public abstract class CRSSelectionListener implements ISelectionChangedListener
{
  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public final void selectionChanged( SelectionChangedEvent event )
  {
    /* Get the selection. */
    ISelection selection = event.getSelection();

    /* If not empty and the right type, the code is told to the listeners. */
    if( !selection.isEmpty() && selection instanceof IStructuredSelection )
    {
      /* Cast. */
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;

      /* Get the selected element. */
      Object selectedElement = structuredSelection.getFirstElement();

      /* Check type. */
      if( selectedElement instanceof ICoordinateSystem )
      {
        /* Cast. */
        ICoordinateSystem coordinateSystem = (ICoordinateSystem) selectedElement;

        /* Tell the code of the selected coordinate system to the listeners. */
        selectionChanged( coordinateSystem.getCode() );

        return;
      }
    }

    /* Tell the code of the selected coordinate system to the listeners. */
    selectionChanged( null );
  }

  /**
   * This function is a simplification of the normal {@link #selectionChanged(SelectionChangedEvent)} function.
   * 
   * @param selectedCRS
   *          The code of the selected coordinate system, or null, if none is selected.
   */
  protected abstract void selectionChanged( String selectedCRS );
}