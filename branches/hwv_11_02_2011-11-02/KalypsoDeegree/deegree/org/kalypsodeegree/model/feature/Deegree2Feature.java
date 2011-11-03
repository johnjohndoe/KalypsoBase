package org.kalypsodeegree.model.feature;

import javax.xml.namespace.QName;

import org.deegree.model.spatialschema.GeometryException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * Some Interface Methods from Deegree 2 Feature Implementation. We don't extend the Deegree2 Feature Interface
 * directly. At the moment we only want to get nearer to the Deegree2 Feature API
 * 
 * @author Dirk Kuch
 */
public interface Deegree2Feature
{
  /**
   * Kalypso: Name of Method changed to getQualifiedName<br>
   * Returns the qualified name of the feature.<br>
   * 
   * @return the qualified name of the feature<br>
   *         Kalypso: Changed to QName
   */
  QName getQualifiedName( );

  /**
   * Returns the description of the feature.
   * 
   * @return the description of the feature.
   */
  String getDescription( );

  /**
   * Returns the id of the feature.
   * 
   * @return the id of the feature
   */
  String getId( );

  /**
   * Sets the id of the feature.
   * 
   * @param fid
   *          the id of the feature to be set
   */
  void setId( String fid );

  /**
   * Kalypso: changed to IFeatureType<br>
   * Returns the feature type of this feature.
   * 
   * @return the feature type of this feature
   */
  IFeatureType getFeatureType( );

  /**
   * Sets the feature type of this feature.
   * 
   * @param ft
   *          feature type to set. Kalypso: changed to IFeatureType
   */
  void setFeatureType( IFeatureType ft );

  /**
   * Kalypso: probably not what you want. Use one of getProperty() instead Returns all properties of the feature in
   * their original order.
   * 
   * @return all properties of the feature. Kalypso. changed to Object[]
   */
  Object[] getProperties( );

// Kalypso: use getProperty() instead
// /**
// * Returns the first property of the feature with the given name.
// *
// * @param name
// * name of the property to look up
// * @return the first property of the feature with the given name or null if the feature has no
// * such property
// */
// FeatureProperty getDefaultProperty( QualifiedName name );

// Kalypso: use GmlXPath instead (for the moment)
// /**
// * Returns the property of the feature identified by the given {@link PropertyPath}.
// *
// * NOTE: Current implementation does not handle multiple properties (on the path) or index
// * addressing in the path.
// *
// * @param path
// * the path of the property to look up
// * @return the property of the feature identified by the given PropertyPath
// * @throws PropertyPathResolvingException
// *
// * @see PropertyPath
// */
// FeatureProperty getDefaultProperty( PropertyPath path )
// throws PropertyPathResolvingException;

// Kalypso: the GML-Parser does not recognise properties with the same qname in a feature,
// /**
// * Returns the properties of the feature with the given name in their original order.
// *
// * @param name
// * name of the properties to look up
// * @return the properties of the feature with the given name or null if the feature has no
// * property with that name
// */
// FeatureProperty[] getProperties( QualifiedName name );

// Deprecated
// /**
// * Returns the properties of the feature at the submitted index of the feature type definition.
// *
// * @param index
// * index of the properties to look up
// * @return the properties of the feature at the submitted index
// * @deprecated
// */
// @Deprecated
// FeatureProperty[] getProperties( int index );

  /**
   * Returns the values of all geometry properties of the feature.
   * 
   * @return the values of all geometry properties of the feature, or a zero-length array if the feature has no geometry
   *         properties. Kalypso: changed to GM_Object[]
   */
  GM_Object[] getGeometryPropertyValues( );

  /**
   * Returns the value of the default geometry property of the feature. If the feature has no geometry property, this is
   * a Point at the coordinates (0,0).
   * 
   * @return default geometry or Point at (0,0) if feature has no geometry. Kalypso: changed to GM_Object
   */
  GM_Object getDefaultGeometryPropertyValue( );

// Kalypso: we do not support mutliple occurences. So makes no sense at the moment
// /**
// * Sets the value for the given property. The index is needed to specify the occurences of the
// * property that is to be replaced. Set to 0 for properties that may only occur once.
// *
// * @param property
// * property name and the property's new value
// * @param index
// * position of the property that is to be replaced
// */
// void setProperty( FeatureProperty property, int index );

// Kalypso: Always just add is too lax in our sense. The feature must obe< its type.
// /**
// * Adds the given property to the feature's properties. The position of the property is
// * determined by the feature type. If the feature already has a property with this name, it is
// * inserted behind it.
// *
// * @param property
// * property to insert
// */
// void addProperty( FeatureProperty property );

// Kalypso: same as addProperty
// /**
// * Removes the properties with the given name.
// *
// * @param propertyName
// * name of the properties to remove
// */
// void removeProperty( QualifiedName propertyName );

// Kalypso: we are not using FeatureProperty's so, this makes no sense
// /**
// * Replaces the given property with a new one.
// *
// * @param oldProperty
// * property to be replaced
// * @param newProperty
// * new property
// */
// void replaceProperty( FeatureProperty oldProperty, FeatureProperty newProperty );

  /**
   * Returns the envelope / bounding box of the feature.
   * 
   * @return the envelope / bounding box of the feature. Kalypso: changed to GM_Envelope
   * @throws GeometryException
   */
  GM_Envelope getBoundedBy( ) throws GeometryException;

  /**
   * Returns the owner of the feature. This is the feature property that has this feature as value or null if this
   * feature is a root feature.
   * 
   * @return the owner of the feature, or null if the feature does not belong to a feature property. Kalypso: changed to
   *         Feature
   */
  Feature getOwner( );

// Kalypso: attributes are not yet supported by Kalypso
// /**
// * Returns the attribute value of the attribute with the specified name.
// *
// * @param name
// * name of the attribute
// * @return the attribute value
// */
// String getAttribute( String name );
//
// /**
// * Returns all attributes of the feature.
// *
// * @return all attributes, keys are names, values are attribute values
// */
// Map<String, String> getAttributes();
//
// /**
// * Sets the value of the attribute with the given name.
// *
// * @param name
// * name of the attribute
// * @param value
// * value to set
// */
// void setAttribute( String name, String value );

  /**
   * Signals that the envelopes of the geometry properties have been updated.
   */
  public void setEnvelopesUpdated( );
}
