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
package org.kalypso.service.wps.utils;

import java.math.BigInteger;
import java.util.List;

import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.ObjectFactory;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;

/**
 * Utility functions for complying to the OGC WPS standard.
 * 
 * @author Holger Albert
 */
public class OGCUtilities
{
  public final static ObjectFactory WPS_OF = new ObjectFactory();

  /**
   * Service type identifier.
   */
  public static final String SERVICE = "WPS";

  /**
   * Version.
   */
  public static final String VERSION = "0.4.0";

  /**
   * The constructor.
   */
  private OGCUtilities( )
  {
  }

  /**
   * Note: The documentation is taken from the specific ows1911subset.xsd for this type.<br>
   * <br>
   * Name or code with an (optional) authority. If the codeSpace attribute is present, its value should reference a
   * dictionary, thesaurus, or authority for the name or code, such as the organisation who assigned the value, or the
   * dictionary from which it is taken.
   * 
   * @param codeSpace
   *          [optional]
   * @param value
   */
  public static CodeType buildCodeType( String codeSpace, String value )
  {
    /* Create the instance via the factory. */
    net.opengeospatial.ows.ObjectFactory factory = new net.opengeospatial.ows.ObjectFactory();
    CodeType code = factory.createCodeType();

    /* Attributes. */
    if( codeSpace != null )
      code.setCodeSpace( codeSpace );

    /* Values. */
    code.setValue( value );

    return code;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsExecute.xsd for this type.<br>
   * <br>
   * Reference to an input or output value that is a web accessible resource.
   * 
   * @param reference
   *          Reference to data or metadata recorded elsewhere, either external to this XML document or within it.
   *          Whenever practical, this attribute should be a URL from which this metadata can be electronically
   *          retrieved. Alternately, this attribute can reference a URN for well-known metadata. For example, such a
   *          URN could be a URN defined in the "ogc" URN namespace.
   * @param format
   *          [optional] The Format of this input or requested for this output (e.g., text/XML). This element shall be
   *          omitted when the Format is indicated in the http header of the output. When included, this format shall be
   *          one published for this output or input in the Process full description.
   * @param encoding
   *          [optional] The encoding of this input or requested for this output (e.g., UTF-8). This "encoding" shall be
   *          included whenever the encoding required is not the default encoding indicated in the Process full
   *          description. When included, this encoding shall be one published for this output or input in the Process
   *          full description.
   * @param schema
   *          [optional] Web-accessible XML Schema Document that defines the content model of this complex resource
   *          (e.g., encoded using GML 2.2 Application Schema). This reference should be included for XML encoded
   *          complex resources to facilitate validation.
   */
  public static ComplexValueReference buildComplexValueReference( String reference, String format, String encoding, String schema )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    ComplexValueReference complexValueReference = factory.createIOValueTypeComplexValueReference();

    /* Attributes. */
    complexValueReference.setReference( reference );

    if( format != null )
      complexValueReference.setFormat( format );

    if( encoding != null )
      complexValueReference.setEncoding( encoding );

    if( schema != null )
      complexValueReference.setSchema( schema );

    return complexValueReference;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * One complex value (such as an image), including a definition of the complex value data structure (i.e., schema,
   * format, and encoding).
   * 
   * @param format
   *          [optional] The Format of this input or requested for this output (e.g., text/XML). This element shall be
   *          omitted when the Format is indicated in the http header of the output. When included, this format shall be
   *          one published for this output or input in the Process full description.
   * @param encoding
   *          [optional] The encoding of this input or requested for this output (e.g., UTF-8). This "encoding" shall be
   *          included whenever the encoding required is not the default encoding indicated in the Process full
   *          description. When included, this encoding shall be one published for this output or input in the Process
   *          full description.
   * @param schema
   *          [optional] Web-accessible XML Schema Document that defines the content model of this complex resource
   *          (e.g., encoded using GML 2.2 Application Schema). This reference should be included for XML encoded
   *          complex resources to facilitate validation.
   * @param value
   */
  public static ComplexValueType buildComplexValueType( String format, String encoding, String schema, List<Object> value )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    ComplexValueType complexValue = factory.createComplexValueType();

    /* Attributes. */
    if( format != null )
      complexValue.setFormat( format );

    if( encoding != null )
      complexValue.setEncoding( encoding );

    if( schema != null )
      complexValue.setSchema( schema );

    /* Elements. */
    complexValue.getContent().addAll( value );

    return complexValue;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * One simple literal value (such as an integer or real number) that is embedded in the Execute operation request or
   * response.
   * 
   * @param value
   *          String containing the Literal value (e.g., "49").
   * @param dataType
   *          [optional] Identifies the data type of this literal input or output. This dataType should be included for
   *          each quantity whose value is not a simple string.
   * @param uom
   *          [optional] Identifies the unit of measure of this literal input or output. This unit of measure should be
   *          referenced for any numerical value that has units (e.g., "meters", but not a more complete reference
   *          system). Shall be a UOM identified in the Process description for this input or output.
   */
  public static LiteralValueType buildLiteralValueType( String value, String dataType, String uom )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    LiteralValueType literalValue = factory.createLiteralValueType();

    /* Values. */
    literalValue.setValue( value );

    /* Attributes. */
    if( dataType != null )
      literalValue.setDataType( dataType );

    if( uom != null )
      literalValue.setUom( uom );

    return literalValue;
  }

