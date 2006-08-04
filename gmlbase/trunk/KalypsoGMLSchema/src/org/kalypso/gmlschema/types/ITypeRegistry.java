package org.kalypso.gmlschema.types;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.IPropertyType;

/**
 * Die Type - Registry dient dazu, neue Typen im System anzumelden und die entsprechende Handler zu finden. TODO use
 * generics?
 * 
 * @author belger
 */
public interface ITypeRegistry<H extends ITypeHandler>
{
  /** Removes every entry from the registry */
  public void clearRegistry( );

  public void registerTypeHandler( final H typeHandler ) throws TypeRegistryException;

  public void unregisterTypeHandler( final H typeHandler );

  public boolean hasTypeName( final QName typeName );

  /**
   * TODO: also depcrecate? see getTypeHandlerForClassName
   * 
   * @deprecated see {@link #getTypeHandlerForClassName(Class)}
   */
  @Deprecated
  public boolean hasClassName( final Class className );

  public H getTypeHandlerForTypeName( final QName typeQName );

  /**
   * @deprecated use getTypehandlerForTypeName(QName qname) or getTypeHandlerFor(IPropertyType pt) there might be more
   *             than one typehandler for a class
   */
  @Deprecated
  public H getTypeHandlerForClassName( final Class clazz );

  public H getTypeHandlerFor( final IPropertyType ftp );

  /**
   * @param a
   * @see java.util.Collection#toArray(T[])
   */
  public H[] getRegisteredTypeHandler( final H[] a );
  
  public int getRegisteredTypeHandlerSize();
}