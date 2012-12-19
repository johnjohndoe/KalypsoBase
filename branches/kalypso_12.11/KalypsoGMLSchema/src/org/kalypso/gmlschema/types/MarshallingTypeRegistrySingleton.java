package org.kalypso.gmlschema.types;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

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

  public synchronized static ITypeRegistry<IMarshallingTypeHandler> getTypeRegistry( )
  {
    if( m_typeRegistry == null )
    {
      m_typeRegistry = new TypeRegistry_impl<>();
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
        catch( final Exception e ) // generic exception caught for simplicity
        {
          e.printStackTrace();
          // this method is also used in headless mode
          if( PlatformUI.isWorkbenchRunning() )
          {
            MessageDialog.openError( PlatformUI.getWorkbench().getDisplay().getActiveShell(), "Interne Applikationsfehler", e.getLocalizedMessage() ); //$NON-NLS-1$
          }
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