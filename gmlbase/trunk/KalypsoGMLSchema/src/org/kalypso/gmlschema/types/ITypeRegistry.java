package org.kalypso.gmlschema.types;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyType;

/**
 * Die Type - Registry dient dazu, neue Typen im System anzumelden und die entsprechende Handler zu finden.
 * 
 * TODO use generics?
 * 
 * @author belger
 */
public interface ITypeRegistry
{
  /** Removes every entry from the registry */
  public void clearRegistry();
  
  public void registerTypeHandler( final ITypeHandler typeHandler ) throws TypeRegistryException;

  public void unregisterTypeHandler( final ITypeHandler typeHandler );

  public boolean hasTypeName( final QName typeName );

  /**
   * TODO: also depcrecate? see getTypeHandlerForClassName
   */
  public boolean hasClassName( final Class className );

  public ITypeHandler getTypeHandlerForTypeName( final QName typeQName );

  /**
   * @deprecated use getTypehandlerForTypeName(QName qname) or getTypeHandlerFor(IPropertyType pt) there might be more
   *             that one typehandler for a class
   */
  @Deprecated
  public ITypeHandler getTypeHandlerForClassName( final Class clazz );

  public ITypeHandler getTypeHandlerFor( IPropertyType ftp );

  public ITypeHandler[] getRegisteredTypeHandler( );
}