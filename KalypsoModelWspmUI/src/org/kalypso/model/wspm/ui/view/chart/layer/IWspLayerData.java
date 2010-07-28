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
package org.kalypso.model.wspm.ui.view.chart.layer;

import java.math.BigDecimal;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * This interface provides functions for data objects, for the {@link WspLayer}.
 * 
 * @author Holger Albert
 */
public interface IWspLayerData
{
  /**
   * This function returns the input which will be given to the content provider.
   * 
   * @return The input.
   */
  Object getInput( ) throws Exception;

  /**
   * This function returns the active elements.
   * 
   * @return The active elements.
   */
  Object[] getActiveElements( ) throws Exception;

  /**
   * This function activates the given elements.
   * 
   * @param elements
   *          The elements to be activated.
   */
  void activateElements( Object[] elements ) throws Exception;

  /**
   * This function tries to find a value for the given element and station.
   * 
   * @param element
   *          The element.
   * @param station
   *          The station.
   * @return The value for the given name and station or Double.NaN.
   */
  double searchValue( Object element, BigDecimal station ) throws Exception;

  /**
   * Creates a label provider that is able to display the element returned by {@link #getAvailableElements()}.<br/>
   * The caller is responsible to dispose the returned label provider.
   */
  ILabelProvider createLabelProvider( );

  /**
   * Create a content provider that is able to return the elements to be shown in a tree<br/>
   * The array of elements returned by {@link #getAvailableElements()} will be given to the content provider as input
   * element.
   */
  ITreeContentProvider createContentProvider( );
}