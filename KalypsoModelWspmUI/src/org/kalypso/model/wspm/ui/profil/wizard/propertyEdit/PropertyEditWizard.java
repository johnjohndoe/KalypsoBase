/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.wizard.ArrayChooserPage;
import org.kalypso.model.wspm.core.gml.ProfileFeatureBinding;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileManipulationOperation.IProfileManipulator;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.observation.result.IComponent;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;

/**
 * @author kimwerner
 */
public class PropertyEditWizard extends Wizard
{
  private final IPageChangedListener m_pageChangedListener = new IPageChangedListener()
  {
    @Override
    public void pageChanged( final PageChangedEvent event )
    {
      handlePageChanged( event.getSelectedPage() );
    }
  };

  final private ProfilesChooserPage m_profileChooserPage;

  private ArrayChooserPage m_propertyChooserPage;

  final private IProfil m_profile;

  private OperationChooserPage m_operationChooserPage;

  final private CommandableWorkspace m_workspace;

  public PropertyEditWizard( final ProfileSelection profileSelection )
  {
    m_profile = null;
    m_workspace = profileSelection.getWorkspace();

    final String message = Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.2" ); //$NON-NLS-1$
    m_profileChooserPage = new ProfilesChooserPage( message, profileSelection, false );
    addPage( m_profileChooserPage );

    init();
  }

  public PropertyEditWizard( final IProfil profile )
  {
    m_profile = profile;
    m_workspace = null;
    m_profileChooserPage = null;

    init();
  }

  private void init( )
  {
    setNeedsProgressMonitor( true );

    final String msg = Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.10" ); //$NON-NLS-1$
    m_operationChooserPage = new OperationChooserPage( msg );
    // FIxME: add table filter if we got a selection
    m_operationChooserPage.setDescription( Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.11" ) ); //$NON-NLS-1$

    m_propertyChooserPage = new ArrayChooserPage( null, null, null, 1, "profilePropertiesChooserPage", Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.PropertyEditWizard.8" ), null, true ); //$NON-NLS-1$ //$NON-NLS-2$
    m_propertyChooserPage.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
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
      ((IPageChangeProvider) oldContainer).removePageChangedListener( m_pageChangedListener );

    super.setContainer( wizardContainer );

    if( wizardContainer instanceof IPageChangeProvider )
      ((IPageChangeProvider) wizardContainer).addPageChangedListener( m_pageChangedListener );
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

    final ProfilChangeHint hint = new ProfilChangeHint();
    hint.setPointValuesChanged();
    m_profile.fireProfilChanged( hint, new IProfilChange[] { null } );

    return true;
  }

  private boolean performFinishMultipleProfiles( )
  {
    final Object[] choosenProperties = m_propertyChooserPage.getChoosen();
    final OperationChooserPage operationChooserPage = m_operationChooserPage;

    final Object[] profileFeatures = m_profileChooserPage.getChoosen();

    final IProfileManipulator manipulator = new IProfileManipulator()
    {
      @Override
      public void performProfileManipulation( final IProfil profile, final IProgressMonitor monitor )
      {
        monitor.beginTask( "", 1 ); //$NON-NLS-1$
        operationChooserPage.changeProfile( profile, choosenProperties );
        monitor.done();
      }
    };
    final ProfileManipulationOperation operation = new ProfileManipulationOperation( getContainer(), getWindowTitle(), profileFeatures, m_workspace, manipulator );
    return operation.perform();
  }

  protected void handlePageChanged( final Object selectedPage )
  {
    if( selectedPage == m_propertyChooserPage )
    {
      final Collection<IComponent> properties = new LinkedHashSet<IComponent>();
      final Object[] profiles = m_profile == null ? m_profileChooserPage.getChoosen() : new Object[] { m_profile };
      for( final Object object : profiles )
      {
        final IProfil profile;
        if( object instanceof IProfil )
          profile = (IProfil) object;
        else if( object instanceof ProfileFeatureBinding )
          profile = ((ProfileFeatureBinding) object).getProfil();
        else
          continue;

        for( final IComponent property : profile.getPointProperties() )
        {
          if( !profile.isPointMarker( property.getId() ) )
            properties.add( property );
        }
      }

      m_propertyChooserPage.setInput( properties );
    }
  }
}