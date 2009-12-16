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
package org.kalypso.ogc.util.timeserieslink;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang.NotImplementedException;
import org.kalypso.ogc.sensor.DateRange;

/**
 * @author Dirk Kuch
 */
public final class CopyObservationTargetFactory
{
  private CopyObservationTargetFactory( )
  {

  }

  public static ICopyObservationTarget getLink( final URL context, final String hrefTargetObservation, final File targetObservationDir, final DateRange targetRange, final DateRange forecastRange )
  {
    if( hrefTargetObservation != null && !hrefTargetObservation.isEmpty() )
    {
      /** brrr ugly code - necessary because of refactoring! don't assume it, proof it! */
      if( targetObservationDir != null )
        throw new IllegalStateException();

      return getLink( context, hrefTargetObservation, targetRange, forecastRange );
    }
    else if( context != null && targetObservationDir != null )
    {
      /** brrr ugly code - necessary because of refactoring! don't assume it, proof it! */
      if( hrefTargetObservation != null )
        throw new IllegalStateException();

      return getLink( context, targetObservationDir, targetRange, forecastRange );
    }

    throw new NotImplementedException();
  }

  public static ICopyObservationTarget getLink( final URL context, final File targetObservationDir, final DateRange targetRange, final DateRange forecastRange )
  {
    return new CopyObservationNATarget( context, targetObservationDir, targetRange, forecastRange );
  }

  public static ICopyObservationTarget getLink( final URL context, final String hrefTargetObservation, final DateRange targetRange, final DateRange forecastRange )
  {
    return new CopyObservationTarget( context, hrefTargetObservation, targetRange, forecastRange );
  }

}
