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
package de.renew.workflow.connector.cases;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.kalypso.afgui.scenarios.Scenario;

import de.renew.workflow.cases.Case;

/**
 * Wrapper Class of workflow {@link Case}
 * 
 * @author Dirk Kuch
 */
public interface IScenario
{
  /** old case base uri (before refactoring case://{projectname}/{foldername} */
  String OLD_CASE_BASE_URI = "case://"; //$NON-NLS-1$

  /** old case base uri (before refactoring case://{foldername} */
  String NEW_CASE_BASE_URI = "scenario://"; //$NON-NLS-1$

  /**
   * @return workflow {@link Scenario}
   */
  Scenario getScenario( );

  /**
   * @return the workflow {@link Case}
   */
  Case getCase( );

  String getName( );

  String getDescription( );

  /**
   * @return {@link IProject} of workflow {@link Case}
   */
  IProject getProject( );

  /**
   * @return working folder of this caze
   */
  IFolder getFolder( );

  /**
   * @return URI of workflow {@link Case}
   */
  String getURI( );

  /**
   * @return list of derived {@link IScenario}s of the workflow {@link IScenario}
   */
  IScenarioList getDerivedScenarios( );

  /**
   * @return parent {@link IScenario} of the workflow {@link IScenario}. NULL if {@link IScenario} is root element
   */
  IScenario getParentScenario( );

  /**
   * Hierarchical Level of IScenario. Base/Root Scenario returns 0, SubScenarion returns 1, SubSubScenario returns 2,
   * aso
   * 
   * @return hierarchical Level of IScenario
   */
  int getHierarchicalLevel( );

  /**
   * Gets the folder of a scenario where sub-scenarios are stored in.
   */
  IFolder getDerivedFolder( );

  /**
   * Gets the folders of a scenario, which should be ignored when the scenario is set as a base scenario.
   */
  IFolder[] getSetAsBaseScenarioBlackList( );
}
