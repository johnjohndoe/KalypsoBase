/*
 * $Id$
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.wss.sample;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import com.sun.xml.wss.impl.callback.*;

public  class ServerSecurityEnvironmentHandler implements CallbackHandler {

    private  UnsupportedCallbackException unsupported = 
        new UnsupportedCallbackException(null, "Unsupported Callback Type Encountered");

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (int i=0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof PasswordValidationCallback) {
                PasswordValidationCallback cb = (PasswordValidationCallback) callbacks[i];

                if (cb.getRequest() instanceof PasswordValidationCallback.PlainTextPasswordRequest) {
                    // plain text handling
                    cb.setValidator(new JaasValidator());
                } else {
                    throw unsupported;
                }

            } else {
                throw unsupported;
            }
        }
    }
}
