/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import org.kalypso.model.wspm.core.gml.ProfileFeatureProvider;
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
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * @author Gernot Belger
 */
public class FeatureSelectionProfileProvider extends AbstractProfilProvider implements ISelectionChangedListener, IProfilListener, ModellEventListener
{
  /**
   * @see org.kalypso.model.wspm.ui.profil.IProfilProvider#getProfil()
   */
  @Override
  public IProfil getProfil( )
  {
    return m_profile;
  }

  private final ISelectionProvider m_provider;

  private IFile m_file;

  private IProfil m_profile = null;

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

  /* find all results connected to this water */
// private IStationResult[] findResults( final IProfileFeature profileMember )
// {
// final WspmWaterBody water = profileMember.getWater();
// if( water == null )
// return new IStationResult[] {};
//
// final GMLWorkspace workspace = water.getFeature().getWorkspace();
//
// final List<IStationResult> results = new ArrayList<IStationResult>();
//
// /* Waterlevel fixations */
// final List< ? > wspFixations = water.getWspFixations();
// for( final Object wspFix : wspFixations )
// {
// final Feature feature = FeatureHelper.getFeature( workspace, wspFix );
//
// final IStationResult result = new ObservationStationResult( feature, profileMember.getStation() );
// results.add( result );
// }
//
// /* Calculated results. */
// // TRICKY: this depends currently on the concrete model
// // so we need to know the model-type (such as tuhh) and
// // delegate the search for results to model-specific code.
// return results.toArray( new IStationResult[results.size()] );
// }

  /**
   * @see org.kalypso.model.wspm.ui.profil.IProfilProvider#getEventManager()
   */
  public IProfil getProfile( )
  {
    return m_profile;
  }

  /**
   * @see com.bce.profil.ui.view.IProfilProvider2#getFile()
   */
  public IFile getFile( )
  {
    return m_file;
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
    if( m_profile != null && m_feature != null )
    {
      try
      {
        if( hint.isObjectChanged() || hint.isObjectDataChanged() || hint.isMarkerDataChanged() || hint.isMarkerMoved() || hint.isPointPropertiesChanged() || hint.isPointsChanged()
            || hint.isPointValuesChanged() || hint.isProfilPropertyChanged() )
        {
          final FeatureChange[] featureChanges = ProfileFeatureFactory.getFeatureChanges( m_profile, m_feature );

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
    final EasyFeatureWrapper[] features = fs.getAllFeatures();

    IProfileFeature profileMember = null;
    Object result = null;
    try
    {
      for( final EasyFeatureWrapper eft : features )
      {
        final Feature feature = eft.getFeature();

        if( feature != null )
        {
          profileMember = ProfileFeatureProvider.findProfile( feature );

          if( profileMember != null )
          {
            result = ProfileFeatureProvider.findResultNode( feature );

            // HACK: If type not set, force it to be the tuhh-profile. We need this, as tuhh-profile are created via
            // the gml-tree which knows nothing about profiles... Everyone else should create profile programatically
            // and directly set the prefered type.
            if( profileMember.getProfileType() == null )
              profileMember.setProfileType( "org.kalypso.model.wspm.tuhh.profiletype" ); //$NON-NLS-1$

            break;
          }
        }
      }
    }
    catch( final Exception e )
    {
      final KalypsoModelWspmUIPlugin wspmPlugin = KalypsoModelWspmUIPlugin.getDefault();
      wspmPlugin.getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }

    // Check if this is the current feature, if true, do not set the profile agin
    if( ObjectUtils.equals( m_feature, profileMember ) )
      return;

    if( profileMember == null )
    {
      setProfile( null, null, null );
      return;
    }

    final CommandableWorkspace workspace = fs.getWorkspace( profileMember );
    final URL workspaceContext = workspace == null ? null : workspace.getContext();
    m_file = workspaceContext == null ? null : ResourceUtilities.findFileFromURL( workspaceContext );

    setProfile( profileMember, workspace, result );
  }

  private void setProfile( final IProfileFeature feature, final CommandableWorkspace workspace, Object result )
  {
    final IProfil oldProfile = m_profile;

    unhookListeners();

    m_feature = feature;
    m_workspace = workspace;
    m_result = result;

    m_profile = feature == null ? null : feature.getProfil();

    if( m_profile != null )
    {
      m_profile.addProfilListener( this );

      if( m_profile != null && m_file != null )
      {
        m_profilValidator = new ValidationProfilListener( m_profile, m_file, null, m_feature.getId() );

        m_profile.addProfilListener( m_profilValidator );
      }
    }

    if( m_feature != null )
      m_feature.getWorkspace().addModellListener( this );

    fireOnProfilProviderChanged( this, oldProfile, m_profile );
  }

  private void unhookListeners( )
  {
    if( m_feature != null )
    {
      m_feature.getWorkspace().removeModellListener( this );
      m_feature = null;
    }

    if( m_profile != null )
    {
      if( m_profilValidator != null )
      {
        m_profile.removeProfilListener( m_profilValidator );
        m_profilValidator.dispose();
        m_profilValidator = null;
      }

      m_profile.removeProfilListener( this );
      m_profile = null;
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
