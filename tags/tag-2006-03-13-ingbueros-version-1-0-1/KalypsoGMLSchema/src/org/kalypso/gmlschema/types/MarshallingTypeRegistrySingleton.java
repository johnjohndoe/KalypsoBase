package org.kalypso.gmlschema.types;


/**
 * Dies sollte irgenwo zentral liegen oder in eine andere solche Klasse integriert werden
 * 
 * @author belger
 */
public class MarshallingTypeRegistrySingleton
{
  private static ITypeRegistry m_typeRegistry = null;

  private MarshallingTypeRegistrySingleton()
  {
  // wird nicht instantiiert
  }

  public static ITypeRegistry getTypeRegistry()
  {
    if( m_typeRegistry == null )
      m_typeRegistry = new TypeRegistry_impl();

    return m_typeRegistry;
  }
}