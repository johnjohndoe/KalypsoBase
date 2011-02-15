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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabItem;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.ogc.gml.featureview.maker.IFeatureviewFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * Helper class that wraps a tabItem as a feature conrol.
 * 
 * @author Gernot Belger
 */
public class FeatureTabItem
{
  private static final String DATA_ME = "me";

  private final TabItem m_item;

  private final Object m_featureObject;

  private final GMLWorkspace m_workspace;

  private FeatureComposite m_fc;

  public static FeatureTabItem get( final TabItem tabItem )
  {
    return (FeatureTabItem) tabItem.getData( DATA_ME );
  }

  public FeatureTabItem( final TabItem item, final GMLWorkspace workspace, final Object featureObject )
  {
    m_item = item;
    m_workspace = workspace;
    m_featureObject = featureObject;
    m_item.setData( DATA_ME, this );

  }

  public IFeatureControl createFeatureConrol( final IFeatureComposite parentComposite )
  {
    final IFeatureviewFactory featureviewFactory = parentComposite.getFeatureviewFactory();
    final IFeatureSelectionManager selectionManager = parentComposite.getSelectionManager();

    final FeatureComposite fc = new FeatureComposite( getFeature(), selectionManager, featureviewFactory );
    fc.setFormToolkit( parentComposite.getFormToolkit() );
    fc.setShowOk( parentComposite.isShowOk() );

    m_fc = fc;

    final Control control = fc.createControl( m_item.getParent(), SWT.NONE );

    m_item.setControl( control );

    return fc;
  }

  public Feature getFeature( )
  {
    return FeatureHelper.resolveLinkedFeature( m_workspace, m_featureObject );
  }

  public Object getFeatureObject( )
  {
    return m_featureObject;
  }

  public void destroy( )
  {
    m_item.dispose();

    m_fc.dispose();
  }

  public void updateControl( )
  {
    final Feature feature = getFeature();
    final String text = FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_LABEL );
    m_item.setText( text );

    m_fc.updateControl();
  }

}
