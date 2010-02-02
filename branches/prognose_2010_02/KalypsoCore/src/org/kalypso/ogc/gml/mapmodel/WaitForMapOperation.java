package org.kalypso.ogc.gml.mapmodel;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;

/**
 * @author Gernot Belger
 */
public final class WaitForMapOperation implements ICoreRunnableWithProgress
{
  private final Object m_panelOrModell;

  public WaitForMapOperation( final Object panelOrModell )
  {
    Assert.isTrue( panelOrModell instanceof IMapModell || panelOrModell instanceof IMapPanel );
    m_panelOrModell = panelOrModell;
  }

  public WaitForMapOperation( final IMapModell model )
  {
    m_panelOrModell = model;
  }

  public WaitForMapOperation( final IMapPanel panel )
  {
    m_panelOrModell = panel;
  }

  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.mapmodel.MapModellHelper.1" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

    Thread.sleep( 250 );

    while( true )
    {
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      try
      {
        final IMapModell modell = getMapModell();
        if( MapModellHelper.isMapLoaded( modell ) )
          return Status.OK_STATUS;

        Thread.sleep( 250 );

        monitor.worked( 10 );
      }
      catch( final InterruptedException e )
      {
        return StatusUtilities.statusFromThrowable( e );
      }
    }
  }

  private IMapModell getMapModell( )
  {
    if( m_panelOrModell instanceof IMapPanel )
      return ((IMapPanel) m_panelOrModell).getMapModell();

    if( m_panelOrModell instanceof IMapModell )
      return (IMapModell) m_panelOrModell;

    throw new IllegalArgumentException();
  }
}