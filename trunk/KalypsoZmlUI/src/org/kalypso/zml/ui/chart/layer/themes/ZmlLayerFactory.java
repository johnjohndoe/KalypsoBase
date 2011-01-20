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
package org.kalypso.zml.ui.chart.layer.themes;

import org.kalypso.zml.ui.chart.layer.provider.ZmlBarLayerProvider;
import org.kalypso.zml.ui.chart.layer.provider.ZmlLineLayerProvider;

import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;

/**
 * @author Dirk Kuch
 */
public final class ZmlLayerFactory
{
  private static ZmlLayerFactory INSTANCE;

  private ZmlLayerFactory( )
  {
  }

  public static ZmlLayerFactory getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new ZmlLayerFactory();

    return INSTANCE;
  }

  public ZmlLineLayer createLineLayer( final ZmlLineLayerProvider provider, final IStyleSet styleSet )
  {
    final ZmlLineLayer layer = new ZmlLineLayer( provider, styleSet );

    return layer;
  }

  public ZmlLineLayer createLineLayer( final IStyleSet styleSet )
  {
    return createLineLayer( null, styleSet );
  }

  public ZmlBarLayer createBarLayer( final ZmlBarLayerProvider provider, final IAreaStyle style )
  {
    final ZmlBarLayer layer = new ZmlBarLayer( provider, style );

    return layer;
  }

  public ZmlBarLayer createBarLayer( final IAreaStyle style )
  {
    return createBarLayer( null, style );
  }

  public ZmlConstantLineLayer createConstantLineLayer( final IParameterContainer parameters, final IStyleSet styleSet, final boolean calculatedRange )
  {
    final ZmlConstantLineLayer layer = new ZmlConstantLineLayer( parameters, styleSet, calculatedRange );

    return layer;
  }

}
