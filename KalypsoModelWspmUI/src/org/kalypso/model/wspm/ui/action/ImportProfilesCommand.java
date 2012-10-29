package org.kalypso.model.wspm.ui.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.kalypso.commons.command.ICommand;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.ProfileFeatureBinding;
import org.kalypso.model.wspm.core.gml.WspmWaterBody;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public final class ImportProfilesCommand implements ICommand
{
  private final IProfile[] m_profiles;

  private final FeatureList m_profileList;

  private final GMLWorkspace m_workspace;

  private final WspmWaterBody m_water;

  private Feature[] m_addedFeatures = null;

  public ImportProfilesCommand( final WspmWaterBody water, final IProfile[] profiles )
  {
    m_water = water;
    m_workspace = m_water.getWorkspace();
    m_profiles = profiles;
    m_profileList = (FeatureList) m_water.getProperty( WspmWaterBody.MEMBER_PROFILE );
  }

  @Override
  public String getDescription( )
  {
    return org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.WspmImportProfileHelper.0" ); //$NON-NLS-1$
  }

  @Override
  public boolean isUndoable( )
  {
    return true;
  }

  @Override
  public void process( ) throws Exception
  {
    final List<Feature> newFeatureList = new ArrayList<>();
    try
    {
      for( final IProfile profile : m_profiles )
      {
        final IProfileFeature feature = m_water.createNewProfile();
        ((ProfileFeatureBinding) feature).setProfile( profile );

        newFeatureList.add( feature );
      }
    }
    finally
    {
      m_addedFeatures = newFeatureList.toArray( new Feature[newFeatureList.size()] );
      final ModellEvent event = new FeatureStructureChangeModellEvent( m_workspace, m_water, m_addedFeatures, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD );
      m_workspace.fireModellEvent( event );
    }
  }

  @Override
  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public void redo( ) throws Exception
  {
    m_profileList.addAll( Arrays.asList( m_addedFeatures ) );
    final ModellEvent event = new FeatureStructureChangeModellEvent( m_workspace, m_water, m_addedFeatures, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_ADD );
    m_workspace.fireModellEvent( event );
  }

  @Override
  @SuppressWarnings("unchecked")//$NON-NLS-1$
  public void undo( ) throws Exception
  {
    m_profileList.removeAll( Arrays.asList( m_addedFeatures ) );
    final ModellEvent event = new FeatureStructureChangeModellEvent( m_workspace, m_water, m_addedFeatures, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_DELETE );
    m_workspace.fireModellEvent( event );
  }
}