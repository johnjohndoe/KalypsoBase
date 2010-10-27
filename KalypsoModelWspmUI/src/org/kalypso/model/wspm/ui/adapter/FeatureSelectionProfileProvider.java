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
package org.kalypso.model.wspm.ui.adapter;

import java.net.URL;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureFactory;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.profil.AbstractProfilProvider;
import org.kalypso.model.wspm.ui.profil.validation.ValidationProfilListener;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * @author Gernot Belger
 */
public class FeatureSelectionProfileProvider extends AbstractProfilProvider implements ISelectionChangedListener, IProfilListener, ModellEventListener
{
  private final ISelectionProvider m_provider;

  private IFile m_file;

  private IProfileFeature m_feature;

  private CommandableWorkspace m_workspace;

  /** Flag to prevent update when source of model change is this */
  private boolean m_lockNextModelChange = false;

  private ValidationProfilListener m_profilValidator;

  private Object m_result;

  public FeatureSelectionProfileProvider( final ISelectionProvider provider )
  {
    m_provider = provider;

    if( m_provider != null )
    {
      m_provider.addSelectionChangedListener( this );
      final ISelection selection = m_provider.getSelection();
      if( selection == null )
        selectionChanged( new SelectionChangedEvent( m_provider, StructuredSelection.EMPTY ) );
      else
        selectionChanged( new SelectionChangedEvent( m_provider, selection ) );
    }
  }

  @Override
  public void dispose( )
  {
    m_provider.removeSelectionChangedListener( this );

    unhookListeners();
  }

  public ISelectionProvider getSelectionProvider( )
  {
    return m_provider;
  }

  /**
   * If the feature changes, write it back to the profile.
   * 
   * @see org.kalypsodeegree.model.feature.event.ModellEventListener#onModellChange(org.kalypsodeegree.model.feature.event.ModellEvent)
   */
  @Override
  public void onModellChange( final ModellEvent modellEvent )
  {
    if( m_lockNextModelChange )
    {
      m_lockNextModelChange = false;
      return;
    }

    // do no react to my own event, beware of recursion
    if( m_feature == null )
      return;

    if( modellEvent.isType( ModellEvent.FEATURE_CHANGE ) )
    {
      try
      {
        setProfile( m_feature, m_workspace, m_result );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
      }
    }
  }

  /**
   * If the profile changes, write it back to the feature.
   * 
   * @see com.bce.eind.core.profil.IProfilListener#onProfilChanged(com.bce.eind.core.profil.changes.ProfilChangeHint,
   *      com.bce.eind.core.profil.IProfilChange[])
   */
  @Override
  public void onProfilChanged( final ProfilChangeHint hint, final IProfilChange[] changes )
  {
    final IProfil profile = getProfil();
    if( profile != null && m_feature != null )
    {
      try
      {
        if( hint.isObjectChanged() || hint.isObjectDataChanged() || hint.isMarkerDataChanged() || hint.isMarkerMoved() || hint.isPointPropertiesChanged() || hint.isPointsChanged()
            || hint.isPointValuesChanged() || hint.isProfilPropertyChanged() )
        {
          final FeatureChange[] featureChanges = ProfileFeatureFactory.toFeatureAsChanges( profile, m_feature );

          final ChangeFeaturesCommand command = new ChangeFeaturesCommand( m_feature.getWorkspace(), featureChanges );
          m_lockNextModelChange = true;
          m_workspace.postCommand( command );
        }
      }
      catch( final Exception e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        KalypsoModelWspmUIPlugin.getDefault().getLog().log( status );
      }
    }
  }

  /**
   * @see org.kalypso.model.wspm.core.profil.IProfilListener#onProblemMarkerChanged(org.kalypso.model.wspm.core.profil.IProfil)
   */
  @Override
  public void onProblemMarkerChanged( final IProfil source )
  {
    // Nothing to do: this class is probably the source for the event anyway
  }

  /**
   * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
   */
  @Override
  public void selectionChanged( final SelectionChangedEvent event )
  {
    final ISelection selection = event.getSelection();
    if( !(selection instanceof IFeatureSelection) )
      return;

    final IFeatureSelection fs = (IFeatureSelection) selection;

    final ProfileAndResults profileAndResults = ProfileAndResults.search( fs );
    if( profileAndResults == null )
    {
      setProfile( null, null, null );
      return;
    }

    final IProfileFeature profile = profileAndResults.getProfile();

    // Check if this is the current feature, if true, do not set the profile again
    if( ObjectUtils.equals( m_feature, profile ) )
      return;

    final CommandableWorkspace workspace = fs.getWorkspace( profile );
    final URL workspaceContext = workspace == null ? null : workspace.getContext();
    m_file = workspaceContext == null ? null : ResourceUtilities.findFileFromURL( workspaceContext );

    final Object result = profileAndResults.getResult();
    setProfile( profile, workspace, result );
  }

  private void setProfile( final IProfileFeature feature, final CommandableWorkspace workspace, final Object result )
  {
    final IProfil oldProfile = getProfil();

    unhookListeners();

    m_feature = feature;
    m_workspace = workspace;
    m_result = result;

    final IProfil newProfile = feature == null ? null : feature.getProfil();

    if( newProfile != null )
    {
      newProfile.addProfilListener( this );

      if( newProfile != null && m_file != null )
      {
        m_profilValidator = new ValidationProfilListener( newProfile, m_file, null, m_feature.getId() );

        newProfile.addProfilListener( m_profilValidator );
      }
    }

    if( m_feature != null )
      m_feature.getWorkspace().addModellListener( this );

    setProfil( oldProfile, newProfile );
  }

  private void unhookListeners( )
  {
    if( m_feature != null )
    {
      m_feature.getWorkspace().removeModellListener( this );
      m_feature = null;
    }

    IProfil profile = getProfil();
    if( profile != null )
    {
      if( m_profilValidator != null )
      {
        profile.removeProfilListener( m_profilValidator );
        m_profilValidator.dispose();
        m_profilValidator = null;
      }

      profile.removeProfilListener( this );
      profile = null;
    }
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.IProfilProvider#getResult()
   */
  @Override
  public Object getResult( )
  {
    return m_result;
  }
}
