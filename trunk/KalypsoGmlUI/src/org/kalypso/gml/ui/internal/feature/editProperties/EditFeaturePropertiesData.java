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
package org.kalypso.gml.ui.internal.feature.editProperties;

import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.ogc.gml.command.RelativeFeatureChange;
import org.kalypso.ogc.gml.gui.GuiTypeRegistrySingleton;
import org.kalypso.ogc.gml.gui.IGuiTypeHandler;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;

/**
 * @author Gernot Belger
 */
public class EditFeaturePropertiesData extends AbstractModelObject
{
  public static final String PROPERTY_PROPERTY = "property"; //$NON-NLS-1$

  public static final String PROPERTY_VALUE = "value"; //$NON-NLS-1$

  public static final String PROPERTY_PROPERTIES = "properties"; //$NON-NLS-1$

  private static final String PROPERTY_OPERATION = "operation"; //$NON-NLS-1$

  public static final String PROPERTY_ISNUMERIC = "numeric"; //$NON-NLS-1$

  public static final String PROPERTY_VALUE_LABEL = "valueLabel"; //$NON-NLS-1$

  public static final String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$

  private Object m_value = null;

  private IPropertyType[] m_properties = null;

  private IPropertyType m_property = null;

  private Feature m_focusedFeature;

  private FeaturePropertyOperation m_operation = FeaturePropertyOperation.constant;

  private Feature[] m_features;

  private CommandableWorkspace m_workspace;

  public void init( final IStructuredSelection selection )
  {
    if( selection instanceof IFeatureSelection )
    {
      final IFeatureSelection featureSelection = (IFeatureSelection) selection;

      m_features = FeatureSelectionHelper.getFeatures( featureSelection );

      final Feature focusedFeature = featureSelection.getFocusedFeature();

      m_focusedFeature = focusedFeature != null ? focusedFeature : m_features[0];

      m_workspace = featureSelection.getWorkspace( m_focusedFeature );

      final IFeatureType featureType = m_focusedFeature.getFeatureType();
      setProperties( featureType.getProperties() );

      final IPropertyType focusedProperty = featureSelection.getFocusedProperty();
      setProperty( focusedProperty );
    }
  }

  public Object getValue( )
  {
    return m_value;
  }

  public void setValue( final Object value )
  {
    final Object oldValue = m_value;

    m_value = value;

    firePropertyChange( PROPERTY_VALUE, oldValue, value );
  }

  public IPropertyType getProperty( )
  {
    return m_property;
  }

  public void setProperty( final IPropertyType property )
  {
    final Object oldIsNumericValue = getNumeric();
    final Object oldValueLabel = getValueLabel();
    final boolean oldEnabled = getEnabled();
    final Object oldValue = m_property;

    m_property = property;

    firePropertyChange( PROPERTY_PROPERTY, oldValue, property );
    firePropertyChange( PROPERTY_ISNUMERIC, oldIsNumericValue, getNumeric() );
    firePropertyChange( PROPERTY_VALUE_LABEL, oldValueLabel, getValueLabel() );
    firePropertyChange( PROPERTY_ENABLED, oldEnabled, getEnabled() );

    if( m_focusedFeature != null && EditFeaturePropertiesFilter.canEditProperty( property ) )
      setValue( m_focusedFeature.getProperty( property ) );
    else
      setValue( null );
  }

  public IPropertyType[] getProperties( )
  {
    return m_properties;
  }

  public void setProperties( final IPropertyType[] properties )
  {
    final Object oldValue = m_properties;

    m_properties = properties;

    firePropertyChange( PROPERTY_PROPERTIES, oldValue, properties );
  }

  public FeaturePropertyOperation getOperation( )
  {
    return m_operation;
  }

  public void setOperation( final FeaturePropertyOperation operation )
  {
    final Object oldValue = m_operation;

    m_operation = operation;

    firePropertyChange( PROPERTY_OPERATION, oldValue, operation );
  }

  public Feature[] getFeatures( )
  {
    return m_features;
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  public boolean getNumeric( )
  {
    return RelativeFeatureChange.isNumeric( m_property );
  }

  public String getValueLabel( )
  {
    if( getNumeric() )
      return Messages.getString( "EditFeaturePropertiesData_0" ); //$NON-NLS-1$
    else
      return Messages.getString( "EditFeaturePropertiesData_1" ); //$NON-NLS-1$
  }

  public boolean getEnabled( )
  {
    final ITypeRegistry<IGuiTypeHandler> uiRegistry = GuiTypeRegistrySingleton.getTypeRegistry();

    if( m_property == null )
      return false;

    if( m_property instanceof IRelationType )
      return false;

    final IGuiTypeHandler typeHandler = uiRegistry.getTypeHandlerFor( m_property );
    if( typeHandler == null )
      return false;

    try
    {
      // Crude test, if we can edit this with a Text-Control: ttry to parse the empty string,
      typeHandler.parseText( StringUtils.EMPTY, null );
      return true;
    }
    catch( final ParseException e )
    {
      // Parse-Error, but parsing seems to be supported
      return true;
    }
    catch( final UnsupportedOperationException e )
    {
      // parsing not supported
      return false;
    }
  }
}