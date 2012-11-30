/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.featureview.control;

import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.Text;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.w3c.dom.Node;

/**
 * @author Gernot Belger
 */
public class TextFeatureControlFactory implements IFeatureControlFactory
{
  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControlFactory#createFeatureControl(org.kalypso.ogc.gml.featureview.control.IFeatureComposite, org.kalypsodeegree.model.feature.Feature,
   *      org.kalypso.gmlschema.property.IPropertyType, org.kalypso.template.featureview.ControlType, org.kalypso.gmlschema.annotation.IAnnotation)
   */
  @Override
  public IFeatureControl createFeatureControl( final IFeatureComposite parentComposite, final Feature feature, final IPropertyType pt, final ControlType controlType, final IAnnotation annotation )
  {
    final Text textType = (Text)controlType;
    final IValuePropertyType vpt = (IValuePropertyType)pt;

    final String format = getFormat( textType );

    final GMLXPath propertyPath = vpt == null ? null : new GMLXPath( vpt.getQName() );

    final TextFeatureControl tfc = new TextFeatureControl( feature, propertyPath, vpt, format );
    tfc.setEditable( textType.isEditable() );

    return tfc;
  }

  private String getFormat( final Text editorType )
  {
    final Object objFormat = editorType.getFormat();
    if( objFormat instanceof String )
      return (String)objFormat;

    if( objFormat instanceof Node )
      return XMLTools.getStringValue( (Node)objFormat );

    return null;
  }
}