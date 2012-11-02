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
package org.kalypso.ogc.gml.table;

import java.awt.Color;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.model.feature.Feature;

/**
 * Represents the styling for one column of a {@link LayerTableViewer}.
 * 
 * @author Gernot Belger
 */
public class LayerTableStyle
{
  private final IPoolableObjectType m_key;

  public LayerTableStyle( final IPoolableObjectType key )
  {
    m_key = key;
  }

  public Color getForeground( final Feature feature )
  {
    try
    {
      final Stroke stroke = findStroke( feature );
      if( stroke == null )
        return null;

      final Color strokeColor = stroke.getStroke( feature );
      final double opacity = stroke.getOpacity( feature );

      return ColorUtilities.applyOpacity( strokeColor, opacity );
    }
    catch( final FilterEvaluationException e )
    {
      final String message = String.format( Messages.getString( "LayerTableStyle_0" ) ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), message, e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
    }

    return null;
  }

  public Color getBackground( final Feature feature )
  {
    try
    {
      final Fill fill = findFill( feature );
      if( fill == null )
        return null;

      final Color fillColor = fill.getFill( feature );
      final double opacity = fill.getOpacity( feature );

      return ColorUtilities.applyOpacity( fillColor, opacity );
    }
    catch( final FilterEvaluationException e )
    {
      final String message = String.format( Messages.getString( "LayerTableStyle_1" ) ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), message, e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
    }

    return null;
  }

  private Fill findFill( final Feature feature ) throws FilterEvaluationException
  {
    final Rule rule = findRule( feature );
    if( rule == null )
      return null;

    final Symbolizer[] symbolizers = rule.getSymbolizers();
    for( final Symbolizer symbolizer : symbolizers )
    {
      if( symbolizer instanceof PolygonSymbolizer )
      {
        final Fill fill = ((PolygonSymbolizer)symbolizer).getFill();
        if( fill != null )
          return fill;
      }
    }

    return null;
  }

  private Stroke findStroke( final Feature feature ) throws FilterEvaluationException
  {
    final Rule rule = findRule( feature );
    if( rule == null )
      return null;

    final Symbolizer[] symbolizers = rule.getSymbolizers();
    for( final Symbolizer symbolizer : symbolizers )
    {
      if( symbolizer instanceof PolygonSymbolizer )
      {
        final Stroke stroke = ((PolygonSymbolizer)symbolizer).getStroke();
        if( stroke != null )
          return stroke;
      }
    }

    return null;
  }

  private Rule findRule( final Feature feature ) throws FilterEvaluationException
  {
    try
    {
      final FeatureTypeStyle fts = LayerTableStyleUtils.getStyle( m_key );
      if( fts == null )
        return null;

      /* Apply type restriction, if present */
      final QName featureTypeName = fts.getFeatureTypeName();
      if( featureTypeName != null && !GMLSchemaUtilities.substitutes( feature.getFeatureType(), featureTypeName ) )
        return null;

      final Rule[] rules = fts.getRules();
      for( final Rule rule : rules )
      {
        final Filter filter = rule.getFilter();
        if( filter == null )
          return rule;

        if( filter.evaluate( feature ) )
          return rule;
      }
    }
    catch( final CoreException e )
    {
      KalypsoGisPlugin.getDefault().getLog().log( e.getStatus() );
    }

    return null;
  }
}