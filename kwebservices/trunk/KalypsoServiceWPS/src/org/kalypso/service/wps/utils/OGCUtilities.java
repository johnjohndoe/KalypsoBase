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

import javax.xml.bind.JAXBElement;

import net.opengeospatial.ows.AllowedValues;
import net.opengeospatial.ows.AnyValue;
import net.opengeospatial.ows.BoundingBoxType;
import net.opengeospatial.ows.CodeType;
import net.opengeospatial.ows.DomainMetadataType;
import net.opengeospatial.ows.ExceptionReport;
import net.opengeospatial.ows.ExceptionType;
import net.opengeospatial.ows.MetadataType;
import net.opengeospatial.ows.RangeType;
import net.opengeospatial.ows.ValueType;
import net.opengeospatial.ows.ValuesReference;
import net.opengeospatial.wps.ComplexDataType;
import net.opengeospatial.wps.ComplexValueType;
import net.opengeospatial.wps.DataInputsType;
import net.opengeospatial.wps.DescribeProcess;
import net.opengeospatial.wps.Execute;
import net.opengeospatial.wps.ExecuteResponseType;
import net.opengeospatial.wps.IOValueType;
import net.opengeospatial.wps.InputDescriptionType;
import net.opengeospatial.wps.LiteralInputType;
import net.opengeospatial.wps.LiteralOutputType;
import net.opengeospatial.wps.LiteralValueType;
import net.opengeospatial.wps.ObjectFactory;
import net.opengeospatial.wps.OutputDefinitionType;
import net.opengeospatial.wps.OutputDefinitionsType;
import net.opengeospatial.wps.OutputDescriptionType;
import net.opengeospatial.wps.ProcessDescriptionType;
import net.opengeospatial.wps.ProcessDescriptions;
import net.opengeospatial.wps.ProcessFailedType;
import net.opengeospatial.wps.ProcessStartedType;
import net.opengeospatial.wps.StatusType;
import net.opengeospatial.wps.SupportedCRSsType;
import net.opengeospatial.wps.SupportedComplexDataType;
import net.opengeospatial.wps.SupportedUOMsType;
import net.opengeospatial.wps.IOValueType.ComplexValueReference;
import net.opengeospatial.wps.ProcessDescriptionType.DataInputs;
import net.opengeospatial.wps.ProcessDescriptionType.ProcessOutputs;

/**
 * Utility functions for complying to the OGC WPS standard.
 * 
 * @author Holger Albert
 */
public class OGCUtilities
{
  /**
   * Factory for WPS Objects.
   */
  public final static ObjectFactory WPS_OF = new ObjectFactory();

