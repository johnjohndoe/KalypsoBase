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
package org.kalypso.gml.ui.internal.feature;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.gmleditor.command.MoveFeatureCommand;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public abstract class MoveFeatureHandler extends AbstractHandler
{
  private final int m_step;

  public MoveFeatureHandler( final int step )
  {
    m_step = step;
  }

  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );
    if( !(selection instanceof IFeatureSelection) )
      throw new ExecutionException( "Handler only works on IFeatureSelection, check enablement" ); //$NON-NLS-1$

    final IFeatureSelection featureSelection = (IFeatureSelection)selection;

    final Object firstElement = featureSelection.getFirstElement();
    if( !(firstElement instanceof Feature) )
      return null;

    final Feature feature = FeatureSelectionHelper.getFirstFeature( featureSelection );
    final CommandableWorkspace workspace = featureSelection.getWorkspace( feature );

    if( feature == null || workspace == null )
      return null;

    final IRelationType rt = feature.getParentRelation();
    final Feature parent = feature.getOwner();
    if( rt == null || parent == null || !rt.isList() )
      return null;

    final MoveFeatureCommand command = new MoveFeatureCommand( parent, rt, feature, m_step );
    try
    {
      workspace.postCommand( command );
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoGmlUIPlugin.id(), StringUtils.EMPTY, e );
      KalypsoGisPlugin.getDefault().getLog().log( status );

      // we are in the ui-thread so we get a shell here
      final Shell shell = HandlerUtil.getActiveShell( event );
      final String title = HandlerUtils.getCommandName( event );
      ErrorDialog.openError( shell, title, Messages.getString( "org.kalypso.ui.editor.actions.AbstractFeatureListElementMoveActionDelegate.1" ), status ); //$NON-NLS-1$
    }

    return null;
  }
}