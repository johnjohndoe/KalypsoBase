/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.contribs.eclipse.jface.viewers;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Dirk Kuch
 */
public class FCVArrayDelegate implements IFCVDelegate
{

  private final Object[] m_input;

  public FCVArrayDelegate( final Object[] input )
  {
    m_input = input;
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getDefaultKey()
   */
  @Override
  public ISelection getDefaultKey( )
  {
    if( m_input.length > 0 )
      return new StructuredSelection( m_input[0] );

    return new StructuredSelection();
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getInputData()
   */
  @Override
  public Object[] getInputData( )
  {
    return m_input;
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getValue(java.lang.Object)
   */
  @Override
  public String getValue( final Object element )
  {
    return element.toString();
  }

}