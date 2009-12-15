/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/
package org.kalypso.simulation.ui.ant;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.java.lang.reflect.ClassUtilities;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.services.observation.KalypsoServiceObsActivator;
import org.kalypso.services.observation.client.CommitPrognoseFeatureVisitor;
import org.kalypso.services.observation.sei.IObservationService;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * Ant task to call the {@link org.kalypso.simulation.ui.wizards.calculation.modelpages.CommitPrognoseFeatureVisitor}.
 * 
 * @author schlienger
 */
public class CommitObservationsTask extends AbstractFeatureVisitorTask
{
  private String m_localObs;
  private String m_remoteObs;
  private String m_sourceFilter;

  public final void setLocalObs( final String localObs )
  {
    m_localObs = localObs;
  }

  public final void setRemoteObs( final String remoteObs )
  {
    m_remoteObs = remoteObs;
  }

  public final void setSourceFilter( final String sourceFilter )
  {
    m_sourceFilter = sourceFilter;
  }

  public CommitObservationsTask()
  {
    super( false );
  }

  /**
   * @see org.kalypso.simulation.ui.ant.AbstractFeatureVisitorTask#createVisitor(java.net.URL,
   *      org.kalypso.contribs.java.net.IUrlResolver, org.kalypso.contribs.java.util.logging.ILogger)
   */
  @Override
  protected final FeatureVisitor createVisitor( final URL context, final ILogger logger )
  {
    final IObservationService srv = KalypsoServiceObsActivator.getDefault().getDefaultObservationService();
    return new CommitPrognoseFeatureVisitor( srv, context, m_localObs, m_remoteObs, m_sourceFilter );
  }

  /**
   * @see org.kalypso.ant.AbstractFeatureVisitorTask#statusFromVisitor(org.kalypsodeegree.model.feature.FeatureVisitor)
   */
  @Override
  protected final IStatus statusFromVisitor( final FeatureVisitor visitor )
  {
    final CommitPrognoseFeatureVisitor v = (CommitPrognoseFeatureVisitor) visitor;
    if( v.getStati().length > 0 )
      return new MultiStatus( KalypsoGisPlugin.getId(), 0, v.getStati(), "", null );

    return Status.OK_STATUS;
  }

  /**
   * @see org.kalypso.ant.AbstractFeatureVisitorTask#validateInput()
   */
  @Override
  protected void validateInput()
  {}

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.IErrorHandler#handleError(org.eclipse.swt.widgets.Shell,
   *      org.eclipse.core.runtime.IStatus)
   */
  public final void handleError( final Shell shell, final IStatus status )
  {
    ErrorDialog.openError( shell, ClassUtilities.getOnlyClassName( getClass() ),
        "Fehler beim Zurückschreiben der Zeitreihen", status );
  }
}
