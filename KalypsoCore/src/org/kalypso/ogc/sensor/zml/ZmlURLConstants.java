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
package org.kalypso.ogc.sensor.zml;

/**
 * Contains constants used within the observation service framework of Kalypso.
 * 
 * @author schlienger (18.05.2005)
 */
public interface ZmlURLConstants
{
  /** fragment part of the url denoting a Zml-Url with a context, not a Zml-Id */
  String FRAGMENT_USEASCONTEXT = "useascontext"; //$NON-NLS-1$

  /** Specification-Tags for the Zml-Request in the URL */
// public final static String TAG_REQUEST1 = "<request"; // !NOTE: without ending '>'
// public final static String TAG_REQUEST2 = "</request>";
  String TAG_REQUEST = "request"; //$NON-NLS-1$

  /** Filter-Tags for the Zml-Filter in the URL */
  String TAG_FILTER1 = "<filter>"; //$NON-NLS-1$

  String TAG_FILTER2 = "</filter>"; //$NON-NLS-1$
}