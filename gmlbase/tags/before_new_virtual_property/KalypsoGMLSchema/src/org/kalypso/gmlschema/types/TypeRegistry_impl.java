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
public class TypeRegistry_impl<H extends ITypeHandler> implements ITypeRegistry<H>
{
  /** typeName -> handler */
  private Map<QName, H> m_typeMap = new HashMap<QName, H>();

  /** className -> handler */
  private Map<Class, H> m_classMap = new HashMap<Class, H>();

  /**
   * Falls TypeName oder ClassName bereits belegt sind
   * 
   * @throws TypeRegistryException
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#registerTypeHandler(org.kalypsodeegree_impl.extension.IMarshallingTypeHandler)
   */
  public void registerTypeHandler( final H typeHandler )
  {
    final QName typeName = typeHandler.getTypeName();
    final Class className = typeHandler.getValueClass();
//    if( m_typeMap.containsKey( typeName ) )
//      throw new TypeRegistryException( "Typname wurde bereits registriert: " + typeName );

    m_typeMap.put( typeName, typeHandler );

    // the getTypeHandlerForClassName is deprecated, but still supported so we return the first registered that is
    // possible the most important
    if( !m_classMap.containsKey( className ) )
      m_classMap.put( className, typeHandler );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#getTypeHandlerForTypeName(java.lang.String)
   */
  public H getTypeHandlerForTypeName( final QName typeName )
  {
    if( !hasTypeName( typeName ) )
      return null;
    return m_typeMap.get( typeName );
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeRegistry#getTypeHandlerForClassName(java.lang.Class)
   */
  public H getTypeHandlerForClassName( final Class className )
  {
    if( !hasClassName( className ) )
      return null;
    return m_classMap.get( className );
  }

  /**
   * @see org.kalypsodeegree_impl.extension.ITypeRegistry#unregisterTypeHandler(org.kalypsodeegree_impl.extension.IMarshallingTypeHandler)
   */
  public void unregisterTypeHandler( final H typeHandler )
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
  public H getTypeHandlerFor( final IPropertyType pt )
  {
    if( !(pt instanceof IValuePropertyType) )
      throw new UnsupportedOperationException( "Could not find type handler for: " + pt );
    return getTypeHandlerForTypeName( ((IValuePropertyType) pt).getValueQName() );
  }

  /**
   * @see org.kalypso.gmlschema.types.ITypeRegistry#getRegisteredTypeHandler()
   */
  public H[] getRegisteredTypeHandler( final H[] a )
  {
    return m_typeMap.values().toArray( a );
  }

  public int getRegisteredTypeHandlerSize()
  {
    return m_typeMap.values().size();
  }
  
  /**
   * @see org.kalypso.gmlschema.types.ITypeRegistry#clearRegistry()
   */
  public void clearRegistry( )
  {
    m_classMap.clear();
    m_typeMap.clear();
  }
}