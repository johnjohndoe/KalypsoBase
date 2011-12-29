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
package org.kalypso.gmlschema;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.visitor.IGMLSchemaVisitor;

/**
 * Empty implementation of {@link IGMLSchema}, used by {@link org.kalypso.gmlschema.feature.CustomFeatureType}.
 *
 * @author Belger
 */
public class EmptyGMLSchema implements IGMLSchema
{
  private final Map<QName, IFeatureType> m_featureTypes = new HashMap<>();

  @Override
  public String getGMLVersion( )
  {
    return "3.1"; //$NON-NLS-1$
  }

  @Override
  public String getTargetNamespace( )
  {
    return "empty"; //$NON-NLS-1$
  }

  @Override
  public URL getContext( )
  {
    return null;
  }

  @Override
  public void accept( final IGMLSchemaVisitor visitor )
  {
    visitor.visit( this );
  }

  @Override
  public IFeatureType getFeatureType( final QName qName )
  {
    return null;
  }

  @Override
  public IFeatureType[] getAllFeatureTypes( )
  {
    final Collection<IFeatureType> values = m_featureTypes.values();
    return values.toArray( new IFeatureType[values.size()] );
  }

  @Override
  public IGMLSchema[] getAdditionalSchemas( )
  {
    return new IGMLSchema[0];
  }
}