package de.openali.odysseus.service.ods.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import de.openali.odysseus.service.ods.Activator;

/**
 * loads operation extensions and prepares the operations metadata section for the ServiceMetadataDocument
 *
 * @author alibu
 */
public class OWSOperationExtensionLoader
{
  private static Map<String, IConfigurationElement> THE_MAP = null;

  private static HashMap<String, ServiceMetadata> SM_MAP;

  public final static String EXTENSION_POINT_ID = Activator.PLUGIN_ID + ".owsoperation";

  private OWSOperationExtensionLoader( )
  {
    // will not be instantiated
  }

  public synchronized static Map<String, IConfigurationElement> getOperations( )
  {
    THE_MAP = new HashMap<>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( EXTENSION_POINT_ID );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        THE_MAP.put( id, element );
        System.out.println( "adding operation: " + id );
      }
    }
    return THE_MAP;
  }

  /**
   * @param service
   * @param operation
   * @return
   */
  public static List<OperationParameter> getParametersForOperation( final String operation )
  {
    final Map<String, ServiceMetadata> smMap = createServiceMetadata();

    if( smMap != null )
    {
      final ServiceMetadata sm = smMap.get( "ODS" );
      if( sm != null )
      {
        final List<OperationMetadata> ops = sm.getOperations();
        for( final OperationMetadata om : ops )
          if( om.getOperationId().equals( operation ) )
            // TODO: Versionsvergleich einbauen
            return om.getParameters();

      }
    }
    return null;
  }

  private synchronized static HashMap<String, ServiceMetadata> createServiceMetadata( )
  {
    if( SM_MAP != null )
      return SM_MAP;
    else
    {

      SM_MAP = new HashMap<>();

      final List<OperationMetadata> operationsList = new ArrayList<>();

      final IExtensionRegistry er = Platform.getExtensionRegistry();
      if( er != null )
      {
        final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( EXTENSION_POINT_ID );
        for( final IConfigurationElement element : configurationElementsFor )
        {

          final String opId = element.getAttribute( "id" );
          final String opDescription = element.getAttribute( "description" );
          final String opService = element.getAttribute( "service" );

          final String isPublicString = element.getAttribute( "isPublic" );
          boolean isPublic = false;
          if( "true".equals( isPublicString ) )
            isPublic = true;

          final List<OperationParameter> parameterList = new ArrayList<>();
          final IConfigurationElement[] parameters = element.getChildren( "parameter" );
          for( final IConfigurationElement parameter : parameters )
          {

            final String paramName = parameter.getAttribute( "name" );
            final String paramDescription = parameter.getAttribute( "description" );

            final String isMandatoryString = parameter.getAttribute( "isMandatory" );
            boolean isMandatory = false;
            if( "true".equals( isMandatoryString ) )
              isMandatory = true;

            final ArrayList<String> valueList = new ArrayList<>();
            final IConfigurationElement[] parameterValues = parameter.getChildren( "parameterValue" );
            for( final IConfigurationElement parameterValue : parameterValues )
            {
              final String value = parameterValue.getAttribute( "value" );
              valueList.add( value );
            }
            final OperationParameter op = new OperationParameter( paramName, paramDescription, isMandatory, valueList );
            parameterList.add( op );
          }
          final OperationMetadata om = new OperationMetadata( opService, opId, opDescription, parameterList, isPublic );
          operationsList.add( om );
        }

      }

      for( final OperationMetadata operation : operationsList )
      {
        ServiceMetadata sm = SM_MAP.get( operation.getService() );
        if( sm == null )
        {
          sm = new ServiceMetadata( operation.getService() );
          sm.addOperation( operation );
          SM_MAP.put( operation.getService(), sm );
        }
        else
          sm.addOperation( operation );
      }

      return SM_MAP;
    }
  }

}
