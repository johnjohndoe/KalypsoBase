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
package org.kalypso.workflow.ui.browser.urlaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.kalypso.workflow.ui.browser.AbstractURLAction;
import org.kalypso.workflow.ui.browser.ICommandURL;

/**
 * example: <br>
 * kalypso://closeAllEditors?exceptThese=org.eclipse.TestEditor1#org.eclipse.TestEditor2&separator=*&doSave=true;
 * 
 * @author kuepfer
 */
public class URLActionCloseAllEditors extends AbstractURLAction
{

  private final static String COMMAND_NAME = "closeAllEditors";

  private final static String PARAM_DO_NOT_CLOSE = "exceptThese";

  private final static String PARAM_DOSAVE = "doSave";

  private final static String PARAM_SEPERATOR = "sparator";

  private String DEFAULT_SEPARATOR = "#";

  /**
   * @see org.kalypso.workflow.ui.browser.IURLAction#getActionName()
   */
  public String getActionName( )
  {
    return COMMAND_NAME;
  }

  /**
   * @see org.kalypso.workflow.ui.browser.IURLAction#run(org.kalypso.workflow.ui.browser.ICommandURL)
   */
  public boolean run( ICommandURL commandURL )
  {
    final String separator = commandURL.getParameter( PARAM_SEPERATOR );
    final boolean doSave = Boolean.parseBoolean( commandURL.getParameter( PARAM_DOSAVE ) );
    final String[] doNotCloseList;
    final String separatedList = commandURL.getParameter( PARAM_DO_NOT_CLOSE );
    final IWorkbenchPage activePage = getActivePage();

    final List<IEditorReference> doClose = new ArrayList<IEditorReference>();
    if( separatedList != null && separatedList.length() > 0 )
    {
      if( separator != null )
        doNotCloseList = separatedList.split( separator );
      else
        doNotCloseList = separatedList.split( DEFAULT_SEPARATOR );
    }
    else
      doNotCloseList = new String[0];
    final IEditorReference[] editorReferences = activePage.getEditorReferences();
    for( IEditorReference reference : editorReferences )
    {
      final String editorId = reference.getId();
      if( ArrayUtils.contains( doNotCloseList, editorId ) )
        continue;
      doClose.add( reference );
    }
    activePage.closeEditors( doClose.toArray( new IEditorReference[doClose.size()] ), doSave );
    return true;
  }
}
