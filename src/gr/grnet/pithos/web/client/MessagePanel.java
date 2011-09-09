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

import gr.grnet.pithos.web.client.animation.FadeIn;
import gr.grnet.pithos.web.client.animation.FadeOut;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * A panel that displays various system messages.
 */
public class MessagePanel extends Composite {
	/**
	 * An image bundle for this widget's images.
	 */
	public interface Images extends ClientBundle {
		@Source("gr/grnet/pithos/resources/messagebox_info.png")
		ImageResource info();

		@Source("gr/grnet/pithos/resources/messagebox_warning.png")
		ImageResource warn();

		@Source("gr/grnet/pithos/resources/messagebox_critical.png")
		ImageResource error();
	}

	/**
	 * The widget's images.
	 */
	public static Images images;

	/**
	 * The system message to be displayed.
	 */
	private HTML message = new HTML("&nbsp;");

	/**
	 * A link to clear the displayed message.
	 */
	private HTML clearMessageLink = new HTML("<a class='pithos-clearMessage' href='javascript:;'>Clear</a>");

	/**
	 * The panel that contains the messages.
	 */
	private HorizontalPanel inner = new HorizontalPanel();

	/**
	 * The panel that enables special effects for this widget.
	 */
	protected SimplePanel simplePanel = new SimplePanel();

	/**
	 * The widget's constructor.
	 *
	 * @param newImages a bundle that provides the images for this widget
	 */
	public MessagePanel(final Images newImages) {
		images = newImages;
		buildPanel();
		simplePanel.setStyleName("effectPanel");
		inner.setStyleName("effectPanel-inner");
		DOM.setStyleAttribute(simplePanel.getElement(), "zoom", "1");
		simplePanel.add(inner);
		initWidget(simplePanel);
	}

	/**
	 * Build the panel that contains the icon, the message and the 'clear' link.
	 */
	private void buildPanel() {
		inner.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		inner.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
		inner.setSpacing(4);
		inner.add(message);
		inner.add(clearMessageLink);
		inner.setCellVerticalAlignment(message, HasVerticalAlignment.ALIGN_MIDDLE);
		clearMessageLink.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){
					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});
	}

	/**
	 * Display an error message.
	 *
	 * @param msg the message to display
	 */
	public void displayError(final String msg) {
		GWT.log(msg, null);
		message = new HTML("<table class='pithos-errorMessage'><tr><td>" + AbstractImagePrototype.create(images.error()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});
		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Display a warning message.
	 *
	 * @param msg the message to display
	 */
	public void displayWarning(final String msg) {
		message = new HTML("<table class='pithos-warnMessage'><tr><td>" + AbstractImagePrototype.create(images.warn()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});

		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Display an informational message.
	 *
	 * @param msg the message to display
	 */
	public void displayInformation(final String msg) {
		message = new HTML("<table class='pithos-infoMessage'><tr><td>" + AbstractImagePrototype.create(images.info()).getHTML() + "</td><td>" + msg + "</td></tr></table>");
		message.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(@SuppressWarnings("unused") ClickEvent event) {
				FadeOut anim = new FadeOut(simplePanel){

					@Override
					protected void onComplete() {
						super.onComplete();
						hideMessage();
					}
				};
				anim.run(500);
			}
		});

		buildPanel();
		setVisible(true);
		FadeIn anim = new FadeIn(simplePanel);
		anim.run(500);
	}

	/**
	 * Clear the displayed message and hide the panel.
	 */
	public void hideMessage() {
		inner.clear();
		message = new HTML("&nbsp;");
		this.setVisible(false);
	}

}
