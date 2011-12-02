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
package org.kalypso.ogc.gml.featureview.control;

import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.Extensioncontrol;
import org.kalypso.template.featureview.Extensioncontrol.Param;
import org.kalypso.ui.KalypsoUIExtensions;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class ExtensionFeatureControlFactory extends AbstractFeatureControlFactory implements IFeatureControlFactory
{
  public ExtensionFeatureControlFactory( final FormToolkit formToolkit )
  {
    super( formToolkit );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureControlFactory#createFeatureControl(org.kalypso.ogc.gml.featureview.control.IFeatureComposite,
   *      org.kalypsodeegree.model.feature.Feature, org.kalypso.gmlschema.property.IPropertyType,
   *      org.kalypso.template.featureview.ControlType, org.kalypso.gmlschema.annotation.IAnnotation)
   */
  @Override
  public IFeatureControl createFeatureControl( final IFeatureComposite parentComposite, final Feature feature, final IPropertyType pt, final ControlType controlType, final IAnnotation annotation )
  {
    final Extensioncontrol extControl = (Extensioncontrol) controlType;
    final String extensionId = extControl.getExtensionId();
    final List<Param> param = extControl.getParam();
    final Properties parameters = new Properties();
    for( final Param controlParam : param )
      parameters.setProperty( controlParam.getName(), controlParam.getValue() );

    try
    {
      final IExtensionsFeatureControlFactory2 controlFactory = KalypsoUIExtensions.getFeatureviewControlFactory( extensionId );
      FormToolkit toolkit = getToolkit();
      return controlFactory.createFeatureControl( toolkit, feature, pt, parameters );
    }
    catch( final CoreException ce )
    {
      final String message = String.format( "%s%n%s", ce.getLocalizedMessage(), ExceptionUtils.getStackTrace( ce ) );
      return new LabelFeatureControl( feature, pt, message );
    }
  }
}
