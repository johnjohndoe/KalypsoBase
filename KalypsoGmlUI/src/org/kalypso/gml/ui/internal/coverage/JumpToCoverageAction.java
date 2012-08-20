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
package org.kalypso.gml.ui.internal.coverage;

import org.apache.commons.lang3.ArrayUtils;
import org.deegree.model.spatialschema.GeometryException;
import org.eclipse.jface.action.Action;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.coverage.CoverageManagementWidget;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * @author Gernot Belger
 */
public class JumpToCoverageAction extends Action implements IUpdateable
{
  private final CoverageManagementWidget m_widget;

  public JumpToCoverageAction( final CoverageManagementWidget widget )
  {
    m_widget = widget;

    setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.16" ) ); //$NON-NLS-1$

    setImageDescriptor( KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_JUMP ) );
  }

  @Override
  public void run( )
  {
    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();
    if( ArrayUtils.isEmpty( selectedCoverages ) )
      return;

    try
    {
      GM_Envelope fullExtent = null;

      for( final ICoverage coverage : selectedCoverages )
      {
        final GM_Envelope boundingBox = coverage.getBoundedBy();
        final GM_Envelope scaledBox = GeometryUtilities.scaleEnvelope( boundingBox, 1.05 );

        if( fullExtent == null )
          fullExtent = scaledBox;
        else
          fullExtent = fullExtent.getMerged( scaledBox );
      }

      m_widget.getMapPanel().setBoundingBox( fullExtent );
    }
    catch( final GeometryException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void update( )
  {
    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();
    setEnabled( selectedCoverages.length > 0 );
  }
}