package org.kalypso.gmlschema.types;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * Dies sollte irgenwo zentral liegen oder in eine andere solche Klasse integriert werden
 * 
 * @author belger
 */
public class MarshallingTypeRegistrySingleton
{
  private static final String TYPE_HANDLERS_EXTENSION_POINT = "org.kalypso.gmlschema.typeHandlers"; //$NON-NLS-1$

  private static final String TYPE_HANDLER_FACTORY_CLASS = "factory"; //$NON-NLS-1$

  private static ITypeRegistry<IMarshallingTypeHandler> m_typeRegistry = null;

  private MarshallingTypeRegistrySingleton( )
  {
    // wird nicht instantiiert
  }

  @SuppressWarnings("unchecked")
  public synchronized static ITypeRegistry<IMarshallingTypeHandler> getTypeRegistry( )
  {
    if( m_typeRegistry == null )
    {
      m_typeRegistry = new TypeRegistry_impl<IMarshallingTypeHandler>();
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IExtensionPoint extensionPoint = registry.getExtensionPoint( TYPE_HANDLERS_EXTENSION_POINT );
      final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();
      for( final IConfigurationElement element : configurationElements )
      {
        try
        {
          final ITypeHandlerFactory<IMarshallingTypeHandler> factory = (ITypeHandlerFactory<IMarshallingTypeHandler>) element.createExecutableExtension( TYPE_HANDLER_FACTORY_CLASS );
          factory.registerTypeHandlers( m_typeRegistry );
        }
        catch( final CoreException e )
        {
          // TODO handle exception
          e.printStackTrace();
        }
        catch( final TypeRegistryException e )
        {
          // TODO handle exception
          e.printStackTrace();
        }
      }
    }

    return m_typeRegistry;
  }

  public synchronized static void reloadTypeRegistry( )
  {
    m_typeRegistry = null;
    getTypeRegistry();
  }
}