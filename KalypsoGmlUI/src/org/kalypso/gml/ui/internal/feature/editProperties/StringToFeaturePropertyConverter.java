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
package org.kalypso.gml.ui.internal.feature.editProperties;

import java.text.ParseException;

import org.eclipse.core.databinding.conversion.IConverter;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;

/**
 * @author Gernot Belger
 */
public class StringToFeaturePropertyConverter implements IConverter
{
  private final EditFeaturePropertiesData m_data;

  public StringToFeaturePropertyConverter( final EditFeaturePropertiesData data )
  {
    m_data = data;
  }

  @Override
  public Object getFromType( )
  {
    return String.class;
  }

  @Override
  public Object getToType( )
  {
    return Object.class;
  }

  @Override
  public Object convert( final Object fromObject )
  {
    final ITypeRegistry<IGuiTypeHandler> registry = GuiTypeRegistrySingleton.getTypeRegistry();

    final IPropertyType property = m_data.getProperty();
    if( property == null )
      return null;

    if( property instanceof IRelationType )
      return null;

    final IGuiTypeHandler typeHandler = registry.getTypeHandlerFor( property );
    if( typeHandler == null )
      return null;

    try
    {
      return typeHandler.parseText( (String) fromObject, null );
    }
    catch( final ParseException e )
    {
      e.printStackTrace();
      return null;
    }
    catch( final UnsupportedOperationException e )
    {
      // ignore, this case is handled elsewhere
      return null;
    }
  }
}