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
package org.kalypso.gml.ui.internal.feature.editProperties;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.command.RelativeFeatureChange;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class EditFeaturePropertiesOperation implements ICoreRunnableWithProgress
{
  private final EditFeaturePropertiesData m_data;

  public EditFeaturePropertiesOperation( final EditFeaturePropertiesData data )
  {
    m_data = data;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
  {
    try
    {
      final Feature[] m_selectedFeatures = m_data.getFeatures();
      final IPropertyType property = m_data.getProperty();
      final FeaturePropertyOperation operation = m_data.getOperation();
      final Object value = m_data.getValue();
      final CommandableWorkspace workspace = m_data.getWorkspace();

      final FeatureChange[] changeArray = new FeatureChange[m_selectedFeatures.length];
      for( int i = 0; i < m_selectedFeatures.length; i++ )
        changeArray[i] = createChange( m_selectedFeatures[i], property, value, operation );

      final ChangeFeaturesCommand changeFeaturesCommand = new ChangeFeaturesCommand( workspace, changeArray );
      workspace.postCommand( changeFeaturesCommand );
      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new InvocationTargetException( e );
    }
  }

  private FeatureChange createChange( final Feature feature, final IPropertyType property, final Object value, final FeaturePropertyOperation operation )
  {
    if( m_data.getNumeric() )
    {
      final Number val = (Number) value;
      return new RelativeFeatureChange( feature, (IValuePropertyType) property, operation.toString(), val.doubleValue() );
    }
    else
      return new FeatureChange( feature, property, value );
  }
}