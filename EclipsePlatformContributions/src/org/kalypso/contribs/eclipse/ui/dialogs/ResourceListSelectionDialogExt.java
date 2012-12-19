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
package org.kalypso.contribs.eclipse.ui.dialogs;

import java.lang.reflect.Field;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;

/**
 * Extension of {@link ResourceListSelectionDialog} that allwo to further restrict resource selection.
 * 
 * @author Gernot Belger
 */
public final class ResourceListSelectionDialogExt extends ResourceListSelectionDialog
{
  private IResourceSelector m_selector;

  private String m_initialPattern;

  public ResourceListSelectionDialogExt( final Shell parentShell, final IContainer container, final int typeMask )
  {
    super( parentShell, container, typeMask );
  }

  public ResourceListSelectionDialogExt( final Shell shell, final IResource[] resources )
  {
    super( shell, resources );
  }

  public void setSelector( final IResourceSelector selector )
  {
    m_selector = selector;
  }

  public void setInitialPattern( final String initialPattern )
  {
    m_initialPattern = initialPattern;
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    final Control area = super.createDialogArea( parent );

    // HACKY: use reflection to set the initial value. But still better than to copy the whole implementation...
    if( m_initialPattern != null )
    {
      try
      {
        final Field field = ResourceListSelectionDialog.class.getDeclaredField( "pattern" ); //$NON-NLS-1$
        field.setAccessible( true );
        final Text pattern = (Text) field.get( this );
        pattern.setText( m_initialPattern );
      }
      catch( final SecurityException e )
      {
        e.printStackTrace();
      }
      catch( final NoSuchFieldException e )
      {
        e.printStackTrace();
      }
      catch( final IllegalArgumentException e )
      {
        e.printStackTrace();
      }
      catch( final IllegalAccessException e )
      {
        e.printStackTrace();
      }
    }

    return area;
  }

  @Override
  protected boolean select( final IResource resource )
  {
    if( m_selector == null )
      return super.select( resource );

    return m_selector.select( resource );
  }
}
