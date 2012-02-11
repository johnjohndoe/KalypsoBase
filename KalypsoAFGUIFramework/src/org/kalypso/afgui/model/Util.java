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
package org.kalypso.afgui.model;

import javax.xml.namespace.QName;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.kalypso.afgui.i18n.Messages;
import org.kalypso.commons.command.ICommand;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

import de.renew.workflow.connector.cases.ICaseDataProvider;
import de.renew.workflow.contexts.ICaseHandlingSourceProvider;

/**
 * TODO: most of the methods should be moved into {@link FeatureHelper}.
 *
 * Holds utility methods
 *
 * @author Patrice Congo
 *
 */
public class Util
{

  @SuppressWarnings("unchecked")
  public static final CommandableWorkspace getCommandableWorkspace( final Class< ? extends IModel> modelClass )
  {
    try
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IHandlerService service = (IHandlerService) workbench.getService( IHandlerService.class );
      final IEvaluationContext currentState = service.getCurrentState();
      final ICaseDataProvider<Feature> caseDataProvider = (ICaseDataProvider<Feature>) currentState.getVariable( ICaseHandlingSourceProvider.ACTIVE_CASE_DATA_PROVIDER_NAME );
      if( caseDataProvider instanceof ICommandPoster )
        return ((ICommandPoster) caseDataProvider).getCommandableWorkSpace( modelClass.getName() );
      else
        throw new RuntimeException( Messages.getString( "org.kalypso.afgui.model.Util.0" ) ); //$NON-NLS-1$
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public static final void postCommand( final Class< ? extends IModel> modelClass, final ICommand command )
  {
    try
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IHandlerService service = (IHandlerService) workbench.getService( IHandlerService.class );
      final IEvaluationContext currentState = service.getCurrentState();
      final ICaseDataProvider<IModel> caseDataProvider = (ICaseDataProvider<IModel>) currentState.getVariable( ICaseHandlingSourceProvider.ACTIVE_CASE_DATA_PROVIDER_NAME );
      if( caseDataProvider instanceof ICommandPoster )
        ((ICommandPoster) caseDataProvider).postCommand( modelClass.getName(), command );
      else
        throw new RuntimeException( Messages.getString( "org.kalypso.afgui.model.Util.1" ) ); //$NON-NLS-1$
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      throw new RuntimeException( th );
    }
  }

  /**
   * Gets the szenario model
   */
  public static final <T extends IModel> T getModel( final String modelId, final Class<T> modelClass )
  {
    try
    {
      final IWorkbench workbench = PlatformUI.getWorkbench();
      final IHandlerService service = (IHandlerService) workbench.getService( IHandlerService.class );
      final IEvaluationContext currentState = service.getCurrentState();
      @SuppressWarnings("unchecked")
      final ICaseDataProvider<IModel> caseDataProvider = (ICaseDataProvider<IModel>) currentState.getVariable( ICaseHandlingSourceProvider.ACTIVE_CASE_DATA_PROVIDER_NAME );
      return caseDataProvider.getModel( modelId, modelClass );
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
      return null;
    }
  }

  public static final Feature createFeatureAsProperty( final Feature parentFeature, final QName propQName, final QName featureQName, final Object[] featureProperties, final QName[] featurePropQNames ) throws IllegalArgumentException
  {
    Assert.isNotNull( propQName, Messages.getString( "org.kalypso.afgui.model.Util.19" ) ); //$NON-NLS-1$
    Assert.isNotNull( parentFeature, Messages.getString( "org.kalypso.afgui.model.Util.20" ) ); //$NON-NLS-1$

    try
    {
      final IPropertyType property = parentFeature.getFeatureType().getProperty( propQName );
      if( property.isList() )
      {
        final Feature feature = FeatureHelper.addFeature( parentFeature, propQName, featureQName, featureProperties, featurePropQNames );

        return feature;
      }
      else
      {
        final GMLWorkspace workspace = parentFeature.getWorkspace();
        final IFeatureType newFeatureType = GMLSchemaUtilities.getFeatureTypeQuiet( featureQName );
        final Feature feature = workspace.createFeature( parentFeature, (IRelationType) property, newFeatureType );
        for( int i = featureProperties.length - 1; i >= 0; i-- )
        {
          feature.setProperty( featurePropQNames[i], featureProperties[i] );
        }

        parentFeature.setProperty( property, feature );
        return feature;
      }

    }
    catch( final GMLSchemaException ex )
    {
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.afgui.model.Util.21" ) + propQName + Messages.getString( "org.kalypso.afgui.model.Util.22" ) + featureQName, ex ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }
}
