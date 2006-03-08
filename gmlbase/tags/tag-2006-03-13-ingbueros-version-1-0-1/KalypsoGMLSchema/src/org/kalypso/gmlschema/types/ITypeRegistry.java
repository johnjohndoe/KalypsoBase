package org.kalypso.gmlschema.types;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyType;

/**
 * Die Type - Registry dient dazu, neue Typen im System anzumelden und die entsprechende Handler zu finden.
 * 
 * @author belger
 */
public interface ITypeRegistry
{
  public void registerTypeHandler( final ITypeHandler typeHandler ) throws TypeRegistryException;

  public void unregisterTypeHandler( final ITypeHandler typeHandler );

  public boolean hasTypeName( final QName typeName );

  public boolean hasClassName( final Class className );

  public ITypeHandler getTypeHandlerForTypeName( final QName typeQName );

  public ITypeHandler getTypeHandlerForClassName( final Class clazz );

  public ITypeHandler getTypeHandlerFor( IPropertyType ftp );
}