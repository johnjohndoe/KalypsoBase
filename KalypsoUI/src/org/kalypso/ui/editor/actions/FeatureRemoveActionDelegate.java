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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate2;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;

/**
 * FeatureRemoveActionDelegate
 * 
 * @author doemming (24.05.2005)
 */
public class FeatureRemoveActionDelegate implements IActionDelegate2
{
  private IFeatureSelection m_selection = null;

  /**
   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
   */
  @Override
  public void run( final IAction action )
  {
    // runWithEvent is used instead
  }

  /**
   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
   *      org.eclipse.jface.viewers.ISelection)
   */
  @Override
  public void selectionChanged( final IAction action, final ISelection selection )
  {
    action.setEnabled( false );
    m_selection = null;

    if( selection instanceof IFeatureSelection )
    {
      m_selection = (IFeatureSelection) selection;

      final int featureCount = FeatureSelectionHelper.getFeatureCount( m_selection );

      final String text = action.getText();
      final String newText = text.replaceAll( " \\([0-9]+\\)", "" ) + " (" + featureCount + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
      action.setText( newText );

      if( featureCount > 0 )
        action.setEnabled( true );
    }
  }

  /**
   * @see org.eclipse.ui.IActionDelegate2#dispose()
   */
  @Override
  public void dispose( )
  {
    // nothing to do?
  }

  /**
   * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
   */
  @Override
  public void init( final IAction action )
  {
  }

  /**
   * @see org.eclipse.ui.IActionDelegate2#runWithEvent(org.eclipse.jface.action.IAction, org.eclipse.swt.widgets.Event)
   */
  @Override
  public void runWithEvent( final IAction action, final Event event )
  {
    /* We are in the ui-thread so we get a shell here. */
    final Shell shell = event.display.getActiveShell();
    if( shell == null )
      return;

    if( m_selection == null )
      return;

    /* Get all selected features. */
    final EasyFeatureWrapper[] allFeatures = m_selection.getAllFeatures();

    /* Build the delete command. */
    final DeleteFeatureCommand command = TableFeatureControlUtils.deleteFeaturesFromSelection( allFeatures, shell );
    if( command != null )
    {
      try
      {
        /* At least one selected feature must exist, otherwise the command would be null. */
        final CommandableWorkspace workspace = allFeatures[0].getWorkspace();
        workspace.postCommand( command );
      }
      catch( final Exception e )
      {
        e.printStackTrace();

        final IStatus status = StatusUtilities.createStatus( IStatus.ERROR, "", e ); //$NON-NLS-1$

        ErrorDialog.openError( shell, action.getText(), Messages.getString( "org.kalypso.ui.editor.actions.FeatureRemoveActionDelegate.5" ), status ); //$NON-NLS-1$
      }
      finally
      {
        final Feature[] features = FeatureSelectionHelper.getFeatures( m_selection );
        m_selection.getSelectionManager().changeSelection( features, new EasyFeatureWrapper[0] );
      }
    }
  }
}