/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editorLauncher;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.IEditorLauncher;
import org.kalypso.ui.editor.gmleditor.part.GmlEditor;

/**
 * @author belger
 */
public class GisTemplateLauncher implements IEditorLauncher
{
  /**
   * @see org.eclipse.ui.IEditorLauncher#open(org.eclipse.core.runtime.IPath)
   */
  @Override
  public void open( final IPath filePath )
  {
    final IOFileFilter gttFilter = FileFilterUtils.suffixFileFilter( ".gmt" ); //$NON-NLS-1$
    final IOFileFilter gmtFilter = FileFilterUtils.suffixFileFilter( ".gtt" ); //$NON-NLS-1$
    final IOFileFilter gftFilter = FileFilterUtils.suffixFileFilter( ".gft" ); //$NON-NLS-1$
    final IOFileFilter gmvFilter = FileFilterUtils.suffixFileFilter( GmlEditor.EXTENSIN_GMV );
    final IOFileFilter filter1 = FileFilterUtils.orFileFilter( gmtFilter, gttFilter );
    final IOFileFilter filter2 = FileFilterUtils.orFileFilter( gftFilter, gmvFilter );
    final IOFileFilter filter = FileFilterUtils.orFileFilter( filter1, filter2 );

    // virtuelle Vorlagen finden
    // final Object gmtDefault = "<Standard Kartenansicht>";
    // final Object gttDefault = "<Standard Datenansicht>";
    final IDefaultTemplateLauncher featureDefault = new FeatureTemplateLauncher();
    final IDefaultTemplateLauncher baumDefault = new GmlEditorTemplateLauncher();
    final IDefaultTemplateLauncher mapDefault = new GisMapEditorTemplateLauncher();

    ViewEditorLauncherHelper.showTemplateDialog( filePath, filter, new IDefaultTemplateLauncher[] { featureDefault, baumDefault, mapDefault } );
  }
}
