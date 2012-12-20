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
package org.kalypso.ui.internal.layoutwizard.gistable;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.kalypso.core.layoutwizard.IModificationProvider;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.table.ILayerTableInput;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class GisTableLayoutPartFeatureListener implements IFeaturesProviderListener
{
  private final LayerTableViewer m_layerTable;

  private String m_selectFid;

  private final IModificationProvider m_modificationProvider;

  public GisTableLayoutPartFeatureListener( final LayerTableViewer layerTable, final String selectFid, final IModificationProvider modificationProvider )
  {
    m_layerTable = layerTable;
    m_selectFid = selectFid;
    m_modificationProvider = modificationProvider;

    final ILayerTableInput input = layerTable.getInput();
    selectFeature( input.getWorkspace() );
  }

  /**
   * @see org.kalypso.ogc.gml.IFeaturesProviderListener#featuresChanged(org.kalypso.ogc.gml.IFeaturesProvider, org.kalypsodeegree.model.feature.event.ModellEvent)
   */
  @Override
  public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
  {
    selectFeature( source.getWorkspace() );
    if( modellEvent != null )
      m_modificationProvider.fireModified();
  }

  private void selectFeature( final GMLWorkspace workspace )
  {
    if( m_selectFid == null )
      return;

    if( workspace == null )
      return;

    /* Selection should be done only on first time, as soon as the workspace has been successfully loaded */
    m_selectFid = null;

    final Feature feature = workspace.getFeature( m_selectFid );
    if( feature == null )
      return;

    final Control control = m_layerTable.getControl();
    if( control == null || control.isDisposed() )
      return;

    final Display display = control.getDisplay();
    if( display.isDisposed() )
      return;

    final LayerTableViewer layerTable = m_layerTable;
    display.syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        layerTable.setSelection( new StructuredSelection( feature ) );
      }
    } );
  }
}
