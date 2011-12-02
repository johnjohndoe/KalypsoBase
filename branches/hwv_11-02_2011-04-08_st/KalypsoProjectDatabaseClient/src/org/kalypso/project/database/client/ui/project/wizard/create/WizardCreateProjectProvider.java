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
package org.kalypso.project.database.client.ui.project.wizard.create;

import org.kalypso.afgui.wizards.INewProjectWizard;
import org.kalypso.afgui.wizards.INewProjectWizardProvider;
import org.kalypso.contribs.eclipse.core.resources.ProjectTemplate;
import org.kalypso.module.IKalypsoModule;

/**
 * @author Gernot Belger
 */
public class WizardCreateProjectProvider implements INewProjectWizardProvider
{
  private final ProjectTemplate[] m_templates;

  private final String[] m_natures;

  private final IKalypsoModule m_module;

  public WizardCreateProjectProvider( final ProjectTemplate[] templates, final String[] natures, final IKalypsoModule module )
  {
    m_templates = templates;
    m_natures = natures;
    m_module = module;
  }

  /**
   * @see org.kalypso.afgui.wizards.INewProjectWizardProvider#createWizard()
   */
  @Override
  public INewProjectWizard createWizard( )
  {
    return new WizardCreateProject( m_templates, m_natures, m_module );
  }

}
