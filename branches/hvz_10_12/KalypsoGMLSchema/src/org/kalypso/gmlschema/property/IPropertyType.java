/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.gmlschema.property;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.builder.IInitialize;
import org.kalypso.gmlschema.feature.IFeatureType;

/**
 * Instances of this interface represent the properties of a {@link org.kalypso.gmlschema.feature.IFeatureType}.
 * 
 * @author Andreas von Dömming
 */
public interface IPropertyType extends IInitialize
{
  public final static int UNBOUND_OCCURENCY = -1;

  QName getQName( );

  public int getMinOccurs( );

  public int getMaxOccurs( );

  public boolean isList( );

  /**
   * A property type is virtual, if it is not defined as regular element inside a feature type of a gml
   * application-schema.<br>
   * Virtual properties are either defined inside an appinfo-element of the schema or registered via an eclipse
   * extension.<br>
   * Virtual properties are always backed-up by function-properties and are never serialized into the gml (else the
   * resulting gml would not validate against its application-schema).
   */
  public boolean isVirtual( );

  public boolean isNillable( );

  /**
   * @deprecated use getQName()
   * @return local part of qualified name
   */
  @Deprecated
  public String getName( );

  /**
   * Returns the annotation (i.e. human readable strings) for this property type.<br>
   */
  IAnnotation getAnnotation( );

  /** Not intended to be used outside the gml schema parser. */
  IPropertyType cloneForFeatureType( IFeatureType featureType );
}
