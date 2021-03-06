/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.wizards.IWizardRegistry;

/**
 * A property tester that tests wether a wizard-iwzard is active or not..
 *
 * @author Gernot Belger
 */
public class GenericWizardTester extends PropertyTester
{
  private static final String PROPERTY_IS_ENABLED = "isEnabled"; //$NON-NLS-1$

  private final Map<String, IWizardRegistry> m_registries = new HashMap<>();

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_IS_ENABLED.equals( property ) )
      return testIsEnabled( args );

    final String message = String.format( "Unknown property '%s'", property ); //$NON-NLS-1$
    throw new IllegalArgumentException( message );
  }

  private boolean testIsEnabled( final Object[] args )
  {
    final String pluginID = (String) args[0];
    final String extensionPoint = (String) args[1];
    final IWizardRegistry registry = getRegistry( pluginID, extensionPoint );

    // TODO: get more specific service, but how?
    final IEvaluationService service = (IEvaluationService) PlatformUI.getWorkbench().getService( IEvaluationService.class );
    final IEvaluationContext globalState = service.getCurrentState();

    final EvaluationContext currentState = new EvaluationContext( globalState, globalState.getDefaultVariable() );
    // currentState.addVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME, receiver );

    final WizardEnablementVisitor wizardEnablementVisitor = new WizardEnablementVisitor( currentState );
    wizardEnablementVisitor.accept( registry.getRootCategory() );

    return wizardEnablementVisitor.hasEnabled();
  }

  private synchronized IWizardRegistry getRegistry( final String pluginID, final String extensionPoint )
  {
    if( !m_registries.containsKey( extensionPoint ) )
    {
      final IWizardRegistry registry = new GenericWizardRegistry( pluginID, extensionPoint );
      m_registries.put( extensionPoint, registry );
    }

    return m_registries.get( extensionPoint );
  }
}
