/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard.shape;

import java.lang.reflect.Method;

import org.kalypso.contribs.java.i18n.I18NBundle;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Layer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Style;
import org.kalypsodeegree.graphics.sld.StyledLayerDescriptor;
import org.kalypsodeegree.graphics.sld.UserStyle;

/**
 * Helper that replaces all translated string of an sld with their real current value.
 *
 * @author Gernot Belger
 */
public class SLDTranslationResolver
{
  private final I18NBundle m_translation;

  public SLDTranslationResolver( final I18NBundle translation )
  {
    m_translation = translation;
  }

  public void resolve( final Object sldElement )
  {
    if( sldElement instanceof StyledLayerDescriptor )
    {
      final Layer[] layers = ((StyledLayerDescriptor) sldElement).getLayers();
      for( final Layer layer : layers )
        resolve( layer );
    }

    if( sldElement instanceof Layer )
    {
      final Layer layer = (Layer) sldElement;

      final Style[] styles = layer.getStyles();
      for( final Style style : styles )
        resolve( style );
    }

    if( sldElement instanceof UserStyle )
    {
      final UserStyle style = (UserStyle) sldElement;
      final FeatureTypeStyle[] featureTypeStyles = style.getFeatureTypeStyles();
      for( final FeatureTypeStyle fts : featureTypeStyles )
        resolve( fts );
    }

    if( sldElement instanceof FeatureTypeStyle )
    {
      final FeatureTypeStyle fts = (FeatureTypeStyle) sldElement;
      replaceTranslationStrings( fts );

      final Rule[] rules = fts.getRules();
      for( final Rule rule : rules )
        resolve( rule );
    }

    if( sldElement instanceof Rule )
      replaceTranslationStrings( sldElement );
  }

  private void replaceTranslationStrings( final Object sldElement )
  {
    replaceTranslationProperty( sldElement, "Title" ); //$NON-NLS-1$
    replaceTranslationProperty( sldElement, "Abstract" ); //$NON-NLS-1$
  }

  private void replaceTranslationProperty( final Object sldElement, final String property )
  {
    try
    {
      final Class< ? extends Object> type = sldElement.getClass();
      final Method getter = type.getMethod( "get" + property ); //$NON-NLS-1$
      final Method setter = type.getMethod( "set" + property, String.class ); //$NON-NLS-1$

      final Object value = getter.invoke( sldElement );
      if( value instanceof String )
      {
        final String text = (String) value;
        final String resolvedText = m_translation.translate( text );
        setter.invoke( sldElement, resolvedText );
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
  }
}
