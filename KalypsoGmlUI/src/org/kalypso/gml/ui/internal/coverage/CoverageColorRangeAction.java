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
package org.kalypso.gml.ui.internal.coverage;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.SortedMap;

import org.apache.commons.lang3.Range;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Event;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.coverage.CoverageManagementWidget;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.RasterSymbolizer;
import org.kalypsodeegree.model.elevation.ElevationUtilities;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;

/**
 * @author Gernot Belger
 */
public class CoverageColorRangeAction extends Action implements IUpdateable
{
  private final CoverageManagementWidget m_widget;

  public CoverageColorRangeAction( final CoverageManagementWidget widget )
  {
    m_widget = widget;

    setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.4" ) ); //$NON-NLS-1$
    setImageDescriptor( KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.STYLE_EDIT ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final IKalypsoFeatureTheme selectedTheme = m_widget.getSelectedTheme();
    final ICoverage[] coverages = m_widget.getCoverages();
    if( selectedTheme == null || coverages == null )
    {
      // no message, should not happen because action is disabled in this case
      return;
    }

    final IKalypsoFeatureTheme[] allCoverageThemes = m_widget.findThemesForCombo();

    final CoverageColormapHandler colormapHandler = new CoverageColormapHandler( selectedTheme, allCoverageThemes );

    final RasterSymbolizer symb = colormapHandler.getRasterSymbolizer();
    if( symb == null )
    {
      MessageDialog.openWarning( event.display.getActiveShell(), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.4" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    final SortedMap<Double, ColorMapEntry> input = symb.getColorMap();

    if( input == null )
      return;

    // convert map into an array
    final Collection<ColorMapEntry> values = input.values();
    final ColorMapEntry[] entries = values.toArray( new ColorMapEntry[values.size()] );

    final Range<Double> minMax = ElevationUtilities.calculateRange( coverages );

    final BigDecimal min = asBigDecimal( minMax.getMinimum() );
    final BigDecimal max = asBigDecimal( minMax.getMaximum() );

    // open dialog
    final GridStyleDialog dialog = new GridStyleDialog( event.display.getActiveShell(), entries, min, max );
    if( dialog.open() == Window.OK )
    {
      colormapHandler.updateRasterSymbolizer( event.display.getActiveShell(), dialog.getColorMap() );

      m_widget.updateStylePanel();
    }
  }

  /**
   * protect against too many digits; still ugly, as number of digits depends on real data type...
   */
  private BigDecimal asBigDecimal( final Double value )
  {
    final BigDecimal decimal = new BigDecimal( Double.toString( value ) );
    if( decimal.scale() < 5 )
      return decimal;

    return decimal.setScale( 4, BigDecimal.ROUND_HALF_UP );
  }

  @Override
  public void update( )
  {
    final ICoverage[] allCoverages = m_widget.getCoverages();
    setEnabled( allCoverages != null && allCoverages.length > 0 );
  }
}