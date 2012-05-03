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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.runnables;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.RGB;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.LanduseProperties;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree_impl.filterencoding.ComplexFilter;
import org.kalypsodeegree_impl.filterencoding.Literal;
import org.kalypsodeegree_impl.filterencoding.PropertyIsLikeOperation;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

import com.google.common.base.Charsets;

/**
 * @author Dirk Kuch
 */
public class LanduseStyledLayerDescriptorBuilder implements ICoreRunnableWithProgress
{

  private final ILanduseModel m_mapping;

  private final IFile m_sldFile;

  public LanduseStyledLayerDescriptorBuilder( final ILanduseModel mapping, final IFile sldFile )
  {
    m_mapping = mapping;
    m_sldFile = sldFile;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException
  {
    final FeatureTypeStyle style = StyleFactory.createFeatureTypeStyle();
    style.setName( Messages.getString( "LanduseStyledLayerDescriptorBuilder.0" ) ); //$NON-NLS-1$

    final IDBFField column = m_mapping.getShapeColumn();
    if( Objects.isNull( column ) )
      return Status.OK_STATUS;

    final LanduseProperties properties = m_mapping.getMapping();
    final Set<Entry<Object, Object>> entries = properties.entrySet();
    for( final Entry<Object, Object> entry : entries )
    {
      final String property = (String) entry.getKey();
      final IClassificationClass clazz = findClass( entry.getValue() );
      if( Objects.isNull( property, clazz ) )
        continue;

      style.addRule( buildRule( column.getName(), property, clazz ) );
    }

    try
    {
      writeXML( style );
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
    finally
    {
      m_sldFile.refreshLocal( IResource.DEPTH_INFINITE, monitor );
    }

    return Status.OK_STATUS;
  }

  private void writeXML( final FeatureTypeStyle style ) throws CoreException
  {
    final String xml = SLDFactory.marshallObject( style, Charsets.UTF_8.name() );
    final ByteArrayInputStream bis = new ByteArrayInputStream( xml.getBytes( Charsets.UTF_8 ) );

    if( m_sldFile.exists() )
      m_sldFile.setContents( bis, false, true, new NullProgressMonitor() );
    else
      m_sldFile.create( bis, false, new NullProgressMonitor() );

// final Document doc = XMLTools.parse( new StringReader( style.exportAsXML() ) );
// final Source source = new DOMSource( doc );
//
// OutputStreamWriter os = null;
// try
// {
// os = new FileWriter( m_sldFile.getLocation().toFile() );
// final StreamResult result = new StreamResult( os );
// final TransformerFactory factory = TransformerFactory.newInstance();
//
// // Dejan: this works only with Java 1.5, in 1.4 it throws IllegalArgumentException
// // also, indentation doesn't works with OutputStream, only with OutputStreamWriter :)
// try
// {
//        factory.setAttribute( "indent-number", new Integer( 4 ) ); //$NON-NLS-1$
// }
// catch( final IllegalArgumentException e )
// {
// }
//
// final Transformer transformer = factory.newTransformer();
//      transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" ); //$NON-NLS-1$
//      transformer.setOutputProperty( OutputKeys.INDENT, "yes" ); //$NON-NLS-1$
// transformer.transform( source, result );
// }
// finally
// {
// IOUtils.closeQuietly( os );
// }
  }

  private Rule buildRule( final String column, final String property, final IClassificationClass clazz )
  {
    final PolygonSymbolizer symbolizer = toSymbolizer( clazz );
    final Rule rule = StyleFactory.createRule( symbolizer );
    rule.setName( property );
    rule.setTitle( clazz.getDescription() );
    rule.setAbstract( String.format( Messages.getString( "LanduseStyledLayerDescriptorBuilder.1" ), property, clazz.getName() ) ); //$NON-NLS-1$

    final Operation operation = new PropertyIsLikeOperation( new PropertyName( column, null ), new Literal( property ), '*', '$', '/' ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    final Filter filter = new ComplexFilter( operation );
    rule.setFilter( filter );

    return rule;
  }

  private PolygonSymbolizer toSymbolizer( final IClassificationClass clazz )
  {
    final PolygonSymbolizer symbolizer = StyleFactory.createPolygonSymbolizer();

    final Fill fill = StyleFactory.createFill();
    final RGB color = clazz.getColor();
    if( Objects.isNotNull( color ) )
      fill.setFill( new Color( color.red, color.green, color.blue ) );
    fill.setOpacity( 0.5 );
    symbolizer.setFill( fill );

    symbolizer.setStroke( StyleFactory.createStroke( Color.BLACK, 1.0, 1.0 ) );

    return symbolizer;
  }

  private IClassificationClass findClass( final Object value )
  {
    if( !(value instanceof String) )
      return null;

    final String name = (String) value;

    final IClassificationClass[] classes = m_mapping.getClasses();
    for( final IClassificationClass clazz : classes )
    {

      if( StringUtils.equals( clazz.getName(), name ) )
        return clazz;
    }

    return null;
  }
}
