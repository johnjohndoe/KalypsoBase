package org.kalypso.chart.ext.observation.data;

import java.net.URL;

import javax.xml.datatype.XMLGregorianCalendar;

import org.kalypso.observation.IObservation;
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
  public String getDomainComponentName( )
  {
    return m_domainComponentName;
  }

  public String getTargetComponentName( )
  {
    return m_targetComponentName;
  }

  final private String m_observationId;

  final private String m_domainComponentName;

  final private String m_targetComponentName;

  final private String m_href;

  final private URL m_context;

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

  public IObservation<TupleResult> getObservation( )
  {
    return m_observation;
  }

  public TupleResultDomainValueData( final IObservation observation, final String domainComponentName, final String targetComponentName )
  {
    // Die Observation ist schon vorhanden, also kann das Layer gleich als open ausgegeben werden
    m_isOpen = true;
    m_context = null;
    m_observationId = null;
    m_href = null;
    m_domainComponentName = domainComponentName;
    m_targetComponentName = targetComponentName;
    m_observation = observation;

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
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
    m_isOpen = true;
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
    return (T_domain[]) getValues( m_domainComponentName );
  }

  protected Object[] getValues( final String compName )
  {
    open();
    if( !m_isOpen )
      return new Object[] {};
    final int iComp = m_observation.getResult().indexOfComponent( compName );
    if( iComp < 0 )
      return new Object[] {};
    final Object[] objArr = new Object[m_observation.getResult().size()];
    int index = 0;
    for( final IRecord record : m_observation.getResult() )
    {
      final Object objVal = record.getValue( iComp );
      if( objVal instanceof XMLGregorianCalendar )
        objArr[index++] = ((XMLGregorianCalendar) objVal).toGregorianCalendar();
      else
        objArr[index++] = objVal;
    }
    return objArr;
  }

  public T_target[] getTargetValues( )
  {
    return (T_target[]) getValues( m_targetComponentName );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataContainer#close()
   */
  @Override
  public void close( )
  {
    m_observation = null;
    m_isOpen = false;

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.data.IDataContainer#isOpen()
   */
  @Override
  public boolean isOpen( )
  {
    return m_isOpen;
  }
}
