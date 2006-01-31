/*
 * $Id$
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.sample;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;

import javax.security.auth.login.*;
import javax.security.auth.Subject;

import javax.security.auth.x500.X500Principal;

public class JaasValidator implements PasswordValidationCallback.PasswordValidator {

    public boolean validate(PasswordValidationCallback.Request request) throws PasswordValidationCallback.PasswordValidationException {

        PasswordValidationCallback.PlainTextPasswordRequest req = (PasswordValidationCallback.PlainTextPasswordRequest)request;

        Subject subject = createSubject(req.getUsername(), req.getPassword());

        if (subject == null) {
            System.out.println("Unable to create Subject, make sure you have right permissions set");
            return false;
        }

        LoginContext lc = null;
        try {
            lc = new LoginContext("XWS_SECURITY_SERVER", subject);
        } catch (LoginException le) {
            System.err.println("Cannot create LoginContext. " + le.getMessage());
        } catch (SecurityException se) {
            System.err .println("Cannot create LoginContext. " + se.getMessage());
        }

        // login now
        try {
             lc.login();
             Subject subj = lc.getSubject();
             if (!subj.getPrincipals().isEmpty()) {
                 return true;
             }
        } catch (Exception e) {
             e.printStackTrace();
             System.err.println("Unexpected Exception while Authenticating User :" + e.getMessage());
             throw new PasswordValidationCallback.PasswordValidationException(e);
        }
        return false;
    }

    public Subject  createSubject(String userName, String password) {
        try {
            Subject subject = new Subject();
            subject.getPrincipals().add(new X500Principal("CN=" + userName));
            subject.getPrivateCredentials().add(password);
            return subject;
         } catch (Throwable t) {
             t.printStackTrace();
             return null;
         }
    }

}
