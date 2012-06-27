/***********************************************************************
 * mt4j Copyright (c) 2008 - 2010 Christopher Ruff, Fraunhofer-Gesellschaft All rights reserved.
 *  
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ***********************************************************************/
package org.mt4j.input.inputSources;

import org.mt4j.util.MT4jSettings;
import org.mt4j.util.logging.ILogger;
import org.mt4j.util.logging.MTLoggerFactory;

/**
 * Input source for native Windows 7 WM_TOUCH messages for single/multi-touch. <br>
 * Be careful to instantiate this class only ONCE!
 * 
 * @author C.Ruff
 * 
 */
public class Win7NativeTouchSource implements IWin7NativeTouchSourceProvider {
	private static final ILogger logger = MTLoggerFactory
			.getLogger(Win7NativeTouchSource.class.getName());
	static {
		// logger.setLevel(ILogger.ERROR);
		// logger.setLevel(ILogger.DEBUG);
		logger.setLevel(ILogger.INFO);
	}

	/** The Constant logger. */
	static boolean loaded = false;

	private int sunAwtCanvasHandle;
	private boolean initialized;
	private boolean success;
	private static final String dllName32 = "Win7Touch";
	private static final String dllName64 = "Win7Touch64";
	private static final String canvasClassName = "SunAwtCanvas";

	// NATIVE METHODS //
	private native int findWindow(String tmpTitle, String subWindowTitle);

	private native boolean init(long HWND);

	private native boolean getSystemMetrics();

	private native boolean quit();

	private native boolean pollEvent(Native_WM_TOUCH_Event myEvent);

	// NATIVE METHODS //

	/**
	 * Instantiates a new win7 native touch source.
	 * 
	 * @param mtApp
	 *            the mt app
	 */
	public Win7NativeTouchSource() {
		this.success = false;

		if (!loaded) {
			loaded = true;
			String dllName = (MT4jSettings.getInstance().getArchitecture() == MT4jSettings.ARCHITECTURE_32_BIT) ? dllName32
					: dllName64;
			System.loadLibrary(dllName);
		} else {
			logger.error("Win7NativeTouchSource may only be instantiated once.");
			return;
		}

		boolean touchAvailable = this.getSystemMetrics();
		if (!touchAvailable) {
			logger.error("Windows 7 Touch Input currently not available!");
			return;
		} else {
			logger.info("Windows 7 Touch Input available.");
		}
		// */

		initialized = false;

		// this.getNativeWindowHandles();
		success = true;

//		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//			public void run() {
//				if (isSuccessfullySetup()) {
//					logger.debug("Cleaning up Win7 touch source..");
//					quit();
//				}
//			}
//		}));
	}

	public boolean pollMTEvent(Native_WM_TOUCH_Event myEvent) {
		return pollEvent(myEvent);
	}

	public boolean setSunAwtCanvasHandle(int HWND) {
		if (HWND > 0) {
			this.sunAwtCanvasHandle = HWND;
			logger.debug("-> Found SunAwtCanvas HWND: "
					+ this.sunAwtCanvasHandle);
			// Initialize c++ core (subclass etc)
			this.init(this.sunAwtCanvasHandle);
			this.initialized = true;
			return true;
		} else {
			logger.error("-> Couldnt retrieve the SunAwtCanvas handle!");
			return false;
		}
	}

	public void cleanup() 
	{
		quit();
        System.out.println("Windows 7 Native support disposed (finalized).");
	}
	
}
