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
package org.kalypso.ogc.gml.schemaeditor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.ISources;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.restriction.EnumerationRestriction;
import org.kalypso.gmlschema.property.restriction.IRestriction;

/**
 * @author kimwerner
 */
public class ExportI18nPropertiesHandler extends AbstractHandler
{
  private final SortedProperties m_properties = new SortedProperties();

  private String m_fileName = null;

  private void writeProperty( final String prefix, final String key, final String kind, final String val )
  {
    if( val == null || key == null || key == "" )// || key.equals( val ) ) //$NON-NLS-1$
      return;

    m_properties.setProperty( prefix + "_" + key + "_" + kind, val ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    m_properties.clear();

    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final GMLSchemaEditor editor = (GMLSchemaEditor) context.getVariable( ISources.ACTIVE_EDITOR_NAME );

    final IGMLSchema schema = editor.getSchema();

    final String targetNamespace = schema.getTargetNamespace();

    final FileDialog dlg = new FileDialog( editor.getEditorSite().getShell(), SWT.SAVE );

    dlg.setFilterExtensions( new String[] { "*.properties", "*.*" } ); //$NON-NLS-1$ //$NON-NLS-2$
    dlg.setFilterNames( new String[] { "Property Files", "all Files" } ); //$NON-NLS-1$ //$NON-NLS-2$
    dlg.setOverwrite( true );

    final String[] url = schema.getContext().getFile().split( "\\/" ); //$NON-NLS-1$

    dlg.setFileName( url[url.length - 1].split( "\\." )[0] + "_" + System.getProperty( "org.osgi.framework.language" ) ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    dlg.open();
    String fName = dlg.getFileName();
    if( fName == null )
      return null;
    if( !fName.contains( "." ) ) //$NON-NLS-1$
      fName = fName + ".properties"; //$NON-NLS-1$
    m_fileName = dlg.getFilterPath() + "\\" + fName; //$NON-NLS-1$

    final NSPrefixProvider nsMapper = NSUtilities.getNSProvider();

    final IFeatureType[] featureTypes = schema.getAllFeatureTypes();
    for( final IFeatureType featureType : featureTypes )
    {
      final QName ftName = featureType.getQName();
      final String ftNamespaceURI = ftName.getNamespaceURI();
      if( ftNamespaceURI.equals( targetNamespace ) )
      {
        final String ftLocalPart = ftName.getLocalPart();
        final String ftPrefix = nsMapper.getPreferredPrefix( ftNamespaceURI, null );

        final String nsURI = ftNamespaceURI.replace( ':', '_' );
        final String nsURI2 = nsURI.replace( '/', '_' );

        m_properties.setProperty( nsURI2, ftPrefix );

        final IAnnotation ftAnno = featureType.getAnnotation();
        final String ftLabel = ftAnno.getLabel();
        final String ftDescripion = ftAnno.getDescription();

        writeProperty( ftPrefix, ftLocalPart, "label", ftLabel ); //$NON-NLS-1$
        writeProperty( ftPrefix, ftLocalPart, "description", ftDescripion ); //$NON-NLS-1$

        final IPropertyType[] properties = featureType.getProperties();
        for( final IPropertyType propertyType : properties )
        {
          final QName ptName = propertyType.getQName();

          final String ptNamespaceURI = ptName.getNamespaceURI();
          final String ptPrefix = nsMapper.getPreferredPrefix( ptNamespaceURI, null );
          final String nsptURI2 = ptNamespaceURI.replace( ':', '_' ).replace( '/', '_' );
          m_properties.setProperty( nsptURI2, ptPrefix );

          final IAnnotation pAnno = propertyType.getAnnotation();

          final String pLabel = pAnno.getLabel();
          final String pTooltip = pAnno.getTooltip();
          final String prefix = ftPrefix + "_" + ftName.getLocalPart() + "_" + ptPrefix; //$NON-NLS-1$ //$NON-NLS-2$

          writeProperty( prefix, ptName.getLocalPart(), "label", pLabel ); //$NON-NLS-1$
          writeProperty( prefix, ptName.getLocalPart(), "tooltip", pTooltip ); //$NON-NLS-1$

          if( propertyType instanceof IValuePropertyType )
          {
            final IValuePropertyType vpt = (IValuePropertyType) propertyType;
            final IRestriction[] restrictions = vpt.getRestriction();
            for( final IRestriction restriction : restrictions )
            {
              if( restriction instanceof EnumerationRestriction )
              {
                final EnumerationRestriction enumRest = (EnumerationRestriction) restriction;
                final Map<Object, IAnnotation> map = enumRest.getMapping();
                for( final Object obj : map.keySet() )
                {

                  final IAnnotation anno = map.get( obj );
                  final String aLabel = anno.getLabel();
                  final String aTooltip = anno.getTooltip();
                  final QName simpleType = enumRest.getSimpleType();
                  if( simpleType == null )
                  {
                    writeProperty( prefix + '_' + ptName.getLocalPart(), obj.toString(), "label", aLabel ); //$NON-NLS-1$
                    writeProperty( prefix + '_' + ptName.getLocalPart(), obj.toString(), "tooltip", aTooltip ); //$NON-NLS-1$
                  }
                  else
                  {
                    writeProperty( ftPrefix+"_"+simpleType.getLocalPart(), obj.toString(), "label", aLabel ); //$NON-NLS-1$ //$NON-NLS-2$
                    writeProperty( ftPrefix+"_"+simpleType.getLocalPart(), obj.toString(), "tooltip", aTooltip ); //$NON-NLS-1$ //$NON-NLS-2$
                  }
                }
              }
            }
          }
        }
      }
    }
    try
    {

      m_properties.store( new FileOutputStream( m_fileName ), schema.getContext().toString() );
    }
    catch( final IOException e )
    {
      e.printStackTrace();

      throw new ExecutionException( "Failed to export schema", e ); //$NON-NLS-1$
    }
    return null;
  }
}
