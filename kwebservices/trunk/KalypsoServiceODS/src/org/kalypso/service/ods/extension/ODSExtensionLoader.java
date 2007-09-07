package org.kalypso.service.ods.extension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.service.ods.IODSOperation;


/**
 * @author alibu
 */
public class ODSExtensionLoader
{
  private static Map<String, IConfigurationElement> THE_MAP = null;
  private static HashMap<String, ServiceMetadata> SM_MAP;
  

  private ODSExtensionLoader( )
  {
    // will not be instantiated
  }

  public static IODSOperation createOWSOperation( final String id ) throws CoreException
  {
    final Map<String, IConfigurationElement> elts = getOperations();
    System.out.println("looking for service: "+id);
    
    final IConfigurationElement element = elts.get( id );
    if( element == null )
      return null;

    System.out.println("found service: "+id);
    return (IODSOperation) element.createExecutableExtension( "class" );
  }

  /**
   * 
   * @param service
   * @return List of all operations registered for the service
   */
  public static Set<String> getOperationsForService(String service)
  {
	  final Map<String, IConfigurationElement> elts = getOperations();
	  if (elts != null)
		  return elts.keySet();
	  return null;
  }
  
  
  private synchronized static Map<String, IConfigurationElement> getOperations( )
  {
    if( THE_MAP != null && THE_MAP.size()>0)
      return THE_MAP;

    THE_MAP = new HashMap<String, IConfigurationElement>();
    final IExtensionRegistry er = Platform.getExtensionRegistry();
    if( er != null )
    {
      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.service.ods.odsoperation" );
      for( final IConfigurationElement element : configurationElementsFor )
      {
        final String id = element.getAttribute( "id" );
        THE_MAP.put( id, element );
        System.out.println("adding service: "+id);
      }
    }
    return THE_MAP;
  }
  
  
  /**
   * TODO: version parameter is not implemented; first operation matching operation parameter is returned
   * 
   * @param service
   * @param operation
   * @param version
   * @return
   */
  public static List<OperationParameter> getParametersForOperation(String service, String operation, String version)
  {
	  final Map<String, ServiceMetadata> smMap = createServiceMetadata();
	  
	  if (smMap != null )
	  {
		  ServiceMetadata sm = smMap.get(service);
		  if (sm!=null)
		  {
			  List<OperationMetadata> ops= sm.getOperations();
			  for (OperationMetadata om : ops) {
				if (om.getOperationId().equals(operation))
				{
					//TODO: Versionsvergleich einbauen
					return om.getParameters();
				}
			}
			  
		  }
	  }
	  return null;
  }
  
  private synchronized static HashMap<String, ServiceMetadata> createServiceMetadata()
  {
	   if (SM_MAP != null )
		   return SM_MAP;
	   else
	   {
		   
		SM_MAP=new HashMap<String, ServiceMetadata>();  
	
		List<OperationMetadata> operationsList=new ArrayList<OperationMetadata>();
	
		final IExtensionRegistry er = Platform.getExtensionRegistry();
	    if( er != null )
	    {
	      final IConfigurationElement[] configurationElementsFor = er.getConfigurationElementsFor( "org.kalypso.service.ods.odsoperation" );
	      for( final IConfigurationElement element : configurationElementsFor )
	      {
	    	  
	        final String opId = element.getAttribute( "id" );
	        final String opDescription = element.getAttribute( "description" );
	        final String opVersion = element.getAttribute( "version" );
	        final String opService = element.getAttribute( "service" );
	        
	        
	        List<OperationParameter> parameterList=new ArrayList<OperationParameter>();
	        IConfigurationElement[] parameters = element.getChildren( "parameter");
	        for (IConfigurationElement parameter : parameters) {
	        	
	        	String paramName=parameter.getAttribute("name");
	        	String paramDescription=parameter.getAttribute("description");
	        	
	        	String isMandatoryString = parameter.getAttribute("isMandatory");
	        	boolean isMandatory=false;
	        	if ("true".equals(isMandatoryString))
	        		isMandatory=true;
	        	
	        	ArrayList<String> valueList=new ArrayList<String>();
				IConfigurationElement[] parameterValues = parameter.getChildren( "parameterValue");
				for (IConfigurationElement parameterValue: parameterValues) {
					String value=parameterValue.getAttribute("value");
					valueList.add(value);
				}
				OperationParameter op=new OperationParameter(paramName, paramDescription, isMandatory, valueList);
				parameterList.add(op);
			}
	        OperationMetadata om=new OperationMetadata(opService, opId, opDescription, opVersion, parameterList);
	        operationsList.add(om);
	      }
	      
	    }
	    
	    for (OperationMetadata operation : operationsList) {
	    	ServiceMetadata sm = SM_MAP.get(operation.getService());
			if ( sm == null)
			{
				sm=new ServiceMetadata(operation.getService());
				sm.addOperation(operation);
				SM_MAP.put(operation.getService(), sm);
			}
			else
			{
				sm.addOperation(operation);
			}
		}
	    
	    
	    return SM_MAP;
	   }
  }

}