  /**
   * Note: The documentation is taken from the specific owsCommon.xsd for this type.<br>
   * <br>
   * XML encoded minimum rectangular bounding box (or region) parameter, surrounding all the associated data.
   * 
   * @param lowerCorner
   *          Position of the bounding box corner at which the value of each coordinate normally is the algebraic
   *          minimum within this bounding box. In some cases, this position is normally displayed at the top, such as
   *          the top left for some image coordinates. For more information, see Subclauses 10.2.5 and C.13.
   * @param upperCorner
   *          Position of the bounding box corner at which the value of each coordinate normally is the algebraic
   *          maximum within this bounding box. In some cases, this position is normally displayed at the bottom, such
   *          as the bottom right for some image coordinates. For more information, see Subclauses 10.2.5 and C.13.
   * @param crs
   *          [optional] Usually references the definition of a CRS, as specified in [OGC Topic 2]. Such a CRS
   *          definition can be XML encoded using the gml:CoordinateReferenceSystemType in [GML 3.1]. For well known
   *          references, it is not required that a CRS definition exist at the location the URI points to. If no anyURI
   *          value is included, the applicable CRS must be either:<br>
   *          a) Specified outside the bounding box, but inside a data structure that includes this bounding box, as
   *          specified for a specific OWS use of this bounding box type.<br>
   *          b) Fixed and specified in the Implementation Specification for a specific OWS use of the bounding box
   *          type.
   * @param dimensions
   *          [optional] The number of dimensions in this CRS (the length of a coordinate sequence in this use of the
   *          PositionType). This number is specified by the CRS definition, but can also be specified here.
   */
  public static BoundingBoxType buildBoundingBoxType( List<Double> lowerCorner, List<Double> upperCorner, String crs, BigInteger dimensions )
  {
    /* Create the instance via the factory. */
    net.opengeospatial.ows.ObjectFactory factory = new net.opengeospatial.ows.ObjectFactory();
    BoundingBoxType boundingBox = factory.createBoundingBoxType();

    /* Elements. */
    boundingBox.getLowerCorner().addAll( lowerCorner );
    boundingBox.getUpperCorner().addAll( upperCorner );

    /* Attributes. */
    if( crs != null )
      boundingBox.setCrs( crs );

    if( dimensions != null )
      boundingBox.setDimensions( dimensions );

    return boundingBox;
  }

