/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.services.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengeospatial.ows.CapabilitiesBaseType;
import net.opengeospatial.ows.GetCapabilitiesType;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.services.processing.IOGCWebService;
import org.kalypso.services.processing.KalypsoWPSPlugin;

/**
 * Provides the content for a service tree based on the registered simulations
 * {@link org.kalypso.simulation.core.ISimulation} provided by
 * {@link KalypsoSimulationCoreExtensions#getRegisteredTypeIDs()}
 * 
 * @author skurzbach
 */
public class ServicesContentProvider implements ITreeContentProvider
{

  /**
   * key is the service identification id
   */
  private Map<String, List<IOGCWebService>> m_services;

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
   */
  public Object[] getChildren( final Object parentElement )
  {
    final Object[] result;
    final List<IOGCWebService> webServices = m_services.get( parentElement );
    if( parentElement.equals( "root" ) )
    {
      result = m_services.keySet().toArray();
    }
    else if( webServices != null )
    {
      result = webServices.toArray();
    }
    else if( parentElement instanceof IOGCWebService )
    {
      final IAdapterManager adapterManager = Platform.getAdapterManager();
      final ITreeContentProvider childContentProvider = (ITreeContentProvider) adapterManager.getAdapter( parentElement, ITreeContentProvider.class );
      if( childContentProvider != null )
      {
        result = childContentProvider.getChildren( parentElement );
      }
      else
      {
        result = null;
      }
    }
    else
    {
      result = null;
    }
    return result;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
   */
  public Object getParent( final Object element )
  {
    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
   */
  public boolean hasChildren( final Object element )
  {
    return m_services.get( element ) != null || (element instanceof IOGCWebService && Platform.getAdapterManager().hasAdapter( element, ITreeContentProvider.class.getName() ));
  }

  /**
   * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
   */
  public Object[] getElements( final Object inputElement )
  {
    return getChildren( inputElement );
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#dispose()
   */
  public void dispose( )
  {
  }

  /**
   * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  public void inputChanged( Viewer viewer, Object oldInput, Object newInput )
  {
    if( m_services == null )
    {
      m_services = new HashMap<String, List<IOGCWebService>>();
      for( final IOGCWebService service : KalypsoWPSPlugin.getDefault().getWebProcessingServices() )
      {
        final CapabilitiesBaseType capabilities = service.getCapabilities( new GetCapabilitiesType() );
        final String title = capabilities.getServiceIdentification().getTitle();
        List<IOGCWebService> list = m_services.get( title );
        if( list == null )
        {
          list = new ArrayList<IOGCWebService>();
          m_services.put( title, list );
        }
        list.add( service );
      }
    }
  }
}
