/*--------------- Kalypso-Header --------------------------------------------------------------------

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

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.diagview.grafik;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.io.ProcessWraper;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.java.io.filter.PrefixSuffixFilter;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.util.DoubleComparator;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.ObservationUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.diagview.DiagView;
import org.kalypso.ogc.sensor.diagview.DiagViewUtils;
import org.kalypso.ogc.sensor.metadata.ITimeseriesConstants;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.ogc.sensor.status.KalypsoStatusUtils;
import org.kalypso.ogc.sensor.template.ObsView;
import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.template.obsdiagview.Obsdiagview;
import org.kalypso.template.obsdiagview.TypeCurve;
import org.kalypso.template.obsdiagview.TypeObservation;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * GrafikLauncher
 *
 * @author schlienger
 */
public class GrafikLauncher
{
  public final static String GRAFIK_ENCODING = "Cp1252"; //$NON-NLS-1$

  /** file extension of the grafik template files */
  public final static String TPL_FILE_EXTENSION = "tpl"; //$NON-NLS-1$

  /** date format understood by the grafik tool */
  protected final static DateFormat GRAFIK_DF = new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss" ); //$NON-NLS-1$

  private final static NumberFormat GRAFIK_NF_W = NumberFormat.getIntegerInstance();

  static
  {
    GRAFIK_NF_W.setGroupingUsed( false ); // set to false, else we got '.' in 1000ers, causing the grafik exe to read it
// as 1.0
    GRAFIK_NF_W.setMaximumFractionDigits( 0 );
  }

  private GrafikLauncher( )
  {
    // no instanciation
  }

  /**
   * Opens the grafik tool using an observation template file. Note: this method should be called using a
   * WorkspaceModifyOperation.
   *
   * @return the created tpl file
   * @throws SensorException
   */
  public static IStatus startGrafikODT( final IFile odtFile, final IFolder dest, final IProgressMonitor monitor ) throws SensorException, CoreException
  {
    final Obsdiagview odt;
    try
    {
      odt = DiagViewUtils.loadDiagramTemplateXML( odtFile.getContents() );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new SensorException( e );
    }

    return startGrafikODT( odtFile.getName(), odt, dest, monitor );
  }

