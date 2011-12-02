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
package org.kalypso.ui.preferences;

import java.util.Arrays;
import java.util.TimeZone;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.kalypso.contribs.eclipse.jface.preference.ComboStringFieldEditor;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.preferences.IKalypsoCorePreferences;
import org.kalypso.i18n.Messages;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing
 * <samp>FieldEditorPreferencePage </samp>, we can use the field support built into JFace that allows us to create a
 * page that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the preference store that belongs to the main
 * plug-in class. That way, preferences can be accessed directly via the preference store.
 */
public class KalypsoGeneralPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
  private ComboStringFieldEditor m_timeZoneFieldEditor;

  public KalypsoGeneralPreferencePage( )
  {
    super( GRID );
    setPreferenceStore( KalypsoCorePlugin.getDefault().getPreferenceStore() );
    setDescription( Messages.getString( "org.kalypso.ui.preferences.KalypsoGeneralPreferencePage.0" ) ); //$NON-NLS-1$
  }

  /**
   * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
   * types of preferences. Each field editor knows how to save and restore itself.
   */
  @Override
  public void createFieldEditors( )
  {
    // fetch list of timezone names and sort it
    final String[] ids = TimeZone.getAvailableIDs();
    Arrays.sort( ids );

    m_timeZoneFieldEditor = new ComboStringFieldEditor( IKalypsoCorePreferences.DISPLAY_TIMEZONE, //
    Messages.getString( "org.kalypso.ui.preferences.KalypsoGeneralPreferencePage.3" ),//$NON-NLS-1$
    Messages.getString( "org.kalypso.ui.preferences.KalypsoGeneralPreferencePage.4" ), getFieldEditorParent(), false, ids );//$NON-NLS-1$
    addField( m_timeZoneFieldEditor );
  }

  /**
   * @see org.eclipse.jface.preference.FieldEditorPreferencePage#initialize()
   */
  @Override
  protected void initialize( )
  {
    super.initialize();

    m_timeZoneFieldEditor.setPreferenceStore( KalypsoCorePlugin.getDefault().getPreferenceStore() );
    m_timeZoneFieldEditor.load();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
   */
  @Override
  public void init( final IWorkbench workbench )
  {
  }

  /**
   * @see org.eclipse.jface.preference.IPreferencePage#performOk()
   */
  @Override
  public boolean performOk( )
  {
    final boolean result = super.performOk();

    // even if on shutdown the preferences are saved, we save them in case of a platfrom crash
    KalypsoCorePlugin.getDefault().savePluginPreferences();

    return result;
  }
}