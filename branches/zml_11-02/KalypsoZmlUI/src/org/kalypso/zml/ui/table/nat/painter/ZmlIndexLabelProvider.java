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
package org.kalypso.zml.ui.table.nat.painter;

import java.text.SimpleDateFormat;

import net.sourceforge.nattable.style.Style;

import org.eclipse.swt.graphics.Image;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.zml.core.table.model.references.IZmlCellStyleProvider;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelCellLabelProvider;

/**
 * @author Dirk Kuch
 */
public class ZmlIndexLabelProvider implements IZmlModelCellLabelProvider
{
  private final IZmlCellStyleProvider m_styleProvider;

  public ZmlIndexLabelProvider( final IZmlCellStyleProvider styleProvider )
  {
    m_styleProvider = styleProvider;
  }

  @Override
  public Image[] getImages( final IZmlModelCell cell )
  {
    return new Image[] {};
  }

  @Override
  public String getText( final IZmlModelCell cell )
  {
    final SimpleDateFormat sdf = new SimpleDateFormat();
    sdf.setTimeZone( KalypsoCorePlugin.getDefault().getTimeZone() );

    return sdf.format( cell.getIndexValue() );
  }

  @Override
  public Style getStyle( )
  {
    // TODO
    return new Style();
  }

}
