package org.kalypso.gmlschema.types;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;

/**
 * Standardimplementation von {@link org.kalypsodeegree_impl.extension.ITypeRegistry}.
 * 
 * @author belger
 */
public class TypeRegistry_impl implements ITypeRegistry
{
  /** typeName -> handler */
  private Map<QName, ITypeHandler> m_typeMap = new HashMap<QName, ITypeHandler>();

  /** className -> handler */
  private Map<Class, ITypeHandler> m_classMap = new HashMap<Class, ITypeHandler>();

  /**
   * Falls TypeName oder ClassName bereits belegt sind
   * 
   * @throws TypeRegistryException
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#registerTypeHandler(org.kalypsodeegree_impl.extension.IMarshallingTypeHandler)
   */
  public void registerTypeHandler( final ITypeHandler typeHandler ) throws TypeRegistryException
  {
    final QName[] typeNames = typeHandler.getTypeName();
    final Class className = typeHandler.getValueClass();

    for( int i = 0; i < typeNames.length; i++ )
    {
      final QName typeName = typeNames[i];
      if( m_typeMap.containsKey( typeName ) )
        throw new TypeRegistryException( "Typname wurde bereits registriert: " + typeName );
    }
    if( m_classMap.containsKey( className ) )
      throw new TypeRegistryException( "Classname wurde bereits registriert: " + className );

    for( int i = 0; i < typeNames.length; i++ )
    {
      final QName typeName = typeNames[i];
      m_typeMap.put( typeName, typeHandler );
    }

    m_classMap.put( className, typeHandler );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#getTypeHandlerForTypeName(java.lang.String)
   */
  public ITypeHandler getTypeHandlerForTypeName( final QName typeName )
  {
    if( !hasTypeName( typeName ) )
      return null;
    return m_typeMap.get( typeName );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#getTypeHandlerForClassName(java.lang.String)
   */
  public ITypeHandler getTypeHandlerForClassName( final Class className )
  {
    if( !hasClassName( className ) )
      return null;
    return m_classMap.get( className );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#unregisterTypeHandler(org.kalypsodeegree_impl.extension.IMarshallingTypeHandler)
   */
  public void unregisterTypeHandler( ITypeHandler typeHandler )
  {
    m_typeMap.remove( typeHandler.getTypeName() );
    m_classMap.remove( typeHandler.getValueClass() );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#hasTypeName(java.lang.String)
   */
  public boolean hasTypeName( final QName typeName )
  {
    return m_typeMap.containsKey( typeName );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#hasClassName(java.lang.String)
   */
  public boolean hasClassName( final Class className )
  {
    return m_classMap.containsKey( className );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#getTypeHandlerFor(org.kalypso.gmlschema.property.IPropertyType)
   */
  public ITypeHandler getTypeHandlerFor( final IPropertyType pt )
  {
    if( !(pt instanceof IValuePropertyType) )
      throw new UnsupportedOperationException();
    return getTypeHandlerForTypeName( ((IValuePropertyType) pt).getValueQName() );
  }
}