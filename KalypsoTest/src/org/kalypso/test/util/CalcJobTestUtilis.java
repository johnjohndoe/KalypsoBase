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
package org.kalypso.test.util;

import java.io.File;

import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.simulation.core.ISimulationMonitor;
import org.kalypso.simulation.core.ISimulationResultEater;

/**
 * TODO: insert type comment here
 * 
 * @author doemming
 */
public class CalcJobTestUtilis
{

  public static ISimulationResultEater createResultEater( )
  {
    return new ISimulationResultEater()
    {
      /**
       * @see org.kalypso.simulation.core.ISimulationResultEater#addResult(java.lang.String, java.lang.Object)
       */
      @Override
      public void addResult( final String id, final Object result )
      {
        System.out.println( "ID" + id + " File:" + ((File) result).getAbsolutePath() );
      }
    };
  }

  public static ISimulationMonitor createMonitor( )
  {
    return new ISimulationMonitor()
    {
      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#cancel()
       */
      @Override
      public void cancel( )
      {
        //
      }

      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#isCanceled()
       */
      @Override
      public boolean isCanceled( )
      {
        return false;
      }

      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#setProgress(int)
       */
      @Override
      public void setProgress( final int progress )
      {
        //
      }

      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#getProgress()
       */
      @Override
      public int getProgress( )
      {
        return 0;
      }

      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#getMessage()
       */
      @Override
      public String getMessage( )
      {
        return null;
      }

      /**
       * @see org.kalypso.services.calculation.job.ICalcMonitor#setMessage(java.lang.String)
       */
      @Override
      public void setMessage( final String message )
      {
        System.out.println( message + "\n" );
      }

      @Override
      public void setFinishInfo( final int status, final String text )
      {
        // TODO Auto-generated method stub

      }

      @Override
      public String getFinishText( )
      {
        // TODO Auto-generated method stub
        return null;
      }

      @Override
      public int getFinishStatus( )
      {
        // TODO Auto-generated method stub
        return 0;
      }
    };

  }

  public static File getTmpDir( )
  {
    final File file = FileUtilities.createNewTempDir( "NA_TEST", new File( "C:\\tmp" ) );
    file.mkdirs();
    return file;
  }

}
