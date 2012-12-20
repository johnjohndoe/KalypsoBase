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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.kalypso.commons.command.EmptyCommand;
import org.kalypso.contribs.eclipse.core.commands.HandlerUtils;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureRelation;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree_impl.model.feature.FeatureComparator;

/**
 * TODO: we should convert this to a wizard in the tools-wizards-wizard
 *
 * @author Gernot Belger
 */
public class SortFeaturesHandler extends AbstractHandler
{
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final ISelection selection = HandlerUtil.getCurrentSelectionChecked( event );
    final Shell shell = HandlerUtil.getActiveShellChecked( event );
    final String title = HandlerUtils.getCommandName( event );

    if( !(selection instanceof IStructuredSelection) )
      return null;

    final IStructuredSelection structSel = (IStructuredSelection) selection;
    final Object firstElement = structSel.getFirstElement();
    if( !(firstElement instanceof IFeatureRelation) )
      throw new ExecutionException( "This handler works only for selection on IFeatureProperty, check enablement" ); //$NON-NLS-1$

    final IFeatureRelation fate = (IFeatureRelation) firstElement;

    final Feature parentFeature = fate.getOwner();
    final IRelationType rt = fate.getPropertyType();

    final IFeatureType targetFeatureType = rt.getTargetFeatureType();
    final IPropertyType ptToSort = askForPropertyToSort( shell, targetFeatureType, title );
    if( ptToSort == null )
      return null;

    final CommandableWorkspace commandTarget = findCommandTarget( selection, parentFeature );
    final FeatureList profiles = (FeatureList) parentFeature.getProperty( rt );
    try
    {
      sort( profiles, ptToSort, commandTarget );
    }
    catch( final Throwable e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      ErrorDialog.openError( shell, title, Messages.getString( "org.kalypso.ui.editor.actions.SortFeaturesActionDelegate.1" ), status ); //$NON-NLS-1$
    }

    return null;
  }

  private CommandableWorkspace findCommandTarget( final ISelection selection, final Feature parentFeature )
  {
    if( selection instanceof IFeatureSelection )
    {
      final IFeatureSelection fs = (IFeatureSelection) selection;
      return fs.getWorkspace( parentFeature );
    }

    return null;
  }

  private IPropertyType askForPropertyToSort( final Shell shell, final IFeatureType ft, final String title )
  {
    final IPropertyType[] properties = ft.getProperties();
    final List<IPropertyType> props = new ArrayList<>( properties.length );
    for( final IPropertyType type : properties )
    {
      if( type instanceof IValuePropertyType )
        props.add( type );
    }

    if( props.size() == 0 )
    {
      MessageDialog.openInformation( shell, title, Messages.getString( "org.kalypso.ui.editor.actions.SortFeaturesActionDelegate.2" ) ); //$NON-NLS-1$
      return null;
    }

    final ListDialog dialog = new ListDialog( shell );
    dialog.setTitle( title );
    dialog.setMessage( Messages.getString( "org.kalypso.ui.editor.actions.SortFeaturesActionDelegate.3" ) ); //$NON-NLS-1$
    dialog.setAddCancelButton( true );
    // dialog.setDialogBoundsSettings( settings, strategy );

    dialog.setContentProvider( new ArrayContentProvider() );
    dialog.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final IAnnotation annotation = ((IPropertyType) element).getAnnotation();
        return annotation.getLabel();
      }
    } );
    dialog.setInput( props );
    dialog.setInitialSelections( new Object[] { props.get( 0 ) } );
    dialog.create();
    dialog.getTableViewer().setSorter( new ViewerSorter() );

    if( dialog.open() != Window.OK )
      return null;

    final Object[] result = dialog.getResult();
    if( result == null || result.length == 0 )
      return null;

    return (IPropertyType) result[0];
  }

  private void sort( final FeatureList list, final IPropertyType propertyToSort, final CommandableWorkspace commandTarget )
  {
    if( !(propertyToSort instanceof IValuePropertyType) )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ui.editor.actions.SortFeaturesActionDelegate.5" ) ); //$NON-NLS-1$

    final Comparator<Object> featureComparator = new FeatureComparator( list.getOwner(), propertyToSort );
    Collections.sort( list, featureComparator );

    final Feature parentFeature = list.getOwner();
    final GMLWorkspace workspace = parentFeature.getWorkspace();
    workspace.fireModellEvent( new FeatureStructureChangeModellEvent( workspace, parentFeature, (Feature[]) null, FeatureStructureChangeModellEvent.STRUCTURE_CHANGE_MOVE ) );

    /* Make workspace dirty */
    if( commandTarget != null )
    {
      try
      {
        // TODO: undoable!
        final EmptyCommand command = new EmptyCommand( StringUtils.EMPTY, false );
        commandTarget.postCommand( command );
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
  }
}