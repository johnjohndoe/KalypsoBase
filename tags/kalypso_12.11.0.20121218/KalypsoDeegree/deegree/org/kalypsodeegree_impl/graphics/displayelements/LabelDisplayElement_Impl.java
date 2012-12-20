/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.graphics.displayelements;

import java.awt.Graphics;
import java.awt.Graphics2D;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.graphics.displayelements.Label;
import org.kalypsodeegree.graphics.displayelements.LabelDisplayElement;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_Object;

/**
 * <tt>DisplayElement</tt> that encapsulates a <tt>GM_Object</tt> (geometry), a <tt>ParameterValueType</tt> (caption)
 * and a <tt>TextSymbolizer</tt> (style).
 * <p>
 * The graphical (say: screen) representations of this <tt>DisplayElement</tt> are <tt>Label</tt> -instances. These are
 * generated either when the <tt>paint</tt> -method is called or assigned externally using the <tt>setLabels</tt>- or
 * <tt>addLabels</tt> -methods.
 * <p>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author <a href="mailto:mschneider@lat-lon.de">Markus Schneider </a>
 * @version $Revision$ $Date$
 */
public class LabelDisplayElement_Impl extends GeometryDisplayElement_Impl implements LabelDisplayElement
{
  /** Use serialVersionUID for interoperability. */
  private final static long serialVersionUID = -7870967255670858503L;

  private ParameterValueType m_label = null;

  private final ILabelPlacementStrategy m_strategy;

  /**
   * Creates a new LabelDisplayElement_Impl object.
   * <p>
   *
   * @param feature
   *          associated <tt>Feature</tt>
   * @param geometry
   *          associated <tt>GM_Object</tt>
   * @param symbolizer
   *          associated <tt>TextSymbolizer</tt>
   */
  LabelDisplayElement_Impl( final Feature feature, final GM_Object[] geometry, final TextSymbolizer symbolizer, final ILabelPlacementStrategy strategy )
  {
    super( feature, geometry, symbolizer );

    m_strategy = strategy;

    setLabel( symbolizer.getLabel() );
  }

  /**
   * Sets the caption of the label.
   */
  @Override
  public void setLabel( final ParameterValueType label )
  {
    m_label = label;
  }

  /**
   * Returns the caption of the label as <tt>ParameterValueType<tt>.
   */
  @Override
  public ParameterValueType getLabel( )
  {
    return m_label;
  }

  /**
   * Renders the <tt>DisplayElement</tt> to the submitted graphic context. If the <tt>Label</tt> -represenations have
   * been assigned externally, these labels are used, else <tt>Label</tt> -instances are created automatically using the
   * <tt>LabelFactory</tt>.
   * <p>
   *
   * @param g
   *          <tt>Graphics</tt> context to be used
   * @param projection
   *          <tt>GeoTransform</tt> to be used
   */
  @Override
  public void paint( final Graphics g, final GeoTransform projection, final IProgressMonitor monitor ) throws CoreException
  {
    try
    {
      if( m_label == null )
        return;

      final Graphics2D g2D = (Graphics2D) g;
      final Label[] labels = new LabelFactory( this, projection, g2D ).createLabels();

      if( m_strategy == null )
      {
        for( final Label label : labels )
          label.paint( g2D );
      }
      else
        m_strategy.add( labels );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new CoreException( new Status( IStatus.ERROR, KalypsoDeegreePlugin.getID(), "Failed to paint labels", e ) );
    }
  }

  @Override
  public TextSymbolizer getSymbolizer( )
  {
    return (TextSymbolizer) super.getSymbolizer();
  }
}