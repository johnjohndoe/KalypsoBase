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
package org.kalypso.contribs.eclipse.ui.editorinput;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.kalypso.contribs.eclipse.core.resources.StringStorage;

/**
 * @author Gernot Belger
 */
public class StringStorageInputFactory implements IElementFactory
{
  public static final String ID = "org.kalypso.contribs.eclipse.ui.editorinput.StringStorageInputFactory"; //$NON-NLS-1$

  private static final String MEMENTO_DATA = "string.storage.memento.data"; //$NON-NLS-1$

  private static final String MEMENTO_PATH = "string.storage.memento.path"; //$NON-NLS-1$

  public static void saveState( final StringStorage storage, final IMemento memento )
  {
    memento.putString( MEMENTO_DATA, storage.getData() );
    final IPath fullPath = storage.getFullPath();
    if( fullPath != null )
      memento.putString( MEMENTO_PATH, fullPath.toPortableString() );
  }

  /**
   * @see org.eclipse.ui.IElementFactory#createElement(org.eclipse.ui.IMemento)
   */
  @Override
  public IAdaptable createElement( final IMemento memento )
  {
    final String data = memento.getString( MEMENTO_DATA );
    final String pathName = memento.getString( MEMENTO_PATH );

    final IPath path = pathName == null ? null : Path.fromPortableString( pathName );

    final StringStorage storage = new StringStorage( data, path );

    return new StorageEditorInput( storage );
  }

}
