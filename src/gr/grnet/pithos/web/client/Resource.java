/*
 * Copyright 2011-2013 GRNET S.A. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 *   1. Redistributions of source code must retain the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above
 *      copyright notice, this list of conditions and the following
 *      disclaimer in the documentation and/or other materials
 *      provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY GRNET S.A. ``AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GRNET S.A OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and
 * documentation are those of the authors and should not be
 * interpreted as representing official policies, either expressed
 * or implied, of GRNET S.A.
 */

package gr.grnet.pithos.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.*;
import gr.grnet.pithos.web.client.Invitations;
import gr.grnet.pithos.web.client.SharingUsers;
import gr.grnet.pithos.web.client.foldertree.AccountResource;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.FileVersions;
import gr.grnet.pithos.web.client.foldertree.Folder;

import java.util.Date;

public abstract class Resource {

    protected static String unmarshallString(JSONObject obj, String key) {
        final JSONValue jsonValue = obj.get(key);
        if(jsonValue != null) {
            JSONString s = jsonValue.isString();
            if(s != null) {
                return s.stringValue();
            }
        }
        return null;
    }

    protected static int unmarshallInt(JSONObject obj, String key) {
        final JSONValue jsonValue = obj.get(key);
        if(jsonValue != null) {
            final JSONNumber jsonNumber = jsonValue.isNumber();
            if(jsonNumber != null) {
                return (int) jsonNumber.doubleValue();
            }
        }
        return -1;
    }

    protected static long unmarshallLong(JSONObject obj, String key) {
        final JSONValue jsonValue = obj.get(key);
        if(jsonValue != null) {
            final JSONNumber jsonNumber = jsonValue.isNumber();
            if(jsonNumber != null) {
                return (long) jsonNumber.doubleValue();
            }
        }
        return -1;
    }

    protected static boolean unmarshallBoolean(JSONObject obj, String key) {
        final JSONValue jsonValue = obj.get(key);
        if(jsonValue != null) {
            final JSONBoolean aBoolean = jsonValue.isBoolean();
            if(aBoolean != null) {
                return aBoolean.booleanValue();
            }
        }
        return false;
    }

    protected static Date unmarshallDate(JSONObject obj, String key) {
        final JSONValue jsonValue = obj.get(key);
        if(jsonValue != null) {
            JSONString jsonString = jsonValue.isString();
            if(jsonString != null) {
                try {
                    return DateTimeFormat.getFormat(PredefinedFormat.ISO_8601).parse(jsonString.stringValue());
                }
                catch(IllegalArgumentException e) {
                    GWT.log("", e);
                }
            }
        }
        return null;
    }

    public static native String getDate(Long ms)/*-{
      return (new Date(ms)).toUTCString();
    }-*/;

    @SuppressWarnings("unchecked")
    public static <T> T createFromResponse(Class<T> aClass, String owner, Response response, T result) {
        T result1 = null;
        if(aClass.equals(AccountResource.class)) {
            result1 = (T) AccountResource.createFromResponse(owner, response, (AccountResource) result);
        }
        else if(aClass.equals(Folder.class)) {
            result1 = (T) Folder.createFromResponse(owner, response, (Folder) result);
        }
        else if(aClass.equals(File.class)) {
            result1 = (T) File.createFromResponse(owner, response, (File) result);
        }
        else if(aClass.equals(SharingUsers.class)) {
            result1 = (T) SharingUsers.createFromResponse(response, (SharingUsers) result);
        }
        else if(aClass.equals(FileVersions.class)) {
            result1 = (T) FileVersions.createFromResponse(response);
        }
        else if(aClass.equals(Invitations.class)) {
            result1 = (T) Invitations.createFromResponse(response);
        }
        return result1;
    }

    public abstract Date getLastModified();
}
