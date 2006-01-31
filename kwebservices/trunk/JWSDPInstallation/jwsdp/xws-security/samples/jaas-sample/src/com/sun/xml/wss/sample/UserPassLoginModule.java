/*
 * $Id$
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.sample;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import javax.security.auth.x500.X500Principal;

import java.util.*;
import java.io.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.sun.xml.wss.impl.callback.PasswordCallback;


public class UserPassLoginModule implements LoginModule {

    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map sharedState;
    private Map options;

    private HashMap userPassList = new HashMap();

    private String userName = null;
    private String password = null;
    private boolean loginSuccessful = false;
    private String userPassXML = null;

    public UserPassLoginModule() {
        setUserPassXML();
        setUserPassList();    
    }

    public boolean abort() {
        userName = null;
        password = null;
        return true;
    }

    public boolean commit() {
        if (!loginSuccessful) {
             userName = null;
             password = null;
             subject.getPrincipals().clear();
             subject.getPrivateCredentials().clear();
             return false;
        } 
        return true;
    }

    public void initialize(
        Subject subject, CallbackHandler callbackHandler, java.util.Map sharedState, java.util.Map options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.sharedState = sharedState;
        this.options = options;
    }

    public boolean login() throws LoginException {
        if (subject == null) {
            System.out.println("Subject passed is null, Unable to obtain Username and Password");
            return false;
        }

        try {
            userName = getUsername(subject);
            password = getPassword(subject);
        } catch (Throwable t) {
          t.printStackTrace();
          return false;
        }

        if (validUser()) {
            loginSuccessful = true;
        }

        return loginSuccessful;
    }

    public boolean logout()  {
        subject.getPrincipals().clear();
        subject.getPrivateCredentials().clear();
        return true;
    }

    private boolean validUser() {
        if (!userPassList.containsKey(userName))
            return false;

        String correctPassword = (String)userPassList.get(userName);
        if (correctPassword.equals(password))
            return true;

        return false;
    }

    private void setUserPassList() {

        if (userPassXML != null && !userPassXML.equals("unknown")) {
            try {
                   FileInputStream xmlStream = new FileInputStream(userPassXML);
                   DocumentBuilderFactory factory =
                       new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
                   factory.setIgnoringComments(true);
                   DocumentBuilder builder = factory.newDocumentBuilder();
                   Document document = builder.parse(xmlStream);
                                                                                                                      
                   NodeList usersList = document.getElementsByTagName("user");
                   for (int j = 0; j < usersList.getLength(); j++) {
                       Element user = (Element) usersList.item(j);
                       userPassList.put(
                           user.getAttribute("username"),
                           user.getAttribute("password"));
                   }
            } catch (java.io.FileNotFoundException fnfe) {
            } catch (java.io.IOException ioe) {
                System.out.println("IOException while trying to read file:" + userPassXML);
            } catch (javax.xml.parsers.ParserConfigurationException pce) {
                  throw new RuntimeException("Failed to create DocumentBuilder");
            } catch (org.xml.sax.SAXException se) {
               throw new RuntimeException("Error parsing xws-security-users.xml");
            }

        } else {
            System.out.println("Error : could not find username-password list file");
        }
    } 

    private String getUsername(Subject subject) {
        Iterator it = subject.getPrincipals().iterator();
        // assuming first principal is username for the sake of this sample
        X500Principal principal = (X500Principal)it.next();
        return getCommonName(principal.getName());
    }

    private String getPassword(Subject subject) {
        Iterator it = subject.getPrivateCredentials().iterator();
        // assuming first cred is password for the sake of this sample
        String password = (String)it.next();
        return password;
    }

    private String getCommonName(String x500DistinguishedName) {
        String name = null;
        int begin = x500DistinguishedName.indexOf("CN=");
        if (begin == -1) {
            return x500DistinguishedName;
        }
        int end = x500DistinguishedName.indexOf(",");
        if (end == -1) {
            name = x500DistinguishedName.substring(begin + 3);
            return name;
        }
        name = x500DistinguishedName.substring(begin + 3, end);
        return name;
    }

    private void setUserPassXML() {
        userPassXML = "unknown";

        String containerType = System.getProperty("jwsdp.container.type");

        if (containerType != null) {
           String containerHome = "";
           if ("appserver".equals(containerType)) {
               containerHome = System.getProperty("catalina.home") + File.separator + ".." + File.separator + "..";
           } else if ("webserver".equals(containerType)) {
               containerHome = System.getProperty("catalina.home") + File.separator + "..";
           } else if ("tomcat".equals(containerType)) {
               containerHome = System.getProperty("catalina.home");
           }
                                                                             
           userPassXML = containerHome + File.separator + "xws-security" + File.separator + "etc" 
               + File.separator + "userpasslist.xml";
           File f = new File(userPassXML);
           if (!f.exists()) {
               System.out.println("Error: Cannot find file: " + userPassXML);
           }
        } else {
               System.out.println("Error: Cannot find  username-password file");
        } 
    }

}
