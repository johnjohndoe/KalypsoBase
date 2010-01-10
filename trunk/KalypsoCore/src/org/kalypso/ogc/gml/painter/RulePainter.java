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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;

/**
 * @author Gernot Belger
 */
class RulePainter implements IStylePainter
{
  private static final String STRING_PAINTING_RULE = Messages.getString( "org.kalypso.ogc.gml.painter.UserStylePainter.1" );

  private static final String STRING_PAINTING_FEATURES = Messages.getString( "org.kalypso.ogc.gml.painter.UserStylePainter.2" );

  private final Rule m_rule;

  private final QName m_qname;

  private final List<Feature> m_features;

  RulePainter( final Rule rule, final QName qname, final List<Feature> features )
  {
    m_rule = rule;
    m_qname = qname;
    m_features = features;
  }

  @Override
  public void paint( final IStylePaintable paintable, final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, STRING_PAINTING_RULE, 100 ); //$NON-NLS-1$

    ProgressUtilities.worked( progress, 15 );

    final SubMonitor loopProgress = progress.newChild( 85 ).setWorkRemaining( m_features.size() );

    for( final Feature feature : m_features )
    {
      // TODO: would be nice to catch the exception here and, so other features may have the chance to get painted.
      // However: be careful, too many exceptions are a performance problem, so we should stop after some dozens

      final SubMonitor childProgress = loopProgress.newChild( 1 );
      paintFeature( paintable, feature, childProgress );
      ProgressUtilities.done( childProgress );
    }

    ProgressUtilities.done( progress );
  }

  private void paintFeature( final IStylePaintable paintable, final Feature feature, final IProgressMonitor monitor ) throws CoreException
  {
    final SubMonitor progress = SubMonitor.convert( monitor, STRING_PAINTING_FEATURES, 100 ); //$NON-NLS-1$

    final Double scale = paintable.getScale();

    /* Check for selection */
    try
    {
      /* Only paint really visible features */
      final Filter filter = m_rule.getFilter();
      if( filterFeature( paintable, feature, filter ) )
      {
        final Symbolizer[] symbolizers = m_rule.getSymbolizers();
        for( final Symbolizer symbolizer : symbolizers )
        {
          final DisplayElement displayElement = DisplayElementFactory.buildDisplayElement( feature, symbolizer );
          // TODO: should'nt there be at least some debug output if this happens?
          if( displayElement != null )
          {
            if( scale == null || displayElement.doesScaleConstraintApply( scale ) )
              paintable.paint( displayElement, progress.newChild( 100 ) );
          }
        }
      }
    }
    catch( final CoreException e )
    {
      if( !e.getStatus().matches( IStatus.CANCEL ) )// do not print cancel stuff
        e.printStackTrace();
      throw e;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  /**
   * Determines if a feature should be drawn or not.<br>
   * Checks for the sld:filter and current selection.
   * 
   * @param selected
   *          Whether to filter selected or non-selected features. If <code>null</code>, selection is not tested.
   */
  private boolean filterFeature( final IStylePaintable paintable, final Feature feature, final Filter filter ) throws FilterEvaluationException
  {
    if( !paintable.shouldPaintFeature( feature ) )
      return false;

    if( filter == null )
      return true;

    return filter.evaluate( feature );
  }

}
