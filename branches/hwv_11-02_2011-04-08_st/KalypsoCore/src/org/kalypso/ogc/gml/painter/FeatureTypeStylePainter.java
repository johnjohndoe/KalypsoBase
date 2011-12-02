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

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
class FeatureTypeStylePainter implements IStylePainter
{
  private final FeatureTypeStyle m_style;

  private final List<Feature> m_features;

  FeatureTypeStylePainter( final FeatureTypeStyle style, final List<Feature> features )
  {
    m_style = style;
    m_features = features;
  }

  @Override
  public void paint( final IStylePaintable paintable, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      doPaint( paintable, monitor );
    }
    finally
    {
      ProgressUtilities.done( monitor );
    }
  }

  private void doPaint( final IStylePaintable paintable, final IProgressMonitor monitor ) throws CoreException
  {
    final Rule[] rules = m_style.getRules();

    final SubMonitor progress = SubMonitor.convert( monitor, rules.length );

    /* Wrap paintable in order to filter by type name */
    final IStylePaintable delegatePaintable = createDelegatePaintable( paintable );

    for( final Rule rule : rules )
    {
      final SubMonitor childProgress = progress.newChild( 1 );
      final IStylePainter rulePainter = StylePainterFactory.create( rule, m_features );
      rulePainter.paint( delegatePaintable, childProgress );
    }
  }

  private IStylePaintable createDelegatePaintable( final IStylePaintable paintable )
  {
    final QName featureTypeName = m_style.getFeatureTypeName();
    if( featureTypeName == null )
      return paintable;

    return new FilterByTypeStylePaintable( paintable, featureTypeName );
  }
}
