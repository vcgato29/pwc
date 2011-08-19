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

package gr.grnet.pithos.web.client.othersharedtree;

import gr.grnet.pithos.web.client.FolderContextMenu;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.SharingUsers;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.mysharedtree.MysharedTreeView;
import gr.grnet.pithos.web.client.othersharedtree.OtherSharedTreeView.Templates;
import gr.grnet.pithos.web.client.rest.GetRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.TreeViewModel;

public class OtherSharedTreeViewModel implements TreeViewModel {

    protected Pithos app;

    private Cell<Folder> folderCell = new AbstractCell<Folder>(ContextMenuEvent.getType().getName()) {

       @Override
        public void render(Context context, Folder folder, SafeHtmlBuilder safeHtmlBuilder) {
            String html = AbstractImagePrototype.create(OtherSharedTreeView.images.folderYellow()).getHTML();
            safeHtmlBuilder.appendHtmlConstant(html);
            safeHtmlBuilder.append(Templates.INSTANCE.nameSpan(folder.getName()));
        }

        @Override
        public void onBrowserEvent(Context context, com.google.gwt.dom.client.Element parent, Folder folder, com.google.gwt.dom.client.NativeEvent event, ValueUpdater<Folder> valueUpdater) {
            if (event.getType().equals(ContextMenuEvent.getType().getName())) {
                OtherSharedTreeViewModel.this.selectionModel.setSelected(folder, true);
                FolderContextMenu menu = new FolderContextMenu(app, OtherSharedTreeView.images, folder);
                menu.setPopupPosition(event.getClientX(), event.getClientY());
                menu.show();
            }
        }
    };

    private ListDataProvider<String> rootDataProvider = new ListDataProvider<String>();
    private ListDataProvider<String> firstLevelDataProvider = new ListDataProvider<String>();

    private Map<String, ListDataProvider<Folder>> userDataProviderMap = new HashMap<String, ListDataProvider<Folder>>();
    private Map<Folder, ListDataProvider<Folder>> dataProviderMap = new HashMap<Folder, ListDataProvider<Folder>>();

    private SingleSelectionModel<Folder> selectionModel;