  /**
   * Factory for OWS Objects.
   */
  public final static net.opengeospatial.ows.ObjectFactory OWS_OF = new net.opengeospatial.ows.ObjectFactory();

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
    CodeType code = OWS_OF.createCodeType();

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
    ComplexValueReference complexValueReference = WPS_OF.createIOValueTypeComplexValueReference();

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
    ComplexValueType complexValue = WPS_OF.createComplexValueType();

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
    LiteralValueType literalValue = WPS_OF.createLiteralValueType();

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
    BoundingBoxType boundingBox = OWS_OF.createBoundingBoxType();

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
   *          <li>ComplexValueReference</li>
   *          <li>ComplexValueType</li>
   *          <li>LiteralValueType</li>
   *          <li>BoundingBoxType</li>
   *          </ol>
   */
  public static IOValueType buildIOValueType( CodeType identifier, String title, String abstrakt, Object valueFormChoice )
  {
    /* Create the instance via the factory. */
    IOValueType ioValue = WPS_OF.createIOValueType();

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
  public static DataInputsType buildDataInputsType( List<IOValueType> ioValues )
  {
    /* Create the instance via the factory. */
    DataInputsType dataInputs = WPS_OF.createDataInputsType();

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
    OutputDefinitionType outputDefinition = WPS_OF.createOutputDefinitionType();

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
  public static OutputDefinitionsType buildOutputDefinitionsType( List<OutputDefinitionType> outputDefinition )
  {
    /* Create the instance via the factory. */
    OutputDefinitionsType outputDefinitions = WPS_OF.createOutputDefinitionsType();

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
  public static Execute buildExecute( CodeType identifier, DataInputsType dataInputs, OutputDefinitionsType outputDefinitions, Boolean store, Boolean status )
  {
    /* Create the instance via the factory. */
    Execute execute = WPS_OF.createExecute();

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
  public static DescribeProcess buildDescribeProcess( List<CodeType> identifier )
  {
    /* Create the instance via the factory. */
    DescribeProcess describeProcess = WPS_OF.createDescribeProcess();

    /* Attributes. */
    describeProcess.setService( SERVICE );
    describeProcess.setVersion( VERSION );

    describeProcess.getIdentifier().addAll( identifier );

    return describeProcess;
  }

  /**
   * Note: The documentation is taken from the specific owsCommon.xsd for this type.<br>
   * <br>
   * This element either references or contains more metadata about the element that includes this element. To reference
   * metadata stored remotely, at least the xlinks:href attribute in xlink:simpleLink shall be included. Either at least
   * one of the attributes in xlink:simpleLink or a substitute for the AbstractMetaData element shall be included, but
   * not both. An Implementation Specification can restrict the contents of this element to always be a reference or
   * always contain metadata. (Informative: This element was adapted from the metaDataProperty element in GML 3.0.)
   * 
   * @param abstractMetaData
   *          Abstract element containing more metadata about the element that includes the containing "metadata"
   *          element. A specific server implementation, or an Implementation Specification, can define concrete
   *          elements in the AbstractMetaData substitution group.
   * @param simpleLink
   *          TODO Change this to all of the attributes for an simple link. Reference to metadata recorded elsewhere,
   *          either external to this XML document or within it. Whenever practical, the xlink:href attribute with type
   *          anyURI should include a URL from which this metadata can be electronically retrieved.
   * @param about
   *          [optional] Optional reference to the aspect of the element which includes this "metadata" element that
   *          this metadata provides more information about.
   */
  public static MetadataType buildMetaDataType( Object abstractMetaData, String about )
  {
    /* Create the instance via the factory. */
    MetadataType metadata = OWS_OF.createMetadataType();

    /* Elements. */
    metadata.setAbstractMetaData( abstractMetaData );

    /* Attributes. TODO This attributes has to be set. */
    metadata.setActuate( "" );
    metadata.setArcrole( "" );
    metadata.setHref( "" );
    metadata.setRole( "" );
    metadata.setShow( "" );
    metadata.setTitle( "" );
    metadata.setType( "" );
    metadata.setAbout( about );

    return metadata;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * A combination of format, encoding, and/or schema supported by a process input or output.
   * 
   * @param format
   *          Format supported for this input or output (e.g., text/XML). This element shall be included when the format
   *          for this ComplexDataType differs from the defaultFormat for this Input/Output. This element shall not be
   *          included if there is only one (i.e., the default) format supported for this Input/Output, or Format does
   *          not apply to this Input/Output.
   * @param encoding
   *          Reference to an encoding supported for this input or output (e.g., UTF-8). This element shall be included
   *          when the encoding for this ComplexDataType differs from the defaultEncoding for this Input/Output. This
   *          element shall not be included if there is only one (i.e., the default) encoding supported for this
   *          Input/Output, or Encoding does not apply to this Input/Output.
   * @param schema
   *          Reference to a definition of XML elements or types supported for this Input or Output (e.g., GML 2.1
   *          Application Schema). Each of these XML elements or types shall be defined in a separate XML Schema
   *          Document. This element shall be included when the schema for this ComplexDataType differs from the
   *          defaultSchema for this Input/Output. This element shall not be included if there is only one (i.e., the
   *          default) XML Schema Document supported for this Input/Output, or Schema does not apply to this
   *          Input/Output.
   */
  public static ComplexDataType buildComplexDataType( String format, String encoding, String schema )
  {
    /* Create the instance via the factory. */
    ComplexDataType complexData = WPS_OF.createComplexDataType();

    /* Elements. */
    complexData.setFormat( format );
    complexData.setEncoding( encoding );
    complexData.setSchema( schema );

    return complexData;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Formats, encodings, and schemas supported by a process input or output.
   * 
   * @param supportedComplexDatas
   *          Unordered list of combinations of format, encoding, and schema supported for this Input or Output (an
   *          example of one such combination is format=text/XML, encoding=UTF-8, schema=GML 2.1). This element should
   *          be included when this process supports more than one combination of format/encoding/schema for this
   *          Input/Output. This element shall be repeated for each combination of Format/Encoding/Schema that is
   *          supported for this Input/Output. This element shall not be included if there is only one (i.e., the
   *          default) Format/Encoding/Schema combination.
   * @param defaultFormat
   *          [optional] Identifier of the default Format supported for this input or output. The process shall expect
   *          input in or produce output in this Format unless the Execute request specifies another supported Format.
   *          This parameter shall be included when the default Format is other than text/XML. This parameter is
   *          optional if the Format is text/XML.
   * @param defaultEncoding
   *          [optional] Reference to the default encoding supported for this input or output. The process will expect
   *          input using or produce output using this encoding unless the Execute request specifies another supported
   *          encoding. This parameter shall be included when the default Encoding is other than the encoding of the XML
   *          response document (e.g. UTF-8). This parameter shall be omitted when there is no Encoding required for
   *          this input/output.
   * @param defaultSchema
   *          [optional] Reference to the definition of the default XML element or type supported for this input or
   *          output. This XML element or type shall be defined in a separate XML Schema Document. The process shall
   *          expect input in or produce output conformant with this XML element or type unless the Execute request
   *          specifies another supported XML element or type. This parameter shall be omitted when there is no XML
   *          Schema associated with this input/output (e.g., a GIF file). This parameter shall be included when this
   *          input/output is XML encoded using an XML schema. When included, the input/output shall validate against
   *          the referenced XML Schema. Note: If the input/output uses a profile of a larger schema, the server
   *          administrator should provide that schema profile for validation purposes.
   */
  public static SupportedComplexDataType buildSupportedComplexDataType( List<ComplexDataType> supportedComplexDatas, String defaultFormat, String defaultEncoding, String defaultSchema )
  {
    /* Create the instance via the factory. */
    SupportedComplexDataType supportedComplexData = WPS_OF.createSupportedComplexDataType();

    /* Elements. */
    supportedComplexData.getSupportedComplexData().addAll( supportedComplexDatas );

    /* Attributes. */
    if( defaultFormat != null )
      supportedComplexData.setDefaultFormat( defaultFormat );

    if( defaultEncoding != null )
      supportedComplexData.setDefaultEncoding( defaultEncoding );

    if( defaultSchema != null )
      supportedComplexData.setDefaultSchema( defaultSchema );

    return supportedComplexData;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd for this type.<br>
   * <br>
   * References metadata about a quantity, and provides a name for this metadata. (Informative: This element was
   * simplified from the metaDataProperty element in GML 3.0.)
   * 
   * @param value
   *          Human-readable name of the metadata described by associated referenced document.
   * @param reference
   *          [optional] Reference to data or metadata recorded elsewhere, either external to this XML document or
   *          within it. Whenever practical, this attribute should be a URL from which this metadata can be
   *          electronically retrieved. Alternately, this attribute can reference a URN for well-known metadata. For
   *          example, such a URN could be a URN defined in the "ogc" URN namespace.
   */
  public static DomainMetadataType buildDomainMetadata( String value, String reference )
  {
    /* Create the instance via the factory. */
    DomainMetadataType domainMetadata = OWS_OF.createDomainMetadataType();

    /* Attributes. */
    if( reference != null )
      domainMetadata.setReference( reference );

    /* Values. */
    domainMetadata.setValue( value );

    return domainMetadata;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * List of supported units of measure for a process input or output.
   * 
   * @param UOMs
   *          Unordered list of references to the Units of Measure supported for this input or output. This element
   *          shall not be included if there is only one (i.e., the default) UOM supported.
   * @param defaultUOM
   *          [optional] Reference to the default UOM supported for this input or output, if any. The process shall
   *          expect input in or produce output in this UOM unless the Execute request specifies another supported UOM.
   */
  public static SupportedUOMsType buildSupportedUOMsType( List<DomainMetadataType> UOMs, String defaultUOM )
  {
    /* Create the instance via the factory. */
    SupportedUOMsType supportedUOMs = WPS_OF.createSupportedUOMsType();

    /* Attributes. */
    if( defaultUOM != null )
      supportedUOMs.setDefaultUOM( defaultUOM );

    supportedUOMs.getUOM().addAll( UOMs );

    return supportedUOMs;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd for this type.<br>
   * <br>
   * 
   * @param values
   *          List of all the valid values and/or ranges of values for this quantity. For numeric quantities, signed
   *          values shall be ordered from negative infinity to positive infinity.<br>
   *          Possible are
   *          <ol>
   *          <li>ValueType</li>
   *          <li>RangeType</li>
   *          </ol>
   */
  public static AllowedValues buildAllowedValues( List<Object> values )
  {
    /* Create the instance via the factory. */
    AllowedValues allowedValues = OWS_OF.createAllowedValues();

    allowedValues.getValueOrRange().addAll( values );

    return allowedValues;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   */
  public static AnyValue buildAnyValue( )
  {
    /* Create the instance via the factory. */
    AnyValue anyValue = OWS_OF.createAnyValue();

    return anyValue;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Reference to externally specified list of all the valid values and/or ranges of values for this quantity.
   * (Informative: This element was simplified from the metaDataProperty element in GML 3.0.)
   * 
   * @param value
   *          Human-readable name of the list of values provided by the referenced document. Can be empty string when
   *          tis list has no name.
   * @param reference
   *          Reference to data or metadata recorded elsewhere, either external to this XML document or within it.
   *          Whenever practical, this attribute should be a URL from which this metadata can be electronically
   *          retrieved. Alternately, this attribute can reference a URN for well-known metadata. For example, such a
   *          URN could be a URN defined in the "ogc" URN namespace.
   */
  public static ValuesReference buildValuesReference( String value, String reference )
  {
    /* Create the instance via the factory. */
    ValuesReference valuesReference = OWS_OF.createValuesReference();

    /* Attributes. */
    valuesReference.setReference( reference );

    /* Values. */
    valuesReference.setValue( value );

    return valuesReference;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd for this type.<br>
   * <br>
   * A single value, encoded as a string. This type can be used for one value, for a spacing between allowed values, or
   * for the default value of a quantity.
   * 
   * @param val
   */
  public static ValueType buildValueType( String val )
  {
    /* Create the instance via the factory. */
    ValueType value = OWS_OF.createValueType();

    /* Values. */
    value.setValue( val );

    return value;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd for this type.<br>
   * <br>
   * A range of values of a numeric quantity. This range can be continuous or discrete, defined by a fixed spacing
   * between adjacent valid values. If the MinimumValue or MaximumValue is not included, there is no value limit in that
   * direction. Inclusion of the specified minimum and maximum values in the range shall be defined by the rangeClosure.
   * 
   * @param minimumValue
   *          Minimum value of this numeric quantity.
   * @param maximumValue
   *          Maximum value of this numeric quantity.
   * @param spacing
   *          The regular distance or spacing between the allowed values in a range.
   * @param rangeClosure
   *          [optional] Shall be included unless the default value applies.
   */
  public static RangeType buildRangeType( ValueType minimumValue, ValueType maximumValue, ValueType spacing, List<String> rangeClosure )
  {
    /* Create the instance via the factory. */
    RangeType range = OWS_OF.createRangeType();

    /* Elements. */
    range.setMinimumValue( minimumValue );
    range.setMaximumValue( maximumValue );
    range.setSpacing( spacing );

    /* Attributes. */
    range.getRangeClosure().addAll( rangeClosure );

    return range;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Description of a process input that consists of a simple literal value (e.g., "2.1"). (Informative: This type is a
   * subset of the ows:UnNamedDomainType defined in owsDomaintype.xsd.)
   * 
   * @param domainMetadata
   *          Data type of this set of values (e.g. integer, real, etc). This data type metadata should be included for
   *          each quantity whose data type is not a string.
   * @param supportedUOMs
   *          List of supported units of measure for this input or output. This element should be included when this
   *          literal has a unit of measure (e.g., "meters", without a more complete reference system). Not necessary
   *          for a count, which has no units.
   * @param literalValuesChoice
   *          Identifies the type of this literal input and provides supporting information.<br>
   *          Possible are
   *          <ol>
   *          <li>AllowedValues:<br>
   *          Indicates that there are a finite set of values and ranges allowed for this input, and contains list of
   *          all the valid values and/or ranges of values. Notice that these values and ranges can be displayed to a
   *          human client.</li>
   *          <li>AnyValue:<br>
   *          Indicates that any value is allowed for this input. This element shall be included when there are no
   *          restrictions, except for data type, on the allowable value of this input.</li>
   *          <li>valuesReference:<br>
   *          Indicates that there are a finite set of values and ranges allowed for this input, which are specified in
   *          the referenced list.</li>
   *          </ol>
   * @param defaultValue
   *          [optional] Optional default value for this quantity, which should be included when this quantity has a
   *          default value.
   */
  public static LiteralInputType buildLiteralInputType( DomainMetadataType domainMetadata, SupportedUOMsType supportedUOMs, Object literalValuesChoice, RangeType defaultValue )
  {
    /* Create the instance via the factory. */
    LiteralInputType literalInput = WPS_OF.createLiteralInputType();

    /* Elements. */
    literalInput.setDataType( domainMetadata );
    literalInput.setSupportedUOMs( supportedUOMs );

    if( literalValuesChoice instanceof AllowedValues )
      literalInput.setAllowedValues( (AllowedValues) literalValuesChoice );
    else if( literalValuesChoice instanceof AnyValue )
      literalInput.setAnyValue( (AnyValue) literalValuesChoice );
    else if( literalValuesChoice instanceof ValuesReference )
      literalInput.setValuesReference( (ValuesReference) literalValuesChoice );

    if( defaultValue != null )
      literalInput.setDefaultValue( defaultValue );

    return literalInput;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * List of supported Coordinate Reference Systems.
   * 
   * @param CRSs
   *          Unordered list of references to the coordinate reference systems supported. This element shall not be
   *          included if there is only one (i.e., the default) CRS supported.
   * @param defaultCRS
   *          Reference to the CRS that will be used unless the Execute operation request specifies another supported
   *          CRS.
   */
  public static SupportedCRSsType buildSupportedCRSsType( List<String> CRSs, String defaultCRS )
  {
    /* Create the instance via the factory. */
    SupportedCRSsType supportedCRSs = WPS_OF.createSupportedCRSsType();

    /* Elements. */
    supportedCRSs.getCRS().addAll( CRSs );
    supportedCRSs.setDefaultCRS( defaultCRS );

    return supportedCRSs;
  }

  /**
   * Note: The documentation is taken from the specific wpsCommons.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Description of an input to a process.
   * 
   * @param identifier
   *          Unambiguous identifier or name of a process, unique for this server, or unambiguous identifier or name of
   *          an input or output, unique for this process.
   * @param title
   *          Title of a process, input, or output, normally available for display to a human.
   * @param abstrakt
   *          Brief narrative description of a process, input, or output, normally available for display to a human.
   * @param inputFormChoice
   *          Identifies the form of this output and provides supporting information. Possible are
   *          <ol>
   *          <li>SupportedComplexDataType</li>
   *          <li>LiteralInputType</li>
   *          <li>SupportedCRSsType</li>
   *          </ol>
   * @param minimumOccurs
   *          The minimum number of times that values for this parameter are required. If MinimumOccurs is "0", this
   *          data input is optional. If MinimumOccurs is "1" or if this element is omitted, this process input is
   *          required.
   */
  public static InputDescriptionType buildInputDescriptionType( CodeType identifier, String title, String abstrakt, Object inputFormChoice, int minimumOccurs )
  {
    /* Create the instance via the factory. */
    InputDescriptionType inputDescription = WPS_OF.createInputDescriptionType();

    /* Elements. */
    inputDescription.setIdentifier( identifier );
    inputDescription.setTitle( title );
    inputDescription.setAbstract( abstrakt );

    if( inputFormChoice instanceof SupportedComplexDataType )
      inputDescription.setComplexData( (SupportedComplexDataType) inputFormChoice );
    else if( inputFormChoice instanceof LiteralInputType )
      inputDescription.setLiteralData( (LiteralInputType) inputFormChoice );
    else if( inputFormChoice instanceof SupportedCRSsType )
      inputDescription.setBoundingBoxData( (SupportedCRSsType) inputFormChoice );

    if( minimumOccurs == 0 || minimumOccurs == 1 )
      inputDescription.setMinimumOccurs( BigInteger.valueOf( minimumOccurs ) );

    return inputDescription;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * List of the inputs to this process. In almost all cases, at least one process input is required. However, no
   * process inputs may be identified when all the inputs are predetermined fixed resources. In this case, those
   * resources shall be identified in the ows:Abstract element that describes the process.
   * 
   * @param inputDescriptions
   *          List of the inputs to this process. In almost all cases, at least one process input is required. However,
   *          no process inputs may be identified when all the inputs are predetermined fixed resources. In this case,
   *          those resources shall be identified in the ows:Abstract element that describes the process.
   */
  public static DataInputs buildDataInputs( List<InputDescriptionType> inputDescriptions )
  {
    /* Create the instance via the factory. */
    DataInputs dataInputs = WPS_OF.createProcessDescriptionTypeDataInputs();

    dataInputs.getInput().addAll( inputDescriptions );

    return dataInputs;
  }

  /**
   * Note: The documentation is taken from the specific owsDomainType.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Description of a literal output (or input).
   * 
   * @param domainMetadata
   *          Data type of this set of values (e.g. integer, real, etc). This data type metadata should be included for
   *          each quantity whose data type is not a string.
   * @param supportedUOMs
   *          List of supported units of measure for this input or output. This element should be included when this
   *          literal has a unit of measure (e.g., "meters", without a more complete reference system). Not necessary
   *          for a count, which has no units.
   */
  public static LiteralOutputType buildLiteralOutputType( DomainMetadataType domainMetadata, SupportedUOMsType supportedUOMs )
  {
    /* Create the instance via the factory. */
    LiteralOutputType literalOutput = WPS_OF.createLiteralOutputType();

    /* Elements. */
    literalOutput.setDataType( domainMetadata );
    literalOutput.setSupportedUOMs( supportedUOMs );

    return literalOutput;
  }

  /**
   * Note: The documentation is taken from the specific wpsCommons.xsd, wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * Description of a process Output.
   * 
   * @param identifier
   *          Unambiguous identifier or name of a process, unique for this server, or unambiguous identifier or name of
   *          an input or output, unique for this process.
   * @param title
   *          Title of a process, input, or output, normally available for display to a human.
   * @param abstrakt
   *          Brief narrative description of a process, input, or output, normally available for display to a human.
   * @param outputFormChoice
   *          Identifies the form of this output and provides supporting information. Possible are
   *          <ol>
   *          <li>SupportedComplexDataType</li>
   *          <li>LiteralOutputType</li>
   *          <li>SupportedCRSsType</li>
   *          </ol>
   */
  public static OutputDescriptionType buildOutputDescriptionType( CodeType identifier, String title, String abstrakt, Object outputFormChoice )
  {
    /* Create the instance via the factory. */
    OutputDescriptionType outputDescription = WPS_OF.createOutputDescriptionType();

    /* Elements. */
    outputDescription.setIdentifier( identifier );
    outputDescription.setTitle( title );
    outputDescription.setAbstract( abstrakt );

    if( outputFormChoice instanceof SupportedComplexDataType )
      outputDescription.setComplexOutput( (SupportedComplexDataType) outputFormChoice );
    else if( outputFormChoice instanceof LiteralInputType )
      outputDescription.setLiteralOutput( (LiteralOutputType) outputFormChoice );
    else if( outputFormChoice instanceof SupportedCRSsType )
      outputDescription.setBoundingBoxOutput( (SupportedCRSsType) outputFormChoice );

    return outputDescription;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * List of outputs which will or can result from executing the process.
   * 
   * @param outputDescriptions
   *          Unordered list of one or more descriptions of all the outputs that can result from executing this process.
   *          At least one output is required from each process.
   */
  public static ProcessOutputs buildProcessOutputs( List<OutputDescriptionType> outputDescriptions )
  {
    /* Create the instance via the factory. */
    ProcessOutputs processOutputs = WPS_OF.createProcessDescriptionTypeProcessOutputs();

    processOutputs.getOutput().addAll( outputDescriptions );

    return processOutputs;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd, wpsCommon.xsd for this type.<br>
   * <br>
   * Full description of a process.
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
   * @param metadata
   *          [optional] Optional unordered list of additional metadata about this process. A list of optional and/or
   *          required metadata elements for this process could be specified in a specific Application Profile for this
   *          service.
   * @param processVersion
   *          [optional] Release version of this Process, included when a process version needs to be included for
   *          clarification about the process to be used. It is possible that a WPS supports a process with different
   *          versions due to reasons such as modifications of process algorithms. Notice that this is the version
   *          identifier for the process, not the version of the WPS interface.
   * @param dataInputs
   *          List of the inputs to this process. In almost all cases, at least one process input is required. However,
   *          no process inputs may be identified when all the inputs are predetermined fixed resources. In this case,
   *          those resources shall be identified in the ows:Abstract element that describes the process.
   * @param processOutputs
   *          List of outputs which will or can result from executing the process.
   * @param storeSupported
   *          [optional] Indicates if the ComplexData outputs from this process can be stored by the WPS server as
   *          web-accessible resources. If "storeSupported" is "true", the Execute operation request may include "store"
   *          equals "true", directing that all ComplexData outputs of the process be stored so that the client can
   *          retrieve them as required. By default for this process, storage is not supported and all outputs are
   *          returned encoded in the Execute response.
   * @param statusSupported
   *          [optional] Indicates if the Execute operation response can be returned quickly with status information, or
   *          will not be returned until process execution is complete. If "statusSupported" is "true", the Execute
   *          operation request may include "status" equals "true", directing that the Execute operation response be
   *          returned quickly with status information. By default, status information is not provided for this process,
   *          and the Execute operation response is not returned until process execution is complete.
   */
  public static ProcessDescriptionType buildProcessDescriptionType( CodeType identifier, String title, String abstrakt, List<MetadataType> metadata, String processVersion, DataInputs dataInputs, ProcessOutputs processOutputs, Boolean storeSupported, Boolean statusSupported )
  {
    /* Create the instance via the factory. */
    ProcessDescriptionType processDescription = WPS_OF.createProcessDescriptionType();

    /* Elements. */
    processDescription.setIdentifier( identifier );
    processDescription.setTitle( title );
    processDescription.setAbstract( abstrakt );

    if( metadata != null )
      processDescription.getMetadata().addAll( metadata );

    processDescription.setDataInputs( dataInputs );
    processDescription.setProcessOutputs( processOutputs );

    /* Attributes. */
    if( processVersion != null )
      processDescription.setProcessVersion( processVersion );

    if( storeSupported != null )
      processDescription.setStoreSupported( storeSupported );

    if( statusSupported != null )
      processDescription.setStatusSupported( statusSupported );

    return processDescription;
  }

  /**
   * Note: The documentation is taken from the specific wpsDescribeProcess.xsd for this type.<br>
   * <br>
   * WPS DescribeProcess operation response.
   * 
   * @param processDescription
   *          Ordered list of one or more full Process descriptions, listed in the order in which they were requested in
   *          the DescribeProcess operation request.
   */
  public static ProcessDescriptions buildProcessDescriptions( List<ProcessDescriptionType> processDescription )
  {
    /* Create the instance via the factory. */
    ProcessDescriptions processDescriptions = WPS_OF.createProcessDescriptions();

    processDescriptions.getProcessDescription().addAll( processDescription );

    return processDescriptions;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * Indicates that this process has been has been accepted by the server, and processing has begun.
   * 
   * @param value
   *          A human-readable text string whose contents are left open to definition by each WPS server, but is
   *          expected to include any messages the server may wish to let the clients know. Such information could
   *          include how much longer the process may take to execute, or any warning conditions that may have been
   *          encountered to date. The client may display this text to a human user.
   * @param percentCompleted
   *          [optional] Percentage of the process that has been completed, where 0 means the process has just started,
   *          and 100 means the process is complete. This attribute should be included if the process is expected to
   *          execute for a long time (i.e. more than a few minutes). This percentage is expected to be accurate to
   *          within ten percent.
   */
  public static ProcessStartedType buildProcessStartedType( String value, int percentCompleted )
  {
    /* Create the instance via the factory. */
    ProcessStartedType processStarted = WPS_OF.createProcessStartedType();

    /* Values. */
    processStarted.setValue( value );

    /* Attributes. */
    if( percentCompleted >= 0 && percentCompleted <= 100 )
      processStarted.setPercentCompleted( BigInteger.valueOf( percentCompleted ) );

    return processStarted;
  }

  /**
   * Note: The documentation is taken from the specific owsExceptionReport.xsd for this type.<br>
   * <br>
   * An Exception element describes one detected error that a server chooses to convey to the client.
   * 
   * @param exceptionText
   *          Ordered sequence of text strings that describe this specific exception or error. The contents of these
   *          strings are left open to definition by each server implementation. A server is strongly encouraged to
   *          include at least one ExceptionText value, to provide more information about the detected error than
   *          provided by the exceptionCode. When included, multiple ExceptionText values shall provide hierarchical
   *          information about one detected error, with the most significant information listed first.
   * @param exceptionCode
   *          A code representing the type of this exception, which shall be selected from a set of exceptionCode values
   *          specified for the specific service operation and server.
   * @param locator
   *          [optional] When included, this locator shall indicate to the client where an exception was encountered in
   *          servicing the client's operation request. This locator should be included whenever meaningful information
   *          can be provided by the server. The contents of this locator will depend on the specific exceptionCode and
   *          OWS service, and shall be specified in the OWS Implementation Specification.
   */
  public static ExceptionType buildExceptionType( List<String> exceptionText, String exceptionCode, String locator )
  {
    /* Create the instance via the factory. */
    ExceptionType exception = OWS_OF.createExceptionType();

    exception.getExceptionText().addAll( exceptionText );

    /* Attributes. */
    exception.setExceptionCode( exceptionCode );

    if( locator != null )
      exception.setLocator( locator );

    return exception;
  }

  /**
   * Note: The documentation is taken from the specific owsExceptionReport.xsd for this type.<br>
   * <br>
   * Report message returned to the client that requested any OWS operation when the server detects an error while
   * processing that operation request.
   * 
   * @param exceptions
   *          Unordered list of one or more Exception elements that each describes an error. These Exception elements
   *          shall be interpreted by clients as being independent of one another (not hierarchical).
   * @param version
   *          Specification version for OWS operation. The string value shall contain one x.y.z "version" value (e.g.,
   *          "2.1.3"). A version number shall contain three non-negative integers separated by decimal points, in the
   *          form "x.y.z". The integers y and z shall not exceed 99. Each version shall be for the Implementation
   *          Specification (document) and the associated XML Schemas to which requested operations will conform. An
   *          Implementation Specification version normally specifies XML Schemas against which an XML encoded operation
   *          response must conform and should be validated. See Version negotiation subclause for more information.
   * @param language
   *          [optional] Identifier of the language used by all included exception text values. These language
   *          identifiers shall be as specified in IETF RFC 1766. When this attribute is omitted, the language used is
   *          not identified.
   */
  public static ExceptionReport buildExceptionReport( List<ExceptionType> exceptions, String version, String language )
  {
    /* Create the instance via the factory. */
    ExceptionReport exceptionReport = OWS_OF.createExceptionReport();

    exceptionReport.getException().addAll( exceptions );

    /* Attributes. */
    exceptionReport.setVersion( version );

    if( language != null )
      exceptionReport.setLanguage( language );

    return exceptionReport;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd, owsExceptionReport.xsd for this type.<br>
   * <br>
   * Indicator that the process has failed to execute successfully. The reason for failure is given in the exception
   * report.
   * 
   * @param exceptionReport
   *          Report message returned to the client that requested any OWS operation when the server detects an error
   *          while processing that operation request.
   */
  public static ProcessFailedType buildProcessFailedType( ExceptionReport exceptionReport )
  {
    /* Create the instance via the factory. */
    ProcessFailedType processFailed = WPS_OF.createProcessFailedType();

    /* Elements. */
    processFailed.setExceptionReport( exceptionReport );

    return processFailed;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * Description of the status of process execution.
   * 
   * @param value
   *          <br>
   *          Possible are
   *          <ol>
   *          <li>ProcessAccepted (as String)<br>
   *          Indicates that this process has been accepted by the server, but is in a queue and has not yet started to
   *          execute. The contents of this human-readable text string is left open to definition by each server
   *          implementation, but is expected to include any messages the server may wish to let the clients know. Such
   *          information could include how long the queue is, or any warning conditions that may have been encountered.
   *          The client may display this text to a human user.</li>
   *          <li>ProcessStartedType<br>
   *          Indicates that this process has been has been accepted by the server, and processing has begun.</li>
   *          <li>ProcessSucceeded (as String)<br>
   *          Indicates that this process has successfully completed execution. The contents of this human-readable text
   *          string is left open to definition by each server, but is expected to include any messages the server may
   *          wish to let the clients know, such as how long the process took to execute, or any warning conditions that
   *          may have been encountered. The client may display this text string to a human user. The client should make
   *          use of the presence of this element to trigger automated or manual access to the results of the process.
   *          If manual access is intended, the client should use the presence of this element to present the results as
   *          downloadable links to the user. </li>
   *          <li>ProcessFailedType<br>
   *          Indicates that execution of this process has failed, and includes error information.</li>
   *          </ol>
   * @param accepted
   *          ATTENTION: This flag is not part of the wpsExecute.xsd. I introduced it to this function, because it is
   *          not possible to decide which value should be set in case of ProcessAccepted and ProcessSucceeded, because
   *          they are both strings.<br>
   *          If you want to set a ProcessAccepted, set this flag to true.<br>
   *          If you want to set a ProcessSucceeded, set this flag to false.<br>
   *          In both other cases, this flag will not be evaluated.
   */
  public static StatusType buildStatusType( Object value, boolean accepted )
  {
    /* Create the instance via the factory. */
    StatusType status = WPS_OF.createStatusType();

    if( value instanceof String )
    {
      if( accepted )
        status.setProcessAccepted( (String) value );
      else
        status.setProcessSucceeded( (String) value );
    }
    else if( value instanceof ProcessStartedType )
      status.setProcessStarted( (ProcessStartedType) value );
    else if( value instanceof ProcessFailedType )
      status.setProcessFailed( (ProcessFailedType) value );

    return status;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * Response to an Execute operation request.
   * 
   * @param identifier
   *          Identifier of the Process requested to be executed. This Process identifier shall be as listed in the
   *          ProcessOfferings section of the WPS Capabilities document.
   * @param status
   *          Execution status of this process.
   * @param dataInputs
   *          [optional] Inputs that were provided as part of the execute request. This element can be omitted as an
   *          implementation decision by the WPS server. However, it is often advisable to have the response include
   *          this information, so the client can confirm that the request was received correctly, and to provide a
   *          source of metadata if the client wishes to store the result for future reference.
   * @param outputDefinitions
   *          [optional] Complete list of Output data types that were requested as part of the Execute request. This
   *          element can be omitted as an implementation decision by the WPS server. However, it is often advisable to
   *          have the response include this information, so the client can confirm that the request was received
   *          correctly, and to provide a source of metadata if the client wishes to store the result for future
   *          reference.
   * @param statusLocation
   *          [optional] The URL referencing the location from which the ExecuteResponse can be retrieved. If "status"
   *          is "true" in the Execute request, the ExecuteResponse should also be found here as soon as the process
   *          returns the initial response to the client. It should persist at this location as long as the outputs are
   *          accessible from the server. The outputs may be stored for as long as the implementer of the server
   *          decides. If the process takes a long time, this URL can be repopulated on an ongoing basis in order to
   *          keep the client updated on progress. Before the process has succeeded, the ExecuteResponse contains
   *          information about the status of the process, including whether or not processing has started, and the
   *          percentage completed. It may also optionally contain the inputs and any ProcessStartedType interim
   *          results. When the process has succeeded, the ExecuteResponse found at this URL shall contain the output
   *          values or references to them.
   * @param version
   *          Version of the WPS interface specification implemented by the server.
   */
  public static ExecuteResponseType buildExecuteResponseType( CodeType identifier, StatusType status, DataInputsType dataInputs, OutputDefinitionsType outputDefinitions, String statusLocation, String version )
  {
    /* Create the instance via the factory. */
    ExecuteResponseType executeResponse = WPS_OF.createExecuteResponseType();

    /* Elements. */
    executeResponse.setIdentifier( identifier );
    executeResponse.setStatus( status );

    if( dataInputs != null )
      executeResponse.setDataInputs( dataInputs );

    if( outputDefinitions != null )
      executeResponse.setOutputDefinitions( outputDefinitions );

    /* Attributes. */
    if( statusLocation != null )
      executeResponse.setStatusLocation( statusLocation );

    executeResponse.setVersion( version );

    return executeResponse;
  }

  /**
   * Note: The documentation is taken from the specific wpsExecute.xsd for this type.<br>
   * <br>
   * WPS Execute operation response. By default, this XML document is delivered to the client in response to an Execute
   * request. If "status" is "false" in the Execute operation request, this document is normally returned when process
   * execution has been completed. If "status" in the Execute request is "true", this response shall be returned as soon
   * as the Execute request has been accepted for processing. In this case, the same XML document is also made available
   * as a web-accessible resource from the URL identified in the statusLocation, and the WPS server shall repopulate it
   * once the process has completed. It may repopulate it on an ongoing basis while the process is executing. However,
   * the response to an Execute request will not include this element in the special case where the output is a single
   * complex value result and the Execute request indicates that "store" is "false". Instead, the server shall return
   * the complex result (e.g., GIF image or GML) directly, without encoding it in the ExecuteResponse. If processing
   * fails in this special case, the normal ExecuteResponse shall be sent, with the error condition indicated. This
   * option is provided to simplify the programming required for simple clients and for service chaining.
   * 
   * @param value
   *          Response to an Execute operation request.
   */
  public static JAXBElement<ExecuteResponseType> buildExecuteResponse( ExecuteResponseType value )
  {
    /* Create the instance via the factory. */
    JAXBElement<ExecuteResponseType> executeResponse = WPS_OF.createExecuteResponse( value );

    return executeResponse;
  }
}