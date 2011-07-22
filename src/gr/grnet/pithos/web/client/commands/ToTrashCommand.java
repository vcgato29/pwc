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
package gr.grnet.pithos.web.client.commands;

import com.google.gwt.core.client.Scheduler;
import gr.grnet.pithos.web.client.Pithos;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.Resource;
import gr.grnet.pithos.web.client.rest.PostRequest;
import gr.grnet.pithos.web.client.rest.RestException;

import java.util.Iterator;
import java.util.List;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 *
 * Move file or folder to trash.
 *
 *
 */
public class ToTrashCommand implements Command{
	private PopupPanel containerPanel;
    private Pithos app;
    private Object resource;

	public ToTrashCommand(Pithos _app, PopupPanel _containerPanel, Object _resource){
		containerPanel = _containerPanel;
        app = _app;
        resource = _resource;
	}

	@Override
	public void execute() {
        if (containerPanel != null)
    		containerPanel.hide();
        if (resource instanceof List) {
            Iterator<File> iter = ((List<File>) resource).iterator();
            trashFiles(iter, new Command() {
                @Override
                public void execute() {
                    app.get().updateFolder(((List<File>) resource).get(0).getParent());
                }
            });
        }
        else if (resource instanceof Folder) {
            final Folder toBeTrashed = (Folder) resource;
            trashFolder(toBeTrashed, new Command() {
                @Override
                public void execute() {
                    app.updateFolder(toBeTrashed.getParent());
                }
            });

        }
	}

    private void trashFolder(final Folder f, final Command callback) {
        String path = f.getUri() + "?update=";
        PostRequest trashFolder = new PostRequest(app.getApiPath(), app.getUsername(), path) {
            @Override
            public void onSuccess(Resource result) {
                Iterator<File> iter = f.getFiles().iterator();
                trashFiles(iter, new Command() {
                    @Override
                    public void execute() {
                        Iterator<Folder> iterf = f.getSubfolders().iterator();
                        trashSubfolders(iterf, new Command() {
                            @Override
                            public void execute() {
                                callback.execute();
                            }
                        });
                    }
                });
            }

            @Override
            public void onError(Throwable t) {
                GWT.log("", t);
                if (t instanceof RestException) {
                    app.displayError("Unable to create folder:" + ((RestException) t).getHttpStatusText());
                }
                else
                    app.displayError("System error creating folder:" + t.getMessage());
            }
        };
        trashFolder.setHeader("X-Auth-Token", app.getToken());
        trashFolder.setHeader("X-Object-Meta-Trash", "true");
        Scheduler.get().scheduleDeferred(trashFolder);
    }

    private void trashFiles(final Iterator<File> iter, final Command callback) {
        if (iter.hasNext()) {
            File file = iter.next();
            String path = file.getUri() + "?update=";
            PostRequest trashFile = new PostRequest(app.getApiPath(), app.getUsername(), path) {
                @Override
                public void onSuccess(Resource result) {
                    trashFiles(iter, callback);
                }

                @Override
                public void onError(Throwable t) {
                    GWT.log("", t);
                    if (t instanceof RestException) {
                        Pithos.get().displayError("Unable to copy file: " + ((RestException) t).getHttpStatusText());
                    }
                    else
                        Pithos.get().displayError("System error unable to copy file: "+t.getMessage());
                }
            };
            trashFile.setHeader("X-Auth-Token", app.getToken());
            trashFile.setHeader("X-Object-Meta-Trash", "true");
            Scheduler.get().scheduleDeferred(trashFile);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }

    private void trashSubfolders(final Iterator<Folder> iter, final Command callback) {
        if (iter.hasNext()) {
            final Folder f = iter.next();
            trashFolder(f, callback);
        }
        else  if (callback != null) {
            callback.execute();
        }
    }
}
