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

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;

/**
 * The top panel, which contains the menu bar icons and the user name.
 */
public class TopPanel extends Composite {

	/**
	 * A constant that denotes the completion of an IncrementalCommand.
	 */
	public static final boolean DONE = false;

    Pithos app;

	/**
	 * An image bundle for this widgets images.
	 */
	public interface Images extends FilePermissionsDialog.Images {

		@Source("gr/grnet/pithos/resources/pithos2-logo.png")
		ImageResource pithosLogo();
		
		@Source("gr/grnet/pithos/resources/desc.png")
		ImageResource downArrow();
	}

	/**
	 * The constructor for the top panel.
	 *
	 * @param images the supplied images
	 */
	public TopPanel(Pithos _app, final Images images) {
        this.app = _app;
		HorizontalPanel outer = new HorizontalPanel();
		outer.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);

		outer.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		outer.setStyleName("pithos-topPanel");

		HorizontalPanel inner = new HorizontalPanel();
		inner.setWidth("75%");
		inner.setVerticalAlignment(HasVerticalAlignment.ALIGN_BOTTOM);
		
		HTML logos = new HTML("<table><tr><td><a href='/'>" + AbstractImagePrototype.create(images.pithosLogo()).getHTML() + "</a></td></tr></table>");
		logos.addStyleName("pithos-logo");
		inner.add(logos);

        MenuBar username = new MenuBar();
        username.setStyleName("pithos-usernameMenu");
        
        MenuBar userItemMenu = new MenuBar(true);
        userItemMenu.addStyleName("pithos-userItemMenu");
        userItemMenu.addItem(new MenuItem("invite friends...", new Command() {
			
			@Override
			public void execute() {
				Window.open("/im/invite", "", "");
			}
		}));
        userItemMenu.addItem(new MenuItem("send feedback...", new Command() {
			
			@Override
			public void execute() {
				Window.open("/im/feedback", "", "");
			}
		}));
        userItemMenu.addItem(new MenuItem("profile...", new Command() {
			
			@Override
			public void execute() {
				Window.open("/im/profile", "", "");
			}
		}));
        userItemMenu.addItem(new MenuItem("logout", new Command() {
			
			@Override
			public void execute() {
				app.logoff();
			}
		}));

        MenuItem userItem = new MenuItem(_app.getUsername(), userItemMenu);
        userItem.addStyleName("pithos-usernameMenuItem");
        username.addItem(userItem);
        username.addSeparator();
        
        MenuItem langItem = new MenuItem("en", (Command) null);
        langItem.addStyleName("pithos-langMenuItem");
        username.addItem(langItem);
        
        inner.add(username);
        inner.setCellHorizontalAlignment(username, HasHorizontalAlignment.ALIGN_RIGHT);
        
        outer.add(inner);
        outer.setCellHorizontalAlignment(inner, HasHorizontalAlignment.ALIGN_CENTER);
        outer.setCellVerticalAlignment(inner, HasVerticalAlignment.ALIGN_BOTTOM);
		initWidget(outer);
	}
}
