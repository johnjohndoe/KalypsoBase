/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.addlayer.IKalypsoDataImportWizard;
import org.kalypso.ui.addlayer.dnd.MapDropData;

/**
 * @author Gernot Belger
 */
public abstract class AbstractDataImportWizard extends Wizard implements IKalypsoDataImportWizard
{
  private ICommandTarget m_commandTarget;

  private IKalypsoLayerModell m_modell;

  @Override
  public final void setCommandTarget( final ICommandTarget commandTarget )
  {
    m_commandTarget = commandTarget;
  }

  protected final ICommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  @Override
  public final void setMapModel( final IKalypsoLayerModell modell )
  {
    m_modell = modell;
  }

  protected final IKalypsoLayerModell getMapModel( )
  {
    return m_modell;
  }

  /**
   * Default implementation does nothing by default.
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    // do nothing
  }

  /**
   * Default implementation throws {@link UnsupportedOperationException}.<br/>
   * Overwrite to implement.
   */
  @Override
  public void initFromDrop( final MapDropData data )
  {
    throw new UnsupportedOperationException();
  }

  protected final void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }
}
