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
package org.kalypso.ogc.gml.featureview.dialog.feature;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.validation.rules.IRule;
import org.kalypsodeegree_impl.model.feature.validation.rules.RuleFactory;

/**
 * This validator validates all properties of the feature, using the rules.
 * 
 * @author Holger Albert
 */
public class PropertyFeatureDialogValidator implements IFeatureDialogValidator
{
  /**
   * The constructor.
   */
  public PropertyFeatureDialogValidator( )
  {
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.dialog.feature.IFeatureDialogValidator#validate(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public IStatus validate( Feature feature )
  {
    /* Memory for the results. */
    MultiStatus results = new MultiStatus( KalypsoGisPlugin.getId(), IStatus.OK, feature.getQualifiedName().toString(), null );

    /* Get the feature type. */
    IFeatureType featureType = feature.getFeatureType();

    /* Get all property types. */
    IPropertyType[] pts = featureType.getProperties();
    for( int i = 0; i < pts.length; i++ )
    {
      /* Get the property type. */
      IPropertyType pt = pts[i];

      /* Validate this property. */
      IStatus result = validateProperty( feature, pt );

      /* Add the result. */
      results.add( result );
    }

    return results;
  }

  /**
   * This function validates one property of the feature.
   * 
   * @param feature
   *          The feature.
   * @param pt
   *          The property type.
   * @return A status object, indicating the result of the validation.
   */
  private IStatus validateProperty( Feature feature, IPropertyType pt )
  {
    /* Memory for the results. */
    MultiStatus results = new MultiStatus( KalypsoGisPlugin.getId(), IStatus.OK, pt.getQName().toString(), null );

    /* Get the property. */
    Object property = feature.getProperty( pt );

    /* Get the rules, which should be applied for this property type. */
    IRule[] rules = RuleFactory.getRules( pt );
    for( int i = 0; i < rules.length; i++ )
    {
      /* Get the rule. */
      IRule rule = rules[i];

      /* Validate. */
      IStatus result = rule.isValid( property );

      /* Add the result. */
      results.add( result );
    }

    return results;
  }
}