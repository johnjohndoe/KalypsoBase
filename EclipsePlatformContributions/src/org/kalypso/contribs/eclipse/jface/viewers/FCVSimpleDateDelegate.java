/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.jface.viewers;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Dirk Kuch
 */
public class FCVSimpleDateDelegate implements IFCVDelegate
{

  private final SimpleDateFormat[] m_array;

  public FCVSimpleDateDelegate( final SimpleDateFormat[] array )
  {
    m_array = array;
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getDefaultKey()
   */
  @Override
  public ISelection getDefaultKey( )
  {
    if( m_array.length > 0 )
      return new StructuredSelection( m_array[0] );

    return new StructuredSelection();
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getInputData()
   */
  @Override
  public Object[] getInputData( )
  {
    return m_array;
  }

  /**
   * @see org.kalypso.nofdpidss.ui.application.widgets.IWComboViewerDelegate#getValue(java.lang.Object)
   */
  @Override
  public String getValue( final Object element )
  {
    if( element instanceof SimpleDateFormat )
    {
      final SimpleDateFormat df = (SimpleDateFormat) element;
      return df.format( new Date() ).toString();
    }

    return element.toString();
  }

}
