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
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.util.Map;

import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

/**
 * @author Gernot Belger
 */
public class WizardFilter implements IWizardFilter
{
  private final Map<String, Boolean> m_enablement;

  public WizardFilter( final Map<String, Boolean> enablement )
  {
    m_enablement = enablement;
  }

  @Override
  public boolean accept( final IWizardDescriptor wizard )
  {
    if( wizard == null )
      return true;

    final String wizardID = wizard.getId();
    final Boolean enabled = m_enablement.get( wizardID );
    if( enabled == null )
      return true;

    return enabled;
  }

  @Override
  public boolean accept( final IWizardCategory category )
  {
    final IWizardDescriptor[] wizards = category.getWizards();
    final IWizardCategory[] categories = category.getCategories();
    for( final IWizardCategory child : categories )
    {
      if( accept( child ) )
        return true;
    }

    for( final IWizardDescriptor wizard : wizards )
    {
      if( accept( wizard ) )
        return true;
    }

    return false;
  }
}