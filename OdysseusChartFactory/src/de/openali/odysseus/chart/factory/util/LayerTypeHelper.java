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
package de.openali.odysseus.chart.factory.util;

import de.openali.odysseus.chartconfig.x020.LayerType;
import de.openali.odysseus.chartconfig.x020.ParameterType;
import de.openali.odysseus.chartconfig.x020.ParametersType;
import de.openali.odysseus.chartconfig.x020.ProviderType;

/**
 * @author Dirk Kuch
 */
public final class LayerTypeHelper
{
  private LayerTypeHelper( )
  {
  }

  public static void appendParameters( final LayerType type, final ParametersType parameters )
  {
    if( parameters == null )
      return;

    final ProviderType provider = type.getProvider();
    if( provider == null )
      return;

    final ParametersType baseType = provider.getParameters();

    final ParameterType[] array = parameters.getParameterArray();
    for( final ParameterType parameter : array )
    {
      final ParameterType baseParameter = getParamter( baseType, parameter.getName() );
      baseParameter.setValue( parameter.getValue() );
    }
  }

  private static ParameterType getParamter( final ParametersType baseType, final String name )
  {
    final ParameterType[] parameters = baseType.getParameterArray();
    for( final ParameterType parameter : parameters )
    {
      if( parameter.getName().equals( name ) )
        return parameter;
    }

    final ParameterType parameter = baseType.addNewParameter();
    parameter.setName( name );

    return parameter;
  }

}
