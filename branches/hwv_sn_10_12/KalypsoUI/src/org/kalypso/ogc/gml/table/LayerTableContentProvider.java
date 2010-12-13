/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.contribs.java.util.Arrays;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.KalypsoFeatureThemeSelection;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.event.FeatureStructureChangeModellEvent;
import org.kalypsodeegree.model.feature.event.FeaturesChangedModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class LayerTableContentProvider implements IStructuredContentProvider
{
  private final ISelectionChangedListener m_tableSelectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      viewerSelectionChanged( (IStructuredSelection) event.getSelection() );
    }
  };

  private final IFeaturesProviderListener m_providerListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      handleFeaturesChanged( modellEvent );
    }
  };

  private final IFeatureSelectionManager m_selectionManager;

  private LayerTableViewer m_viewer;

  public LayerTableContentProvider( final IFeatureSelectionManager selectionManager )
  {
    m_selectionManager = selectionManager;
  }

  /**
   * Input muss ein IKalypsoFeatureTheme sein Output sind die Features
   * 
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  @Override
  public Object[] getElements( final Object inputElement )
  {
    if( inputElement instanceof IFeaturesProvider )
    {
      final IFeaturesProvider featuresProvider = (IFeaturesProvider) inputElement;
      final FeatureList featureList = featuresProvider.getFeatureList();
      // FIXME: unterscheide lade und fehler
      if( featureList == null )
        return new Object[] {};

      if( featureList != null )
      {
        final IFeaturesProvider featureProvider = featuresProvider;
        // TODO; hm, quite heavy, as the complete list is copied here...
        final List<Feature> features = featureProvider.getFeatures();
        if( features != null )
          return features.toArray();
      }
    }

    return new Object[] {};

  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    // all listeners are unhooked when inputChanged( ..., null) is called
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    if( oldInput instanceof IFeaturesProvider )
    {
      final IFeaturesProvider oldProvider = (IFeaturesProvider) oldInput;
      oldProvider.removeFeaturesProviderListener( m_providerListener );
    }

    if( m_viewer != null )
      m_viewer.removePostSelectionChangedListener( m_tableSelectionListener );

    m_viewer = (LayerTableViewer) viewer;

    if( m_viewer != null )
      m_viewer.addPostSelectionChangedListener( m_tableSelectionListener );

    if( newInput instanceof IFeaturesProvider )
    {
      final IFeaturesProvider newProvider = (IFeaturesProvider) newInput;
      newProvider.addFeaturesProviderListener( m_providerListener );
    }
  }

  /**
   * @param selection
   */
  protected void viewerSelectionChanged( final IStructuredSelection selection )
  {
    // remove all features in input from manager
    final IFeaturesProvider featureProvider = m_viewer.getInput();
    if( featureProvider == null )
      return;

    final FeatureList featureList = featureProvider.getFeatureList();
    final List<Feature> featuresToRemove = featureProvider == null ? null : featureProvider.getFeatures();
    if( featureList == null || featuresToRemove == null )
      return;

    // if viewer selection and tree selection are the same, do nothing
    if( m_selectionManager == null )
      return;

    final IStructuredSelection managerSelection = KalypsoFeatureThemeSelection.filter( m_selectionManager.toList(), featureList );
    final Object[] managerFeatures = managerSelection.toArray();
    if( Arrays.equalsUnordered( managerFeatures, selection.toArray() ) )
      return;


    // add current selection
    final List<EasyFeatureWrapper> wrappers = new ArrayList<EasyFeatureWrapper>( selection.size() );
    for( final Iterator< ? > sIt = selection.iterator(); sIt.hasNext(); )
    {
      final Object object = sIt.next();
      if( object instanceof Feature )
      {
        final Feature feature = (Feature) object;
        final EasyFeatureWrapper wrapper = new EasyFeatureWrapper( featureProvider.getWorkspace(), feature );
        wrappers.add( wrapper );
      }
    }

    final EasyFeatureWrapper[] izis = wrappers.toArray( new EasyFeatureWrapper[wrappers.size()] );
    final Feature[] featureArray = featuresToRemove.toArray( new Feature[featuresToRemove.size()] );
    m_selectionManager.changeSelection( featureArray, izis );
  }

  protected void handleFeaturesChanged( final ModellEvent event )
  {
    if( event == null )
    {
      final LayerTableViewer viewer = m_viewer;

      final Control control = viewer.getControl();
      if( !control.isDisposed() )
      {
        control.getDisplay().syncExec( new Runnable()
        {
          @Override
          public void run( )
          {
            if( control.isDisposed() )
              return;

            viewer.refreshAll();
          }
        } );
      }
    }
    else if( event instanceof FeaturesChangedModellEvent )
    {
      final Feature[] features = ((FeaturesChangedModellEvent) event).getFeatures();
      ViewerUtilities.update( m_viewer, features, null, false );
    }
    else if( event instanceof FeatureStructureChangeModellEvent )
    {
      final IFeaturesProvider featuresProvider = m_viewer.getInput();
      final FeatureList featureList = featuresProvider == null ? null : featuresProvider.getFeatureList();
      final Feature parentFeature = featureList == null ? null : featureList.getParentFeature();

      final Feature[] features = ((FeatureStructureChangeModellEvent) event).getParentFeatures();
      for( final Feature feature : features )
      {
        if( feature == parentFeature )
        {
          ViewerUtilities.refresh( m_viewer, false );
          return;
        }
      }
    }
  }

  protected void handleRepaintRequested( )
  {
    ViewerUtilities.refresh( m_viewer, true );
  }

}