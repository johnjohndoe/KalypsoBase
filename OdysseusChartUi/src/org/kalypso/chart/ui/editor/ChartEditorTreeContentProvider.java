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
package org.kalypso.chart.ui.editor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;

/**
 * @author alibu
 * 
 */
public class ChartEditorTreeContentProvider implements ITreeContentProvider
{

  private final IChartModel m_model;

  public ChartEditorTreeContentProvider( IChartModel model )
  {
    m_model = model;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( Object element )
  {
    if( element instanceof IChartModel )
    {
      IChartModel model = (IChartModel) element;
      IChartLayer[] layers = model.getLayerManager().getLayers();

      IChartLayer[] reverted = new IChartLayer[layers.length];
      for( int i = 0; i < layers.length; i++ )
      {
        reverted[i] = layers[layers.length - 1 - i];

      }
      return reverted;
    }
    if( element instanceof IExpandableChartLayer )
    {
      return ((IExpandableChartLayer) element).getLayers();
    }

    if( element instanceof IChartLayer )
    {
      IChartLayer layer = (IChartLayer) element;
      ILegendEntry[] entries = layer.getLegendEntries();
      if( entries!= null && entries.length > 1 )
      {
        return entries;
      }
      else
      {
        return new Object[0];
      }

    }
    return new Object[0];
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( Object element )
  {
    if( element instanceof IChartLayer )
    {
      return findParent( m_model, m_model.getLayerManager().getLayers(), (IChartLayer) element );
    }
    if( element instanceof ILegendEntry )
    {
      return ((ILegendEntry) element).getParentLayer();
    }

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( Object element )
  {
    if( element instanceof IChartModel )
    {
      IChartModel model = (IChartModel) element;
      return (model.getLayerManager().getLayers().length > 0);
    }
    if( element instanceof IExpandableChartLayer )
    {
      IExpandableChartLayer layer = (IExpandableChartLayer) element;
      return (layer.getLayers().length > 0);
    }
    if( element instanceof IChartLayer )
    {
      IChartLayer layer = (IChartLayer) element;
      ILegendEntry[] entries = layer.getLegendEntries();
      return entries == null ? false : entries.length > 1;
    }
    return false;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( Object inputElement )
  {
    return getChildren( inputElement );
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose( )
  {
    // TODO Auto-generated method stub

  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
    // TODO Auto-generated method stub

  }

  private final Object findParent( final Object parent, final IChartLayer[] childs, final IChartLayer child )
  {
    for( final IChartLayer layer : childs )
    {
      if( layer == child )
        return parent;
      if( layer instanceof IExpandableChartLayer )
      {
        final Object o = findParent( layer, ((IExpandableChartLayer) layer).getLayers(), child );
        if( o != null )
          return o;
      }
    }
    return null;
  }
}
