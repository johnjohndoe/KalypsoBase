/*--------------- Kalypso-Header ------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 --------------------------------------------------------------------------*/

package org.kalypso.ui.editor.mapeditor.commands;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Selects the next feature from the current active theme of the map.
 * 
 * @author Gernot Belger
 */
public class SelectNextFeatureHandler extends AbstractHandler implements IExecutableExtension
{
  /** Move forward (<code>true</code>) or backward (<code>false</code>) */
  private boolean m_forward = false;

  /** If <code>true</code>, jump to first/last index if maxIndex or 0 is exceeded. */
  private boolean m_rotate = false;

  /** If <code>true</code>, go to the very first/last element */
  private boolean m_firstLast = false;

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    if( data instanceof Map )
    {
      final Map<String, String> m_map = (Map<String, String>) data;
      m_forward = Boolean.valueOf( m_map.get( "forward" ) ).booleanValue(); //$NON-NLS-1$
      m_rotate = Boolean.valueOf( m_map.get( "rotate" ) ).booleanValue(); //$NON-NLS-1$
      m_firstLast = Boolean.valueOf( m_map.get( "firstlast" ) ).booleanValue(); //$NON-NLS-1$
      if( m_firstLast )
        m_rotate = false;
    }
  }

  /**
   * @see org.eclipse.ui.commands.IHandler#execute(java.util.Map)
   */
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();
    final IMapPanel mapPanel = MapHandlerUtils.getMapPanel( context );
    final IKalypsoTheme activeTheme = MapHandlerUtils.getActiveTheme( context );
    if( !(activeTheme instanceof IKalypsoFeatureTheme) )
      return null;

    final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) activeTheme;
    final FeatureList featureList = featureTheme.getFeatureList();
    if( featureList.isEmpty() )
      return null;

    final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();
    final Object currentElement = selectionManager.getFirstElement();
    final Feature featureToSelect = findFeatureToSelect( featureTheme, featureList, currentElement );
    if( featureToSelect == null )
      return null;

    // do change the selection
    final CommandableWorkspace workspace = featureTheme.getWorkspace();
    final EasyFeatureWrapper wrapperToSelect = new EasyFeatureWrapper( workspace, featureToSelect, featureToSelect.getOwner(), featureToSelect.getParentRelation() );
    final Feature[] toRemove = FeatureSelectionHelper.getFeatures( selectionManager );
    selectionManager.changeSelection( toRemove, new EasyFeatureWrapper[] { wrapperToSelect } );

    return null;
  }

  private Feature findFeatureToSelect( final IKalypsoFeatureTheme featureTheme, final FeatureList featureList, final Object currentElement )
  {
    final Object objectToSelect = findObjectToSelect( featureTheme, featureList, currentElement );
    if( objectToSelect == null )
      return null;

    final CommandableWorkspace workspace = featureTheme.getWorkspace();
    return FeatureHelper.getFeature( workspace, objectToSelect );
  }

  private Object findObjectToSelect( final IKalypsoFeatureTheme featureTheme, final FeatureList featureList, final Object currentElement )
  {
    // TRICKY: next feature must be a visible feature of the theme. Might be a performance problem to do
    // the list checks here. But it is the simplest way for now....
    final FeatureList featureListVisible = featureTheme.getFeatureListVisible( null );

    if( m_firstLast )
      return findLastFeature( featureList, featureListVisible );

    return findNextFeature( featureList, currentElement, featureListVisible );
  }

  /**
   * Find the last (first) visible feature.
   */
  private Object findLastFeature( final FeatureList featureList, final FeatureList featureListVisible )
  {
    int index = m_forward ? featureList.size() - 1 : 0;
    while( true )
    {
      final Object featureToSelect = featureList.get( index );
      if( featureListVisible.contains( featureToSelect ) )
        return featureToSelect;

      if( m_forward )
      {
        index++;
        if( index == featureList.size() )
          return null;
      }
      else
      {
        index--;
        if( index < 0 )
          return null;
      }
    }
  }

  private Object findNextFeature( final FeatureList featureList, final Object previousElement, final FeatureList featureListVisible )
  {
    final int index = featureList.indexOf( previousElement );
    int newIndex = index;
    final Set<Integer> indexChecked = new HashSet<Integer>();
    while( !indexChecked.contains( newIndex ) )
    {
      // Remember all checked indices, to prevent endless-loop
      indexChecked.add( newIndex );

      newIndex = computeNewIndex( newIndex, featureList.size() - 1 );
      final Object featureToSelect = featureList.get( newIndex );
      if( featureListVisible.contains( featureToSelect ) )
        return featureToSelect;
    }

    // Nothing found, return null
    return null;
  }

  /**
   * @param index
   *          The current index
   * @param maxIndex
   *          The maximal possible index (the minimal possible index is assumed to be 0)
   */
  private int computeNewIndex( final int index, final int maxIndex )
  {
    if( index < 0 || index > maxIndex )
      return m_forward ? 0 : maxIndex;

    final int nextIndex = m_forward ? index + 1 : index - 1;

    if( nextIndex <= 0 )
      return m_rotate ? maxIndex : 0;

    if( nextIndex > maxIndex )
      return m_rotate ? 0 : maxIndex;

    return nextIndex;
  }

  /**
   * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object)
   */
  @Override
  public void setEnabled( final Object evaluationContext )
  {
    final boolean enabled = checkEnabled( evaluationContext );
    setBaseEnabled( enabled );
  }

  private boolean checkEnabled( final Object evaluationContext )
  {
    try
    {
      final IEvaluationContext context = (IEvaluationContext) evaluationContext;
      final IMapPanel mapPanel = MapHandlerUtils.getMapPanel( context );
      final IKalypsoTheme activeTheme = MapHandlerUtils.getActiveTheme( context );
      if( !(activeTheme instanceof IKalypsoFeatureTheme) )
        return false;

      final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) activeTheme;
      final FeatureList featureList = featureTheme.getFeatureList();
      if( featureList == null || featureList.isEmpty() )
        return false;

      final IFeatureSelectionManager selectionManager = mapPanel.getSelectionManager();
      final Object currentElement = selectionManager.getFirstElement();
      final Feature objectToSelect = findFeatureToSelect( featureTheme, featureList, currentElement );

      if( objectToSelect == currentElement )
        return false;

      return objectToSelect != null;
    }
    catch( final ExecutionException e )
    {
      // ignore, happens often
      // e.printStackTrace();
      return false;
    }
  }

  /**
   * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    return super.isEnabled();
  }

}
