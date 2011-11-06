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

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.IFeatureModifier;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Belger
 */
public class LayerTableLabelProvider extends BaseLabelProvider implements ITableLabelProvider, IColorProvider
{
  private final LayerTableViewer m_viewer;

  private final Color m_noSelectionColor;

  public LayerTableLabelProvider( final LayerTableViewer layerTable )
  {
    m_viewer = layerTable;
    m_noSelectionColor = m_viewer.getControl().getBackground();
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
   */
  @Override
  public Image getColumnImage( final Object element, final int columnIndex )
  {
    // Extrawurscht, wenn Tabelle leer, da trotzdem mit index 0 aufgerufen wird
    if( m_viewer.getColumnCount() == 0 )
      return null;

    final Feature feature = (Feature) element;

    final IFeatureModifier modifier = m_viewer.getModifier( columnIndex );
    if( modifier == null )
      return null;

    return modifier.getImage( feature );
  }

  /**
   * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
   */
  @Override
  public String getColumnText( final Object element, final int columnIndex )
  {
    // Extrawurscht, wenn Tabelle leer, da trotzdem mit index 0 aufgerufen wird
    if( m_viewer.getColumnCount() == 0 )
    {
      if( element instanceof String )
        return (String) element;

      return ""; //$NON-NLS-1$
    }

    final Feature feature = (Feature) element;

    final IFeatureModifier modifier = m_viewer.getModifier( columnIndex );
    if( modifier == null )
      return ""; //$NON-NLS-1$

    final IPropertyType pt = modifier.getFeatureTypeProperty();
    final IPropertyType realPt = feature.getFeatureType().getProperty( pt.getQName() );
    if( realPt == null )
      return Messages.getString("org.kalypso.ogc.gml.table.LayerTableLabelProvider.0"); //$NON-NLS-1$

    final String label = modifier.getLabel( feature );
    return label == null ? "" : label; //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
   */
  @Override
  public Color getForeground( final Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
   */
  @Override
  public Color getBackground( final Object element )
  {
// if( element instanceof Feature )
// {
// final ISelection selection = m_viewer.getSelection();
// if( selection instanceof IStructuredSelection )
// {
// final Iterator iterator = ( (IStructuredSelection)selection ).iterator();
// while( iterator.hasNext() )
// {
// final Object object = iterator.next();
// if( element == object )
// return m_selectionColor;
// }
// }
// }
    return m_noSelectionColor;
  }
}