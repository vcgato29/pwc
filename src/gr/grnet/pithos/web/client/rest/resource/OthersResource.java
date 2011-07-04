/*
 * Copyright 2011 GRNET S.A. All rights reserved.
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
package gr.grnet.pithos.web.client.rest.resource;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.ui.TreeItem;

public class OthersResource extends RestResource {

	public OthersResource(String aUri) {
		super(aUri);
	}

	List<String> others = new ArrayList<String>();
	List<OtherUserResource> otherUsers = new ArrayList<OtherUserResource>();

	/**
	 * Retrieve the others.
	 *
	 * @return the others
	 */
	public List<String> getOthers() {
		return others;
	}

	/**
	 * Modify the others.
	 *
	 * @param newOthers the others to set
	 */
	public void setOthers(List<String> newOthers) {
		others = newOthers;
	}

	public List<OtherUserResource> getOtherUsers() {
		return otherUsers;
	}

	public void setOtherUsers(List<OtherUserResource> newOtherUsers) {
		otherUsers = newOtherUsers;
	}

	@Override
	public void createFromJSON(String text) {
		JSONArray array = (JSONArray) JSONParser.parse(text);
		if (array != null)
			for (int i = 0; i < array.size(); i++) {
				JSONObject js = array.get(i).isObject();
				if (js != null) {
					String othersUri = unmarshallString(js, "uri");
					String username = unmarshallString(js, "username");
					if(othersUri != null){
						getOthers().add(othersUri);
						OtherUserResource r = new OtherUserResource(othersUri);
						r.setUsername(username);
						getOtherUsers().add(r);
					}
				}
			}
	}

	public String getUsernameOfUri(String u){
		if(!u.endsWith("/"))
			u=u+"/";
		for(OtherUserResource o : getOtherUsers()){
			GWT.log("CHECKING USER URI:"+o.getUri(), null);
			String toCheck = o.getUri();
			if(!toCheck.endsWith("/"))
				toCheck=toCheck+"/";
			if(toCheck.equals(u))
				return o.getUsername();
		}
		return null;
	}

	@Override
	public String getLastModifiedSince() {
		return null;
	}

	@Override
	public String constructUri(TreeItem treeItem,String path){
		String constructedUri = "Files/"+ path.substring(path.lastIndexOf("/")+1) + "others/";
		return constructedUri;
	}
}
