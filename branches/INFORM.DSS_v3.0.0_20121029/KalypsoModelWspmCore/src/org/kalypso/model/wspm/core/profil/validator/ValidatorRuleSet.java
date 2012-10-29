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
package org.kalypso.model.wspm.core.profil.validator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.debug.KalypsoModelWspmCoreDebug;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfile;

/**
 * A set of validation rules and the means to use them.
 *
 * @author belger
 */
public class ValidatorRuleSet
{
  private final IValidatorRule[] m_rules;

  public ValidatorRuleSet( final IValidatorRule[] rules )
  {
    m_rules = rules;
  }

  public IStatus validateProfile( final IProfile profil, final IValidatorMarkerCollector collector, final String[] excludeIDs, final IProgressMonitor monitor )
  {
    final IValidatorRule[] rules = m_rules;

    final List<IStatus> stati = new ArrayList<>();
    final List<String> excludeRules = java.util.Arrays.asList( excludeIDs );

    monitor.beginTask( "Validating profile", ArrayUtils.getLength( rules ) ); //$NON-NLS-1$

    for( final IValidatorRule rule : rules )
    {
      if( monitor.isCanceled() )
        return Status.CANCEL_STATUS;

      if( !excludeRules.contains( rule.getID() ) )
      {
        try
        {
          KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( "rule: %s, %s\n", rule.getID(), DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$

          rule.validate( profil, collector );

          KalypsoModelWspmCoreDebug.DEBUG_VALIDATION_MARKER.printf( " end: %s, %s\n", rule.getID(), DateFormat.getTimeInstance().format( Calendar.getInstance().getTime() ) ); //$NON-NLS-1$
        }
        catch( final CoreException e )
        {
          stati.add( e.getStatus() );
        }
      }

      monitor.worked( 1 );
    }

    monitor.done();

    if( stati.size() == 0 )
      return Status.OK_STATUS;

    return new MultiStatus( KalypsoModelWspmCorePlugin.getID(), 0, stati.toArray( new IStatus[stati.size()] ), Messages.getString( "org.kalypso.model.wspm.core.profil.validator.ValidatorRuleSet.0" ), null ); //$NON-NLS-1$
  }

  public IValidatorRule[] getRules( )
  {
    return m_rules;
  }
}
