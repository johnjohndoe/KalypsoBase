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
package org.kalypso.gml.ui.internal.gmltable;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.util.AddFeatureHandlerUtil;
import org.kalypso.ui.editor.gistableeditor.command.GmlTableHandlerUtils;
import org.kalypso.ui.editor.gmleditor.command.AddFeatureCommand;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

/**
 * Command handler that implements adding a new feature into a list property.
 *
 * @author Gernot Belger
 */
public class AddFeatureHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final String dialogTitle = HandlerUtils.getCommandName( event );

    final ISelection selection = HandlerUtil.getCurrentSelection( event );

    final IFeatureSelectionManager selectionManager = getSelectionManager( selection );

    final LayerTableViewer tableViewer = GmlTableHandlerUtils.getTableViewerChecked( event );

    final ILayerTableInput input = tableViewer.getInput();
    if( input == null )
      throw new ExecutionException( "Table contains no data" ); //$NON-NLS-1$

    final FeatureList featureProperty = input.getFeatureList();

    if( !AddFeatureHandlerUtil.checkPrecondition( shell, featureProperty ) )
      return null;

    /* Do add the feature */
    final CommandableWorkspace workspace = input.getWorkspace();
    final IRelationType parentRelation = featureProperty.getPropertyType();
    final Feature parentFeature = featureProperty.getOwner();

    final IFeatureType targetFeatureType = AddFeatureHandlerUtil.chooseFeatureType( shell, dialogTitle, parentRelation, workspace );
    if( targetFeatureType == null )
      return null;

    final int position = findPosition( selection, featureProperty );

    final AddFeatureCommand command = new AddFeatureCommand( workspace, targetFeatureType, parentFeature, parentRelation, position, null, selectionManager, -1 );
    input.getCommandTarget().postCommand( command, null );

    return null;
  }

  private int findPosition( final ISelection selection, final FeatureList featureProperty )
  {
    if( !(selection instanceof IStructuredSelection) )
      return -1;

    final IStructuredSelection structuredSelection = (IStructuredSelection) selection;

    final Object selectedElement = structuredSelection.getFirstElement();

    return featureProperty.indexOf( selectedElement );
  }

  private IFeatureSelectionManager getSelectionManager( final ISelection selection )
  {
    if( selection instanceof IFeatureSelectionManager )
      return (IFeatureSelectionManager) selection;

    return null;
  }
}