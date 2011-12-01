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

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.StyleEditorHelper;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.InputWithContextObservableValue;
import org.kalypsodeegree.graphics.sld.Geometry;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class GeometryValue<S extends Symbolizer> extends InputWithContextObservableValue<S, QName>
{
  static QName ELEMENT_NOT_SET = new QName( StringUtils.EMPTY );

  public GeometryValue( final IStyleInput<S> input )
  {
    super( input, QName.class );
  }

  @Override
  protected QName getValueFromData( final S data )
  {
    final Geometry geometry = data.getGeometry();
    if( geometry == null )
      return ELEMENT_NOT_SET;

    final PropertyName propertyName = geometry.getPropertyName();
    if( propertyName == null )
      return ELEMENT_NOT_SET;

    final IStyleInput<S> input = getSource();
    final IFeatureType ft = input.getFeatureType();
    final IPropertyType pt = StyleEditorHelper.getFeatureTypeProperty( ft, propertyName );
    if( pt == null )
      return ELEMENT_NOT_SET;

    return pt.getQName();
  }

  @Override
  protected void setValueToInput( final S data, final QName value )
  {
    if( value == null )
      data.setGeometry( null );
    else
    {
      final PropertyName propertyName = new PropertyName( value );
      final Geometry geometry = StyleFactory.createGeometry( propertyName );
      data.setGeometry( geometry );
    }
  }

  public void configureViewer( final ComboViewer viewer )
  {
    final IStyleInput<S> input = getSource();

    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        if( element == ELEMENT_NOT_SET )
          return MessageBundle.STYLE_EDITOR_FIELD_NOT_SET;

        if( element instanceof QName )
        {
          final IFeatureType ft = input.getFeatureType();
          final IPropertyType pt = ft.getProperty( (QName) element );
          return AnnotationUtilities.getAnnotation( pt.getAnnotation(), null, IAnnotation.ANNO_LABEL );
        }

        return super.getText( element );
      }
    } );

    viewer.setInput( createInput( input ) );
  }

  private Object createInput( final IStyleInput<S> input )
  {
    // TODO: we should also descend into sub-features and show their geometry properties as well
    // Maybe instead, we should provide a more suited ui instead of a Combo

    final IFeatureType featureType = input.getFeatureType();
    if( featureType == null )
      return null;

    final IPropertyType[] geometryProperties = featureType.getAllGeomteryProperties();
    final QName[] comboInput = new QName[geometryProperties.length + 1];
    comboInput[0] = ELEMENT_NOT_SET;
    for( int i = 0; i < geometryProperties.length; i++ )
      comboInput[i + 1] = geometryProperties[i].getQName();

    return comboInput;
  }

}
