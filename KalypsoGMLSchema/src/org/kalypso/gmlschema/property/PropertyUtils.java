/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gmlschema.property;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;

/**
 * Utilities around {@link org.kalypso.gmlschema.property.IPropertyType}
 * 
 * @author Gernot Belger
 */
public class PropertyUtils
{
  private PropertyUtils( )
  {
    // never instantiate
  }

  /**
   * Reads the values and label of an restricted type into a map.
   * 
   * @return Map 'value' -> 'label'. Empty if the type has no enumeration-restrictions.
   */
  public static Map<Object, String> createComboEntries( final IValuePropertyType vpt )
  {
    // comboValue -> comboLabel
    final Map<Object, String> comboEntries = new LinkedHashMap<Object, String>();

    final IMarshallingTypeHandler typeHandler = (IMarshallingTypeHandler) vpt.getTypeHandler();

    // if we have an enumeration, create a combo
    final IRestriction[] restrictions = vpt.getRestriction();

    // join all enumeration constants
    for( final IRestriction restriction : restrictions )
    {
      if( restriction instanceof EnumerationRestriction )
      {
        final String[] values = ((EnumerationRestriction) restriction).getEnumeration();
        final String[] labels = ((EnumerationRestriction) restriction).getLabels();

        for( int i = 0; i < labels.length; i++ )
        {
          final String label = labels[i];
          final String valueString = values[i];
          Object value = null;
          try
          {
            value = typeHandler.parseType( valueString );
            comboEntries.put( value, label );
          }
          catch( final ParseException e )
          {
            final IStatus status = StatusUtilities.statusFromThrowable( e );
            KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
          }
        }
      }
    }
    return comboEntries;
  }

  public static IPropertyType[] filterProperties( final IFeatureType ft, final IPropertyTypeFilter filter )
  {
    final IPropertyType[] properties = ft.getProperties();
    final List<IPropertyType> filtered = new ArrayList<IPropertyType>( properties.length );
    for( final IPropertyType type : properties )
    {
      if( filter.accept( type ) )
        filtered.add( type );
    }

    return filtered.toArray( new IPropertyType[filtered.size()] );
  }

}
