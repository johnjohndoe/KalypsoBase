package org.kalypso.gmlschema.swe;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.property.restriction.IRestriction;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;

public class RepresentationType
{
  private final KIND m_kind;

  private final QName m_valueTypeName;

  private final String m_unit;

  private final String m_frame;

  private final String m_classification;

  private final IRestriction[] m_restrictions;

  public RepresentationType( final KIND kind, final QName valueTypeName, final String unit, final String frame, final IRestriction[] restrictions, final String classification )
  {
    m_kind = kind;
    m_valueTypeName = valueTypeName;
    m_unit = unit;
    m_frame = frame;
    m_restrictions = restrictions;
    m_classification = classification;
  }

  public QName getValueTypeName( )
  {
    return m_valueTypeName;
  }

  public String getUnit( )
  {
    return m_unit;
  }

  public String getFrame( )
  {
    return m_frame;
  }

  public IRestriction[] getRestrictions( )
  {
    return m_restrictions;
  }

  public String getClassification( )
  {
    return m_classification;
  }

  public IMarshallingTypeHandler getTypeHandler( )
  {
    return MarshallingTypeRegistrySingleton.getTypeRegistry().getTypeHandlerForTypeName( m_valueTypeName );
  }

  public KIND getKind( )
  {
    return m_kind;
  }

  public static enum KIND
  {
    SimpleType,
    Number,
    Word,
    Boolean;
  }
}