  /**
   * Note: The documentation is taken from the specific wpsCommon.xsd, wpsExecute.xsd for this type.<br>
   * <br>
   * Value of one input to a process or one output from a process.
   * 
   * @param identifier
   *          Unambiguous identifier or name of a process, unique for this server, or unambiguous identifier or name of
   *          an input or output, unique for this process.
   * @param title
   *          Title of a process, input, or output, normally available for display to a human.
   * @param abstrakt
   *          Brief narrative description of a process, input, or output, normally available for display to a human.
   * @param valueFormChoice
   *          Identifies the form of this input or output value, and provides supporting information. <br>
   *          Possible are
   *          <ol>
   *          <li> ComplexValueReference</li>
   *          <li> ComplexValueType</li>
   *          <li> LiteralValueType</li>
   *          <li> BoundingBoxType</li>
   *          </ol>
   */
  public static IOValueType buildIOValueType( CodeType identifier, String title, String abstrakt, Object valueFormChoice )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    IOValueType ioValue = factory.createIOValueType();

    /* Elements. */
    ioValue.setIdentifier( identifier );
    ioValue.setTitle( title );
    ioValue.setAbstract( abstrakt );

    if( valueFormChoice instanceof ComplexValueReference )
      ioValue.setComplexValueReference( (ComplexValueReference) valueFormChoice );
    else if( valueFormChoice instanceof ComplexValueType )
      ioValue.setComplexValue( (ComplexValueType) valueFormChoice );
    else if( valueFormChoice instanceof LiteralValueType )
      ioValue.setLiteralValue( (LiteralValueType) valueFormChoice );
    else if( valueFormChoice instanceof BoundingBoxType )
      ioValue.setBoundingBoxValue( (BoundingBoxType) valueFormChoice );

    return ioValue;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * List of the Inputs provided as part of the Execute Request.
   * 
   * @param ioValues
   *          Unordered list of one or more inputs to be used by the process, including each of the Inputs needed to
   *          execute the process.
   */
  public static DataInputsType buildDataInputs( List<IOValueType> ioValues )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    DataInputsType dataInputs = factory.createDataInputsType();

    dataInputs.getInput().addAll( ioValues );

    return dataInputs;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * Definition of a format, encoding, schema, and unit-of-measure for an output to be returned from a process.
   * 
   * @param identifier
   *          Unambiguous identifier or name of an output, unique for this process.
   * @param title
   *          Title of the process output, normally available for display to a human. This element should be used if the
   *          client wishes to customize the Title in the execute response. This element should not be used if the Title
   *          provided for this output in the ProcessDescription is adequate.
   * @param abstrakt
   *          Brief narrative description of a process output, normally available for display to a human. This element
   *          should be used if the client wishes to customize the Abstract in the execute response. This element should
   *          not be used if the Abstract provided for this output in the ProcessDescription is adequate.
   * @param uom
   *          [optional] Reference to the unit of measure (if any) requested for this output. A uom can be referenced
   *          when a client wants to specify one of the units of measure supported for this output. This uom shall be a
   *          unit of measure referenced for this output of this process in the Process full description.
   * @param format
   *          [optional] The Format of this input or requested for this output (e.g., text/XML). This element shall be
   *          omitted when the Format is indicated in the http header of the output. When included, this format shall be
   *          one published for this output or input in the Process full description.
   * @param encoding
   *          [optional] The encoding of this input or requested for this output (e.g., UTF-8). This "encoding" shall be
   *          included whenever the encoding required is not the default encoding indicated in the Process full
   *          description. When included, this encoding shall be one published for this output or input in the Process
   *          full description.
   * @param schema
   *          [optional] Web-accessible XML Schema Document that defines the content model of this complex resource
   *          (e.g., encoded using GML 2.2 Application Schema). This reference should be included for XML encoded
   *          complex resources to facilitate validation.
   */
  public static OutputDefinitionType buildOutputDefinitionType( CodeType identifier, String title, String abstrakt, String uom, String format, String encoding, String schema )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    OutputDefinitionType outputDefinition = factory.createOutputDefinitionType();

    /* Elements. */
    outputDefinition.setIdentifier( identifier );
    outputDefinition.setTitle( title );
    outputDefinition.setAbstract( abstrakt );

