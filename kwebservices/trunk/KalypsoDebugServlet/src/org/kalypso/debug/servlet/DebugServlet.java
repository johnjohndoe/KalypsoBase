package org.kalypso.debug.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.framework.internal.core.FrameworkProperties;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("serial")
public class DebugServlet extends HttpServlet
{
  public final static String PARAM_INFO_LOGS = "logs";
  public final static String PARAM_INFO_PLATFORM = "platform";
  public final static String PARAM_INFO_SYSPROPS = "sysProps";
  public final static String PARAM_INFO_FWPROPS = "fwProps";
  public final static String PARAM_INFO_TEST = "test";

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doGet( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    final String type = request.getParameter( "info" );
    if( PARAM_INFO_SYSPROPS.equals( type ) )
      doSystemProperties( request, response );
    else if( PARAM_INFO_FWPROPS.equals( type ) )
      doFrameworkProperties( request, response );
    else if( PARAM_INFO_PLATFORM.equals( type ) )
      doPlatformProperties( request, response );
    else if( PARAM_INFO_LOGS.equals( type ) )
      doLogs( request, response );
    else if( PARAM_INFO_TEST.equals( type ) )
      doTest( request, response );
    else
      doMainPage( request, response );
  }

  private void doLogs( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    final PrintWriter pw = doDefaultHeader( request, response );

    pw.println( "<h2>Platform Log</h2>" );

    pw.println( "<pre>" );
    Activator.getDefault().appendLogContent( pw );
    pw.println( "</pre>" );

    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doPlatformProperties( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    final PrintWriter pw = doDefaultHeader( request, response );

    printPlatformTable( pw );

    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doSystemProperties( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    final PrintWriter pw = doDefaultHeader( request, response );

    printSystemPropertiesTable( pw );

    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doFrameworkProperties( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    final PrintWriter pw = doDefaultHeader( request, response );
    
    printFrameworkPropertiesTable( pw );
    
    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doTest( HttpServletRequest request, HttpServletResponse response ) throws IOException
  {
    
    Display d;
    d=Display.getDefault();
    
    final PrintWriter pw = doDefaultHeader( request, response );
    pw.println( "<h2>Test Stuff</h2>" );
    pw.println("RequestURI: "+request.getRequestURI()+"<br>");
    pw.println("ServerName: "+request.getServerName()+"<br>");
    pw.println("ServerPort: "+request.getServerPort()+"<br>");
    pw.println("ServletPath: "+request.getServletPath()+"<br>");
    pw.println("Path Translated: "+request.getPathTranslated()+"<br>");
    String url="";
    url=request.getRequestURL().toString();
    pw.println("Request URL: "+url+"<br>");
    
    

    
    //Properties properties = System.getProperties();
    Properties properties = FrameworkProperties.getProperties();
    Enumeration<Object> propKeys = properties.keys();
    while (propKeys.hasMoreElements())
    {
      String key=(String) propKeys.nextElement();
      pw.println(key + ": "+System.getProperty( key )+"<br>");
    }
    
    
    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doMainPage( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    final PrintWriter pw = doDefaultHeader( request, response );

    doDefaultFooter( request.getContextPath(), pw );
  }

  private void doDefaultFooter( final String context, final PrintWriter pw )
  {
    // link back to main page
    printAnchor( context + "/sp_debug", "Main page", pw );

    pw.println( "</body>" );
    pw.println( "</html>" );

    pw.flush();
  }

  private void printAnchor( final String href, final String text, final PrintWriter pw )
  {
    pw.print( "<a href=\"" );
    pw.print( href );
    pw.print( "\">" );
    pw.print( text );
    pw.println( "</a>" );
  }

  private PrintWriter doDefaultHeader( final HttpServletRequest request, final HttpServletResponse response ) throws IOException
  {
    response.setContentType( "text/html" );

    final PrintWriter pw = response.getWriter();

    pw.println( "<html>" );
    pw.println( "<body>" );
    
    final String contextPath = request.getContextPath();
    final String baseUrl = contextPath + "/sp_debug?info=";
    
    pw.println( "<h1>Kalypso Debug Servlet</h1>" );
    printAnchor( baseUrl + PARAM_INFO_LOGS, "Platform Log", pw );
    pw.println( "<br/>" );
    printAnchor( baseUrl + PARAM_INFO_SYSPROPS, "System Properties", pw );
    pw.println( "<br/>" );
    printAnchor( baseUrl + PARAM_INFO_FWPROPS, "Framework Properties", pw );
    pw.println( "<br/>" );
    printAnchor( baseUrl + PARAM_INFO_PLATFORM, "Platform Properties", pw );
    pw.println( "<br/>" );
    printAnchor( baseUrl + PARAM_INFO_TEST, "Test Stuff", pw );
    pw.println( "<br/>" );
    
    return pw;
  }

  private void printPlatformTable( final PrintWriter pw )
  {
    pw.println( "<h2>Platform Eigenschaften</h2>" );

    pw.println( "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" >" );

    pw.println( "<colgroup width=\"90%\" span=\"3\"></colgroup>" );

    printTableRow( pw, true, "Variable", "Inhalt" );

    printTableRow( pw, "Logfile location", Platform.getLogFileLocation().toOSString() );
    printTableRow( pw, "Activator state location", Activator.getDefault().getStateLocation().toOSString() );

    pw.println( "" );
    pw.println( "</table>" );
  }

  private void printSystemPropertiesTable( final PrintWriter pw )
  {
    pw.println( "<h2>System Properties</h2>" );

    pw.println( "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">" );

    pw.println( "<colgroup width=\"90%\" span=\"3\"></colgroup>" );

    printTableRow( pw, true, "Property", "Value" );

    final Properties sysProps = System.getProperties();

    final Set<Entry<Object, Object>> entrySet = sysProps.entrySet();
    final TreeSet<Entry<Object, Object>> sortedEntries = new TreeSet<Entry<Object, Object>>( new Comparator<Entry<Object, Object>>()
    {
      public int compare( final Entry<Object, Object> o1, final Entry<Object, Object> o2 )
      {
        final Object key1 = o1.getKey();
        final Object key2 = o2.getKey();

        final String k1 = key1 == null ? null : key1.toString();
        final String k2 = key2 == null ? null : key2.toString();

        if( k1 == null )
          return -1;

        return k1.compareTo( k2 );
      }
    } );
    sortedEntries.addAll( entrySet );

    for( final Entry<Object, Object> entry : sortedEntries )
      printTableRow( pw, entry.getKey().toString(), entry.getValue().toString() );

    pw.println( "" );
    pw.println( "</table>" );
  }

  
  private void printFrameworkPropertiesTable( final PrintWriter pw )
  {
    pw.println( "<h2>Framework Properties</h2>" );
    
    pw.println( "<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\">" );
    
    pw.println( "<colgroup width=\"90%\" span=\"3\"></colgroup>" );
    
    printTableRow( pw, true, "Property", "Value" );
    
    final Properties fProps = FrameworkProperties.getProperties();
    
    final Set<Entry<Object, Object>> entrySet = fProps.entrySet();
    final TreeSet<Entry<Object, Object>> sortedEntries = new TreeSet<Entry<Object, Object>>( new Comparator<Entry<Object, Object>>()
        {
      public int compare( final Entry<Object, Object> o1, final Entry<Object, Object> o2 )
      {
        final Object key1 = o1.getKey();
        final Object key2 = o2.getKey();
        
        final String k1 = key1 == null ? null : key1.toString();
        final String k2 = key2 == null ? null : key2.toString();
        
        if( k1 == null )
          return -1;
        
        return k1.compareTo( k2 );
      }
        } );
    sortedEntries.addAll( entrySet );
    
    for( final Entry<Object, Object> entry : sortedEntries )
      printTableRow( pw, entry.getKey().toString(), entry.getValue().toString() );
    
    pw.println( "" );
    pw.println( "</table>" );
  }

  private void printTableRow( final PrintWriter pw, final String... content )
  {
    printTableRow( pw, false, content );
  }

  private void printTableRow( final PrintWriter pw, final boolean header, final String... content )
  {
    pw.println( "<tr>" );
    for( final String string : content )
      printTableData( pw, header, string );
    pw.println( "</tr>" );
  }

  private void printTableData( final PrintWriter pw, final boolean header, final String content )
  {
    if( header )
      pw.print( "<th>" );
    else
      pw.print( "<td>" );
    pw.print( content );
    if( header )
      pw.println( "</th>" );
    else
      pw.println( "</td>" );
  }
}
