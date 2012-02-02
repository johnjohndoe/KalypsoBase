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
package org.kalypso.ui.wizard.gml;

import javax.xml.namespace.QName;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.editor.gmleditor.part.FeatureAssociationTypeElement;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureRelation;

/**
 * @author Gernot Belger
 */
public class GmlFileSelectionValidator implements IValidator
{
  private final GmlFileImportData m_data;

  public GmlFileSelectionValidator( final GmlFileImportData data )
  {
    m_data = data;
  }

  @Override
  public IStatus validate( final Object selection )
  {
    if( selection == null )
      return new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.6" ) ); //$NON-NLS-1$

    if( m_data.isValidAllowFeature() && selection instanceof Feature )
    {
      final Feature f = (Feature) selection;
      final IFeatureType featureType = f.getFeatureType();

      if( !checkFeatureTypeValid( featureType ) )
        return new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.7" ) ); //$NON-NLS-1$

      return Status.OK_STATUS;
    }
    else if( m_data.isValidAllowFeatureAssociation() && selection instanceof IFeatureRelation )
    {
      final IFeatureRelation fate = (FeatureAssociationTypeElement) selection;
      final IRelationType associationRt = fate.getPropertyType();
      final IFeatureType targetFeatureType = associationRt.getTargetFeatureType();
      if( !checkFeatureTypeValid( targetFeatureType ) )
        return new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.8" ) ); //$NON-NLS-1$

      return Status.OK_STATUS;
    }

    return new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString( "org.kalypso.ui.wizard.gmlGmlFileImportPage.9" ) ); //$NON-NLS-1$
  }

  /**
   * Check if given feature type substitutes at least one of our valid qnames.
   * <p>
   * Returns always true if {@link #m_validQnames} is not set or empty.
   */
  private boolean checkFeatureTypeValid( final IFeatureType featureType )
  {
    final QName[] validQnames = m_data.getValidQnames();
    if( validQnames == null || validQnames.length == 0 )
      return true;

    for( final QName qname : validQnames )
    {
      if( GMLSchemaUtilities.substitutes( featureType, qname ) )
        return true;
    }

    return false;
  }

}