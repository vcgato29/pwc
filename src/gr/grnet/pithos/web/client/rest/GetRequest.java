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

package gr.grnet.pithos.web.client.rest;

import gr.grnet.pithos.web.client.Const;
import gr.grnet.pithos.web.client.Helpers;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.Resource;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

public abstract class GetRequest<T extends Resource> implements ScheduledCommand {
	
	protected static final int MAX_RETRIES = 3; 

	protected int retries = 0; 

	protected Class<T> resourceClass;

    private String api;

    protected String owner;
    
    private String path;

    private int okCode;
    
    protected T result;

    private Map<String, String> headers = new HashMap<String, String>();

    public abstract void onSuccess(T _result);

    public abstract void onError(Throwable t);

    public GetRequest(Class<T> resourceClass, String api, String owner, String path, int okCode, T result) {
        this.resourceClass = resourceClass;
        this.api = api;
        this.owner = owner;
        this.path = path;
        this.okCode = okCode;
        this.result = result;
    }

    public GetRequest(Class<T> resourceClass, String api, String owner, String path) {
        this(resourceClass, api, owner, path, -1, null);
        Pithos.LOG("GetRequest() resourceClass = ", resourceClass.getName(), ", api=", api, ", owner=", owner, ", path=", path);
    }

    public GetRequest(Class<T> resourceClass, String api, String owner, String path, T result) {
        this(resourceClass, api, owner, path, -1, result);
    }

    @Override
    public void execute() {
    	if (path.contains(Const.QUESTION_MARK)) {
    		path += "&t=" + System.currentTimeMillis();
        }
    	else {
    		path += "?t=" + System.currentTimeMillis();
        }

        Pithos.LOG("GET ", api + owner + path);

        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, api + owner + path);
        Helpers.setHeaders(builder, headers);

        try {
            builder.sendRequest("", new RestRequestCallback<T>(api + owner + path, okCode) {
                @Override
                public void onSuccess(T object) {
                    GetRequest.this.onSuccess(object);
                }

                @Override
                public T deserialize(Response response) {
                    return Resource.createFromResponse(resourceClass, owner, response, result);
                }

                @Override
                public void onError(Request request, Throwable throwable) {
                    if (throwable instanceof RestException) {
                        if (((RestException) throwable).getHttpStatusCode() == 304 && result != null){
                            Pithos.LOG("Using cache: ", result.toString());
                            onSuccess(result);
                            return;
                        }
                    }
                    GetRequest.this.onError(throwable);
                }

				@Override
				public void onUnauthorized(Response response) {
                    GetRequest.this.onUnauthorized(response);
				}
            });
            retries++;
        }
        catch (RequestException e) {
            Pithos.LOG("Error in GetRequest", e);
        }
    }

    protected abstract void onUnauthorized(Response response);

	public void setHeader(String header, String value) {
        headers.put(header, value);
    }
}
