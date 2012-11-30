/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.model.wspm.ui.profil.wizard.propertyEdit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureBinding;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;
import org.kalypso.model.wspm.core.profil.base.IProfileManipulator;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.action.ProfilesSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileHandlerUtils;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.observation.result.IComponent;

/**
 * @author Kim Werner
 */
public class PropertyEditWizard extends Wizard implements IWorkbenchWizard
{
  private final IPageChangedListener m_pageChangedListener = new IPageChangedListener()
  {
    @Override
    public void pageChanged( final PageChangedEvent event )
    {
      handlePageChanged( event.getSelectedPage() );
    }
  };

  private ProfilesChooserPage m_profileChooserPage;

  private ArrayChooserPage m_propertyChooserPage;

  private IProfile m_profile;

  private OperationChooserPage m_operationChooserPage;

  public PropertyEditWizard( )
  {
    // empty, needed for tools wizard
  }

  public PropertyEditWizard( final IProfile profile )
  {
    m_profile = profile;
    m_profileChooserPage = null;

    init();
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    m_profile = null;

    final ProfilesSelection profileSelection = ProfileHandlerUtils.getSelectionChecked( selection );

    final String message = Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.2" ); //$NON-NLS-1$
    m_profileChooserPage = new ProfilesChooserPage( message, profileSelection, false );
    addPage( m_profileChooserPage );

    init();
  }

  private void init( )
  {
    setNeedsProgressMonitor( true );
    setWindowTitle( Messages.getString( "org.kalypso.model.wspm.ui.action.PropertyEditActionDelegate.0" ) ); //$NON-NLS-1$

    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoModelWspmUIPlugin.getDefault(), getClass().getName() ) );

    final String msg = Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.10" ); //$NON-NLS-1$
    m_operationChooserPage = new OperationChooserPage( msg );
    // FIxME: add table filter if we got a selection
    m_operationChooserPage.setDescription( Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.11" ) ); //$NON-NLS-1$

    m_propertyChooserPage = new ArrayChooserPage( null, null, null, 1, "profilePropertiesChooserPage", Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.8" ), null, true ); //$NON-NLS-1$ //$NON-NLS-2$
    m_propertyChooserPage.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element instanceof IComponent )
          return ((IComponent) element).getName();

        return element.toString();
      }
    } );
    m_propertyChooserPage.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.9" ) ); //$NON-NLS-1$

    addPage( m_propertyChooserPage );
    addPage( m_operationChooserPage );
  }

  public void addFilter( final IProfilePointFilter filter )
  {
    m_operationChooserPage.addFilter( filter );
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#setContainer(org.eclipse.jface.wizard.IWizardContainer)
   */
  @Override
  public void setContainer( final IWizardContainer wizardContainer )
  {
    final IWizardContainer oldContainer = getContainer();
    if( oldContainer instanceof IPageChangeProvider )
    {
      ((IPageChangeProvider) oldContainer).removePageChangedListener( m_pageChangedListener );
    }

    super.setContainer( wizardContainer );

    if( wizardContainer instanceof IPageChangeProvider )
    {
      ((IPageChangeProvider) wizardContainer).addPageChangedListener( m_pageChangedListener );
    }
  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    if( m_profile == null )
      return performFinishMultipleProfiles();
    else
      return performFinishSingleProfile();
  }

  private boolean performFinishSingleProfile( )
  {
    final Object[] choosenProperties = m_propertyChooserPage.getChoosen();
    m_operationChooserPage.changeProfile( m_profile, choosenProperties );

    return true;
  }

  private boolean performFinishMultipleProfiles( )
  {
    final OperationChooserPage operationChooserPage = m_operationChooserPage;

    final Object[] properties = m_propertyChooserPage.getChoosen();

    final Set<IProfileFeature> profiles = new LinkedHashSet<>();

    final Object[] choosen = m_profileChooserPage.getChoosen();
    for( final Object object : choosen )
    {
      if( object instanceof IProfileFeature )
        profiles.add( (IProfileFeature) object );
    }

    final IProfileManipulator manipulator = new IProfileManipulator()
    {
      @Override
      public Pair<IProfileChange[], IStatus> performProfileManipulation( final IProfile profile, final IProgressMonitor monitor )
      {
        monitor.beginTask( "", 1 ); //$NON-NLS-1$
        operationChooserPage.changeProfile( profile, properties );
        monitor.done();

        return Pair.of( new IProfileChange[] {}, Status.OK_STATUS );
      }
    };

    final ProfileManipulationOperation operation = new ProfileManipulationOperation( getContainer(), getWindowTitle(), profiles.toArray( new IProfileFeature[] {} ), manipulator );
    return operation.perform();
  }

  protected void handlePageChanged( final Object selectedPage )
  {
    if( selectedPage == m_propertyChooserPage )
    {

      final Collection<IComponent> properties = new LinkedHashSet<>();
      final Object[] profiles = m_profile == null ? m_profileChooserPage.getChoosen() : new Object[] { m_profile };
      for( final Object object : profiles )
      {
        final IProfile profile = getProfile( object );
        if( Objects.isNull( profile ) )
          continue;

        final PropertyFilter filter = new PropertyFilter( profile );

        for( final IComponent property : profile.getPointProperties() )
        {
          if( filter.select( property ) )
            properties.add( property );
        }
      }

      m_propertyChooserPage.setInput( properties );
    }
  }

  private IProfile getProfile( final Object object )
  {
    if( object instanceof IProfile )
      return (IProfile) object;
    else if( object instanceof ProfileFeatureBinding )
      return ((ProfileFeatureBinding) object).getProfile();

    return null;
  }
}
