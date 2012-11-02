/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.model.wspm.ui.view.chart.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.kalypso.chart.ui.editor.commandhandler.ChartHandlerUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.ui.dialog.compare.ProfileChartComposite;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;

import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Holger Albert
 */
public class SwitchProfileHandler extends AbstractHandler
{
  private static final String PREVIOUS_PROFILE_COMMAND_ID = "org.kalypso.model.wspm.ui.commands.PreviousProfileCommand"; //$NON-NLS-1$

  private static final String NEXT_PROFILE_COMMAND_ID = "org.kalypso.model.wspm.ui.commands.NextProfileCommand"; //$NON-NLS-1$

  public SwitchProfileHandler( )
  {
  }

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();
    final IChartComposite chart = ChartHandlerUtilities.getChart( context );
    if( !(chart instanceof ProfileChartComposite) )
      return null;

    final Command command = event.getCommand();
    final String commandId = command.getId();

    if( PREVIOUS_PROFILE_COMMAND_ID.equals( commandId ) )
      doSwitch( (ProfileChartComposite)chart, -1 );

    if( NEXT_PROFILE_COMMAND_ID.equals( commandId ) )
      doSwitch( (ProfileChartComposite)chart, 1 );

    return null;
  }

  /**
   * This function changes the selection of the profile in one direction.
   * 
   * @param chart
   *          The chart composite.
   * @param differencePositions
   *          The number of positions to change (negative or positive).
   */
  private void doSwitch( final ProfileChartComposite chart, final int differencePositions )
  {
    /* Get the feature of the chart. */
    final Feature chartFeature = getChartFeature( chart );
    if( chartFeature == null )
      return;

    /* Get the workspace of the feature of the chart. */
    /* No workspace means, the feature of the chart is not selected in the global selection manager. */
    final CommandableWorkspace chartWorkspace = getChartWorkspace( chartFeature );
    if( chartWorkspace == null )
      return;

    /* Get the new feature to switch to. */
    final Feature newFeature = getNewFeature( chartFeature, differencePositions );
    if( newFeature == null )
      return;

    /* Change the selection. */
    final IFeatureSelectionManager selectionManager = KalypsoCorePlugin.getDefault().getSelectionManager();
    selectionManager.changeSelection( new Feature[] { chartFeature }, new EasyFeatureWrapper[] { new EasyFeatureWrapper( chartWorkspace, newFeature ) } );
  }

  public static Feature getChartFeature( final ProfileChartComposite chart )
  {
    final IProfileSelection profileSelection = chart.getProfileSelection();
    if( profileSelection == null || profileSelection.isEmpty() )
      return null;

    final Object source = profileSelection.getSource();
    if( !(source instanceof Feature) )
      return null;

    return (Feature)source;
  }

  /**
   * This function returns the workspace of the feature.
   * 
   * @param feature
   *          The feature to get the workspace from.
   * @return The workspace of the feature or null, if it is not selected.
   */
  public static CommandableWorkspace getChartWorkspace( final Feature feature )
  {
    final IFeatureSelectionManager selectionManager = KalypsoCorePlugin.getDefault().getSelectionManager();
    return selectionManager.getWorkspace( feature );
  }

  public static Feature getNewFeature( final Feature chartFeature, final int differencePositions )
  {
    final FeatureList featureList = getFeatureList( chartFeature );
    final int newIndex = getNewIndex( featureList, chartFeature, differencePositions );
    if( newIndex < 0 )
      return null;

    return (Feature)featureList.get( newIndex );
  }

  public static FeatureList getFeatureList( final Feature feature )
  {
    final IRelationType parentRelation = feature.getParentRelation();
    final Feature owner = feature.getOwner();
    return (FeatureList)owner.getProperty( parentRelation );
  }

  /**
   * This function returns the index of the given feature, adjusted by the difference positions parameter.
   * 
   * @param featureList
   *          The feature list containing the feature.
   * @param feature
   *          The current feature.
   * @param differencePositions
   *          The number of positions to change (negative or positive).
   * @return The new index or -1, if no switch can be done.
   */
  public static int getNewIndex( final FeatureList featureList, final Feature feature, final int differencePositions )
  {
    /* It cannot be switched by zero positions. */
    if( differencePositions == 0 )
      return -1;

    /* Get the current index. */
    final int currentIndex = featureList.indexOf( feature );

    /* If it is 0 and the direction to the beginning, no switch can be done. */
    if( currentIndex == 0 && differencePositions < 0 )
      return -1;

    /* If it is size()-1 and the direction is to the end, no switch can be done. */
    if( currentIndex == featureList.size() - 1 && differencePositions > 0 )
      return -1;

    /* Calculate the new index. */
    final int newIndex = currentIndex + differencePositions;
    if( newIndex < 0 )
      return 0;

    if( newIndex >= featureList.size() )
      return featureList.size() - 1;

    return newIndex;
  }
}