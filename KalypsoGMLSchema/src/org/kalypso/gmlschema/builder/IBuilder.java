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
package org.kalypso.gmlschema.builder;

import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaException;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public interface IBuilder
{
  /**
   * build GML-Schema java object types and register them to the gmlSchema
   * 
   * @param gmlSchema
   * @param typeObject
   * @return array of objects that will be build in next turn if neccessary
   */
  public Object[] build( final GMLSchema gmlSchema, final Object typeObject ) throws GMLSchemaException;;

  /**
   * returns <code>true</code> if this builder is intended to build something for the given <code>object</code> in
   * this build run named <code>namedPass</code>
   * 
   * @param gmlSchema
   *          current schema
   * @param object
   *          object to build something for
   * @param namedPass
   *          name of the current build run
   * @return true if builder can build something for this object
   */
  public boolean isBuilderFor( final GMLSchema gmlSchema, final Object object, final String namedPass ) throws GMLSchemaException;

  /**
   * This method is called, when two builder are concurrent for an object to build because both builders methodes
   * isBuilderFor(GMLSchema, Object, String) return <code>true</code> for the same object to build.
   * 
   * @returns returns <code>true</code> if this builder replaces the other builder.
   */
  public boolean replaces( final IBuilder other );

}
