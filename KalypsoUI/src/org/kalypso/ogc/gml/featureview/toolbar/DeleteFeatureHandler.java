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
package org.kalypso.ogc.gml.featureview.toolbar;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.featureview.control.TableFeatureContol;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ui.editor.actions.FeatureActionUtilities;
import org.kalypso.ui.editor.actions.TableFeatureControlUtils;

/**
 * @author Dirk Kuch
 */
public class DeleteFeatureHandler extends AbstractTableFeatureControlHandler
{
  public static final String ID = "org.kalypso.ogc.gml.featureview.toolbar.DeleteFeatureHandler";

  /**
   * This function checks, if there are features, which can be deleted.
   * 
   * @return True, if so.
   */
  public boolean canDelete( final IFeatureSelection selection )
  {
    final int featureCount = FeatureSelectionHelper.getFeatureCount( selection );
    if( featureCount > 0 )
      return true;

    return false;
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IFeatureSelection selection = getSelection( event );
    final TableFeatureContol tableFeatureControl = getFeatureControl( event );

    final Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

    if( !canDelete( selection ) )
    {
      /* Get the needed properties. */
      final IRelationType parentRelation = tableFeatureControl.getParentRealtion();

      final String actionLabel = parentRelation == null ? Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.0" ) : FeatureActionUtilities.newFeatureActionLabel( parentRelation.getTargetFeatureType() ); //$NON-NLS-1$
      MessageDialog.openInformation( shell, actionLabel + Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.5" ), Messages.getString( "org.kalypso.ogc.gml.featureview.control.TableFeatureContol.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      return Status.CANCEL_STATUS;
    }

    /* Get all selected features. */
    final EasyFeatureWrapper[] allFeatures = selection.getAllFeatures();

    /* Build the delete command. */
    final DeleteFeatureCommand command = TableFeatureControlUtils.deleteFeaturesFromSelection( allFeatures, shell );
    if( command != null )
    {
      /* Execute the command. */
      tableFeatureControl.execute( command );

      /* Reset the selection. */
      tableFeatureControl.setSelection( new StructuredSelection() );
    }

    return Status.OK_STATUS;
  }

}
