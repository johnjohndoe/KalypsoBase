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
package org.kalypso.gml.ui.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.contribs.eclipse.core.runtime.AdapterUtils;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ui.editor.gmleditor.ui.FeatureAssociationTypeElement;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public class GenericFeatureSelection implements IFeatureSelection
{
  public static IFeatureSelection create( final ISelection selection, final CommandableWorkspace defaultWorkspace )
  {
    final GenericFeatureSelection genericSelection = new GenericFeatureSelection();
    genericSelection.addSelection( selection, defaultWorkspace );
    return genericSelection;
  }

  private final Map<Feature, EasyFeatureWrapper> m_data = new HashMap<Feature, EasyFeatureWrapper>();

  private Feature m_focusedFeature;

  private IPropertyType m_focusedProperty;

  private IFeatureSelectionManager m_selectionManager;

  private void addSelection( final ISelection selection, final CommandableWorkspace defaultWorkspace )
  {
    if( selection instanceof IFeatureSelection )
      addFeatureSelection( (IFeatureSelection) selection );
    else if( selection instanceof IStructuredSelection )
      addStructuredSelection( selection, defaultWorkspace );
  }

  private void addStructuredSelection( final ISelection selection, final CommandableWorkspace defaultWorkspace )
  {
    for( final Iterator< ? > iterator = ((IStructuredSelection) selection).iterator(); iterator.hasNext(); )
      addItem( iterator.next(), defaultWorkspace );
  }

  private void addItem( final Object item, final CommandableWorkspace defaultWorkspace )
  {
    final IFeatureSelection featureSelection = AdapterUtils.getAdapter( item, IFeatureSelection.class );
    if( featureSelection != null )
      addFeatureSelection( featureSelection );

    final IStructuredSelection structuredSelection = AdapterUtils.getAdapter( item, IStructuredSelection.class );
    if( structuredSelection != null )
      addStructuredSelection( structuredSelection, defaultWorkspace );

    final EasyFeatureWrapper featureWithWorkspace = AdapterUtils.getAdapter( item, EasyFeatureWrapper.class );
    if( featureWithWorkspace != null )
      addFeatureWithWorkspace( featureWithWorkspace );

    final Feature feature = AdapterUtils.getAdapter( item, Feature.class );
    if( feature != null )
    {
      final CommandableWorkspace workspace = AdapterUtils.getAdapter( item, CommandableWorkspace.class );
      addFeature( feature, workspace );
    }

    final FeatureList featureList = AdapterUtils.getAdapter( item, FeatureList.class );
    if( featureList != null )
    {
      final CommandableWorkspace workspace = AdapterUtils.getAdapter( item, CommandableWorkspace.class );
      addFeatureList( featureList, workspace );
    }

    final FeatureAssociationTypeElement fate = AdapterUtils.getAdapter( item, FeatureAssociationTypeElement.class );
    if( fate != null )
    {
      CommandableWorkspace workspace = AdapterUtils.getAdapter( item, CommandableWorkspace.class );
      if( workspace == null )
        workspace = AdapterUtils.getAdapter( fate, CommandableWorkspace.class );

      addFeatureAssociation( fate, workspace );
    }
  }

  private void addFeatureAssociation( final FeatureAssociationTypeElement fate, final CommandableWorkspace defaultWorkspace )
  {
    final IRelationType rt = fate.getAssociationTypeProperty();
    final Feature parentFeature = fate.getParentFeature();
    final Object value = parentFeature.getProperty( rt );
    addItem( value, defaultWorkspace );
  }

  private void addFeatureList( final FeatureList featureList, final CommandableWorkspace defaultWorkspace )
  {
    final Feature parentFeature = featureList.getParentFeature();
    final GMLWorkspace owner = parentFeature == null ? defaultWorkspace : parentFeature.getWorkspace();

    for( final Object object : featureList )
    {
      final Feature feature = FeatureHelper.resolveLinkedFeature( owner, object );
      if( feature != null )
        addFeature( feature, defaultWorkspace );
    }
  }

  private void addFeature( final Feature feature, final CommandableWorkspace workspace )
  {
    addFeatureWithWorkspace( new EasyFeatureWrapper( workspace, feature ) );
  }

  private void addFeatureWithWorkspace( final EasyFeatureWrapper featureWithWorkspace )
  {
    m_data.put( featureWithWorkspace.getFeature(), featureWithWorkspace );
  }

  private void addFeatureSelection( final IFeatureSelection selection )
  {
    final EasyFeatureWrapper[] allFeatures = selection.getAllFeatures();
    for( final EasyFeatureWrapper easyFeatureWrapper : allFeatures )
      addFeatureWithWorkspace( easyFeatureWrapper );
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getAllFeatures()
   */
  @Override
  public EasyFeatureWrapper[] getAllFeatures( )
  {
    return m_data.values().toArray( new EasyFeatureWrapper[m_data.size()] );
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getFocusedFeature()
   */
  @Override
  public Feature getFocusedFeature( )
  {
    return m_focusedFeature;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getFocusedProperty()
   */
  @Override
  public IPropertyType getFocusedProperty( )
  {
    return m_focusedProperty;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getSelectionManager()
   */
  @Override
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  /**
   * @see org.kalypso.ogc.gml.selection.IFeatureSelection#getWorkspace(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public CommandableWorkspace getWorkspace( final Feature feature )
  {
    final EasyFeatureWrapper wrapper = m_data.get( feature );
    if( wrapper == null )
      return null;

    return wrapper.getWorkspace();
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredSelection#getFirstElement()
   */
  @Override
  public Object getFirstElement( )
  {
    return m_data.values().iterator().next();
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredSelection#iterator()
   */
  @Override
  public Iterator< ? > iterator( )
  {
    return m_data.values().iterator();
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredSelection#size()
   */
  @Override
  public int size( )
  {
    return m_data.size();
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredSelection#toArray()
   */
  @Override
  public Object[] toArray( )
  {
    return getAllFeatures();
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredSelection#toList()
   */
  @Override
  public List< ? > toList( )
  {
    return Arrays.asList( getAllFeatures() );
  }

  /**
   * @see org.eclipse.jface.viewers.ISelection#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return size() == 0;
  }

}