    /* Attributes. */
    if( uom != null )
      outputDefinition.setUom( uom );

    if( encoding != null )
      outputDefinition.setEncoding( encoding );

    if( format != null )
      outputDefinition.setFormat( format );

    if( schema != null )
      outputDefinition.setSchema( schema );

    return outputDefinition;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * List of definitions of the outputs (or parameters) requested from the process.
   * 
   * @param outputDefinition
   *          Unordered list of one or more definitions of the outputs requested. This element shall be repeated for
   *          each Output that offers a choice of format, and the client wishes to use one that is not identified as the
   *          default, and/or for each Output that the client wishes to customize the descriptive information about the
   *          output.
   */
  public static OutputDefinitionsType buildOutputDefinitions( List<OutputDefinitionType> outputDefinition )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    OutputDefinitionsType outputDefinitions = factory.createOutputDefinitionsType();

    outputDefinitions.getOutput().addAll( outputDefinition );

    return outputDefinitions;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * WPS Execute operation request, to execute one identified Process. If a process is to be run multiple times, each
   * run shall be submitted as a separate Execute request.
   * 
   * @param identifier
   *          Identifier of the process to be executed. This process identifier shall be as listed in the
   *          ProcessOfferings section of the WPS Capabilities document.
   * @param dataInputs
   *          List of input (or parameter) values provided to the process, including each of the Inputs needed to
   *          execute the process. It is possible to have no inputs provided only when all the inputs are predetermined
   *          fixed resources. In all other cases, at least one input is required.
   * @param outputDefinitions
   *          List of definitions of the outputs (or parameters) requested from the process. These outputs are not
   *          normally identified, unless the client is specifically requesting a limited subset of outputs, and/or is
   *          requesting output formats and/or schemas and/or encodings different from the defaults and selected from
   *          the alternatives identified in the process description, or wishes to customize the descriptive information
   *          about the output.
   * @param store
   *          [optional] Specifies if the complex valued output(s) of this process should be stored by the process as
   *          web-accessible resources. If store is "true", the server shall store all the complex valued output(s) of
   *          the process so that the client can retrieve them as required. If store is "false", all the complex valued
   *          output(s) shall be encoded in the Execute operation response. This parameter shall not be included unless
   *          the corresponding "storeSupported" parameter is included and is "true" in the ProcessDescription for this
   *          process.
   * @param status
   *          [optional] Specifies if the Execute operation response shall be returned quickly with status information,
   *          or not returned until process execution is complete. This parameter shall not be included unless the
   *          corresponding "statusSupported" parameter is included and is "true" in the ProcessDescription for this
   *          process.
   */
  public static Execute buildExecuteOperationRequest( CodeType identifier, DataInputsType dataInputs, OutputDefinitionsType outputDefinitions, Boolean store, Boolean status )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    Execute execute = factory.createExecute();

    /* Attributes. */
    execute.setService( SERVICE );
    execute.setVersion( VERSION );

    if( store != null )
      execute.setStore( store );

    if( store != null )
      execute.setStatus( status );

    /* Elements. */
    execute.setIdentifier( identifier );
    execute.setDataInputs( dataInputs );
    execute.setOutputDefinitions( outputDefinitions );

    return execute;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * WPS DescribeProcess operation request.
   * 
   * @param identifier
   *          Unordered list of one or more identifiers of the processes for which the client is requesting detailed
   *          descriptions. This element shall be repeated for each process for which a description is requested. These
   *          Identifiers are unordered, but the WPS shall return the descriptions in the order in which they were
   *          requested.
   */
  public static DescribeProcess buildDescribeProcessOperationRequest( List<CodeType> identifier )
  {
    /* Create the instance via the factory. */
    ObjectFactory factory = new ObjectFactory();
    DescribeProcess describeProcess = factory.createDescribeProcess();

    /* Attributes. */
    describeProcess.setService( SERVICE );
    describeProcess.setVersion( VERSION );

    describeProcess.getIdentifier().addAll( identifier );

    return describeProcess;
  }
}