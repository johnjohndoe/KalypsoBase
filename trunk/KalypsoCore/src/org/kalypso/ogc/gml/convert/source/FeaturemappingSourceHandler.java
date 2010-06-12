package org.kalypso.ogc.gml.convert.source;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBElement;

import org.eclipse.core.runtime.Assert;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.util.AddFeaturesMappingType;
import org.kalypso.gml.util.ChangeFeaturesMappingType;
import org.kalypso.gml.util.FeaturemappingSourceType;
import org.kalypso.gml.util.MappingType;
import org.kalypso.gml.util.SourceType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypso.ogc.gml.convert.GmlConvertFactory;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.FilteredFeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.filterencoding.AbstractFilter;
import org.kalypsodeegree_impl.model.feature.visitors.AddFeaturesToFeaturelist;
import org.kalypsodeegree_impl.model.feature.visitors.ChangeFeaturesFromFeaturelist;

/**
 * @author belger
 */
public class FeaturemappingSourceHandler implements ISourceHandler
{
  private final FeaturemappingSourceType m_source;

  private final URL m_context;

  private final IUrlResolver m_resolver;

  private final Map< ? , ? > m_externData;

  public FeaturemappingSourceHandler( final IUrlResolver resolver, final URL context,
      final FeaturemappingSourceType source, final Map< ? , ? > externData )
  {
    m_resolver = resolver;
    m_context = context;
    m_source = source;
    m_externData = externData;
  }

  /**
   * @throws GmlConvertException
   * @see org.kalypso.ogc.gml.convert.source.ISourceHandler#getWorkspace()
   */
  @Override
  public GMLWorkspace getWorkspace() throws GmlConvertException
  {
    final List<JAXBElement<? extends SourceType>> sourceList = m_source.getSource();

    // XSD schreibt vor, dass es genau 2 sources gibt
    Assert.isTrue( sourceList.size() == 2 );
    final GMLWorkspace firstGML = GmlConvertFactory.loadSource( m_resolver, m_context, sourceList.get( 0 ).getValue(),
        m_externData );
    final GMLWorkspace secondGML = GmlConvertFactory.loadSource( m_resolver, m_context, sourceList.get( 1 ).getValue(),
        m_externData );

    final List<JAXBElement<? extends MappingType>> mappingList = m_source.getMapping();
    for( final JAXBElement< ? extends MappingType> name : mappingList )
    {
      final MappingType mapping = name.getValue();
      applyMapping( firstGML, secondGML, mapping );
    }

    return secondGML;
  }

  private void applyMapping( final GMLWorkspace firstGML, final GMLWorkspace secondGML, final MappingType mapping ) throws GmlConvertException
  {
    final String fromPath = mapping.getFromPath();
    final String toPath = mapping.getToPath();

    final FeatureList fromFeatures = getFeatureList( firstGML, fromPath );
    final String fromID = mapping.getFromID();
    final FeatureList toFeatures = getFeatureList( secondGML, toPath );
    final String toID = mapping.getToID();
    final IFeatureType toFeatureType = secondGML.getFeatureTypeFromPath( toPath );

    final Properties properties = readProperties( mapping );

    final FeatureVisitor visitor = createVisitorAndFilter( mapping, toFeatures, toFeatureType, fromID, toID, properties );
    fromFeatures.accept( visitor );
  }

  private Filter readFilter( final MappingType mapping ) throws GmlConvertException
  {
    try
    {
      final Object anyType = mapping.getFilter();
      if( anyType == null )
        return null;
      
      return AbstractFilter.buildFromAnyType( anyType );
    }
    catch( final FilterConstructionException e )
    {
      throw new GmlConvertException( "Failed to parse filter expression", e );
    }
  }

  private Properties readProperties( final MappingType mapping )
  {
    final Properties properties = new Properties();
    final List<MappingType.Map> mapList = mapping.getMap();
    for( final org.kalypso.gml.util.MappingType.Map map : mapList )
      properties.setProperty( map.getFrom(), map.getTo() );
    return properties;
  }

  private FeatureVisitor createVisitorAndFilter( final MappingType mapping, final FeatureList toFeatures,
      final IFeatureType toFeatureType, final String fromID, final String toID, final Properties properties )
  throws GmlConvertException
  {
    final Filter filter = readFilter( mapping );
    
    final FeatureVisitor visitor = createVisitor( mapping, toFeatures, toFeatureType, fromID, toID, properties );
    
    if( filter == null )
        return visitor;
    
    return new FilteredFeatureVisitor( visitor, filter );
  }

  private FeatureVisitor createVisitor( final MappingType mapping, final FeatureList toFeatures,
      final IFeatureType toFeatureType, final String fromID, final String toID, final Properties properties )
      throws GmlConvertException
  {
    if( mapping instanceof AddFeaturesMappingType )
    {
      final AddFeaturesMappingType addType = (AddFeaturesMappingType)mapping;
      final String handleExisting = addType.getHandleExisting().value();
      final String fID = addType.getFid();
      final String targetFeatureType = addType.getTargetFeatureType();
      return new AddFeaturesToFeaturelist( toFeatures, properties, toFeatureType, fromID, toID, handleExisting, fID, targetFeatureType );
    }
    else if( mapping instanceof ChangeFeaturesMappingType )
      return new ChangeFeaturesFromFeaturelist( toFeatures, properties, fromID, toID );
    else
      throw new GmlConvertException( Messages.getString("org.kalypso.ogc.gml.convert.source.FeaturemappingSourceHandler.0") + mapping.getClass().getName() ); //$NON-NLS-1$
  }

  private FeatureList getFeatureList( final GMLWorkspace workspace, final String path ) throws GmlConvertException
  {
    final Object featureFromPath = workspace.getFeatureFromPath( path );
    if( featureFromPath instanceof FeatureList )
      return (FeatureList)featureFromPath;

    throw new GmlConvertException( Messages.getString("org.kalypso.ogc.gml.convert.source.FeaturemappingSourceHandler.1") + path ); //$NON-NLS-1$
  }
}
