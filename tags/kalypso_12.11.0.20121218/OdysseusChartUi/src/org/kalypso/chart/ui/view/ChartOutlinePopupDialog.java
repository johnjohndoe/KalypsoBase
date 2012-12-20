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
package org.kalypso.chart.ui.view;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.chart.ui.editor.ChartEditorTreeOutlinePage;
import org.kalypso.chart.ui.internal.OdysseusChartUiPlugin;
import org.kalypso.commons.java.lang.Strings;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;

import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author burtscher1
 */
public class ChartOutlinePopupDialog extends PopupDialog
{
  private final IChartComposite m_chartComposite;

  public ChartOutlinePopupDialog( final Shell parentShell, final IChartComposite chartComposite )
  {
    super( parentShell, SWT.RESIZE, true, true, true, false, false, "", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    m_chartComposite = chartComposite;
  }

  @Override
  protected Control createDialogArea( final Composite parent )
  {
    if( m_chartComposite != null )
    {
      final Composite da = (Composite) super.createDialogArea( parent );
      da.setLayout( new GridLayout( 1, true ) );

      final ChartEditorTreeOutlinePage cop = new ChartEditorTreeOutlinePage();
      cop.setModel( m_chartComposite.getChartModel() );
      cop.createControl( da );

      final TitleTypeBean[] titles = m_chartComposite.getChartModel().getSettings().getTitles();
      final String title = getTitle( titles );

      setTitleText( title );

      return da;
    }

    return new Composite( parent, SWT.NONE );
  }

  private String getTitle( final TitleTypeBean[] titles )
  {
    if( ArrayUtils.isEmpty( titles ) )
      return ""; //$NON-NLS-1$

    final TitleTypeBean bean = titles[0];
    final String text = bean.getText();
    if( Strings.isEmpty( text ) )
      return ""; //$NON-NLS-1$

    return text;
  }

  @Override
  protected IDialogSettings getDialogSettings( )
  {
    return DialogSettingsUtils.getDialogSettings( OdysseusChartUiPlugin.getDefault(), ChartOutlinePopupDialog.class.getName() );
  }

  protected IChartComposite getChartComposite( )
  {
    return m_chartComposite;
  }
}
