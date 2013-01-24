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
package org.kalypso.zml.ui.core.provider.style;

import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractStyleSetProvider implements IStyleSetProvider
{
  private final StyleSet m_styleSet = new StyleSet();

  @Override
  public StyleSet getStyleSet( )
  {
    return m_styleSet;
  }

  @Override
  public ILineStyle getDefaultLineStyle( )
  {
    return getStyleSet().getStyle( LINE_PREFIX, ILineStyle.class );
  }

  @Override
  public ILineStyle getLineStyle( final String id )
  {
    if( id == null )
      return getDefaultLineStyle();

    return getStyleSet().getStyle( LINE_PREFIX + id, ILineStyle.class );
  }

  @Override
  public ITextStyle getDefaultTextStyle( )
  {
    return getStyleSet().getStyle( TEXT_PREFIX, ITextStyle.class );
  }

  @Override
  public ITextStyle getTextStyle( final String id )
  {
    return getStyleSet().getStyle( TEXT_PREFIX + id, ITextStyle.class );
  }

  @Override
  public IAreaStyle getDefaultAreaStyle( )
  {
    return getStyleSet().getStyle( AREA_PREFIX, IAreaStyle.class );
  }

  @Override
  public IAreaStyle getAreaStyle( final String id )
  {
    return getStyleSet().getStyle( AREA_PREFIX + id, IAreaStyle.class );
  }

  @Override
  public IPointStyle getDefaultPointStyle( )
  {
    return getStyleSet().getStyle( POINT_PREFIX, IPointStyle.class );
  }

  @Override
  public IPointStyle getPointStyle( final String id )
  {
    return getStyleSet().getStyle( POINT_PREFIX + id, IPointStyle.class );
  }
}
