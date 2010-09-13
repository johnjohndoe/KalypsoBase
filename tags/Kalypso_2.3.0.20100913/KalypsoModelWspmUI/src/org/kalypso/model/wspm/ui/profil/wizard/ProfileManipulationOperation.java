package org.kalypso.model.wspm.ui.profil.wizard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureFactory;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.util.swt.StatusDialog;
import org.kalypsodeegree_impl.model.feature.AbstractCachedFeature2;

/**
 * @author Gernot Belger
 */
public final class ProfileManipulationOperation implements ICoreRunnableWithProgress
{
  public interface IProfileManipulator
  {
    void performProfileManipulation( IProfil profile, IProgressMonitor monitor ) throws CoreException;
  }

  private final Object[] m_profileFeatures;

  private final List<FeatureChange> m_featureChanges = new ArrayList<FeatureChange>();

  private final IProfileManipulator m_manipulator;

  private final String m_windowTitle;

  private final IWizardContainer m_wizardContainer;

  private final CommandableWorkspace m_workspace;

  public ProfileManipulationOperation( final IWizardContainer wizardContainer, final String windowTitle, final Object[] profileFeatures, final CommandableWorkspace workspace, final IProfileManipulator manipulator )
  {
    m_wizardContainer = wizardContainer;
    m_windowTitle = windowTitle;
    m_profileFeatures = profileFeatures;
    m_workspace = workspace;
    m_manipulator = manipulator;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    monitor.beginTask( "Changing Profiles", m_profileFeatures.length );

    final Collection<IStatus> problems = new ArrayList<IStatus>();
    for( final Object profileObject : m_profileFeatures )
    {
      try
      {
        final IProfileFeature profileFeature = (IProfileFeature) profileObject;

        final String subTask = String.format( "%s (km %s)", profileFeature.getName(), profileFeature.getBigStation() );
        monitor.subTask( subTask );

        final IProfil profile = profileFeature.getProfil();
        m_manipulator.performProfileManipulation( profile, new SubProgressMonitor( monitor, 1 ) );

        m_featureChanges.addAll( Arrays.asList( ProfileFeatureFactory.toFeatureAsChanges( profile, profileFeature ) ) );
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
    return new MultiStatus( KalypsoModelWspmUIPlugin.ID, 0, problemStati, "Problems occured while changing the profiles. Continue anyways?", null );
  }

  public boolean perform( )
  {
    final IStatus result = RunnableContextHelper.execute( m_wizardContainer, true, true, this );

    final boolean doContinue = checkResult( result );
    if( doContinue )
      return applyChanges();
    else
      return revertChanges();
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
    final FeatureChange[] featureChanges = m_featureChanges.toArray( new FeatureChange[0] );
    final ChangeFeaturesCommand command = new ChangeFeaturesCommand( m_workspace, featureChanges );
    try
    {
      m_workspace.postCommand( command );
      return true;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      final Shell shell = m_wizardContainer.getShell();
      ErrorDialog.openError( shell, m_windowTitle, "Failed to apply profile changes", status );
      return false;
    }
  }

  private boolean revertChanges( )
  {
    for( final Object profileObject : m_profileFeatures )
    {
      final IProfileFeature profileFeature = (IProfileFeature) profileObject;
      // TODO: introduce ICachedFeature interface or similar?
      if( profileFeature instanceof AbstractCachedFeature2 )
        ((AbstractCachedFeature2) profileFeature).clearCachedProperties();
      // TODO: fire change event as well?
    }

    return false;
  }
}