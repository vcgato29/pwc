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

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.*;
import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.rest.HeadRequest;
import gr.grnet.pithos.web.client.rest.PostRequest;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;

import java.util.Map;

/**
 * UI for the "Share" command.
 */
public class FileShareDialog extends AbstractPropertiesDialog {
    private static final class PermissionsUncheckWarning extends AbstractPropertiesDialog {
        private PermissionsUncheckWarning(Pithos app) {
            super(app);
            final Anchor close = new Anchor("close");
            close.addStyleName("close");
            close.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });
            final String dialogText = "Info";
            setText(dialogText);
            setStyleName("pithos-DialogBox");

            final VerticalPanel panel = new VerticalPanel();
            panel.add(close);

            VerticalPanel inner = new VerticalPanel();
            inner.addStyleName("inner");

            inner.add(
                new InlineHTML(
                    "It seems you are already sharing this file with some users." +
                    "<br>" +
                    "Please remove all users from the sharing list, to be able to uncheck this option."
                )
            );

            final Button ok = new Button("OK", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    hide();
                }
            });

            ok.addStyleName("button");
            inner.add(ok);

            panel.add(inner);
            panel.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

            setWidget(panel);
        }

        @Override
        protected boolean accept() {
            return true;
        }
    }

    // For public sharing
	private final HorizontalPanel publicPathPanel = new HorizontalPanel();
	private final TextBox publicPathText = new TextBox();

    // For private sharing
    private final HorizontalPanel privatePathPanel = new HorizontalPanel();
    private final TextBox privatePathText = new TextBox();
    private PermissionsList permList;

    public interface PrivateSharingImages extends MessagePanel.Images {

        @Source("gr/grnet/pithos/resources/edit_user.png")
        ImageResource permUser();

        @Source("gr/grnet/pithos/resources/groups22.png")
        ImageResource permGroup();

        @Source("gr/grnet/pithos/resources/delete.gif")
        ImageResource delete();
    }

    private final File file;

    private final PrivateSharingImages privateSharingImages = GWT.create(PrivateSharingImages.class);

	/**
	 * The widget's constructor.
	 */
	public FileShareDialog(Pithos app, File file) {
        super(app);
        this.file = file;

		Anchor close = new Anchor("close");
		close.addStyleName("close");
		close.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		// Set the dialog's caption.
		setText(Const.TXT_SHARE_FILE);
		setGlassEnabled(true);
		setStyleName("pithos-DialogBox");

		// Outer contains inner and buttons.
		final VerticalPanel outer = new VerticalPanel();
		outer.add(close);
		final FocusPanel focusPanel = new FocusPanel(outer);
		// Inner contains generalPanel and permPanel.
		inner = new VerticalPanel();
		inner.addStyleName("inner");

        inner.add(createMainPanel());

        outer.add(inner);

		final Button ok = new Button("OK", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				closeDialog();
			}
		});
		ok.addStyleName("button");

        outer.add(ok);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);

        focusPanel.setFocus(true);
        setWidget(outer);
	}

    private boolean IamOwner() {
        return file.getOwnerID().equals(app.getUserID());
    }

    private void populatePublicSharingPanel(VerticalPanel publicSharingPanel) {
        if(IamOwner()) {
            final HorizontalPanel publicCheckPanel = new HorizontalPanel();
            publicCheckPanel.setSpacing(8);

            // Check box header
            final CheckBox publicCheckBox = new CheckBox();
            Label publicCheckTitle = new InlineHTML("<b>Public on the Internet</b>");
            Label publicCheckInfo = new Label("Anyone who has the public link can access. No sign-in required.", true);

            publicCheckBox.setValue(isFilePubliclyShared());
            publicCheckBox.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    final Boolean published;
                    if(publicCheckBox.getValue() != file.isPublished() && IamOwner()) {
                        published = publicCheckBox.getValue();
                    }
                    else {
                        published = Boolean.FALSE;
                    }

                    updateMetaDataForPublicSharing(published);
                }
            });

            publicCheckPanel.add(publicCheckBox);
            publicCheckPanel.add(publicCheckTitle);
            publicCheckPanel.add(publicCheckInfo);

            // Public Link
            publicPathPanel.setVisible(false);
            publicPathPanel.setWidth(Const.PERCENT_100);
            publicPathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
            publicPathPanel.add(new Label(Const.TXT_PUBLIC_LINK));
            publicPathPanel.setSpacing(8);
            publicPathPanel.addStyleName("pithos-TabPanelBottom");

            publicPathText.setWidth(Const.PERCENT_100);
            publicPathText.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    Pithos.enableIESelection();
                    ((TextBox) event.getSource()).selectAll();
                    Pithos.preventIESelection();
                }
            });

            publicPathText.setText(""); // check: showLinkForPublicSharing();
            publicPathText.setTitle("Use this link for sharing the file via e-mail, IM, etc. (crtl-C/cmd-C to copy to system clipboard)");
            publicPathText.setReadOnly(true);
            publicPathPanel.add(publicPathText);

            publicSharingPanel.add(publicCheckPanel);
            publicSharingPanel.add(publicPathPanel);

            Scheduler.get().scheduleDeferred(new Command() {
                @Override
                public void execute() {
                    showLinkForPublicSharing();
                }
            });
        }
    }

    private void populatePrivateSharingPanel(VerticalPanel privateSharingPanel) {
        final HorizontalPanel privateCheckPanel = new HorizontalPanel();
        final VerticalPanel privatePermPanel = new VerticalPanel();
        final HorizontalPanel permButtons = new HorizontalPanel();

        privateCheckPanel.setSpacing(8);
        privatePermPanel.setVisible(isFilePrivatelyShared());
        permButtons.setSpacing(8);

        // Check box header
        final CheckBox privateCheckBox = new CheckBox();
        final Label privateCheckTitle = new  InlineHTML("<b>Private sharing</b>");
        final Label privateCheckInfo = new Label("Only people explicitly granted permission can access. Sign-in required.", true);

        privateCheckBox.setValue(isFilePrivatelyShared());
        privateCheckBox.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // This is the value *after* the click is applied :)
                boolean userCheckedIt = privateCheckBox.getValue();
                boolean userUncheckedIt = !userCheckedIt;

                if(isFilePrivatelyShared()) {
                    // ignore the click, set it always to "checked"
                    privateCheckBox.setValue(true);
                    // show permissions
                    privatePermPanel.setVisible(true);

                    // Refs #3593
                    if(userUncheckedIt) {
                        new PermissionsUncheckWarning(app).center();
                    }
                }
                else {
                    privatePermPanel.setVisible(userCheckedIt);
                }
            }
        });

        privateCheckPanel.add(privateCheckBox);
        privateCheckPanel.add(privateCheckTitle);
        privateCheckPanel.add(privateCheckInfo);

        // Permission list
        permList = new PermissionsList(app, privateSharingImages, file.getPermissions(), file.getOwnerID(), false, new Command() {
            @Override
            public void execute() {
                updateMetaDataForPrivateSharing();
            }
        });

        // Permission Add buttons
        Button addUser = new Button("Add User", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, true);
                dlg.center();
                permList.updatePermissionTable();
            }
        });
        addUser.addStyleName("button");
        permButtons.add(addUser);

        final boolean haveGroups = app.getAccount().getGroups().size() > 0;

        if(haveGroups) {
            Button addGroup = new Button("Add Group", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    PermissionsAddDialog dlg = new PermissionsAddDialog(app, app.getAccount().getGroups(), permList, false);
                    dlg.center();
                    permList.updatePermissionTable();
                }
            });
            addGroup.addStyleName("button");

            permButtons.add(addGroup);
            permButtons.setCellHorizontalAlignment(addGroup, HasHorizontalAlignment.ALIGN_CENTER);
        }

        final Button addEverybody = new Button("Add everybody", new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Pithos.LOG("Adding to Everybody");
                Pithos.LOG("");
                permList.addPermission("*", true, false);
                permList.updatePermissionTable();
            }
        });
        addEverybody.addStyleName("button");
        permButtons.add(addEverybody);

        privatePermPanel.add(permList);
        privatePermPanel.add(permButtons);

        // Private Link
        privatePathPanel.setVisible(false);
        privatePathPanel.setWidth(Const.PERCENT_100);
        privatePathPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
        privatePathPanel.add(new Label(Const.TXT_PRIVATE_LINK));
        privatePathPanel.setSpacing(8);
        privatePathPanel.addStyleName("pithos-TabPanelBottom");

        privatePathText.setWidth(Const.PERCENT_100);
        privatePathText.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Pithos.enableIESelection();
                ((TextBox) event.getSource()).selectAll();
                Pithos.preventIESelection();
            }
        });

        privatePathText.setText(""); // check: showLinkForPrivateSharing();
        privatePathText.setTitle("Use this link for sharing the file via e-mail, IM, etc. (crtl-C/cmd-C to copy to system clipboard)");
        privatePathText.setWidth(Const.PERCENT_100);
        privatePathText.setReadOnly(true);
        privatePathPanel.add(privatePathText);

        Scheduler.get().scheduleDeferred(new Command() {
            @Override
            public void execute() {
                showLinkForPrivateSharing();
            }
        });

        privateSharingPanel.add(privateCheckPanel);
        privateSharingPanel.add(privatePermPanel);
        privateSharingPanel.add(privatePathPanel);
    }

    private Panel createMainPanel() {
        VerticalPanel panelAll = new VerticalPanel();
        VerticalPanel panelPublic = new VerticalPanel();
        VerticalPanel panelPrivate = new VerticalPanel();

        populatePrivateSharingPanel(panelPrivate);
        populatePublicSharingPanel(panelPublic);

        panelAll.add(panelPrivate);
        panelAll.add(new InlineHTML("<hr/>"));
        panelAll.add(panelPublic);

        return panelAll;
    }

    private boolean isFilePubliclyShared() {
        return file.isPublished();
    }

    private boolean isFilePrivatelyShared() {
        return file.isShared();
    }

    private void showLinkForPublicSharing() {
		if (isFilePubliclyShared()) {
            // Transitional: When the server returns the full URL in X-Object-Public,
            //               just use it.
            final String filePublicURI = file.getPublicUri();
            final String shownPublicURI;
            if(filePublicURI.toLowerCase().startsWith("http")) {
                shownPublicURI = filePublicURI;
            }
            else {
                UrlBuilder b = Window.Location.createUrlBuilder();
                b.setPath(filePublicURI);
                shownPublicURI = b.buildString();
            }

			publicPathText.setText(shownPublicURI);
	        publicPathPanel.setVisible(true);
		}
		else {
			publicPathPanel.setVisible(false);
		}
    }

    private void showLinkForPrivateSharing() {
        if (isFilePrivatelyShared()) {
            final String fileViewURL = Pithos.getFileViewURL(file);
            final String shownViewURL;
            if(fileViewURL.toLowerCase().startsWith("http")) {
                shownViewURL = fileViewURL;
            }
            else {
                UrlBuilder b = Window.Location.createUrlBuilder();
                b.setPath(fileViewURL);
                shownViewURL = b.buildString();
            }

            privatePathText.setText(shownViewURL);
            privatePathPanel.setVisible(true);
        }
        else {
            privatePathPanel.setVisible(false);
        }
    }

	protected void updateMetaDataForPublicSharing(String api, String owner, final String path, final Boolean published) {
        if (published != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(Resource result) {
                	HeadRequest<File> headFile = new HeadRequest<File>(File.class, Pithos.getStorageAPIURL(), file.getOwnerID(), path, file) {

						@Override
						public void onSuccess(File _result) {
							showLinkForPublicSharing();
							if (!app.isMySharedSelected()) {
			                    app.updateFolder(file.getParent(), true, new Command() {
									
									@Override
									public void execute() {
										app.updateMySharedRoot();
									}
								}, true);
                            }
							else {
								app.updateSharedFolder(file.getParent(), true);
                            }
						}

						@Override
						public void onError(Throwable t) {
							app.setError(t);
		                    app.displayError("System error modifying file:" + t.getMessage());
						}

						@Override
						protected void onUnauthorized(Response response) {
							app.sessionExpired();
						}
					};
					headFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
					Scheduler.get().scheduleDeferred(headFile);
                }

                @Override
                public void onError(Throwable t) {
					app.setError(t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

				@Override
				protected void onUnauthorized(Response response) {
					app.sessionExpired();
				}
            };
            updateFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
            updateFile.setHeader(Const.X_OBJECT_PUBLIC, published.toString());
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (!app.isMySharedSelected())
            app.updateFolder(file.getParent(), true, new Command() {
				@Override
				public void execute() {
					if (file.isSharedOrPublished()) {
                        app.updateMySharedRoot();
                    }
				}
			}, true);
        else
        	app.updateSharedFolder(file.getParent(), true);
    }
    protected void updateMetaDataForPublicSharing(Boolean published) {
        updateMetaDataForPublicSharing(
            Pithos.getStorageAPIURL(),
            app.getUserID(),
            file.getUri() + Const.QUESTION_MARK_UPDATE_EQ,
            published
        );
    }

    protected void updateMetaDataForPrivateSharing(String api, String owner, final String path, final Map<String, Boolean[]> newPermissions) {
        if (newPermissions != null) {
            PostRequest updateFile = new PostRequest(api, owner, path) {
                @Override
                public void onSuccess(Resource result) {
                    HeadRequest<File> headFile = new HeadRequest<File>(File.class, Pithos.getStorageAPIURL(), file.getOwnerID(), path, file) {

                        @Override
                        public void onSuccess(File _result) {
                            showLinkForPrivateSharing();
                            if (!app.isMySharedSelected())
                                app.updateFolder(file.getParent(), true, new Command() {

                                    @Override
                                    public void execute() {
                                        app.updateMySharedRoot();
                                    }
                                }, true);
                            else
                                app.updateSharedFolder(file.getParent(), true);
                        }

                        @Override
                        public void onError(Throwable t) {
                            app.setError(t);
                            app.displayError("System error modifying file:" + t.getMessage());
                        }

                        @Override
                        protected void onUnauthorized(Response response) {
                            app.sessionExpired();
                        }
                    };
                    headFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());
                    Scheduler.get().scheduleDeferred(headFile);
                }

                @Override
                public void onError(Throwable t) {
                    app.setError(t);
                    app.displayError("System error modifying file:" + t.getMessage());
                }

                @Override
                protected void onUnauthorized(Response response) {
                    app.sessionExpired();
                }
            };
            updateFile.setHeader(Const.X_AUTH_TOKEN, app.getUserToken());

            String readPermHeader = Const.READ_EQ;
            String writePermHeader = Const.WRITE_EQ;
            for (String u : newPermissions.keySet()) {
                Boolean[] p = newPermissions.get(u);
                if (p[0] != null && p[0]) {
                    readPermHeader += u + Const.COMMA;
                }
                if (p[1] != null && p[1]) {
                    writePermHeader += u + Const.COMMA;
                }
            }
            if (readPermHeader.endsWith(Const.EQ)) {
                readPermHeader = "";
            }
            else if (readPermHeader.endsWith(Const.COMMA)) {
                readPermHeader = readPermHeader.substring(0, readPermHeader.length() - 1);
            }
            if (writePermHeader.endsWith(Const.EQ)) {
                writePermHeader = "";
            }
            else if (writePermHeader.endsWith(Const.COMMA)) {
                writePermHeader = writePermHeader.substring(0, writePermHeader.length() - 1);
            }
            String permHeader = readPermHeader +
                ((readPermHeader.length()  > 0 && writePermHeader.length() > 0) ?  Const.SEMI : "") + writePermHeader;
            if (permHeader.length() == 0) {
                permHeader=Const.TILDE;
            }
            else {
                permHeader = URL.encodePathSegment(permHeader);
            }
            updateFile.setHeader(Const.X_OBJECT_SHARING, permHeader);
            Scheduler.get().scheduleDeferred(updateFile);
        }
        else if (!app.isMySharedSelected()) {
            app.updateFolder(file.getParent(), true, new Command() {
                @Override
                public void execute() {
                    if (file.isSharedOrPublished())
                        app.updateMySharedRoot();
                }
            }, true);
        }
        else {
            app.updateSharedFolder(file.getParent(), true);
        }
    }
    protected void updateMetaDataForPrivateSharing() {
        updateMetaDataForPrivateSharing(
            Pithos.getStorageAPIURL(),
            app.getUserID(),
            file.getUri() + Const.QUESTION_MARK_UPDATE_EQ,
            permList.getPermissions()
        );
    }

    @Override
	protected void onPreviewNativeEvent(NativePreviewEvent preview) {
	    super.onPreviewNativeEvent(preview);

	    NativeEvent evt = preview.getNativeEvent();
	    if (evt.getType().equals(Const.EVENT_TYPE_KEYDOWN) &&
            evt.getKeyCode() == KeyCodes.KEY_ENTER) {

            closeDialog();
        }
	}

    @Override
    protected boolean accept() {
        return true;
    }
}
