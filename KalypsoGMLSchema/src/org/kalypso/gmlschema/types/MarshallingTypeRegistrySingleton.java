package org.kalypso.gmlschema.types;


/**
 * Dies sollte irgenwo zentral liegen oder in eine andere solche Klasse integriert werden
 * 
 * @author belger
 */
public class MarshallingTypeRegistrySingleton
{
  private static ITypeRegistry<IMarshallingTypeHandler> m_typeRegistry = null;

  private MarshallingTypeRegistrySingleton()
  {
  // wird nicht instantiiert
  }

  public static ITypeRegistry<IMarshallingTypeHandler> getTypeRegistry()
  {
    if( m_typeRegistry == null )
      m_typeRegistry = new TypeRegistry_impl<IMarshallingTypeHandler>();

    return m_typeRegistry;
  }
}