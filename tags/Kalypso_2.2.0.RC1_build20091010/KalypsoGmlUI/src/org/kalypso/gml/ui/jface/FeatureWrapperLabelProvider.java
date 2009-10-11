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
package org.kalypso.gml.ui.jface;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.binding.IFeatureWrapper2;

/**
 * @author Gernot Belger
 */
public class FeatureWrapperLabelProvider extends LabelProvider implements ITableLabelProvider
{
  private final TableViewer m_tableViewer;

  /**
   * The table viewer is used to lookup the column property for the clumn index.
   */
  public FeatureWrapperLabelProvider( final TableViewer tableViewer )
  {
    Assert.isNotNull( tableViewer );

    m_tableViewer = tableViewer;
  }

  private QName getProperty( final Object element, final int columnIndex )
  {
    final Object[] columnProperties = m_tableViewer.getColumnProperties();
    if( columnProperties == null )
      return null;

    if( columnIndex < 0 || columnIndex >= columnProperties.length )
      return null;

    if( !(element instanceof IFeatureWrapper2) )
      return null;

    final Object columnProperty = columnProperties[columnIndex];
    if( columnProperty == null )
      return null;

    return QName.valueOf( columnProperty.toString() );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  public Image getColumnImage( final Object element, final int columnIndex )
  {
    final QName prop = getProperty( element, columnIndex );
    if( prop == null )
      return null;

    final Feature feature = ((IFeatureWrapper2) element).getFeature();
    final IPropertyType pt = feature.getFeatureType().getProperty( prop );
    final Object value = feature.getProperty( prop );

    final ITypeRegistry<IGuiTypeHandler> guiTypeRegistry = GuiTypeRegistrySingleton.getTypeRegistry();

    final IGuiTypeHandler typeHandler = guiTypeRegistry.getTypeHandlerFor( pt );
    if( typeHandler == null )
      return null;

    return typeHandler.getImage( value );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  public String getColumnText( final Object element, final int columnIndex )
  {
    final QName prop = getProperty( element, columnIndex );
    if( prop == null )
      return super.getText( element );

    final Feature feature = ((IFeatureWrapper2) element).getFeature();
    final IPropertyType pt = feature.getFeatureType().getProperty( prop );
    final Object value = feature.getProperty( prop );

    final ITypeRegistry<IGuiTypeHandler> guiTypeRegistry = GuiTypeRegistrySingleton.getTypeRegistry();

    final IGuiTypeHandler typeHandler = guiTypeRegistry.getTypeHandlerFor( pt );
    if( typeHandler == null )
      return value.toString();

    return typeHandler.getText( value );
  }

}
