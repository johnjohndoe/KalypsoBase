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
package org.kalypso.model.wspm.ui.view.chart;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.changes.ProfilChangeHint;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.observation.result.IComponent;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.IEditableChartLayer;

/**
 * @author Kim Werner
 */
public interface IProfilChartLayer extends IEditableChartLayer
{
  /** key to store IChartLayer.getData() in ProfilViewdataObject */
  String VIEW_DATA_KEY = "org.kalypso.model.wspm.ui.view.ProfilViewData"; //$NON-NLS-1$

  /** values to store */
  Integer ALLOW_HORIZONTAL_EDITING = 1;

  Integer ALLOW_VERTICAL_EDITING = 2;

  String TOOLTIP_FORMAT = "%-12s %10.4f [m]%n%-12s %10.4f [%s]"; //$NON-NLS-1$

  RGB COLOR_ACTIVE = new RGB( 255, 0, 0 );

  float[] HOVER_DASH = new float[] { 1, 1, 1 };

  int POINT_STYLE_WIDTH = 5;

  /** Erzeugt eine Profil-View, welche die Spezifika dieses Layers anzeigt. */
  IProfilView createLayerPanel( );

  /**
   * L�scht diesen Layer aus dem Profil. Besser gesagt, l�scht die Daten aus dem Profil, die durch diesen Layer
   * repr�sentiert werden.
   * 
   * @throws IllegalProfileOperationException
   * @throws UnsupportedOperationException
   *           Falls diese Art von Layer nicht gel�scht werden kann.
   */

  void removeYourself( );

  void onProfilChanged( final ProfilChangeHint hint );

  IProfil getProfil( );

  void setProfil( final IProfil profil );

  IComponent getTargetComponent( );

  IComponent getDomainComponent( );

  void executeDrop( Point point, EditInfo dragStartData );

  void executeClick( EditInfo dragStartData );
}
