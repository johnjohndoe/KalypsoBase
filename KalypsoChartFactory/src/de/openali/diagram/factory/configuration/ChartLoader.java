package de.openali.diagram.factory.configuration;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;

import de.openali.diagram.factory.DiagramExtensions;
import de.openali.diagram.factory.configuration.exception.AxisProviderException;
import de.openali.diagram.factory.configuration.exception.AxisRendererProviderException;
import de.openali.diagram.factory.configuration.exception.ConfigDiagramNotFoundException;
import de.openali.diagram.factory.configuration.exception.ConfigurationException;
import de.openali.diagram.factory.configuration.exception.LayerProviderException;
import de.openali.diagram.factory.configuration.exception.MapperProviderException;
import de.openali.diagram.factory.configuration.exception.StyledElementProviderException;
import de.openali.diagram.factory.configuration.xsd.AxisRendererType;
import de.openali.diagram.factory.configuration.xsd.AxisType;
import de.openali.diagram.factory.configuration.xsd.DiagramType;
import de.openali.diagram.factory.configuration.xsd.LayerType;
import de.openali.diagram.factory.configuration.xsd.MapperRefType;
import de.openali.diagram.factory.configuration.xsd.MapperType;
import de.openali.diagram.factory.configuration.xsd.ProviderType;
import de.openali.diagram.factory.configuration.xsd.RefType;
import de.openali.diagram.factory.configuration.xsd.StyleType;
import de.openali.diagram.factory.configuration.xsd.DiagramType.Layers;
import de.openali.diagram.factory.configuration.xsd.LayerType.Mapper;
import de.openali.diagram.factory.configuration.xsd.LayerType.Style;
import de.openali.diagram.factory.provider.IAxisProvider;
import de.openali.diagram.factory.provider.IAxisRendererProvider;
import de.openali.diagram.factory.provider.ILayerProvider;
import de.openali.diagram.factory.provider.IMapperProvider;
import de.openali.diagram.factory.provider.IStyledElementProvider;
import de.openali.diagram.factory.util.DiagramFactoryUtilities;
import de.openali.diagram.factory.util.IReferenceResolver;
import de.openali.diagram.framework.logging.Logger;
import de.openali.diagram.framework.model.IDiagramModel;
import de.openali.diagram.framework.model.layer.IChartLayer;
import de.openali.diagram.framework.model.mapper.IAxis;
import de.openali.diagram.framework.model.mapper.IMapper;
import de.openali.diagram.framework.model.mapper.registry.IMapperRegistry;
import de.openali.diagram.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.diagram.framework.model.styles.impl.LayerStyle;


/**
 * @author alibu
 * Creates a chart object from a configration
 */
public class ChartLoader
{

	public static void configureDiagramModel( IDiagramModel model, ConfigLoader cl, String configDiagramName, URL context ) throws ConfigurationException
	{
		  DiagramType dt=null;
		  // ChartConfig auslesen
		  if( cl != null )
		  {
			  DiagramType[] diagrams = cl.getDiagrams();
			  for (DiagramType diagram : diagrams) {
				if (diagram.getId().equals(configDiagramName))
				{
					dt=diagram;
					break;
				}
			}
		  }
		  if( dt == null )
		  {
			  throw new ConfigDiagramNotFoundException( configDiagramName );
		  }
		  else
		  {
			  try
			  {
				  doConfiguration( model, cl, dt, context );
			  }
			  catch (ConfigurationException e)
			  {
				  Logger.logError(Logger.TOPIC_LOG_CONFIG, " DiagramModel could not be created");
			  }
		  }
	  }

