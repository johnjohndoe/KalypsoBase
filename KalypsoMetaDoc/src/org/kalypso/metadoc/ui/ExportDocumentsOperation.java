package org.kalypso.metadoc.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.metadoc.IExportTarget;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.KalypsoMetaDocPlugin;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;

/**
 * @author Gernot Belger
 *
 */
final class ExportDocumentsOperation extends WorkspaceModifyOperation
{
  private final IExportableObjectFactory m_factory;

  private final IPublishingConfiguration m_configuration;

  private final IExportTarget m_target;

  ExportDocumentsOperation( final IExportableObjectFactory factory, final IPublishingConfiguration configuration, final IExportTarget target )
  {
    m_factory = factory;
    m_configuration = configuration;
    m_target = target;
  }

  @Override
  protected void execute( final IProgressMonitor monitor ) throws CoreException, InterruptedException
  {
    final IStatusCollector stati = new StatusCollector( KalypsoMetaDocPlugin.getId() );

    try
    {
      final IExportableObject[] objects = m_factory.createExportableObjects( m_configuration );

      monitor.beginTask( "Export", objects.length );

      for( final IExportableObject exportableObject : objects )
      {
        if( monitor.isCanceled() )
          throw new InterruptedException();

        monitor.subTask( exportableObject.getPreferredDocumentName() );

        try
        {
          final IStatus status = m_target.commitDocument( exportableObject, m_configuration, new SubProgressMonitor( monitor, 1 ) );
          stati.add( status );
          KalypsoMetaDocPlugin.getDefault().getLog().log( status );
        }
        catch( final Exception e )
        {
          final String message = String.format( "Fehler beim Dokumentenexport: %s", exportableObject.getPreferredDocumentName() );

          final IStatus status = stati.add( IStatus.ERROR, message, e );
          KalypsoMetaDocPlugin.getDefault().getLog().log( status );
        }
      }
    }
    catch( final CoreException e )
    {
      KalypsoMetaDocPlugin.getDefault().getLog().log( e.getStatus() );

      stati.add( e.getStatus() );
    }
    finally
    {
      monitor.done();
    }

    final IStatus status;
    if( stati.size() == 0 )
      status = new Status( IStatus.INFO, KalypsoMetaDocPlugin.getId(), 0, "Es wurden keine Dokumente erzeugt.", null );
    else
      status = StatusUtilities.createStatus( stati, "Siehe Details" );
    if( !status.isOK() )
      throw new CoreException( status );
  }
}