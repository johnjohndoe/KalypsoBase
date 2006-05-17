package org.kalypso.ogc.swe;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;

public class RepresentationType
{
  private final KIND m_kind;

  private final QName m_valueTypeName;

  public RepresentationType( final KIND kind, final QName valueTypeName )
  {
    m_kind = kind;
    m_valueTypeName = valueTypeName;
  }

  public IMarshallingTypeHandler getTypeHandler( )
  {
    return (IMarshallingTypeHandler) MarshallingTypeRegistrySingleton.getTypeRegistry().getTypeHandlerForTypeName( m_valueTypeName );
  }
  
  public KIND getKind( )
  {
    return m_kind;
  }
  
  public static enum KIND
  {
    SimpleType, Number, Word, Boolean;
  }
}
