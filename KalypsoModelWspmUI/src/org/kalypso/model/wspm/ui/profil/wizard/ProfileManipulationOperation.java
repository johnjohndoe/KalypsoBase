package org.kalypso.model.wspm.ui.profil.wizard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.contribs.eclipse.ui.plugin.AbstractUIPluginExt;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.base.IProfileManipulator;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperation;
import org.kalypso.model.wspm.core.profil.operation.ProfilOperationJob;
import org.kalypso.model.wspm.ui.i18n.Messages;

/**
 * @author Gernot Belger
 */
public final class ProfileManipulationOperation implements ICoreRunnableWithProgress
{

  private final Object[] m_profileFeatures;

  private final IProfileManipulator m_manipulator;

  private final String m_windowTitle;

  private final IWizardContainer m_wizardContainer;

  Set<ProfilOperation> m_profileOperations = new LinkedHashSet<>();

  public ProfileManipulationOperation( final IWizardContainer wizardContainer, final String windowTitle, final Object[] profileFeatures, final IProfileManipulator manipulator )
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

    final Collection<IStatus> problems = new ArrayList<IStatus>();
    for( final Object profileObject : m_profileFeatures )
    {
      try
      {
        final IProfileFeature profileFeature = (IProfileFeature) profileObject;
        final IProfil profile = profileFeature.getProfil();

        final String subTask = String.format( Messages.getString( "ProfileManipulationOperation_1" ), profileFeature.getName(), profileFeature.getBigStation() ); //$NON-NLS-1$
        monitor.subTask( subTask );

        final ProfilOperation operation = new ProfilOperation( "Guessing rouhness classes", profile, true );
        final IProfilChange[] changes = m_manipulator.performProfileManipulation( profile, new SubProgressMonitor( monitor, 1 ) );
        operation.addChange( changes );

        m_profileOperations.add( operation );
      }
      catch( final CoreException e )
      {
        problems.add( e.getStatus() );
      }

      if( monitor.isCanceled() )
        throw new InterruptedException();
    }

    if( problems.size() == 0 )
      return Status.OK_STATUS;

    final IStatus[] problemStati = problems.toArray( new IStatus[problems.size()] );
    return new MultiStatus( AbstractUIPluginExt.ID, 0, problemStati, Messages.getString( "ProfileManipulationOperation_2" ), null ); //$NON-NLS-1$
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
    for( final ProfilOperation operation : m_profileOperations )
    {
      new ProfilOperationJob( operation ).schedule();
    }

    return true;
  }
}