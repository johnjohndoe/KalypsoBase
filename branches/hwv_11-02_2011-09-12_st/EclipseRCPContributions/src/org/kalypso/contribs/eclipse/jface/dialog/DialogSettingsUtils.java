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
package org.kalypso.contribs.eclipse.jface.dialog;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * Helper code for {@link org.eclipse.jface.dialogs.IDialogSettings}.
 * 
 * @author Gernot Belger
 */
public final class DialogSettingsUtils
{
  private DialogSettingsUtils( )
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the settings with the given name for a plugin. If no settings with this name already exists, one is
   * created.
   */
  public static IDialogSettings getDialogSettings( final AbstractUIPlugin plugin, final String sectionName )
  {
    final IDialogSettings workbenchSettings = plugin.getDialogSettings();
    return getSection( workbenchSettings, sectionName );
  }

  /**
   * Returns a sub-section of a given {@link IDialogSettings}.<br>
   * If the section does not yet exist, it is created.
   * 
   * @return <code>null</code>, if the given <code>settings</code> are <code>null</code>.
   */
  public static IDialogSettings getSection( final IDialogSettings settings, final String sectionName )
  {
    if( settings == null )
      return null;

    final IDialogSettings section = settings.getSection( sectionName );
    if( section == null )
      return settings.addNewSection( sectionName );
    return section;
  }

  /**
   * Calls {@link IDialogSettings#getInt(String)} but catches the {@link NumberFormatException} (e.g. if the key does
   * not exist) and returns the defaultValue.
   * 
   * @param defaultValue
   *          Returned, if the settings do not contain an integer for the given key.
   */
  public static int getInt( final IDialogSettings dialogSettings, final String key, final int defaultValue )
  {
    try
    {
      return dialogSettings.getInt( key );
    }
    catch( final NumberFormatException e )
    {
      // ignored
    }

    return defaultValue;
  }

  /**
   * Calls {@link IDialogSettings#getLong(String)} but catches the {@link NumberFormatException} (e.g. if the key does
   * not exist) and returns the defaultValue.
   * 
   * @param defaultValue
   *          Returned, if the settings do not contain a long for the given key.
   */
  public static long getLong( final IDialogSettings dialogSettings, final String key, final long defaultValue )
  {
    try
    {
      return dialogSettings.getLong( key );
    }
    catch( final NumberFormatException e )
    {
      // ignored
    }

    return defaultValue;
  }

  /**
   * Calls {@link IDialogSettings#getFloat(String)} but catches the {@link NumberFormatException} (e.g. if the key does
   * not exist) and returns the defaultValue.
   * 
   * @param defaultValue
   *          Returned, if the settings do not contain a float for the given key.
   */
  public static float getFloat( final IDialogSettings dialogSettings, final String key, final float defaultValue )
  {
    try
    {
      return dialogSettings.getFloat( key );
    }
    catch( final NumberFormatException e )
    {
      // ignored
    }

    return defaultValue;
  }

  /**
   * Calls {@link IDialogSettings#getDouble(String)} but catches the {@link NumberFormatException} (e.g. if the key does
   * not exist) and returns the defaultValue.
   * 
   * @param defaultValue
   *          Returned, if the settings do not contain a double for the given key.
   */
  public static double getDouble( final IDialogSettings dialogSettings, final String key, final double defaultValue )
  {
    try
    {
      return dialogSettings.getDouble( key );
    }
    catch( final NumberFormatException e )
    {
      // ignored
    }

    return defaultValue;
  }
}
