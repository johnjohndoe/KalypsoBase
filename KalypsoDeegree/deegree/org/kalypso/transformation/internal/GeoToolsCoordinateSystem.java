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
package org.kalypso.transformation.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.geotools.referencing.CRS;
import org.kalypso.transformation.crs.ICoordinateSystem;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This coordinate system uses geo tools.
 * 
 * @author Holger Albert
 */
public class GeoToolsCoordinateSystem implements ICoordinateSystem
{
  /**
   * The code for the coordinate system (e.g. 'EPSG:31467').
   */
  private final String m_code;

  /**
   * The geo tools coordinate system.
   */
  private CoordinateReferenceSystem m_coordinateSystem;

  /**
   * Not null, if this coordinate system was once initialized. It contains the error, if initializing was not
   * successfull.
   */
  private IStatus m_initStatus;

  /**
   * The constructor.
   * 
   * @param code
   *          The code for the coordinate system (e.g. 'EPSG:31467').
   */
  public GeoToolsCoordinateSystem( final String code )
  {
    m_code = code;
    m_coordinateSystem = null;
    m_initStatus = null;

    init( m_code );
  }

  /**
   * @see org.kalypso.transformation.crs.ICoordinateSystem#getCode()
   */
  @Override
  public String getCode( )
  {
    return m_code;
  }

  /**
   * @see org.kalypso.transformation.crs.ICoordinateSystem#getName()
   */
  @Override
  public String getName( )
  {
    if( !m_initStatus.isOK() )
      return m_initStatus.getMessage();

    return m_coordinateSystem.getName().getCode();
  }

  /**
   * @see org.kalypso.transformation.crs.ICoordinateSystem#getDimension()
   */
  @Override
  public int getDimension( )
  {
    if( !m_initStatus.isOK() )
      return -1;

    return m_coordinateSystem.getCoordinateSystem().getDimension();
  }

  /**
   * @see org.kalypso.transformation.crs.ICoordinateSystem#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return m_initStatus.isOK();
  }

  /**
   * This function initializes the coordinate system.
   * 
   * @param code
   *          The code for the coordinate system (e.g. 'EPSG:31467').
   */
  private void init( final String code )
  {
    try
    {
      /* Create the geo tools coordinate system. */
      m_coordinateSystem = CRS.decode( code );

      /* Store OK. */
      m_initStatus = new Status( IStatus.OK, KalypsoDeegreePlugin.getID(), "OK" );
    }
    catch( final Exception ex )
    {
      /* Store the error. */
      m_initStatus = new Status( IStatus.ERROR, KalypsoDeegreePlugin.getID(), ex.getLocalizedMessage(), ex );
    }
  }
}