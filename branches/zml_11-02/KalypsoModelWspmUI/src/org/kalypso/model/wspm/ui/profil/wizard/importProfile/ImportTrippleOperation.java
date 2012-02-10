package org.kalypso.model.wspm.ui.profil.wizard.importProfile;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.model.wspm.core.imports.ImportTrippleHelper;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.action.ImportProfilesCommand;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author Gernot Belger
 */
final class ImportTrippleOperation implements ICoreRunnableWithProgress
{
  private final File m_trippelFile;

  private final String m_separator;

  private final String m_crs;

  private final WspmWaterBody m_water;

  private final CommandableWorkspace m_workspace;

  public ImportTrippleOperation( final File trippelFile, final String separator, final String crs, final WspmWaterBody water, final CommandableWorkspace workspace )
  {
    m_trippelFile = trippelFile;
    m_separator = separator;
    m_crs = crs;
    m_water = water;
    m_workspace = workspace;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    monitor.beginTask( Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.2" ), 2 ); //$NON-NLS-1$

    try
    {
      /* Import Trippel Data */
      monitor.subTask( Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.3" ) ); //$NON-NLS-1$

      /* get file name from wizard */

      final List<IProfil> profiles = ImportTrippleHelper.importTrippelData( m_trippelFile, m_separator, ImportProfileWizard.PROFIL_TYPE_PASCHE, m_crs );

      monitor.worked( 1 );

      /* Convert Trippel Data */
      monitor.subTask( Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.4" ) ); //$NON-NLS-1$

      final ImportProfilesCommand command = new ImportProfilesCommand( m_water, profiles );
      if( m_workspace != null )
        m_workspace.postCommand( command );

      monitor.worked( 1 );

      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.model.wspm.ui.wizard.ImportProfileWizard.5" ) ); //$NON-NLS-1$
    }

    finally
    {
      monitor.done();
    }
  }
}