/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.template;

import java.util.Set;

import org.kalypso.contribs.eclipse.ui.IViewable;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.request.IRequest;

/**
 * Default implementation of the <code>ITableViewColumn</code> interface
 * 
 * @author schlienger
 */
public abstract class ObsViewItem implements IObsProviderListener, IViewable
{
  private final ObsView m_view;

  private final IObsProvider m_obsProvider;

  private boolean m_shown = true;

  private String m_name = ""; //$NON-NLS-1$

  private ITupleModel m_model = null;

  public ObsViewItem( final ObsView view, final IObsProvider obsProvider, final String name )
  {
    m_obsProvider = obsProvider;
    m_view = view;
    m_name = name;
    obsProvider.addListener( this );
  }

  public void dispose( )
  {
    m_obsProvider.removeListener( this );
    m_obsProvider.dispose();
  }

  public String getName( )
  {
    return m_name;
  }

  @Override
  public String toString( )
  {
    return getName();
  }

  public void setName( final String name )
  {
    m_name = name;

    getView().refreshItemState( this, null );
  }

  public ObsView getView( )
  {
    return m_view;
  }

  @Override
  public boolean isShown( )
  {
    return m_shown;
  }

  public void setShown( final boolean shown )
  {
    if( shown != m_shown )
    {
      m_shown = shown;

      getView().refreshItemState( this, null );
    }
  }

  /**
   * @see org.kalypso.ogc.sensor.template.IObsProviderListener#obsProviderChanged()
   */
  @Override
  public void observationReplaced( )
  {
    m_model = null;

    observationChanged( null );
  }

  @Override
  public void observationChanged( final Object source )
  {
    m_view.refreshItemData( this, source );
  }

  public IObservation getObservation( )
  {
    return m_obsProvider.getObservation();
  }

  public ITupleModel getValues( ) throws SensorException
  {
    m_model = loadModel();
    return m_model;
  }

  private ITupleModel loadModel( ) throws SensorException
  {
    if( m_model != null )
      return m_model;

    final IObservation observation = getObservation();
    if( observation == null )
      return null;

    final IRequest request = m_obsProvider.getArguments();
    return observation.getValues( request );
  }

  /**
   * Return true if this item is concerned by the list of hidden axis-types
   * 
   * @param hiddenTypes
   *          list of axis-types that the user does not want to see
   */
  public abstract boolean shouldBeHidden( final Set<String> hiddenTypes );
}