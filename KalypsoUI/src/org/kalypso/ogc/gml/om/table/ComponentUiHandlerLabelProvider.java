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
package org.kalypso.ogc.gml.om.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.kalypso.observation.result.IRecord;
import org.kalypso.ogc.gml.om.table.handlers.IComponentUiHandler;

/**
 * @author Gernot Belger
 */
class ComponentUiHandlerLabelProvider extends ColumnLabelProvider
{
  private final IComponentUiHandler m_handler;

  public ComponentUiHandlerLabelProvider( final IComponentUiHandler handler )
  {
    m_handler = handler;
  }

  @Override
  public String getText( final Object element )
  {
    if( element instanceof IRecord )
    {
      try
      {
        final IRecord record = (IRecord)element;
        return m_handler.getStringRepresentation( record );
      }
      catch( final IllegalArgumentException e )
      {
        e.printStackTrace();
      }
    }

    return super.getText( element );
  }

  @Override
  public String getToolTipText( final Object element )
  {
    return getText( element );
  }

  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof IRecord )
    {
      try
      {
        final IRecord record = (IRecord)element;
        return m_handler.getImage( record );
      }
      catch( final IllegalArgumentException e )
      {
        e.printStackTrace();
      }
    }

    return null;
  }
}