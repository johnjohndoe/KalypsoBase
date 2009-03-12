package org.kalypso.util.command;

import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.ui.KalypsoGisPlugin;

/** Job to process / undo / redo a ICommand 
 * Wird sofort gestartet  
 */
public final class CommandJob extends Job
{
  public final static TYPE POST = new TYPE( "POST" );

  public final static TYPE UNDO = new TYPE( "UNDO" );

  public final static TYPE REDO = new TYPE( "REDO" );

  private final ICommand myCommand;

  private final ICommandManager myCommandManager;

  private static final Logger LOGGER = Logger.getLogger( CommandJob.class.getName() );

  private final Runnable myRunnable;

  private final TYPE m_type;

  public CommandJob( final ICommand command, final ICommandManager commandManager,
      final ISchedulingRule rule, final Runnable runnable, final TYPE type )
  {
    super( "Kalypso: " + getCommandDescription( commandManager, command, type ) );

    // Nein: dann bleiben die Jobs als 'Waiting' in der ProgressAnzeige
    //setUser( true );

    setPriority( Job.SHORT );
    setRule( rule );

    myCommand = command;
    myCommandManager = commandManager;
    myRunnable = runnable;
    m_type = type;

    schedule();
  }

  protected IStatus run( final IProgressMonitor monitor )
  {
    final String description = getCommandDescription( myCommandManager, myCommand, m_type );

    LOGGER.info( m_type.toString() + ": " + description );

    try
    {
      if( m_type == POST )
        myCommandManager.postCommand( myCommand );
      else if( m_type == UNDO )
        myCommandManager.undo();
      else if( m_type == REDO )
        myCommandManager.redo();

      if( myRunnable != null )
        myRunnable.run();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      
      LOGGER.warning( "Failed " + m_type + ": " + description );

      return new Status( IStatus.ERROR,
          KalypsoGisPlugin.getDefault().getBundle().getSymbolicName(), 0, "Fehler: " + m_type
              + ": " + description, e );
    }

    if( description == null )
      System.out.print(false);
    
    LOGGER.info( "Finished " + m_type + ": " + description );

    return Status.OK_STATUS;
  }

  private static String getCommandDescription( final ICommandManager cm, final ICommand c,
      final TYPE type )
  {
    if( type == POST )
      return c.getDescription();
    else if( type == UNDO )
      return cm.getUndoDescription();
    else if( type == REDO )
      return cm.getRedoDescription();

    return "";
  }

  private static final class TYPE
  {
    private final String m_name;

    public TYPE( final String name )
    {
      m_name = name;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
      return m_name;
    }
  }
}