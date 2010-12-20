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
package org.kalypso.ogc.gml.painter;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.core.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.model.feature.Feature;

/**
 * This user style painter paints a list of features according to one (sld) user style.
 * 
 * @author Gernot Belger
 */
class UserStylePainter implements IStylePainter
{
  private static final String STRING_PAINTING_USERSTYLE = Messages.getString( "org.kalypso.ogc.gml.painter.UserStylePainter.3" );

  private final IKalypsoUserStyle m_style;

  private final List<Feature> m_features;

  UserStylePainter( final IKalypsoUserStyle style, final List<Feature> features )
  {
    m_style = style;
    m_features = features;
  }

  @Override
  public void paint( final IStylePaintable paintable, final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, STRING_PAINTING_USERSTYLE, m_style.getFeatureTypeStyles().length ); //$NON-NLS-1$

    final FeatureTypeStyle[] fts = m_style.getFeatureTypeStyles();
    for( final FeatureTypeStyle element : fts )
    {
      final IStylePainter painter = StylePainterFactory.create( m_style.getName(), element, m_features );
      painter.paint( paintable, progress.newChild( 1 ) );
    }
  }

}