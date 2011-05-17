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
package org.kalypso.ogc.gml.featureview.control;

import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class DynamicTabFolderFeatureControl extends AbstractFeatureControl
{
  private TabFolder m_tabFolder;

  private final IFeatureComposite m_parentComposite;

  public DynamicTabFolderFeatureControl( final IFeatureComposite parentComposite, final Feature feature, final IRelationType rt )
  {
    super( feature, rt );

    m_parentComposite = parentComposite;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl#dispose()
   */
  @Override
  public void dispose( )
  {
    destroyAllItems();

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void addModifyListener( final ModifyListener l )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void removeModifyListener( final ModifyListener l )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#createControl(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  public Control createControl( final Composite parent, final int style )
  {
    m_tabFolder = new TabFolder( parent, style );
    updateControl();
    return m_tabFolder;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControl#updateControl()
   */
  @Override
  public void updateControl( )
  {
    if( m_tabFolder == null || m_tabFolder.isDisposed() )
      return;

    final Feature feature = getFeature();
    if( feature == null )
    {
      destroyAllItems();
      return;
    }

    final GMLWorkspace workspace = feature.getWorkspace();
    final IPropertyType featureTypeProperty = getFeatureTypeProperty();
    final Object property = feature.getProperty( featureTypeProperty );
    if( !(property instanceof FeatureList) )
    {
      destroyAllItems();
      return;
    }

    final FeatureList featureList = (FeatureList) property;
    destroyObsoleteItems( featureList );

    // Destroy or add
    int count = 0;
    for( final Iterator< ? > iterator = featureList.iterator(); iterator.hasNext(); )
    {
      final Object object = iterator.next();

      final FeatureTabItem featureItem = getFeatureItem( count );
      final Object tabObject = featureItem == null ? null : featureItem.getFeatureObject();
      
      if( object.equals( tabObject ) )
      {
        featureItem.updateControl();
      }
      else
      {
        final FeatureTabItem newFeatureItem = createItem( count, workspace, object );
        newFeatureItem.updateControl();
      }

      count++;
    }
  }

  private FeatureTabItem getFeatureItem( final int index )
  {
    if( index < m_tabFolder.getItemCount() )
      return FeatureTabItem.get( m_tabFolder.getItem( index ) );

    return null;
  }

  private FeatureTabItem createItem( final int index, final GMLWorkspace workspace, final Object featureObject )
  {
    final TabItem tabItem = new TabItem( m_tabFolder, SWT.NONE, index );
    final FeatureTabItem featureTabItem = new FeatureTabItem( tabItem, workspace, featureObject );
// final Feature feature = featureTabItem.getFeature();
// final String text = FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_LABEL );
// tabItem.setText( text );

    /* Delegate any events to the next higher level */
    final IFeatureControl featureControl = featureTabItem.createFeatureConrol( m_parentComposite );
    featureControl.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        fireFeatureChange( changeCommand );
      }

      @Override
      public void openFeatureRequested( final Feature featureToOpen, final IPropertyType ftpToOpen )
      {
        fireOpenFeatureRequested( featureToOpen, ftpToOpen );
      }
    } );

    return featureTabItem;
  }

  private void destroyObsoleteItems( final FeatureList featureList )
  {
    final TabItem[] items = m_tabFolder.getItems();
    for( final TabItem tabItem : items )
    {
      final FeatureTabItem wrapper = FeatureTabItem.get( tabItem );
      final Object featureObject = wrapper.getFeatureObject();
      if( !featureList.contains( featureObject ) )
        wrapper.destroy();
    }
  }

  private void destroyAllItems( )
  {
    if( m_tabFolder.isDisposed() )
      return;

    final TabItem[] items = m_tabFolder.getItems();
    for( final TabItem tabItem : items )
    {
      final FeatureTabItem wrapper = FeatureTabItem.get( tabItem );
      wrapper.destroy();
    }
  }
}