    public OtherSharedTreeViewModel(Pithos _app, SingleSelectionModel<Folder> selectionModel) {
        app = _app;
        this.selectionModel = selectionModel;
    }

    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            rootDataProvider.getList().add("Other 's Shared");
            final SingleSelectionModel<String> selectionModel2 = new SingleSelectionModel<String>();
            selectionModel2.addSelectionChangeHandler(new Handler() {

                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    if (selectionModel2.getSelectedObject() != null) {
                    	app.deselectOthers(selectionModel2);
                    }
                }
            });
            app.addSelectionModel(selectionModel2);
            return new DefaultNodeInfo<String>(rootDataProvider, new TextCell(new SafeHtmlRenderer<String>() {
                @Override
                public SafeHtml render(String object) {
                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
                    render(object, builder);
                    return builder.toSafeHtml();
                }

                @Override
                public void render(String object, SafeHtmlBuilder builder) {
                    String html = AbstractImagePrototype.create(OtherSharedTreeView.images.othersShared()).getHTML();
                    builder.appendHtmlConstant(html);
                    builder.append(OtherSharedTreeView.Templates.INSTANCE.nameSpan(object));
                }
            }),  selectionModel2, null);
        }
        else if (value instanceof String) {
        	if (value.equals("Other 's Shared")) {
	        	fetchSharingUsers(firstLevelDataProvider);
	            final SingleSelectionModel<String> selectionModel3 = new SingleSelectionModel<String>();
	            selectionModel3.addSelectionChangeHandler(new Handler() {

	                @Override
	                public void onSelectionChange(SelectionChangeEvent event) {
	                    if (selectionModel3.getSelectedObject() != null) {
	                    	app.deselectOthers(selectionModel3);
	                    }
	                }
	            });
	            app.addSelectionModel(selectionModel3);
	            return new DefaultNodeInfo<String>(firstLevelDataProvider, new TextCell(new SafeHtmlRenderer<String>() {

					@Override
					public SafeHtml render(String object) {
	                    SafeHtmlBuilder builder = new SafeHtmlBuilder();
	                    render(object, builder);
	                    return builder.toSafeHtml();
					}

					@Override
					public void render(String object, SafeHtmlBuilder builder) {
	                    String html = AbstractImagePrototype.create(OtherSharedTreeView.images.folderYellow()).getHTML();
	                    builder.appendHtmlConstant(html);
	                    builder.append(OtherSharedTreeView.Templates.INSTANCE.nameSpan(object));
					}
				}), selectionModel3, null);
        	}
        	else {
        		return new DefaultNodeInfo<Folder>(new ListDataProvider<Folder>(), folderCell, selectionModel, null);
        	}
        }
        else {
            final Folder f = (Folder) value;
            if (dataProviderMap.get(f) == null) {
                dataProviderMap.put(f, new ListDataProvider<Folder>());
            }
            final ListDataProvider<Folder> dataProvider = dataProviderMap.get(f);
            fetchFolder(f, dataProvider, false);
            return new DefaultNodeInfo<Folder>(dataProvider, folderCell, selectionModel, null);
        }
    }

    private void fetchSharingUsers(final ListDataProvider<String> dataProvider) {
        GetRequest<SharingUsers> getSharingUsers = new GetRequest<SharingUsers>(SharingUsers.class, app.getApiPath(), "", "?format=json") {
            @Override
            public void onSuccess(final SharingUsers result) {
                dataProvider.getList().clear();
                dataProvider.getList().addAll(result.getUsers());
//                Iterator<Folder> iter = result.getSubfolders().iterator();
//                fetchFolder(iter, new Command() {
//                    @Override
//                    public void execute() {
//                        dataProvider.getList().clear();
//                        dataProvider.getList().addAll(result.getSubfolders());
//                    }
//                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
                if (t instanceof RestException)
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching folder: " + t.getMessage());
            }
        };
        getSharingUsers.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getSharingUsers);
	}

    private void fetchSharedFolders(final ListDataProvider<Folder> dataProvider) {
    	Folder pithos = new Folder(Pithos.HOME_CONTAINER);
    	pithos.setContainer(Pithos.HOME_CONTAINER);
        String path = "/" + pithos.getContainer()  + "?format=json&shared=";
        GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), app.getUsername(), path, pithos) {
            @Override
            public void onSuccess(final Folder result) {
//                if (showfiles)
//                    app.showFiles(result);
                Iterator<Folder> iter = result.getSubfolders().iterator();
                fetchFolder(iter, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(result.getSubfolders());
//                        app.getMySharedTreeView().updateChildren(f);
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
                if (t instanceof RestException)
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching folder: " + t.getMessage());
            }
        };
        getFolder.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getFolder);
	}

	@Override
    public boolean isLeaf(Object o) {
        if (o instanceof Folder) {
            Folder f = (Folder) o;
            return f.getSubfolders().isEmpty();
        }
        return false;
    }

    protected void fetchFolder(final Iterator<Folder> iter, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();

            String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
            GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), app.getUsername(), path, f) {
                @Override
                public void onSuccess(Folder result) {
                    fetchFolder(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("Error getting folder", t);
                    if (t instanceof RestException)
                        app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                    else
                        app.displayError("System error fetching folder: " + t.getMessage());
                }
            };
            getFolder.setHeader("X-Auth-Token", app.getToken());
            Scheduler.get().scheduleDeferred(getFolder);
        }
        else if (callback != null)
            callback.execute();
    }

    public Folder getSelection() {
        return selectionModel.getSelectedObject();
    }

    public void updateFolder(Folder folder, boolean showfiles) {
        if (dataProviderMap.get(folder) == null) {
            dataProviderMap.put(folder, new ListDataProvider<Folder>());
        }
        final ListDataProvider<Folder> dataProvider = dataProviderMap.get(folder);
        fetchFolder(folder, dataProvider, showfiles);
    }

    public void fetchFolder(final Folder f, final ListDataProvider<Folder> dataProvider, final boolean showfiles) {
        String path = "/" + f.getContainer() + "?format=json&delimiter=/&prefix=" + f.getPrefix();
        GetRequest<Folder> getFolder = new GetRequest<Folder>(Folder.class, app.getApiPath(), app.getUsername(), path, f) {
            @Override
            public void onSuccess(final Folder result) {
                if (showfiles)
                    app.showFiles(result);
                Iterator<Folder> iter = result.getSubfolders().iterator();
                fetchFolder(iter, new Command() {
                    @Override
                    public void execute() {
                        dataProvider.getList().clear();
                        dataProvider.getList().addAll(result.getSubfolders());
                        app.getOtherSharedTreeView().updateChildren(f);
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("Error getting folder", t);
                if (t instanceof RestException)
                    app.displayError("Error getting folder: " + ((RestException) t).getHttpStatusText());
                else
                    app.displayError("System error fetching folder: " + t.getMessage());
            }
        };
        getFolder.setHeader("X-Auth-Token", app.getToken());
        Scheduler.get().scheduleDeferred(getFolder);
    }
}