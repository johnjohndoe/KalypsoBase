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
package org.kalypso.zml.ui.chart.layer.boundaries;

import jregex.Pattern;
import jregex.RETokenizer;

import org.apache.commons.lang.ArrayUtils;
import org.kalypso.ogc.sensor.metadata.IMetadataBoundary;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author Dirk Kuch
 */
public class KodBoundaryLayer implements IMetadataLayerBoundary
{
  private static final Pattern PATTERN_STYLE_ID = new Pattern( "\\w*_" ); //$NON-NLS-1$

  private static final Pattern PATTERN_BOUNDARY = new Pattern( "\\w*\\s" ); //$NON-NLS-1$

  private final String m_label;

  private final String m_labelTokenizer;

  private final IStyleSet m_styles;

  private final IMetadataBoundary m_boundary;

  public KodBoundaryLayer( final IMetadataBoundary boundary, final String label, final String labelTokenizer, final IStyleSet styleSet )
  {
    m_boundary = boundary;
    m_label = label;
    m_labelTokenizer = labelTokenizer;
    m_styles = styleSet;
  }

  @Override
  public IMetadataBoundary getBoundary( )
  {
    return m_boundary;
  }

  /**
   * @see org.kalypso.hwv.core.chart.provider.style.boundary.IMetadataLayerBoundary#getLabel()
   */
  @Override
  public String getLabel( )
  {
    if( m_labelTokenizer == null )
      return m_label;

    final String name = m_boundary.getName();

    final Pattern pattern = new Pattern( m_labelTokenizer );
    final RETokenizer tokenizer = new RETokenizer( pattern, name );

    return String.format( m_label, tokenizer.nextToken() );
  }

  /**
   * @see org.kalypso.hwv.core.chart.provider.style.boundary.IMetadataLayerBoundary#getLineStyle()
   */
  @Override
  public ILineStyle getLineStyle( )
  {
    final StyleSetVisitor visitor = new StyleSetVisitor();

    final String[] styles = visitor.findReferences( m_styles, ILineStyle.class );
    if( ArrayUtils.isEmpty( styles ) )
      return visitor.visit( m_styles, ILineStyle.class, 0 ); // should return default style!
    else if( styles.length == 1 )
      return (ILineStyle) m_styles.getStyle( styles[0] );

    // like Alarmstufe 4
    final String boundary = m_boundary.getName();

    final RETokenizer tokenizer = new RETokenizer( PATTERN_BOUNDARY, boundary );
    final String id = tokenizer.nextToken();

    for( final String style : styles )
    {
      final RETokenizer styleTokenizer = new RETokenizer( PATTERN_STYLE_ID, style );
      final String token = styleTokenizer.nextToken();

      if( token.equals( id ) )
        return (ILineStyle) m_styles.getStyle( style );
    }

    return visitor.visit( m_styles, ILineStyle.class, 0 );
  }

  /**
   * @see org.kalypso.hwv.core.chart.provider.style.boundary.IMetadataLayerBoundary#getTextStyle()
   */
  @Override
  public ITextStyle getTextStyle( )
  {
    final StyleSetVisitor visitor = new StyleSetVisitor();

    return visitor.visit( m_styles, ITextStyle.class, 0 );
  }

}
