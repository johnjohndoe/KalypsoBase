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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.kalypso.commons.databinding.conversion.ITypedConverter;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.InputWithContextObservableValue;
import org.kalypsodeegree.filterencoding.Expression;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class TextSymbolizerLabelField extends InputWithContextObservableValue<TextSymbolizer, ParameterValueType>
{
  private final ITypedConverter<ParameterValueType, String> m_converter;

  public TextSymbolizerLabelField( final IStyleInput<TextSymbolizer> input, final ITypedConverter<ParameterValueType, String> converter )
  {
    super( input, ParameterValueType.class );

    m_converter = converter;
  }

  @Override
  protected ParameterValueType getValueFromData( final TextSymbolizer data )
  {
    return data.getLabel();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.forms.InputWithContextObservableValue#setValueToInput(java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  protected void setValueToInput( final TextSymbolizer data, final ParameterValueType value )
  {
    data.setLabel( value );
  }

  @Override
  protected boolean valueEquals( final ParameterValueType value1, final ParameterValueType value2 )
  {
    final String oldExpression = m_converter.convert( value1 );
    final String newExpression = m_converter.convert( value2 );
    return ObjectUtils.equals( oldExpression, newExpression );
  }

  public void configureViewer( final ComboViewer viewer )
  {
    viewer.setLabelProvider( new LabelProvider() );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setInput( createInput( getSource() ) );
  }

  private Object createInput( final IStyleInput<TextSymbolizer> input )
  {
    final IFeatureType featureType = input.getFeatureType();

    final Collection<String> patterns = new ArrayList<String>();

    final IPropertyType[] properties = featureType.getProperties();
    for( final IPropertyType pt : properties )
    {
      if( pt instanceof IValuePropertyType )
      {
        final IValuePropertyType vpt = (IValuePropertyType) pt;
        if( !vpt.isGeometry() )
        {
          final String path = vpt.getQName().getLocalPart();
          final ParameterValueType pvt = StyleFactory.createParameterValueType( new Expression[] { new PropertyName( path, null ) } );
          patterns.add( m_converter.convert( pvt ) );
        }
      }
    }

    return patterns.toArray( new String[patterns.size()] );
  }
}