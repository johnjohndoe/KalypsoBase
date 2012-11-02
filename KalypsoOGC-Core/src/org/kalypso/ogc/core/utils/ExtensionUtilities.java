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
package org.kalypso.ogc.core.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.ogc.core.exceptions.ExceptionCode;
import org.kalypso.ogc.core.exceptions.OWSException;
import org.kalypso.ogc.core.operations.IOGCOperation;
import org.kalypso.ogc.core.service.IOGCService;

/**
 * This utilities class provides functions for reading the extension point <code>org.kalypso.ogc.core.service</code>.
 *
 * @author Toni DiNardo
 */
public class ExtensionUtilities
{
  private static final String SERVICE_EXTENSION_POINT = "org.kalypso.ogc.core.service"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT = "service"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_VERSION_ATTRIBUTE = "version"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT = "operation"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT_NAME_ATTRIBUTE = "name"; //$NON-NLS-1$

  private static final String SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT_CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$

  /**
   * The constructor.
   */
  private ExtensionUtilities( )
  {
  }

  /**
   * This function returns all in the extension registry registered OGC services.
   *
   * @return All in the extension registry registered OGC services.
   */
  public static IOGCService[] getServices( ) throws OWSException
  {
    try
    {
      /* Memory for the OGC services. */
      final List<IOGCService> services = new ArrayList<>();

      /* Get the extension registry. */
      final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

      /* Get the extension point. */
      final IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint( SERVICE_EXTENSION_POINT );

      /* Get all configuration elements. */
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      for( final IConfigurationElement configurationElement : configurationElements )
      {
        /* If the configuration element is not the right one, continue. */
        if( !SERVICE_EXTENSION_POINT_SERVICE_ELEMENT.equals( configurationElement.getName() ) )
          continue;

        /* Add the OGC service. */
        services.add( (IOGCService) configurationElement.createExecutableExtension( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_CLASS_ATTRIBUTE ) );
      }

      return services.toArray( new IOGCService[] {} );
    }
    catch( final Exception ex )
    {
      throw new OWSException( String.format( "Encountered an error while preparing the execution of the request. Cause: %s", ex.getMessage() ), ex, OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
    }
  }

  /**
   * This function searches the extension registry for a OGC service matching the given name and version.
   *
   * @param serviceName
   *          The name of the requested OGC service.
   * @param serviceVersion
   *          The version of the requested OGC service.
   * @return The OGC service matching the given name and version.
   */
  public static IOGCService getService( final String serviceName, final String serviceVersion ) throws OWSException
  {
    /* Get the service element. */
    final IConfigurationElement serviceElement = getServiceElement( serviceName, serviceVersion );
    if( serviceElement == null )
      throw new OWSException( String.format( "This server does not provide a service with the name '%s' and version '%s'.", serviceName, serviceVersion ), OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    try
    {
      return (IOGCService) serviceElement.createExecutableExtension( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_CLASS_ATTRIBUTE );
    }
    catch( final Exception ex )
    {
      throw new OWSException( String.format( "Encountered an error while preparing the execution of the request. Cause: %s", ex.getMessage() ), ex, OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
    }
  }

  /**
   * This function returns all in the extension registry registered OGC operations.
   *
   * @param serviceName
   *          The name of the requested OGC service.
   * @param serviceVersion
   *          The version of the requested OGC service.
   * @return All in the extension registry registered OGC operations.
   */
  public static IOGCOperation[] getOperations( final String serviceName, final String serviceVersion ) throws OWSException
  {
    try
    {
      /* Memory for the OGC operations. */
      final List<IOGCOperation> operations = new ArrayList<>();

      /* Get the service element. */
      final IConfigurationElement serviceElement = getServiceElement( serviceName, serviceVersion );
      if( serviceElement == null )
        throw new OWSException( String.format( "This server does not provide a service with the name '%s' and version '%s'.", serviceName, serviceVersion ), OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

      /* Get all configuration elements. */
      final IConfigurationElement[] configurationElements = serviceElement.getChildren( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT );
      for( final IConfigurationElement configurationElement : configurationElements )
      {
        /* If the configuration element is not the right one, continue. */
        if( !SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT.equals( configurationElement.getName() ) )
          continue;

        /* Add the OGC operation. */
        operations.add( (IOGCOperation) configurationElement.createExecutableExtension( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT_CLASS_ATTRIBUTE ) );
      }

      return operations.toArray( new IOGCOperation[] {} );
    }
    catch( final Exception ex )
    {
      throw new OWSException( String.format( "Encountered an error while preparing the execution of the request. Cause: %s", ex.getMessage() ), ex, OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
    }
  }

  /**
   * This function searches the extension registry for an OGC operation matching the given name.
   *
   * @param serviceName
   *          The name of the requested OGC service.
   * @param serviceVersion
   *          The version of the requested OGC service.
   * @param operationName
   *          The name of the requested OGC operation.
   * @return The OGC operation matching the given name.
   */
  public static IOGCOperation getOperation( final String serviceName, final String serviceVersion, final String operationName ) throws OWSException
  {
    /* Get the service element. */
    final IConfigurationElement serviceElement = getServiceElement( serviceName, serviceVersion );
    if( serviceElement == null )
      throw new OWSException( String.format( "This server does not provide a service with the name '%s' and version '%s'.", serviceName, serviceVersion ), OWSUtilities.OWS_VERSION, "en", ExceptionCode.INVALID_PARAMETER_VALUE, null );

    /* Get the operation element. */
    final IConfigurationElement operationElement = getOperationElement( serviceElement, operationName );
    if( operationElement == null )
      throw new OWSException( String.format( "The service with the name '%s' and version '%s' does not provide an operation with the name '%s'.", serviceName, serviceVersion, operationName ), OWSUtilities.OWS_VERSION, "en", ExceptionCode.OPERATION_NOT_SUPPORTED, null );

    try
    {
      return (IOGCOperation) operationElement.createExecutableExtension( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT_CLASS_ATTRIBUTE );
    }
    catch( final Exception ex )
    {
      throw new OWSException( String.format( "Encountered an error while preparing the execution of the request. Cause: %s", ex.getMessage() ), ex, OWSUtilities.OWS_VERSION, "en", ExceptionCode.NO_APPLICABLE_CODE, null );
    }
  }

  /**
   * This function searches the extension registry for a service element matching the given name and version.
   *
   * @param serviceName
   *          The name of the requested service element.
   * @param serviceVersion
   *          The version of the requested service element.
   * @return The service element matching the given name and version.
   */
  private static IConfigurationElement getServiceElement( final String serviceName, final String serviceVersion )
  {
    /* Get the extension registry. */
    final IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();

    /* Get the extension point. */
    final IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint( SERVICE_EXTENSION_POINT );

    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
    for( final IConfigurationElement configurationElement : configurationElements )
    {
      /* If the configuration element is not the right one, continue. */
      if( !SERVICE_EXTENSION_POINT_SERVICE_ELEMENT.equals( configurationElement.getName() ) )
        continue;

      /* Get the attributes. */
      final String nameAttribute = configurationElement.getAttribute( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_NAME_ATTRIBUTE );
      final String versionAttribute = configurationElement.getAttribute( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_VERSION_ATTRIBUTE );
      if( nameAttribute.equals( serviceName ) && versionAttribute.equals( serviceVersion ) )
        return configurationElement;
    }

    return null;
  }

  /**
   * This function searches the extension registry for an operation element matching the given name.
   *
   * @param serviceElement
   *          The service element.
   * @param operationName
   *          The name of the requested operation element.
   * @return The operation element matching the given name.
   */
  private static IConfigurationElement getOperationElement( final IConfigurationElement serviceElement, final String operationName )
  {
    /* Get all configuration elements. */
    final IConfigurationElement[] configurationElements = serviceElement.getChildren( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT );
    for( final IConfigurationElement configurationElement : configurationElements )
    {
      /* If the configuration element is not the right one, continue. */
      if( !SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT.equals( configurationElement.getName() ) )
        continue;

      /* Get the attributes. */
      final String nameAttribute = configurationElement.getAttribute( SERVICE_EXTENSION_POINT_SERVICE_ELEMENT_OPERATION_ELEMENT_NAME_ATTRIBUTE );
      if( nameAttribute.equals( operationName ) )
        return configurationElement;
    }

    return null;
  }
}