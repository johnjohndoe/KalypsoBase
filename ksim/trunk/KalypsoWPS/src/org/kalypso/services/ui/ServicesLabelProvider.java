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
package org.kalypso.services.ui;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * @author skurzbach
 */
public class ServicesLabelProvider extends LabelProvider
{
  private static final String UNKNOWN_TYPE_ERROR_TEXT = "unknown element type";

  private static final String NO_LABEL_PROVIDER_ERROR_TEXT = "no label provider";

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    final String result;
    final IAdapterManager adapterManager = Platform.getAdapterManager();
    if( element instanceof String )
    {
      result = (String) element;
    }
    else if( adapterManager.hasAdapter( element, ILabelProvider.class.getName() ) )
    {
      final ILabelProvider childLabelProvider = (ILabelProvider) adapterManager.getAdapter( element, ILabelProvider.class );
      if( childLabelProvider != null )
      {
        result = childLabelProvider.getText( element );
      }
      else
      {
        result = NO_LABEL_PROVIDER_ERROR_TEXT;
      }
    }
    else
    {
      result = UNKNOWN_TYPE_ERROR_TEXT;
    }
    return result;
  }
}
