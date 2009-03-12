package org.kalypso.services.calculation.job.impl;

import java.io.File;

import org.kalypso.services.calculation.service.CalcJobDataBean;
import org.kalypso.services.calculation.service.CalcJobServiceException;

/**
 * @author belger
 */
public final class CountJob extends AbstractCalcJob
{
  /**
   * @see org.kalypso.services.calculation.job.ICalcJob#run(java.io.File, org.kalypso.services.calculation.service.CalcJobDataBean[])
   */
  public void run( final File basedir, final CalcJobDataBean[] arguments ) throws CalcJobServiceException
  {
    while( !isCanceled() )
    {
      try
      {
        Thread.sleep( 500 );
      }
      catch( final InterruptedException e )
      {
        throw new CalcJobServiceException( "Thread interrupted", e );
      }

      final int progress = getProgress();
      if( progress == 100 )
        return;

      progress( 1 );
    }
  }
}