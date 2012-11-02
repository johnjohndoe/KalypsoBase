package org.kalypso.ogc.gml.gui;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.kalypso.gmlschema.types.ITypeHandlerFactory;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.TypeRegistry_impl;
import org.kalypso.ui.KalypsoUIExtensions;

/**
 * Dies sollte irgenwo zentral liegen oder in eine andere solche Klasse integriert werden
 * 
 * @author belger
 */
public class GuiTypeRegistrySingleton
{
  private static ITypeRegistry<IGuiTypeHandler> m_typeRegistry = null;

  private GuiTypeRegistrySingleton( )
  {
    // wird nicht instantiiert
  }

  public static synchronized ITypeRegistry<IGuiTypeHandler> getTypeRegistry( )
  {
    if( m_typeRegistry == null )
    {
      m_typeRegistry = new TypeRegistry_impl<>();

      final ITypeHandlerFactory<IGuiTypeHandler>[] factories = KalypsoUIExtensions.createGuiTypeHandlerFactories();
      for( final ITypeHandlerFactory<IGuiTypeHandler> factory : factories )
      {
        try
        {
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

  public static synchronized void reloadTypeRegistry( )
  {
    m_typeRegistry = null;
    getTypeRegistry();
  }
}