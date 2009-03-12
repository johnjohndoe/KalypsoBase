/*----------------    FILE HEADER  ------------------------------------------

 This file is part of deegree.
 Copyright (C) 2001 by:
 EXSE, Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/exse/
 lat/lon Fitzke/Fretter/Poth GbR
 http://www.lat-lon.de

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

 Andreas Poth
 lat/lon Fitzke/Fretter/Poth GbR
 Meckenheimer Allee 176
 53115 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Jens Fitzke
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: jens.fitzke@uni-bonn.de

 
 ---------------------------------------------------------------------------*/
package org.deegree.gml;

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
public interface GMLFeatureCollection extends GMLFeature
{
  /**
   * @link aggregation
   * @clientCardinality 0..*
   */
  /* #GMLFeature lnkGMLFeature; */
  /* #GMLFeatureCollection lnkGMLFeatureCollection; */

  /**
   * returns all features of the collection
   */
  public GMLFeature[] getFeatures();

  /**
   * returns the feature that matvhes the submitted id
   */
  public GMLFeature getFeature( String id );

  /**
   * returns the features that matvhes the submitted name
   */
  public GMLFeature[] getFeatures( String name );

  /**
   * adds a feature to the collection
   */
  public void addFeature( GMLFeature feature );

  /**
   * removes a feature from the collection
   */
  public void removeFeature( String id );

  /**
   * sets the bounding box of the feature collection
   */
  public void setBoundingBox( double minx, double miny, double maxx, double maxy );

  /**
   * sets the bounding box of the feature collection
   */
  public void setBoundingBox( GMLBox box );

}
/*
 * Changes to this class. What the people haven been up to:
 * 
 * $Log$
 * Revision 1.3  2004/10/07 14:09:01  doemming
 * *** empty log message ***
 *
 * Revision 1.1  2004/09/02 23:56:51  doemming
 * *** empty log message ***
 * Revision 1.3 2004/08/31 12:45:01 doemming
 * *** empty log message *** Revision 1.1.1.1 2002/09/25 16:01:45 poth no
 * message
 * 
 * Revision 1.3 2002/08/19 15:59:20 ap no message
 * 
 * Revision 1.2 2002/07/19 14:46:09 ap no message
 * 
 * Revision 1.1 2002/04/04 16:17:15 ap no message
 * 
 * Revision 1.3 2001/11/23 10:40:53 axel as: CVS change-log comment added
 * 
 *  
 */
