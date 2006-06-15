package com.bce.ext.jargs;

import java.io.File;
import java.util.Locale;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;

/**
 * <p>
 * An CmdLineParser Option which parses the option as a file
 * </p>
 * <p>
 * parseValue always returns an file object
 * </p>
 * 
 * @author belger
 */
public class FileOption extends Option
{
	private final boolean m_bfileMustExist;

	public FileOption( final char shortForm, final String longForm, final boolean fileMustExist )
	{
		super( shortForm, longForm, true );

		m_bfileMustExist = fileMustExist;
	}

	/**
	 * @see jargs.gnu.CmdLineParser.Option#parseValue(java.lang.String,
	 *      java.util.Locale)
	 */
	@Override
	protected Object parseValue( final String arg, final Locale locale ) throws IllegalOptionValueException
	{
		final File f = new File( arg );

		if( m_bfileMustExist && !f.exists() )
			throw new IllegalOptionValueException( this, "Eingabedatei " + f.getAbsolutePath() + " existiert nicht." );

		return f;
	}

}
