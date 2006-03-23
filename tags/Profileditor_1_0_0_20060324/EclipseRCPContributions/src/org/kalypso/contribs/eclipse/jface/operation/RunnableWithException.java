package org.kalypso.contribs.eclipse.jface.operation;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author belger
 */
public abstract class RunnableWithException implements IRunnableWithProgress
{
  public abstract void runWithException( final IProgressMonitor monitor ) throws Throwable;
  
  public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
  {
    try
    {
      runWithException( monitor );
    }
    catch( final InterruptedException ie )
    {
      throw ie;
    }
    catch( final Throwable target )
    {
      throw new InvocationTargetException( target );
    }
  }
}
