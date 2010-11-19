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
package org.kalypso.model.wspm.ui.dialog.compare;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.view.chart.IProfilChart;

import de.openali.odysseus.chart.framework.util.ChartUtilities;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author Dirk Kuch
 */
public class CompareSwitchProfileButtonDialog extends SwitchProfileButtonDialog
{
  private final ICompareProfileProvider m_provider;

  private final IProfilChart[] m_additionalViews;

  public CompareSwitchProfileButtonDialog( final Composite parent, final IProfilChart base, final ICompareProfileProvider provider, final IProfilChart... additionalViews )
  {
    super( parent, base, provider.getBaseProfiles() );
    m_provider = provider;
    m_additionalViews = additionalViews;
  }

  /**
   * @see org.kalypso.planer.client.aw.measures.wspm.altdeich.dialog.SwitchProfileButtonDialog#setProfile(org.kalypso.model.wspm.core.profil.IProfil)
   */
  @Override
  protected void setProfile( final IProfil baseProfile )
  {
    super.setProfile( baseProfile );

    final CompareProfileWrapper[] additional = m_provider.getAdditionalProfiles( baseProfile );
    Assert.isTrue( additional.length == m_additionalViews.length );

    for( int i = 0; i < additional.length; i++ )
    {
      final IProfilChart view = m_additionalViews[i];
      final CompareProfileWrapper wrapper = additional[i];

      view.setProfil( wrapper.getProfil(),null );

      final IChartComposite chart = view.getChart();
      if( chart != null )
        ChartUtilities.maximize( chart.getChartModel() );
    }
  }

}
