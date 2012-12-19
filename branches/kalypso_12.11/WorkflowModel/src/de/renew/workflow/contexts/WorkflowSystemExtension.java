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
package de.renew.workflow.contexts;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.xml.bind.JAXBContext;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.bind.JaxbUtilities;
import org.kalypso.commons.i18n.ResourceBundleUtils;
import org.osgi.framework.Bundle;

import de.renew.workflow.base.IWorkflow;
import de.renew.workflow.base.Workflow;
import de.renew.workflow.base.impl.Workflow_Impl;
import de.renew.workflow.utils.IgnoreFolder;
import de.renew.workflow.utils.ScenarioConfiguration;

/**
 * Helper class to read and cache workflow systems from extension point.
 *
 * @author Stefan Kurzbach
 */
public class WorkflowSystemExtension
{
  public static final JAXBContext JC = JaxbUtilities.createQuiet( de.renew.workflow.base.ObjectFactory.class, de.renew.workflow.contexts.ObjectFactory.class, de.renew.workflow.cases.ObjectFactory.class );

  private static Map<String, IWorkflow> THE_WORKFLOW_MAP = null;

  private final static String WORKFLOW_SYSTEM_EXTENSION_POINT = "de.renew.workflow.model.workflowSystem"; //$NON-NLS-1$

  private static final String WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_ELEMENT = "scenarioConfiguration"; //$NON-NLS-1$

  private static final String WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_DERIVED_FOLDER = "derivedFolder"; //$NON-NLS-1$

  private static final String WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_ELEMENT = "ignoreFolder"; //$NON-NLS-1$

  private static final String WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_ROLE_NAME = "roleName"; //$NON-NLS-1$

  private static final String WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_FOLDER_NAME = "folderName"; //$NON-NLS-1$

  public static IWorkflow getWorkflow( final String id )
  {
    final Map<String, IWorkflow> map = getWorkflowMap();
    if( map == null )
      return null;

    return map.get( id );
  }

  private synchronized static Map<String, IWorkflow> getWorkflowMap( )
  {
    if( THE_WORKFLOW_MAP == null )
    {
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( WORKFLOW_SYSTEM_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      THE_WORKFLOW_MAP = new HashMap<>( configurationElements.length );

      for( final IConfigurationElement element : configurationElements )
      {
        final String id = element.getAttribute( "id" ); //$NON-NLS-1$
        try
        {
          final String filePath = element.getAttribute( "definition" ); //$NON-NLS-1$
          final IContributor contributor = element.getContributor();
          final Bundle bundle = Platform.getBundle( contributor.getName() );
          final URL location = bundle.getEntry( filePath );
          if( location != null )
          {
            final IWorkflow workflow = loadModel( location, getScenarioConfiguration( element ) );
            THE_WORKFLOW_MAP.put( id, workflow );
          }
          else
          {
            throw new CoreException( new Status( IStatus.ERROR, "de.renew.workflow.model", "Invalid path " + filePath ) ); //$NON-NLS-1$ //$NON-NLS-2$
          }
        }
        catch( final CoreException e )
        {
          // in this moment logger is not available...
          e.printStackTrace();
        }
      }
    }

    return THE_WORKFLOW_MAP;
  }

  /**
   * This function returns the scenario configuration. May be null, if the configuration element contains no children or
   * only children of the wrong type.
   *
   * @param element
   *          The configuration element.
   *
   * @return The scenario configuration or null.
   */
  private static ScenarioConfiguration getScenarioConfiguration( final IConfigurationElement element )
  {
    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = element.getChildren();
    for( final IConfigurationElement configurationElement : configurationElements )
    {
      /* If the configuration element is not the correct element, continue. */
      if( !WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_ELEMENT.equals( configurationElement.getName() ) )
        continue;

      /* Get the attributes. */
      final String derivedFolder = configurationElement.getAttribute( WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_DERIVED_FOLDER );

      /* The ignore folders. */
      final List<IgnoreFolder> ignoreFolders = new ArrayList<>();

      /* Get the configuration element for the ignore folders. */
      final IConfigurationElement[] children = configurationElement.getChildren( WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_ELEMENT );
      for( final IConfigurationElement oneChildren : children )
      {
        final String roleName = oneChildren.getAttribute( WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_ROLE_NAME );
        final String folderName = oneChildren.getAttribute( WORKFLOW_SYSTEM_SCENARIO_CONFIGURATION_IGNORE_FOLDER_FOLDER_NAME );

        ignoreFolders.add( new IgnoreFolder( roleName, folderName ) );
      }

      /* We know, that there is only one configuration element of that type. */
      return new ScenarioConfiguration( derivedFolder, ignoreFolders.toArray( new IgnoreFolder[] {} ) );
    }

    return null;
  }

  private static IWorkflow loadModel( final URL location, final ScenarioConfiguration scenarioConfiguration ) throws CoreException
  {
    try
    {
      final Workflow workflow = (Workflow) JC.createUnmarshaller().unmarshal( location );
      final ResourceBundle resourceBundle = ResourceBundleUtils.loadResourceBundle( location );
      return new Workflow_Impl( workflow, resourceBundle, location, scenarioConfiguration );
    }
    catch( final Throwable e )
    {
      final IStatus status = new Status( IStatus.ERROR, "de.renew.workflow.model", "", e ); //$NON-NLS-1$ //$NON-NLS-2$
      throw new CoreException( status );
    }
  }
}
