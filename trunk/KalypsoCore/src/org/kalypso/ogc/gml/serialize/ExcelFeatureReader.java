package org.kalypso.ogc.gml.serialize;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gml.util.Excelsource;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.convert.GmlConvertException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree_impl.gml.binding.shape.AbstractShape;

/**
 * Lädt und schreibt ein Excel als {@link org.kalypsodeegree.model.feature.GMLWorkspace}. Die Information, welche Spalte
 * wie gelesen wird, wird per {@link #addInfo(IPropertyType, CSVInfo)}übergeben.<br/>
 * <br/>
 * Es wird nur das erste sheet gelesen.
 * 
 * @author Gernot Belger
 */
public final class ExcelFeatureReader extends AbstractTabularFeatureReader
{
  private final String m_href;

  public ExcelFeatureReader( final Excelsource type, final IUrlResolver resolver, final URL context ) throws GmlConvertException, GMLSchemaException
  {
    super( type, resolver, context );

    m_href = type.getHref();
  }

  @Override
  public void read( ) throws GmlConvertException
  {
    try
    {
      final URL url = super.resolve( m_href );

      final File excelFile = ResourceUtilities.findJavaFileFromURL( url );

      if( excelFile == null )
      {
        final String message = String.format( Messages.getString( "ExcelFeatureReader.0" ), m_href ); //$NON-NLS-1$
        throw new GmlConvertException( message );
      }

      /* Create the poi file system. */
      final POIFSFileSystem fs = new POIFSFileSystem( new FileInputStream( excelFile ) );

      /* Create the workbook. */
      final HSSFWorkbook wb = new HSSFWorkbook( fs );

      final int numberOfSheets = wb.getNumberOfSheets();
      if( numberOfSheets == 0 )
        throw new GmlConvertException( Messages.getString( "ExcelFeatureReader.1" ) ); //$NON-NLS-1$

      final HSSFSheet sheet = wb.getSheetAt( 0 );

      loadSheet( sheet );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new GmlConvertException( Messages.getString( "org.kalypso.ogc.gml.convert.source.CsvSourceHandler.1" ) + m_href, e ); //$NON-NLS-1$
    }
  }

  private void loadSheet( final HSSFSheet sheet ) throws CsvException, FilterEvaluationException
  {
    final IFeatureBindingCollection<AbstractShape> shapeCollection = getFeatureList();
    final Feature parentFeature = shapeCollection.getParentFeature();
    final IRelationType parentRelation = shapeCollection.getFeatureList().getPropertyType();
    final IFeatureType featureType = parentRelation.getTargetFeatureType();

    final int firstRowNum = sheet.getFirstRowNum();
    final int lastRowNum = sheet.getLastRowNum();

    int skippedLines = getLineskip();

    for( int i = firstRowNum; i < lastRowNum + 1; i++ )
    {
      if( skippedLines-- > 0 )
        continue;

      final HSSFRow row = sheet.getRow( i );

      final String[] tokens = parseRow( row );

      final Feature newFeature = createFeatureFromTokens( parentFeature, parentRelation, "" + i, tokens, featureType ); //$NON-NLS-1$
      if( acceptFeature( newFeature ) )
        shapeCollection.getFeatureList().add( newFeature );
    }
  }

  private String[] parseRow( final HSSFRow row )
  {
    final short firstCellNum = row.getFirstCellNum();
    final short lastCellNum = row.getLastCellNum();

    final String[] tokens = new String[lastCellNum - firstCellNum];

    for( int i = firstCellNum; i < lastCellNum; i++ )
    {
      final HSSFCell cell = row.getCell( i );
      tokens[i] = parseCell( cell );
    }

    return tokens;
  }

  private String parseCell( final HSSFCell cell )
  {
    if( cell == null )
      return StringUtils.EMPTY;

    final int cellType = cell.getCellType();
    switch( cellType )
    {
      case Cell.CELL_TYPE_BLANK:
        return cell.getStringCellValue();

      case Cell.CELL_TYPE_BOOLEAN:
        return Boolean.toString( cell.getBooleanCellValue() );

      case Cell.CELL_TYPE_ERROR:
        return "" + cell.getErrorCellValue(); //$NON-NLS-1$

      case Cell.CELL_TYPE_FORMULA:
        return cell.getStringCellValue();

      case Cell.CELL_TYPE_NUMERIC:
      {
        // TODO: strange: we get numeric even if the cell is marked as 'Text';
        // regardless of the real type (int,...) we always get a double.
        final double numericCellValue = cell.getNumericCellValue();
        final BigDecimal asDecimal = new BigDecimal( numericCellValue );
        return asDecimal.toPlainString();
      }

      case Cell.CELL_TYPE_STRING:
        return cell.getStringCellValue();

      default:
        throw new IllegalStateException();
    }
  }
}