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
package org.kalypso.contribs.eclipse.jface.wizard;

import org.eclipse.jface.wizard.WizardPage;

/**
 * This class should be helpfull when handling with wizard pages extending {@link org.eclipse.jface.wizard.WizardPage}.
 * 
 * @author Holger Albert
 */
public class WizardPageUtilities
{
  /**
   * Should not be instanziated.
   */
  private WizardPageUtilities( )
  {
  }

  /**
   * Appends a warning message to the existing ones. If none exists, a new one is set.
   * 
   * @param message
   *          The warning that should be appended.
   * @param page
   *          The wizard page, which should display the warning.
   */
  public static void appendWarning( String message, WizardPage page )
  {
    /* If the message should be resetted ... */
    if( message == null || message.equals( "" ) )
    {
      /* ... do it! */
      page.setMessage( null );
      return;
    }

    if( page.getMessage() == null )
      page.setMessage( message, WizardPage.WARNING );
    else
      page.setMessage( page.getMessage() + "\n" + message, WizardPage.WARNING );
  }

  /**
   * Appends an error message to the existing ones. If none exists, a new one is set.
   * 
   * @param message
   *          The error that should be appended.
   * @param page
   *          The wizard page, which should display the error.
   */
  public static void appendError( String message, WizardPage page )
  {
    /* If the message should be resetted ... */
    if( message == null || message.equals( "" ) )
    {
      /* ... do it! */
      page.setErrorMessage( null );
      return;
    }

    if( page.getMessage() == null )
      page.setErrorMessage( message );
    else
      page.setErrorMessage( page.getMessage() + "\n" + message );
  }
}