/*--------------- Kalypso-Deegree-Header ------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 
 history:
  
 Files in this package are originally taken from deegree and modified here
 to fit in kalypso. As goals of kalypso differ from that one in deegree
 interface-compatibility to deegree is wanted but not retained always. 
     
 If you intend to use this software in other ways than in kalypso 
 (e.g. OGC-web services), you should consider the latest version of deegree,
 see http://www.deegree.org .

 all modifications are licensed as deegree, 
 original copyright:
 
 Copyright (C) 2001 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon GmbH
 http://www.lat-lon.de
 
---------------------------------------------------------------------------------------------------*/
package org.kalypsodeegree.gml;

import org.w3c.dom.Element;

/**
 * 
 * 
 * <p>
 * ----------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version 07.02.2001
 *          <p>
 */
public interface GMLFeature
{
  /* #GMLDocument lnkGMLDocument; */

  /**
   * @link aggregation
   * @clientCardinality 0..*
   */
  /* #GMLProperty lnkGMLProperty; */
  /* #GMLProperty lnkGMLProperty1; */

  /**
   * returns the ID of the feature
   */
  public String getId();

  /**
   * @see #getId()
   */
  public void setId( String id );

  /**
   * returns the description of the feature
   */
  public String getDescription();

  /**
   * @see #getDescription()
   */
  public void setDescription( String describtion );

  /**
   * returns the name of the Geometry.  (with namespace qualifier, shortcut)
   */
  public String getName();

  /**
   * returns the name of the Geometry.
   */
  public String getLocalName();
  
  public String getNamespaceURI();

  /**
   * @see #getName()
   */
  public void setName( String name );

  /**
   * return the name of the feature type the feature based on
   */
  public String getFeatureTypeName();

  /**
   * returns the boundingbox of the feature
   */
  public GMLBox getBoundedBy();

  /**
   * returns all properties of the feature
   */
  public GMLProperty[] getProperties();

  /**
   * returns alls properties that are a GMLGeometry
   */
  public GMLGeoProperty[] getGeoProperties();

  /**
   * returns alls properties that are not a GMLGeometry
   */
  public GMLProperty[] getNoneGeoProperties();

  /**
   * returns a specific property identified by its name
   */
  public GMLProperty getProperty( String name );

  /**
   * adds a property to the feature
   */
  public void addProperty( GMLProperty property ) throws GMLException;

  public Element getAsElement();
}
/*
 * Changes to this class. What the people haven been up to:
 * 
 * $Log$
 * Revision 1.6  2005/03/08 11:01:10  doemming
 * *** empty log message ***
 *
 * Revision 1.5  2005/02/03 18:37:42  belger
 * *** empty log message ***
 *
 * Revision 1.4  2005/01/18 12:50:41  doemming
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/07 14:09:01  doemming
 * *** empty log message ***
 *
 * Revision 1.1  2004/09/02 23:56:51  doemming
 * *** empty log message ***
 * Revision 1.3 2004/08/31 12:45:01 doemming *** empty
 * log message *** Revision 1.3 2004/04/27 15:40:15 poth no message
 * 
 * Revision 1.2 2003/07/21 07:50:47 poth no message
 * 
 * Revision 1.1.1.1 2002/09/25 16:01:45 poth no message
 * 
 * Revision 1.2 2002/08/19 15:59:20 ap no message
 * 
 * Revision 1.1 2002/04/04 16:17:15 ap no message
 * 
 * Revision 1.4 2001/11/23 10:40:53 axel as: CVS change-log comment added
 * 
 *  
 */
