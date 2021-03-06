/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.module.welcome.utils;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;

/**
 * @author Dirk Kuch
 */
// FIXME: PLEASE COMMMMMMMMMENT WHAT THE HELL THIS STUFF IS DOING!
public class DisableCreateProjectWizardPageElements
{
  public static void disableElementsForProjectDownload( final WizardNewProjectCreationPage myPage )
  {
    final Composite myParent = (Composite) myPage.getControl();
    final Control[] children = myParent.getChildren();

    /* project name */
    final Composite subChildOne = (Composite) children[0];
    final Control[] subChildrenOne = subChildOne.getChildren();
    subChildrenOne[1].setEnabled( false );

    /* workspace location */
    final Composite subChildTwo = (Composite) children[1];
    final Control[] subChildrenTwo = subChildTwo.getChildren();
    subChildrenTwo[0].setEnabled( false );

    /* working sets */
    final Composite subChildThree = (Composite) children[2];
    final Control[] subChildrenThree = subChildThree.getChildren();
    final Composite subSubChildThree = (Composite) subChildrenThree[0];
    final Control[] subSubChildrenTwo = subSubChildThree.getChildren();
    subSubChildrenTwo[0].setEnabled( false );
  }

  // FIXME: PLEASE COMMMMMMMMMENT WHAT THE HELL THIS STUFF IS DOING!
  public static void disableElementsForProjectCreation( final WizardNewProjectCreationPage myPage )
  {
    final Composite myParent = (Composite) myPage.getControl();
    final Control[] children = myParent.getChildren();

    /* project name */
    final Composite subChildOne = (Composite) children[0];
    final Control[] subChildrenOne = subChildOne.getChildren();
    final Text projectName = (Text) subChildrenOne[1];
    projectName.setText( "" ); //$NON-NLS-1$

    /* workspace location */
    final Composite subChildTwo = (Composite) children[1];
    final Control[] subChildrenTwo = subChildTwo.getChildren();
    subChildrenTwo[0].setEnabled( false );

    /* working sets */
    final Composite subChildThree = (Composite) children[2];
    final Control[] subChildrenThree = subChildThree.getChildren();
    final Composite subSubChildThree = (Composite) subChildrenThree[0];
    final Control[] subSubChildrenTwo = subSubChildThree.getChildren();
    subSubChildrenTwo[0].setEnabled( false );
  }

}
