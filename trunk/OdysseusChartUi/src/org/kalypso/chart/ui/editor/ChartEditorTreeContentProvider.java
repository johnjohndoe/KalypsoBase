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
package org.kalypso.chart.ui.editor;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;

/**
 * @author alibu
 */
public class ChartEditorTreeContentProvider implements ITreeContentProvider
{

  private IChartModel m_model;

  public ChartEditorTreeContentProvider( final IChartModel model )
  {
    m_model = model;
  }

  private final Object[] revertLayer( final ILayerManager mngr )
  {
    if( mngr == null )
      return new Object[] {};
    final IChartLayer[] layers = mngr.getLayers();
    final IChartLayer[] reverted = new IChartLayer[layers.length];
    for( int i = 0; i < layers.length; i++ )
    {
      reverted[i] = layers[layers.length - 1 - i];

    }
    return reverted;
  }
 

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( final Object element )
  {
    if( element instanceof IChartModel )
    {
      /**
       * so you see the topmost Layer as first item
       */
      return revertLayer( ((IChartModel) element).getLayerManager() );

    }
    if( element instanceof IExpandableChartLayer )
    {
      /**
       * so you see the topmost Layer as first item
       */
      return revertLayer( ((IExpandableChartLayer) element).getLayerManager() );
    }

    if( element instanceof IChartLayer )
    {
      final IChartLayer layer = (IChartLayer) element;
      final ILegendEntry[] entries = layer.getLegendEntries();
      if( entries != null && entries.length > 1 )
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
  public Object getParent( final Object element )
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
  public boolean hasChildren( final Object element )
  {
    if( element instanceof IChartModel )
    {
      final IChartModel model = (IChartModel) element;
      return (model.getLayerManager().getLayers().length > 0);
    }
    if( element instanceof IExpandableChartLayer )
    {
      final IExpandableChartLayer layer = (IExpandableChartLayer) element;
      return (layer.getLayerManager().getLayers().length > 0);
    }
    if( element instanceof IChartLayer )
    {
      final IChartLayer layer = (IChartLayer) element;
      final ILegendEntry[] entries = layer.getLegendEntries();
      return entries == null ? false : entries.length > 1;
    }
    return false;
  }

  protected final IChartModel getModel( )
  {
    return m_model;
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( final Object inputElement )
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
  public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
  {
    if( oldInput == newInput )
      return;
    if( newInput instanceof IChartModel )
      m_model = (IChartModel) newInput;
  }

  private final Object findParent( final Object parent, final IChartLayer[] childs, final IChartLayer child )
  {
    for( final IChartLayer layer : childs )
    {
      if( layer == child )
        return parent;
      if( layer instanceof IExpandableChartLayer )
      {
        final Object o = findParent( layer, ((IExpandableChartLayer) layer).getLayerManager().getLayers(), child );
        if( o != null )
          return o;
      }
    }
    return null;
  }
}
