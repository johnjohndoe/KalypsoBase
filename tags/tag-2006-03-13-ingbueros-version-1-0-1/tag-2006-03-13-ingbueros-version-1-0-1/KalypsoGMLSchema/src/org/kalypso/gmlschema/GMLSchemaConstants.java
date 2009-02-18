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
package org.kalypso.gmlschema;


/**
 * 
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public abstract class GMLSchemaConstants
{
  public final static String NS_XMLSCHEMA = "http://www.w3.org/2001/XMLSchema";

  public static final String NS_GML2 = "http://www.opengis.net/gml";

  public static final String NS_XLINK = "http://www.w3.org/1999/xlink";

  public static final String NS_OBSLINK = "obslink.zml.kalypso.org";

  public static final String ELEMENTS_TO_PASS = "ELEMENTS_TO_PASS";

  /*
   * not sure if this is valid to GML
   */
  public static final String ELEMENTS_TO_CHECK = "ELEMENTS_TO_CHECK";

  public static final int TYPE_FT = 1;

  public static final int TYPE_PROP_SIMPLE = 2;

  public static final int TYPE_PROP_LINK = 3;


}