  @SuppressWarnings("unchecked")
private static void doConfiguration( final IDiagramModel model, final IReferenceResolver rr, final DiagramType diagramType, final URL context ) throws ConfigurationException
  {
	  
	 Layers layers = diagramType.getLayers();
	 RefType[] layerRefArray = layers.getLayerRefArray();
	 for (RefType layerRef : layerRefArray) {
		String ref = layerRef.getRef();
		LayerType layerType = (LayerType) rr.resolveReference(ref);
		if (layerType!=null)
		{
			  // Achsen hinzufügen
			Mapper mapper = layerType.getMapper();
			AxisType domainAxisType = (AxisType) rr.resolveReference(mapper.getDomainAxisRef().getRef());
			addAxis (model.getAxisRegistry(), rr, domainAxisType);
			AxisType valueAxisType = (AxisType) rr.resolveReference(mapper.getValueAxisRef().getRef());
			addAxis (model.getAxisRegistry(), rr, valueAxisType);
			
			//Restliche Mapper hinzufügen
			MapperRefType[] mapperRefArray = mapper.getMapperRefArray();
			for (MapperRefType mapperRef : mapperRefArray) 
			{
				MapperType mapperType = (MapperType) rr.resolveReference(mapperRef.getRef());
				if (mapperType != null)
				{
        		  addMapper( model.getAxisRegistry(), mapperType );
				}
			}
			
			 //Styles erzeugen
	          LayerStyle ls=createStyle(layerType, rr);
	          
	         //Layer erzeugen
        	ProviderType provider2 = layerType.getProvider();
			final String providerId = provider2.getEpid();
	          
	          try
	          {
	            final ILayerProvider provider = DiagramExtensions.createLayerProvider( providerId );
	            if( provider != null )
	            {
	              Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "LayerProvider loaded:" + provider.getClass().toString() );
	              provider.init( model, layerType );
	              IChartLayer icl = provider.getLayer( context );
	              if( icl != null )
	              {
	                Logger.logInfo(Logger.TOPIC_LOG_CONFIG, "Adding Layer " + icl.getTitle() );
	                icl.setStyle( ls );
	                icl.setId( layerType.getId() );
	                icl.setTitle( layerType.getTitle() );
	                icl.setDescription( layerType.getDescription() );
	                icl.setVisibility( layerType.getVisible() );
	                model.getLayerManager().addLayer( icl );
	              }
	            }
	            else
	            {
	              Logger.logError( Logger.TOPIC_LOG_GENERAL, "No LayerProvider for " + providerId );
	            }
	          }
	          catch( final CoreException e )
	          {
	            e.printStackTrace();
	          }
	          catch( final LayerProviderException e )
	          {
	            e.printStackTrace();
	          }
		}
      else
    	  Logger.logWarning(Logger.TOPIC_LOG_CONFIG, "a reference to a layer type could not be resolved: "+layerRef.getRef());
	 }
	 
  }

  /**
   */
  private static void addMapper( final IMapperRegistry ar, final MapperType mapperType )
  {
	  
    if( mapperType != null )
    {
      final String mpId = mapperType.getProvider().getEpid();
      if( mpId != null && mpId.length() > 0 )
      {
        try
        {
          final IMapperProvider mp = DiagramExtensions.createMapperProvider( mpId );
          mp.init( mapperType );
          final IMapper mapper = mp.getMapper() ;
          ar.addMapper( mapper );
        }
        catch( CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( MapperProviderException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
      else
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + mpId + " not known" );
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
    }
  }
  
  
  /**
   * creates a concrete IAxis-Implementation from an AbstractAxisType derived from a ChartConfiguration, sets the
   * corresponding renderer and adds both to a given Chart
   */
  private static void addAxis( final IMapperRegistry ar, final IReferenceResolver rr, final AxisType axisType )
  {
	  
    if( axisType != null )
    {
      final String apId = axisType.getProvider().getEpid();
      if( apId != null && apId.length() > 0 )
      {
        try
        {
          final IAxisProvider ap = DiagramExtensions.createAxisProvider( apId );
          ap.init( axisType );
          final IAxis axis = ap.getAxis() ;
          axis.setRegistry(ar);
          ar.addMapper( axis );

          if( ar.getRenderer( axis ) == null )
          {
        	RefType rendererRef = axisType.getRendererRef();
        	AxisRendererType rendererType = (AxisRendererType) rr.resolveReference(rendererRef.getRef()); 
        	if (rendererType !=null )
        	{
	        	String arpId = rendererType.getProvider().getEpid();
	        	final IAxisRendererProvider arp = DiagramExtensions.createAxisRendererProvider( arpId );
	        	
	        	//Überprüfen, ob Renderer- und Axis-DataClass zusammenpassen
	        	if (DiagramFactoryUtilities.isSubclassOf(arp.getDataClass(), ap.getDataClass()))
	        	{
		        	arp.init(rendererType);
		            IAxisRenderer axisRenderer = arp.getAxisRenderer();
		            ar.setRenderer( axis.getIdentifier(), axisRenderer );
		            Logger.trace("Adding AxisRenderer for: "+arp.getClass());
	        	}
	        	else
	        	{
	        		Logger.logError(Logger.TOPIC_LOG_CONFIG, "incompatible renderer for axis: "+arp.getDataClass()+ " != "+ap.getDataClass());
	        	}
        	}
          }
        }
        catch( CoreException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        catch( AxisProviderException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (AxisRendererProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      }
      else
      {
        Logger.logError( Logger.TOPIC_LOG_CONFIG, "AxisProvider " + apId + " not known" );
      }
    }
    else
    {
      Logger.logError( Logger.TOPIC_LOG_GENERAL, "AxisFactory: given axis is NULL." );
    }
  }
  
  
  
  private static LayerStyle createStyle(LayerType l, IReferenceResolver rr)
  {
	  LayerStyle ls=new LayerStyle();
	  Style style = l.getStyle();
	  if (style!=null)
	  {
		  RefType[] styleRefArray = style.getStyleRefArray();
		  for (RefType styleRef : styleRefArray)
		  {
			  StyleType st=(StyleType) rr.resolveReference(styleRef.getRef());
			  ProviderType provider = st.getProvider();
			  if (provider !=null)
			  {
				  try {
					final IStyledElementProvider sp = DiagramExtensions.createStyledElementProvider(provider.getEpid() );
					sp.init(st);
					ls.add(sp.getStyledElement());
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (StyledElementProviderException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			  }
			  
		  }
	  }
	  return ls;
  }
}