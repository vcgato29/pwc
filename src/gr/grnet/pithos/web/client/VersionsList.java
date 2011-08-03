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
package gr.grnet.pithos.web.client;

import gr.grnet.pithos.web.client.FilePropertiesDialog.Images;
import gr.grnet.pithos.web.client.rest.DeleteCommand;
import gr.grnet.pithos.web.client.rest.GetCommand;
import gr.grnet.pithos.web.client.rest.PostCommand;
import gr.grnet.pithos.web.client.rest.RestCommand;
import gr.grnet.pithos.web.client.rest.RestException;
import gr.grnet.pithos.web.client.rest.resource.FileResource;
import gr.grnet.pithos.web.client.rest.resource.UserResource;
import gr.grnet.pithos.web.client.rest.resource.UserSearchResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;

public class VersionsList extends Composite {

    private Pithos app;

	int selectedRow = -1;

	int permissionCount = -1;

	List<FileResource> versions = null;

	final Images images;

	final VerticalPanel permPanel = new VerticalPanel();

	final FlexTable permTable = new FlexTable();

	FileResource toRemove = null;

	FilePropertiesDialog container;

	public VersionsList(Pithos _app, FilePropertiesDialog aContainer, final Images theImages, List<FileResource> theVersions) {
        app = _app;
		images = theImages;
		container = aContainer;
		versions = theVersions;
		Collections.sort(theVersions, new Comparator<FileResource>(){

			@Override
			public int compare(FileResource o1, FileResource o2) {
				return o1.getVersion().compareTo(o2.getVersion());
			}

		});
		permTable.setText(0, 0, "Version");
		permTable.setText(0, 1, "Created");
		permTable.setText(0, 2, "Modified");
		permTable.setText(0, 3, "Size");
		permTable.setText(0, 4, "");
		permTable.setText(0, 5, "");
		permTable.getFlexCellFormatter().setStyleName(0, 0, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 1, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 2, "props-toplabels");
		permTable.getFlexCellFormatter().setStyleName(0, 3, "props-toplabels");
		permTable.getFlexCellFormatter().setColSpan(0, 1, 2);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 1, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_CENTER);
		permTable.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_CENTER);
		permPanel.add(permTable);
		permPanel.addStyleName("pithos-TabPanelBottom");
		permTable.addStyleName("pithos-permList");
		initWidget(permPanel);
		updateTable();
	}

	public void updateTable() {
		copyListAndContinue(versions);		
	}
	
	public void showVersionsTable(){
		int i = 1;
		if (toRemove != null) {
			versions.remove(toRemove);
			toRemove = null;
		}
		for (final FileResource dto : versions) {
			HTML restoreVersion = new HTML("<a href='#' class='hidden-link info'><span>"+AbstractImagePrototype.create(images.restore()).getHTML()+"</span><div>Restore this Version</div></a>");
			restoreVersion.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					restoreVersion(dto);
				}
			});

			permTable.setHTML(i, 0, "<span>" + dto.getVersion() + "</span>");
			permTable.setHTML(i, 1, "<span>" + formatDate(dto.getCreationDate()) + " by " + app.findUserFullName(dto.getCreatedBy()) + "</span>");
			permTable.setHTML(i, 2, "<span>" + formatDate(dto.getModificationDate()) + " by " + app.findUserFullName(dto.getModifiedBy()) + "</span>");
			permTable.setHTML(i, 3, "<span>" + dto.getFileSizeAsString() + "</span>");
			HTML downloadHtml = new HTML("<a class='hidden-link info' href='#'><span>"+AbstractImagePrototype.create(images.download()).getHTML()+"</span><div>View this Version</div></a>");
			downloadHtml.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String dateString = RestCommand.getDate();
					String resource = dto.getUri().substring(app.getApiPath().length()-1, dto.getUri().length());
					String sig = app.getCurrentUserResource().getUsername()+" "+RestCommand.calculateSig("GET", dateString, resource, RestCommand.base64decode(app.getToken()));
					String fileUrl = dto.getUri() + "?version=" + dto.getVersion() + "&Authorization=" + URL.encodeComponent(sig) + "&Date="+URL.encodeComponent(dateString);
					Window.open(fileUrl, "_BLANK", "");
				}
			});
			permTable.setWidget(i, 4, downloadHtml);
			permTable.setWidget(i, 5, restoreVersion);
			permTable.getFlexCellFormatter().setStyleName(i, 0, "props-labels");
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 0, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 1, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setColSpan(i, 1, 2);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 2, HasHorizontalAlignment.ALIGN_CENTER);
			permTable.getFlexCellFormatter().setHorizontalAlignment(i, 3, HasHorizontalAlignment.ALIGN_CENTER);
			i++;
		}
		for (; i < permTable.getRowCount(); i++)
			permTable.removeRow(i);
	}

	void removeVersion(final FileResource version) {
		DeleteCommand df = new DeleteCommand(app, version.getUri()){

			@Override
			public void onComplete() {
				toRemove = version;
				updateTable();
				app.getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException){
					int statusCode = ((RestException)t).getHttpStatusCode();
					if(statusCode == 405)
						app.displayError("You don't have the necessary permissions");
					else if(statusCode == 404)
						app.displayError("Versions does not exist");
					else
						app.displayError("Unable to remove version:"+((RestException)t).getHttpStatusText());
				}
				else
					app.displayError("System error removing version:"+t.getMessage());
			}
		};
		DeferredCommand.addCommand(df);

	}

	void restoreVersion(final FileResource version) {
		FileResource selectedFile = (FileResource) app.getCurrentSelection();
		PostCommand ep = new PostCommand(app, selectedFile.getUri()+"?restoreVersion="+version.getVersion(),"",200){


			@Override
			public void onComplete() {
				container.hide();
                app.getTreeView().refreshCurrentNode(false);
			}

			@Override
			public void onError(Throwable t) {
				GWT.log("", t);
				if(t instanceof RestException)
					app.displayError("Unable to restore version:"+((RestException)t).getHttpStatusText());
				else
					app.displayError("System error restoring version:"+t.getMessage());
			}

		};
		DeferredCommand.addCommand(ep);
	}

	private String formatDate(Date date){
		DateTimeFormat format = DateTimeFormat.getFormat("dd/MM/yyyy : HH:mm");
		return format.format(date);
	}
	
	/**
	 * Copies the input List to a new List
	 * @param input
	 */
	private void copyListAndContinue(List<FileResource> input){
		List<FileResource> copiedInput = new ArrayList<FileResource>();		
		for(FileResource dto : input) {
			copiedInput.add(dto);
		}
		handleFullNames(copiedInput);
	}
	
	/**
	 * Examines whether or not the user's full name exists in the 
	 * userFullNameMap in the Pithos.java for every element of the input list.
	 * If the user's full name does not exist in the map then a request is being made
	 * for the specific username.  
	 * 
	 * @param input
	 */
	private void handleFullNames(List<FileResource> input){		
		if(input.isEmpty()){
			showVersionsTable();
			return;
		}
		
		if(app.findUserFullName(input.get(0).getOwner()) == null){
			findFullNameAndUpdate(input);		
			return;
		}
				
		if(input.size() >= 1){
			input.remove(input.get(0));
			if(input.isEmpty()){
				showVersionsTable();			
			}else{
				handleFullNames(input);
			}
		}					
	}
	
	/**
	 * Makes a request to search for full name from a given username
	 * and continues checking the next element of the List.
	 *  
	 * @param input
	 */

	private void findFullNameAndUpdate(final List<FileResource> input){				
		final String aUserName = input.get(0).getOwner();
		String path = app.getApiPath() + "users/" + aUserName;

		GetCommand<UserSearchResource> gg = new GetCommand<UserSearchResource>(app, UserSearchResource.class, path, false,null) {
			@Override
			public void onComplete() {
				final UserSearchResource result = getResult();
				for (UserResource user : result.getUsers()){
					String username = user.getUsername();
					String userFullName = user.getName();
					app.putUserToMap(username, userFullName);
					if(input.size() >= 1){
						input.remove(input.get(0));						
						if(input.isEmpty()){
							showVersionsTable();
							return;
						}
						handleFullNames(input);										
					}									
				}
			}
			@Override
			public void onError(Throwable t) {				
				app.displayError("Unable to fetch user's full name from the given username " + aUserName);
				if(input.size() >= 1){
					input.remove(input.get(0));
					handleFullNames(input);					
				}
			}
		};
		DeferredCommand.addCommand(gg);
	
	}

}