  /**
   * Open the grafik tool using a zml file.
   */
  public static IStatus startGrafikZML( final IFile zmlFile, final IFolder dest, final IProgressMonitor monitor ) throws SensorException
  {
    final DiagView diag = new DiagView( zmlFile.getName(), Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.3" ), true ); //$NON-NLS-1$

    try
    {
      final URL context = ResourceUtilities.createURL( zmlFile );

      final IStatus status = diag.loadObservation( context, context.toExternalForm(), false, ObservationTokenHelper.DEFAULT_ITEM_NAME, ObsView.DEFAULT_ITEM_DATA, true );

      if( !status.isOK() )
        return status;

      final Obsdiagview odt = DiagViewUtils.buildDiagramTemplateXML( diag, null );
      return startGrafikODT( zmlFile.getName(), odt, dest, monitor );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new SensorException( e );
    }
    finally
    {
      diag.dispose();
    }
  }

  /**
   * Opens the grafik tool using an observation template xml object. Note: this method should be called using a
   * WorkspaceModifyOperation.
   *
   * @param fileName
   *          the filename to use for the grafik template file
   * @param odt
   *          the xml binding object
   * @throws SensorException
   */
  public static IStatus startGrafikODT( final String fileName, final Obsdiagview odt, final IFolder dest, final IProgressMonitor monitor ) throws SensorException, CoreException
  {
    final String taskName = "Starting grafik.exe";

    monitor.beginTask( taskName, 100 );
    monitor.setTaskName( taskName );

    monitor.subTask( "Cancel to return to Kalypso. If cancelled, changes in grafik.exe will not be copied to Kalypso." );

    try
    {
      if( !dest.exists() )
        dest.create( true, true, new SubProgressMonitor( monitor, 5 ) );

      final IFile tplFile = dest.getFile( FileUtilities.nameWithoutExtension( fileName ) + ".tpl" ); //$NON-NLS-1$

      final StringWriter strWriter = new StringWriter();
      final RememberForSync[] syncs = odt2tpl( odt, dest, strWriter, new SubProgressMonitor( monitor, 5 ) );
      strWriter.close();

      // use the windows encoding for the Vorlage because of the grafik tool
      // which uses it when reading...
      final SetContentHelper sch = new SetContentHelper( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.5" ) ) //$NON-NLS-1$
      {
        @Override
        protected void write( final OutputStreamWriter writer ) throws Throwable
        {
          writer.write( strWriter.toString() );
        }
      };

      sch.setFileContents( tplFile, false, false, new SubProgressMonitor( monitor, 5 ), GRAFIK_ENCODING );

      startGrafikTPL( tplFile, syncs, new SubProgressMonitor( monitor, 85, SubMonitor.SUPPRESS_NONE ) );

      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Throwable e ) // generic exception caught
    {
      throw new SensorException( e );
    }
  }

  /**
   * Starts the grafik.exe with an eclipse IFile tpl-File.
   *
   * @param tplFile
   *          the Grafik-Vorlage
   * @throws SensorException
   */
  public static IStatus startGrafikTPL( final IFile tplFile, final RememberForSync[] syncs, final IProgressMonitor monitor ) throws SensorException
  {
    final File file = ResourceUtilities.makeFileFromPath( tplFile.getFullPath() );

    return startGrafikTPL( file, syncs, monitor );
  }

  /**
   * Starts the grafik with a java.lang.File tpl-File.
   */
  private static IStatus startGrafikTPL( final File tplFile, final RememberForSync[] syncs, final IProgressMonitor monitor ) throws SensorException
  {
    try
    {
      final File grafikExe = getGrafikProgramPath();

      final String cmdLine = '"' + grafikExe.getAbsolutePath() + "\" /V\"" + tplFile.getAbsolutePath() + '"';

      final Process proc = Runtime.getRuntime().exec( cmdLine, null, grafikExe.getParentFile() ); //$NON-NLS-1$

      final IStatusCollector stati = new StatusCollector( KalypsoGisPlugin.getId() );

      final ProcessWraper wraper = new ProcessWraper( proc, null )
      {
        @Override
        public void processCanceled( )
        {
          // empty
        }

        @Override
        public void processTerminated( final int returnCode )
        {
          synchroniseZml();
        }

        private void synchroniseZml( )
        {
          for( final RememberForSync rfs : syncs )
          {
            try
            {
              rfs.synchronizeZml();
            }
            catch( final Exception e )
            {
              e.printStackTrace();

              final String msg = Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.8" ) + rfs.getDatFile().getName() + Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.9" ) + rfs.getZmlFile().getName(); //$NON-NLS-1$ //$NON-NLS-2$
              stati.add( IStatus.ERROR, msg, e );
            }
          }
        }
      };

      // wait for grafik to finish and eventually synchronize data
      wraper.waitForProcess( false, monitor );

      return stati.asMultiStatusOrOK( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.7" ) ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      throw new SensorException( e );
    }
  }

  /**
   * Return the file representing the Grafik Program. Since the Grafik.exe is located in the resources of this plugin,
   * it must first be extracted into a temp file in the local file system.
   */
  public static File getGrafikProgramPath( ) throws IOException
  {
    // create the grafik exe
    final File grafikExe = makeFileFromURL( "grafik", ".exe", GrafikLauncher.class.getResource( "/org/kalypso/ui/resources/exe/grafik.exe_" ), true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    grafikExe.deleteOnExit();

    // also create the help file if not already existing
    final File grafikHelp = new File( grafikExe.getParentFile(), FileUtilities.nameWithoutExtension( grafikExe.getName() ) + ".hlp" ); //$NON-NLS-1$
    grafikHelp.deleteOnExit();
    if( !grafikHelp.exists() )
    {
      final File tmp = makeFileFromURL( "grafik", ".hlp", GrafikLauncher.class.getResource( "/org/kalypso/ui/resources/exe/grafik.hlp" ), true ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      // the help must have the same name as the exe (except file-extension)
      FileUtils.copyFile( tmp, grafikHelp );
      tmp.delete();
    }

    return grafikExe;
  }

  /**
   * Creates a new temporary file given its pathName and an InputStream. The content from the InputStream is written
   * into the file. The file will be deleted after the VM shuts down
   *
   * @param charMode
   * @param prefix
   *          prefix of file name
   * @param suffix
   *          suffix of file name
   * @param ins
   *          the input stream, that is the source
   * @param useCache
   *          if true tries to use an existing file with these prefix/suffix
   * @return the newly created file or null if an exception was thrown.
   * @throws IOException
   *           problems reading from stream or writing to temp. file
   */
  private static File makeFileFromURL( final String prefix, final String suffix, final URL input, final boolean useCache ) throws IOException
  {
    if( useCache )
    {
      try
      {
        return fileExistsInDir( prefix, suffix, System.getProperty( FileUtilities.JAVA_IO_TMPDIR ) );
      }
      catch( final FileNotFoundException ignored )
      {
        // ignored
        // ignored.printStackTrace();
      }
    }

    final File tmp = File.createTempFile( prefix, suffix );
    tmp.deleteOnExit();

    FileUtils.copyURLToFile( input, tmp );

    return tmp;
  }

  /**
   * Looks in the given path if a file with the given prefix and suffix exists. Returns the file in the positive. If
   * more than one such file is found, returns the first of them.
   *
   * @param prefix
   *          name of the file should begin with this prefix
   * @param suffix
   *          name of the file should end with this suffix
   * @param path
   * @return the (first) File found
   * @throws FileNotFoundException
   *           when file was not found or path does not denote a directory
   * @see PrefixSuffixFilter
   */
  private static File fileExistsInDir( final String prefix, final String suffix, final String path ) throws FileNotFoundException
  {
    final File dir = new File( path );

    if( dir.isDirectory() )
    {
      final PrefixSuffixFilter filter = new PrefixSuffixFilter( prefix, suffix );

      final File[] files = dir.listFiles( filter );

      if( files.length > 0 )
        return files[0];
    }

    throw new FileNotFoundException( Messages.getString( "org.kalypso.commons.java.io.FileUtilities.0", prefix, suffix, path ) ); //$NON-NLS-1$
  }

  /**
   * Converts a diagram template file to a grafik tpl.
   * <p>
   * Important note: the XML-Schema for the diag template file says that if no curve element or specified for a given
   * observation, then all curves of that observation should be displayed. This is not possible here using the grafik
   * tool. As a conclusion: when a template file is meant to be used with the grafik tool, then curves need to be
   * explicitely specified in the xml.
   */
  private static RememberForSync[] odt2tpl( final Obsdiagview odt, final IFolder dest, final Writer writer, final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final List<RememberForSync> sync = new ArrayList<>();

    final UrlResolver urlRes = new UrlResolver();
    final URL context = ResourceUtilities.createURL( dest.getParent() );

    final GrafikAchsen gAchsen = new GrafikAchsen( odt.getAxis() );
    final GrafikKurven gKurven = new GrafikKurven( gAchsen );

    Date xLower = null;
    Date xUpper = null;
    Number yLower = new Double( Double.MAX_VALUE );
    Number yUpper = new Double( -Double.MAX_VALUE );
    final Set<XLine> xLines = new TreeSet<>();
    final Map<Double, ValueAndColor> yLines = new HashMap<>();

    // set the timezone of the dateformat
    if( odt.getTimezone() != null && odt.getTimezone().length() > 0 )
    {
      // FIXME: changing static data here....
      final TimeZone timeZone = TimeZone.getTimeZone( odt.getTimezone() );
      GRAFIK_DF.setTimeZone( timeZone );
    }

    final Logger logger = Logger.getLogger( GrafikLauncher.class.getName() );

    final IStatusCollector stati = new StatusCollector( KalypsoGisPlugin.getId() );

    int cc = 1;
    final TypeObservation[] tobs = odt.getObservation().toArray( new TypeObservation[0] );
    for( final TypeObservation element : tobs )
    {
      // now try to locate observation file
      final URL url = urlRes.resolveURL( context, element.getHref() );
      final IFile zmlFile = ResourceUtilities.findFileFromURL( url );

      // if file cannot be found, that probably means it is not local...
      // maybe make a better test later?
      if( zmlFile == null )
      {
        final String msg = Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.18" ) + url.toExternalForm(); //$NON-NLS-1$
        logger.warning( msg );
        stati.add( IStatus.WARNING, msg );
        continue;
      }

      final IObservation obs;
      final ITupleModel values;
      try
      {
        obs = ZmlFactory.parseXML( zmlFile.getLocationURI().toURL() );
        values = obs.getValues( null );
      }
      catch( final Exception e )
      {
        final String msg = Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.19" ) + zmlFile.getName() + Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.20" ) + e.getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$
        logger.warning( msg );
        stati.add( IStatus.WARNING, msg, e );
        continue;
      }

      // find out which axes to use
      final IAxis[] axes = obs.getAxes();
      final IAxis dateAxis = ObservationUtilities.findAxisByClass( axes, Date.class );
      // REMARK: we use the version with many classes, so no exception is thrown if now number-axis was found.
      final IAxis[] numberAxes = KalypsoStatusUtils.findAxesByClasses( axes, new Class[] { Number.class }, true );
      // Just ignore this obs, if it has no number axises
      if( numberAxes.length == 0 )
        continue;

      final List<IAxis> displayedAxes = new ArrayList<>( numberAxes.length );

      final List<TypeCurve> curves = element.getCurve();
      for( final TypeCurve tc : curves )
      {
        // create a corresponding dat-File for the current observation file
        final String datFileProtoName = FileUtilities.nameWithoutExtension( zmlFile.getName() ) + "-" + cc + ".dat"; //$NON-NLS-1$ //$NON-NLS-2$
        final String datFileName = datFileProtoName.replace( ' ', '_' );
        final IFile datFile = dest.getFile( datFileName );

        final IAxis axis = gKurven.addCurve( datFile, tc, numberAxes );

        if( axis != null )
        {
          displayedAxes.add( axis );

          // convert to dat-file, ready to be read by the grafik tool
          zml2dat( values, datFile, dateAxis, axis, monitor );

          final RememberForSync rfs = new RememberForSync( zmlFile, datFile, axis );
          sync.add( rfs );

          cc++;

          try
          {
            // fetch Y axis range for placing possible scenario text item
            final IAxisRange range = values.getRange( axis );

            if( range != null )
            {
              final DoubleComparator dc = new DoubleComparator( 0.001 );
              final Number lower = (Number) range.getLower();
              final Number upper = (Number) range.getUpper();
              if( dc.compare( lower, yLower ) < 0 )
                yLower = lower;
              if( dc.compare( upper, yUpper ) > 0 )
                yUpper = upper;
            }
          }
          catch( final SensorException e )
          {
            e.printStackTrace();
          }
        }
        else
          Logger.getLogger( GrafikLauncher.class.getName() ).warning( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.23" ) + tc.getName() + Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.24" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      }

      try
      {
        // fetch X axis range for placing possible scenario text item
        final IAxisRange range = values.getRange( dateAxis );
        if( range != null )
        {
          final Date d1 = (Date) range.getLower();
          final Date d2 = (Date) range.getUpper();

          if( xLower == null || d1.before( xLower ) )
            xLower = d1;
          if( xUpper == null || d2.after( xUpper ) )
            xUpper = d2;
        }
      }
      catch( final SensorException e )
      {
        e.printStackTrace();
      }

      // is this obs a forecast?
      // TODO: check if odt wants forecast to be shown
      final DateRange fr = TimeseriesUtils.isTargetForecast( obs );
      if( fr != null )
      {
        final String strDate = GRAFIK_DF.format( fr.getFrom() );
        xLines.add( new XLine( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.25" ) + strDate, strDate ) ); //$NON-NLS-1$
      }

      // does is have Alarmstufen? only check if we are displaying at least a
      // W-axis
      try
      {
        ObservationUtilities.findAxisByType( displayedAxes.toArray( new IAxis[displayedAxes.size()] ), ITimeseriesConstants.TYPE_WATERLEVEL );

        final MetadataList mdl = obs.getMetadataList();
        final String[] mds = TimeseriesUtils.findOutMDAlarmLevel( obs );
        for( final String element2 : mds )
        {
          final String alarmLevel = mdl.getProperty( element2 );
          final Double value = NumberUtils.parseQuietDouble( alarmLevel );
          yLines.put( value, new ValueAndColor( element2 + " (" + GRAFIK_NF_W.format( value ) + ")", value.doubleValue() ) ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
      catch( final NoSuchElementException e )
      {
        // ignored
      }

      displayedAxes.clear();
    }

    writer.write( gKurven.toVorlagentext() );
    writer.write( "\n" ); //$NON-NLS-1$
    writer.write( "HTitel:\t" + odt.getTitle() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    writer.write( "xTitel:\t" + gAchsen.getBottomLabel() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    writer.write( "yTitel1:\t" + gAchsen.getLeftLabel() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$
    writer.write( "yTitel2:\t" + gAchsen.getRightLabel() + "\n" ); //$NON-NLS-1$ //$NON-NLS-2$

    // constant vertical lines...
    for( final Object element : xLines )
    {
      final String strDate = element.toString();
      writer.write( "Senkrechte:" + strDate + '\n' ); //$NON-NLS-1$
    }

    // constant horizontal lines...
    for( final Object element : yLines.keySet() )
    {
      final ValueAndColor vac = yLines.get( element );
      writer.write( "yKonst:" + GRAFIK_NF_W.format( vac.value ) + " " + vac.label + '\n' ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    final IStatus status = stati.asMultiStatusOrOK( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.17" ) ); //$NON-NLS-1$
    if( !status.isOK() )
      throw new CoreException( status );

    final RememberForSync[] syncs = sync.toArray( new RememberForSync[sync.size()] );
    return syncs;
  }

  /**
   * Converts a zml file to a dat file that the grafik tool can load.
   */
  private static IStatus zml2dat( final ITupleModel values, final IFile datFile, final IAxis dateAxis, final IAxis axis, final IProgressMonitor monitor ) throws CoreException
  {
    final SetContentHelper sch = new SetContentHelper( Messages.getString( "org.kalypso.ogc.sensor.diagview.grafik.GrafikLauncher.45" ) ) //$NON-NLS-1$
    {
      @Override
      protected void write( final OutputStreamWriter writer ) throws Throwable
      {
        for( int i = 0; i < values.size(); i++ )
        {
          if( monitor.isCanceled() )
            return;

          final Object elt = values.get( i, dateAxis );
          final String text = GRAFIK_DF.format( elt );
          writer.write( text );
          writer.write( '\t' );

          writer.write( values.get( i, axis ).toString() );

          writer.write( '\n' );
        }
      }
    };

    sch.setFileContents( datFile, false, false, monitor );

    return Status.OK_STATUS;
  }

  /**
   * mini helper class for storing a value and a color
   *
   * @author schlienger
   */
  private final static class ValueAndColor
  {
    final double value;

    final String label;

    public ValueAndColor( final String lbl, final double val )
    {
      label = lbl;
      value = val;
    }
  }

  private final static class XLine implements Comparable<XLine>
  {
// public final String label;

    public final String strDate;

    public XLine( @SuppressWarnings("unused") final String lbl, final String strdate )
    {
// label = lbl;
      strDate = strdate;
    }

    @Override
    public String toString( )
    {
      return strDate;
    }

    @Override
    public int compareTo( final XLine o )
    {
      return strDate.compareTo( o.strDate );
    }
  }
}