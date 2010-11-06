package org.kalypso.gmlschema.types;


/**
 * This class tells us by which properties a geometry may be specified, setting a TypeHandler for each of these
 * properties.
 * <br>
 * For now it will support only for properties parsed via SAX ({@link IPropertyMarshallingTypeHandler})
 * 
 * @author Felipe Maximino
 */
public interface IGeometrySpecification
{ 
  public void fillSpecifications( GeometrySpecificationCatalog specs );
}
