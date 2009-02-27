/*
* Copyright (c) 2004 Sun Microsystems, Inc.
* All rights reserved. 
*/

package com.sun.wsi.scm.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostClient {
	public static void main(String[] args) {
		try {
			System.out.println("POSTing to " + System.getProperty("endpoint"));
			URL endpoint = new URL(System.getProperty("endpoint"));
			HttpURLConnection httpConnection =
				(HttpURLConnection) endpoint.openConnection();
			String soapAction = System.getProperty("SOAPAction");
			if (soapAction != null)
				httpConnection.setRequestProperty(
					"SOAPAction",
					System.getProperty("SOAPAction"));
			httpConnection.setDoOutput(true);
			httpConnection.setDoInput(true);
			httpConnection.setRequestMethod("POST");
			httpConnection.setRequestProperty("Content-Type", "text/xml");
			PrintStream out = new PrintStream(httpConnection.getOutputStream());

			FileInputStream fis =
				new FileInputStream(System.getProperty("soap-message"));
			BufferedReader in = new BufferedReader(new InputStreamReader(fis));
			try {
				System.out.println("Sending the request ...");
				String line = in.readLine();
				while (line != null) {
					System.out.println("request: " + line);
					out.print(line);
					line = in.readLine();
				}
			} catch (IOException ex) {
				ex.printStackTrace(System.out);
			} finally {
				System.out.println(" ... done\n");
			}

			try {
				System.out.println("Reading the response stream ...");
				BufferedReader in2 =
					new BufferedReader(
						new InputStreamReader(httpConnection.getInputStream()));
				String line = in2.readLine();
				while (line != null) {
					System.out.println("response: " + line);
					line = in2.readLine();
				}
			} catch (IOException ex) {
				ex.printStackTrace(System.out);
			} finally {
				System.out.println(" ... done\n");
			}

			try {
				System.out.println("Reading the error stream ...");
				InputStream errorStream = httpConnection.getErrorStream();
				if (errorStream != null) {
					BufferedReader in3 =
						new BufferedReader(new InputStreamReader(errorStream));
					String line = in3.readLine();
					while (line != null) {
						System.out.println("error: " + line);
						line = in3.readLine();
					}
				}
			} catch (IOException ex) {
				ex.printStackTrace(System.out);
			} finally {
				System.out.println(" ... done\n");
			}

		} catch (IOException ex) {
			ex.printStackTrace(System.out);
		}
	}
}
