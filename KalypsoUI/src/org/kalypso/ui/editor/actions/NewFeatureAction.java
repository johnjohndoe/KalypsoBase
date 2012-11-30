/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 *
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 *
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 *
 * and
 *
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact:
 *
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 *
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ui.editor.actions;

import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Event;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.catalogs.FeatureTypeImageCatalog;
import org.kalypso.ui.catalogs.FeatureTypePropertiesCatalog;
import org.kalypso.ui.catalogs.IFeatureTypePropertiesConstants;
import org.kalypso.ui.editor.gmleditor.command.AddFeatureCommand;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author kuepfer
 */
public class NewFeatureAction extends Action
{
  private final IFeatureType m_featureType;

  private final CommandableWorkspace m_workspace;

  private final Feature m_parentFeature;

  private final IRelationType m_targetRelation;

  private final IFeatureSelectionManager m_selectionManager;

  public NewFeatureAction( final CommandableWorkspace workspace, final Feature parentFeature, final IRelationType targetRelation, final IFeatureType featureType, final IFeatureSelectionManager selectionManager )
  {
    m_workspace = workspace;
    m_parentFeature = parentFeature;
    m_targetRelation = targetRelation;
    m_featureType = featureType;
    m_selectionManager = selectionManager;

    final String actionLabel = FeatureActionUtilities.newFeatureActionLabel( featureType );
    setText( actionLabel );

    setImageDescriptor( createImage() );
  }

  private ImageDescriptor createImage( )
  {
    final ImageDescriptor catalogDescriptor = FeatureTypeImageCatalog.getImage( null, m_featureType.getQName() );

    if( catalogDescriptor == null )
      return ImageProvider.IMAGE_FEATURE_NEW;

    return catalogDescriptor;
  }

  @Override
  public void runWithEvent( final Event event )
  {
    try
    {
      createNewFeature();
    }
    catch( final Exception e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      ErrorDialog.openError( event.widget.getDisplay().getActiveShell(), getText(), Messages.getString( "org.kalypso.ui.editor.gmleditor.part.NewFeatureAction.0" ), status ); //$NON-NLS-1$
    }
  }

  void createNewFeature( ) throws Exception
  {
    final Properties uiProperties = FeatureTypePropertiesCatalog.getProperties( m_workspace.getContext(), m_featureType.getQName() );
    final String depthStr = uiProperties.getProperty( IFeatureTypePropertiesConstants.FEATURE_CREATION_DEPTH );

    final int depth = NumberUtils.parseQuietInt( depthStr, 0 );

    final ICommand command = new AddFeatureCommand( m_workspace, m_featureType, m_parentFeature, m_targetRelation, 0, null, m_selectionManager, depth );
    m_workspace.postCommand( command );
  }
}