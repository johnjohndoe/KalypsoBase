/*
 * $Id$
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.sample;

import java.io.*;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.security.auth.callback.TextCallbackHandler;

import com.sun.xml.wss.impl.callback.*;

import java.util.StringTokenizer;

public  class ClientSecurityEnvironmentHandler implements CallbackHandler {

    private  UnsupportedCallbackException unsupported = 
        new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");


    // Use a read mechanism that works with ANT's inputstring attribute of <java> task
    private String usernamePassword = null;
    private StringTokenizer tokenizer = null;
    
    private String userName = "Ron";
    private String passWord = "noR";

    private String readLine() throws IOException {
        /* <java> task inputstring attribute not supported by older ANTs
        return new BufferedReader
 	    (new InputStreamReader(System.in)).readLine();
        */
        return System.getProperty("username.password"); 
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i=0; i < callbacks.length; i++) {
           if (callbacks[i] instanceof UsernameCallback) {
                UsernameCallback cb = (UsernameCallback)callbacks[i];

                usernamePassword = readLine();
                if (usernamePassword != null) {
                    tokenizer = new StringTokenizer(usernamePassword); 
                    userName = tokenizer.nextToken(" ");
                    System.out.println("Username read=" + userName);
                    cb.setUsername(userName);
                }

            } else if (callbacks[i] instanceof PasswordCallback) {
                PasswordCallback cb = (PasswordCallback)callbacks[i];
                if (usernamePassword != null) {
                    passWord = tokenizer.nextToken(" "); 
                    System.out.println("Password read=" + passWord);
                    cb.setPassword(passWord);
                }
            }  
        }
    }
}
