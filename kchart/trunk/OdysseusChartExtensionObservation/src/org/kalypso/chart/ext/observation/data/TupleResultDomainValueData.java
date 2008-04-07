package org.kalypso.chart.ext.observation.data;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.observation.IObservation;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;
import org.kalypso.ogc.gml.om.ObservationFeatureFactory;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

import de.openali.odysseus.chart.framework.logging.impl.Logger;
import de.openali.odysseus.chart.framework.model.data.IDataContainer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.ComparableDataRange;

public class TupleResultDomainValueData<T_domain, T_target> implements IDataContainer<T_domain, T_target>
{
  final private String m_observationId;

  final private String m_domainComponentName;

  final private String m_targetComponentName;

  final private String m_href;

  final private URL m_context;

  private TupleResult m_result;

  private IComponent m_domainComponent;

  private IComponent m_targetComponent;

  // TODO: @Alex This is exactly that, what i never wanted to have in the new Chart Framework! Please do NOT! copy the
  // values out out of the real data object
  // That is really something we have to avoid!
  private List<T_domain> m_domainValues;

  private List<T_target> m_targetValues;

  private IObservation<TupleResult> m_observation;

  private boolean m_isOpen = false;

  public TupleResultDomainValueData( final URL context, final String href, final String observationId, final String domainComponentName, final String targetComponentName )
  {
    m_context = context;
    m_href = href;
    m_observationId = observationId;
    m_domainComponentName = domainComponentName;
    m_targetComponentName = targetComponentName;
  }

  public TupleResultDomainValueData( final TupleResult result, final String domainComponentName, final String targetComponentName )
  {
    // Die Observation ist schon vorhanden, also kann das Layer gleich als open ausgegeben werden
    m_isOpen = true;
    m_context = null;
    m_observationId = null;
    m_href = null;
    m_domainComponentName = domainComponentName;
    m_targetComponentName = targetComponentName;
    m_result = result;

    resolveComponents();
    createValueLists();
  }

  /**
   * sets all dynamically created data variables to null and m_isOpen to false;
   */
  public void close( )
  {
    m_result = null;
    m_domainComponent = null;
    m_targetComponent = null;
    m_domainValues = null;
    m_targetValues = null;
    m_observation = null;
    m_isOpen = false;
  }

  public boolean isOpen( )
  {
    return m_isOpen;
  }

  public void open( )
  {
    if( !m_isOpen )
    {
      try
      {
        final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( new URL( m_context, m_href ), null );
        final Feature feature = workspace.getFeature( m_observationId );
        if( feature != null )
        {
          Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Found feature: " + feature.getId() );
        }
        m_observation = ObservationFeatureFactory.toObservation( feature );

        m_result = m_observation.getResult();

        resolveComponents();

        createValueLists();
      }
      catch( final MalformedURLException e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch( final Exception e )
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    m_isOpen = true;
  }

  private void createValueLists( )
  {
    if( m_domainComponent != null && m_targetComponent != null )
    {
      // Value-Listen aufbauen
      final ArrayList<T_domain> domainValues = new ArrayList<T_domain>();
      final ArrayList<T_target> targetValues = new ArrayList<T_target>();
      for( int i = 0; i < m_result.size(); i++ )
      {
        final IRecord record = m_result.get( i );
        /**
         * Hier muss ich prüfen, ob das ein XMLGregorianCalendar ist und ihn bei Bedarf in einen Calendar umwandeln;
         * Problem ist, dass der XMLGregorianCalendar nicht Comparable implementiert
         */
        T_domain domainValue = null;
        final Object domainValueObj = record.getValue( m_domainComponent );
        if( domainValueObj instanceof XMLGregorianCalendar )
          domainValue = (T_domain) ((XMLGregorianCalendar) domainValueObj).toGregorianCalendar();
        else
          domainValue = (T_domain) domainValueObj;

        T_target targetValue = null;
        final Object targetValueObj = record.getValue( m_targetComponent );
        if( targetValueObj instanceof XMLGregorianCalendar )
          targetValue = (T_target) ((XMLGregorianCalendar) targetValueObj).toGregorianCalendar();
        else
          targetValue = (T_target) targetValueObj;

        domainValues.add( domainValue );
        targetValues.add( targetValue );
        Logger.logInfo( Logger.TOPIC_LOG_DATA, domainValue.toString() );
      }
      setDomainValues( domainValues );
      setTargetValues( targetValues );
    }
  }

  private void resolveComponents( )
  {
    // ResultComponent rausfinden
    final IComponent[] comps = m_result.getComponents();
    final TreeMap<String, IComponent> map = new TreeMap<String, IComponent>();
    for( final IComponent comp : comps )
    {
      Logger.logInfo( Logger.TOPIC_LOG_GENERAL, "Got component: " + comp.getName() );
      if( comp.getId().compareTo( m_domainComponentName ) == 0 )
        m_domainComponent = comp;
      else if( comp.getId().compareTo( m_targetComponentName ) == 0 )
        m_targetComponent = comp;
      if( map.size() == 2 )
        break;
    }
  }

  public IDataRange<T_domain> getDomainRange( )
  {
    return new ComparableDataRange<T_domain>( getDomainValues() );
  }

  public IDataRange<T_target> getTargetRange( )
  {
    return new ComparableDataRange<T_target>( getTargetValues() );
  }

  public T_domain[] getDomainValues( )
  {
    open();
    return (T_domain[]) m_domainValues.toArray();
  }

  public T_target[] getTargetValues( )
  {
    open();
    return (T_target[]) m_targetValues.toArray();
  }

  public void setDomainValues( final List<T_domain> domainValues )
  {
    m_domainValues = domainValues;
  }

  public void setTargetValues( final List<T_target> targetValues )
  {
    m_targetValues = targetValues;
  }

  public TupleResult getTupleResult( )
  {
    return m_result;
  }

}
