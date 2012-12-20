package org.kalypso.model.wspm.ui.profil.wizard;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.base.IProfileManipulator;
import org.kalypso.model.wspm.core.profil.operation.ProfileOperation;
import org.kalypso.model.wspm.core.profil.operation.ProfileOperationJob;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class ProfileManipulationOperation implements ICoreRunnableWithProgress
{
  private final IProfileFeature[] m_profileFeatures;

  private final IProfileManipulator m_manipulator;

  private final String m_windowTitle;

  private final IWizardContainer m_wizardContainer;

  Set<ProfileOperation> m_profileOperations = new LinkedHashSet<>();

  public ProfileManipulationOperation( final IWizardContainer wizardContainer, final String windowTitle, final IProfileFeature[] profileFeatures, final IProfileManipulator manipulator )
  {
    m_wizardContainer = wizardContainer;
    m_windowTitle = windowTitle;
    m_profileFeatures = profileFeatures;
    m_manipulator = manipulator;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    monitor.beginTask( Messages.getString( "ProfileManipulationOperation_0" ), m_profileFeatures.length ); //$NON-NLS-1$

    final IStatusCollector problems = new StatusCollector( AbstractUIPluginExt.ID );

    for( final IProfileFeature profileFeature : m_profileFeatures )
    {
      try
      {
        final IProfile profile = profileFeature.getProfile();

        final String subTask = String.format( Messages.getString( "ProfileManipulationOperation_1" ), profileFeature.getName(), profileFeature.getBigStation() ); //$NON-NLS-1$
        monitor.subTask( subTask );

        final ProfileOperation operation = new ProfileOperation( "Performing profile operation", profile, true ); //$NON-NLS-1$

        final Pair<IProfileChange[], IStatus> result = m_manipulator.performProfileManipulation( profile, new SubProgressMonitor( monitor, 1 ) );
        final IProfileChange[] changes = result.getKey();
        operation.addChange( changes );

        m_profileOperations.add( operation );

        final IStatus status = result.getValue();

        final BigDecimal station = profileFeature.getBigStation();
        final String messageWithStation = Messages.getString( "ProfileManipulationOperation_3", station, status.getMessage() ); //$NON-NLS-1$

        final IStatus statusWithStation = StatusUtilities.cloneStatus( status, messageWithStation );

        problems.add( statusWithStation );
      }
      catch( final CoreException e )
      {
        problems.add( e.getStatus() );
      }

      if( monitor.isCanceled() )
        throw new InterruptedException();
    }

    return problems.asMultiStatusOrOK( Messages.getString( "ProfileManipulationOperation_2" ) ); //$NON-NLS-1$
  }

  public boolean perform( )
  {
    final IStatus result = RunnableContextHelper.execute( m_wizardContainer, true, true, this );

    final boolean doContinue = checkResult( result );
    if( doContinue )
      return applyChanges();

    return true;
  }

  private boolean checkResult( final IStatus result )
  {
    if( result.isOK() )
      return true;

    final String[] buttonLabels = { IDialogConstants.PROCEED_LABEL, IDialogConstants.ABORT_LABEL };
    final Shell shell = m_wizardContainer.getShell();
    final StatusDialog statusDialog = new StatusDialog( shell, result, m_windowTitle, buttonLabels, 0 );
    return statusDialog.open() == 0;
  }

  private boolean applyChanges( )
  {
    final ProfileOperationJob job = new ProfileOperationJob( m_profileOperations.toArray( new ProfileOperation[] {} ) );
    job.schedule();

    return true;
  }
}