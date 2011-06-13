/***********************************************************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved. This program and the accompanying
 * materials! are made available under the terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation Sebastian Davids <sdavids@gmx.de>- Fix for bug 19346 -
 * Dialog font should be activated and used by other components.
 **********************************************************************************************************************/
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
