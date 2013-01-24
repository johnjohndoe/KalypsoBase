package org.kalypso.ogc.gml.gui;

import org.kalypso.gmlschema.types.ITypeHandlerFactory;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.TypeRegistryException;
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
      m_typeRegistry = new TypeRegistry_impl<IGuiTypeHandler>();

      final ITypeHandlerFactory<IGuiTypeHandler>[] factories = KalypsoUIExtensions.createGuiTypeHandlerFactories();
      for( final ITypeHandlerFactory<IGuiTypeHandler> factory : factories )
      {
        try
        {
          factory.registerTypeHandlers( m_typeRegistry );
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

  public static synchronized void reloadTypeRegistry( )
  {
    m_typeRegistry = null;
    getTypeRegistry();
  }
}