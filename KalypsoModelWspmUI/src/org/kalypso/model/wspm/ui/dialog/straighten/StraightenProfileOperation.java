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
package org.kalypso.model.wspm.ui.dialog.straighten;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_AMOUNT;
import org.kalypso.model.wspm.ui.dialog.straighten.data.CORRECT_POINTS_ENABLEMENT;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

/**
 * This operation straightens a profile between two points.
 * 
 * @author Holger Albert
 */
public class StraightenProfileOperation implements ICoreRunnableWithProgress
{
  /**
   * The straighten profile data.
   */
  private final StraightenProfileData m_data;

  /**
   * The constructor.
   * 
   * @param data
   *          The straighten profile data.
   */
  public StraightenProfileOperation( final StraightenProfileData data )
  {
    m_data = data;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus execute( IProgressMonitor monitor )
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( "Straightening profile", 1000 );
      monitor.subTask( "Defining straight line..." );

      /* Define straight line. */
      defineLine( m_data );

      /* Monitor. */
      monitor.worked( 300 );
      monitor.subTask( "Projecting all points to the line..." );

      /* Project all points to the line. */
      projectPoints( m_data );

      /* Monitor. */
      monitor.worked( 400 );
      monitor.subTask( "Recalculating the width of the points..." );

      /* Recalculate the width of the points. */
      if( m_data.getCorrectPointsEnablement() == CORRECT_POINTS_ENABLEMENT.ON )
        recalculateWidth( m_data );

      /* Monitor. */
      monitor.worked( 300 );

      return new Status( IStatus.OK, KalypsoModelWspmUIPlugin.ID, "Straightening successfull." );
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoModelWspmUIPlugin.ID, ex.getLocalizedMessage(), ex );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  private void defineLine( final StraightenProfileData data )
  {
    /* Get the first and the second point. */
    final Point firstPoint = data.getFirstPoint();
    final Point secondPoint = data.getSecondPoint();

    /* Create a straight line. */
    final GeometryFactory factory = new GeometryFactory( firstPoint.getPrecisionModel(), firstPoint.getSRID() );
    final LineString straightLine = factory.createLineString( new Coordinate[] { firstPoint.getCoordinate(), secondPoint.getCoordinate() } );

    // TODO
  }

  private void projectPoints( final StraightenProfileData data )
  {
    /* Add the first and second point to the profile, if equivalents do not exist there. */
    // TODO

    /* Find all points in the profile between the first and the second point. */
    // TODO

    /* Project them to the straight line and adjust the x/y coordinates. */
    // TODO
  }

  private void recalculateWidth( final StraightenProfileData data )
  {
    /* Corrent only the points found between the first and the second point. */
    if( m_data.getCorrectPointsAmount() == CORRECT_POINTS_AMOUNT.BETWEEN )
    {
      // TODO
    }

    /* Correct all points of the profile. */
    // TODO
  }
}