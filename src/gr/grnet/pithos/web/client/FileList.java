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

import gr.grnet.pithos.web.client.foldertree.File;
import gr.grnet.pithos.web.client.foldertree.Folder;
import gr.grnet.pithos.web.client.foldertree.FolderTreeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A composite that displays the list of files in a particular folder.
 */
public class FileList extends Composite {

	ListDataProvider<File> provider = new ListDataProvider<File>();

    /**
       * The styles applied to the table.
       */
    interface TableStyle extends CellTable.Style {
    }

	interface TableResources extends CellTable.Resources {
	    @Override
		@Source({CellTable.Style.DEFAULT_CSS, "GssCellTable.css"})
	    TableStyle cellTableStyle();
	}
	
	static interface Templates extends SafeHtmlTemplates {
	    Templates INSTANCE = GWT.create(Templates.class);

	    @Template("<div id='dragHelper' style='border:1px solid black; background-color:#ffffff; color:black; width:150px;z-index:100'></div>")
	    SafeHtml outerHelper();

        @Template("<span id='{0}'>{0}</span>")
        public SafeHtml filenameSpan(String filename);

        @Template("<a href='{0}' title='{1}' rel='lytebox[mnf]' onclick='myLytebox.start(this, false, false); return false;'>(view)</a>")
        public SafeHtml viewLink(String link, String title);

        @Template("<table><tr><td rowspan='3'>{0}</td><td style='font-size:95%;' id='{1}'>{1}</td></tr><tr><td>{2}</td></tr></table>")
        public SafeHtml rendelContactCell(String imageHtml, String name, String fileSize);

        @Template("<span id='{0}' class='{1}'>{2}</span>")
        public SafeHtml spanWithIdAndClass(String id, String cssClass, String content);
	}

	protected final DateTimeFormat formatter = DateTimeFormat.getFormat("d/M/yyyy h:mm a");

	/**
	 * Specifies that the images available for this composite will be the ones
	 * available in FileContextMenu.
	 */
	public interface Images extends FolderTreeView.Images {

		@Source("gr/grnet/pithos/resources/blank.gif")
		ImageResource blank();

		@Source("gr/grnet/pithos/resources/asc.png")
		ImageResource asc();

		@Source("gr/grnet/pithos/resources/desc.png")
		ImageResource desc();

		@Source("gr/grnet/pithos/resources/mimetypes/document_shared.png")
		ImageResource documentShared();

		@Source("gr/grnet/pithos/resources/mimetypes/kcmfontinst.png")
		ImageResource wordprocessor();

		@Source("gr/grnet/pithos/resources/mimetypes/log.png")
		ImageResource spreadsheet();

		@Source("gr/grnet/pithos/resources/mimetypes/kpresenter_kpr.png")
		ImageResource presentation();

		@Source("gr/grnet/pithos/resources/mimetypes/acroread.png")
		ImageResource pdf();

		@Source("gr/grnet/pithos/resources/mimetypes/image.png")
		ImageResource image();

		@Source("gr/grnet/pithos/resources/mimetypes/video2.png")
		ImageResource video();

		@Source("gr/grnet/pithos/resources/mimetypes/knotify.png")
		ImageResource audio();

		@Source("gr/grnet/pithos/resources/mimetypes/html.png")
		ImageResource html();

		@Source("gr/grnet/pithos/resources/mimetypes/txt.png")
		ImageResource txt();

		@Source("gr/grnet/pithos/resources/mimetypes/ark2.png")
		ImageResource zip();

		@Source("gr/grnet/pithos/resources/mimetypes/kcmfontinst_shared.png")
		ImageResource wordprocessorShared();

		@Source("gr/grnet/pithos/resources/mimetypes/log_shared.png")
		ImageResource spreadsheetShared();

		@Source("gr/grnet/pithos/resources/mimetypes/kpresenter_kpr_shared.png")
		ImageResource presentationShared();

		@Source("gr/grnet/pithos/resources/mimetypes/acroread_shared.png")
		ImageResource pdfShared();

		@Source("gr/grnet/pithos/resources/mimetypes/image_shared.png")
		ImageResource imageShared();

		@Source("gr/grnet/pithos/resources/mimetypes/video2_shared.png")
		ImageResource videoShared();

		@Source("gr/grnet/pithos/resources/mimetypes/knotify_shared.png")
		ImageResource audioShared();

		@Source("gr/grnet/pithos/resources/mimetypes/html_shared.png")
		ImageResource htmlShared();

		@Source("gr/grnet/pithos/resources/mimetypes/txt_shared.png")
		ImageResource txtShared();

		@Source("gr/grnet/pithos/resources/mimetypes/ark2_shared.png")
		ImageResource zipShared();

	}
	
	/**
	 * The number of files in this folder.
	 */
	int folderFileCount;

	/**
	 * Total folder size
	 */
	long folderTotalSize;

	/**
	 * A cache of the files in the list.
	 */
	private List<File> files;

	/**
	 * The widget's image bundle.
	 */
	protected final Images images;
	
	protected CellTable<File> celltable;

	private final MultiSelectionModel<File> selectionModel;

	protected final List<SortableHeader> allHeaders = new ArrayList<SortableHeader>();

	SortableHeader nameHeader;

    FolderTreeView treeView;

    protected Pithos app;

    /**
	 * Construct the file list widget. This entails setting up the widget
	 * layout, fetching the number of files in the current folder from the
	 * server and filling the local file cache of displayed files with data from
	 * the server, as well.
	 *
	 * @param _images
	 */
	public FileList(final Pithos _app, Images _images, FolderTreeView _treeView) {
        app = _app;
		images = _images;
        this.treeView = _treeView;

        CellTable.Resources resources = GWT.create(TableResources.class);

        ProvidesKey<File> keyProvider = new ProvidesKey<File>(){

			@Override
			public Object getKey(File item) {
				return item.getUri();
			}
		};

		celltable = new CellTable<File>(10, resources, keyProvider);
        celltable.setWidth("100%");
        celltable.setStyleName("pithos-List");

		Column<File, ImageResource> status = new Column<File, ImageResource>(new ImageResourceCell() {
		    @Override
	        public boolean handlesSelection() {
	            return false;
	        }
		})
        {
	         @Override
	         public ImageResource getValue(File entity) {
	             return getFileIcon(entity);
	         }
	    };
	    celltable.addColumn(status,"");

        final Column<File,SafeHtml> nameColumn = new Column<File,SafeHtml>(new SafeHtmlCell()) {

			@Override
			public SafeHtml getValue(File object) {
				SafeHtmlBuilder sb = new SafeHtmlBuilder();
                sb.append(Templates.INSTANCE.filenameSpan(object.getName()));
				if (object.getContentType().endsWith("png") || object.getContentType().endsWith("gif") || object.getContentType().endsWith("jpeg")) {
        			sb.appendHtmlConstant("&nbsp;")
                      .append(Templates.INSTANCE.viewLink(app.getApiPath() + object.getOwner() + object.getUri() + "?X-Auth-Token=" + app.getToken(), object.getName()));
				}
				
				return sb.toSafeHtml();
			}
			
		};
        celltable.addColumn(nameColumn, nameHeader = new SortableHeader("Name"));
		allHeaders.add(nameHeader);
		nameHeader.setUpdater(new FileValueUpdater(nameHeader, "name"));

		celltable.redrawHeaders();
		
        Column<File,String> aColumn = new Column<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				// TODO Auto-generated method stub
				return object.getSizeAsString();
			}
		};
        SortableHeader aheader = new SortableHeader("Size");
        celltable.addColumn(aColumn, aheader);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "size"));

        aColumn = new Column<File,String>(new TextCell()) {
			@Override
			public String getValue(File object) {
				return object.getLastModified() != null ? formatter.format(object.getLastModified()) : "";
			}
		};
        aheader = new SortableHeader("Last Modified");
		celltable.addColumn(aColumn, aheader);
		allHeaders.add(aheader);
		aheader.setUpdater(new FileValueUpdater(aheader, "date"));
	       
		provider.addDataDisplay(celltable);

		VerticalPanel vp = new VerticalPanel();
		vp.setWidth("100%");

        vp.add(celltable);

		vp.setCellWidth(celltable, "100%");
        vp.addHandler(new ContextMenuHandler() {
            @Override
            public void onContextMenu(ContextMenuEvent event) {
            	TreeView tree = app.getSelectedTree();
            	if (tree != null && (tree.equals(app.getFolderTreeView()) || tree.equals(app.getOtherSharedTreeView()))) {
	                Folder selectedFolder = app.getSelection();
	                FileContextMenu contextMenu = new FileContextMenu(app, images, tree, selectedFolder, getSelectedFiles());
	                int x = event.getNativeEvent().getClientX();
	                int y = event.getNativeEvent().getClientY();
	                contextMenu.setPopupPosition(x, y);
	                contextMenu.show();
            	}
            }
        }, ContextMenuEvent.getType());
		initWidget(vp);

		selectionModel = new MultiSelectionModel<File>(keyProvider);

		celltable.setSelectionModel(selectionModel, GSSSelectionEventManager.<File> createDefaultManager());
//		celltable.setPageSize(Pithos.VISIBLE_FILE_COUNT);
		
		sinkEvents(Event.ONCONTEXTMENU);
//		sinkEvents(Event.ONMOUSEUP);
//		sinkEvents(Event.ONMOUSEDOWN);
//		sinkEvents(Event.ONCLICK);
//		sinkEvents(Event.ONKEYDOWN);
//		sinkEvents(Event.ONDBLCLICK);
		Pithos.preventIESelection();
	}

	public List<File> getSelectedFiles() {
        return new ArrayList<File>(selectionModel.getSelectedSet());
	}
	
//	@Override
//	public void onBrowserEvent(Event event) {
//
//		if (files == null || files.size() == 0) {
//			if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
//				contextMenu = new FileContextMenu(images, false, true);
//                contextMenu.show();
//				event.preventDefault();
//				event.cancelBubble(true);
//			}
//			return;
//		}
//		if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() != 0) {
//			GWT.log("*****GOING TO SHOW CONTEXT MENU ****", null);
//			contextMenu =  new FileContextMenu(images, false, false);
//			contextMenu = contextMenu.onEvent(event);
//			event.cancelBubble(true);
//			event.preventDefault();
//		} else if (DOM.eventGetType(event) == Event.ONCONTEXTMENU && getSelectedFiles().size() == 0) {
//			contextMenu = new FileContextMenu(images, false, true);
//			contextMenu = contextMenu.onEmptyEvent(event);
//			event.cancelBubble(true);
//			event.preventDefault();
//		} else if (DOM.eventGetType(event) == Event.ONDBLCLICK)
//			if (getSelectedFiles().size() == 1) {
//				Pithos app = app;
//				File file = getSelectedFiles().get(0);
//				Window.open(file.getUri(), "_blank", "");
//				event.preventDefault();
//				return;
//			}
//		super.onBrowserEvent(event);
//	}

	/**
	 * Update the display of the file list.
	 */
	void update(@SuppressWarnings("unused") boolean sort) {
		showCellTable();
	}

	/**
	 * Return the proper icon based on the MIME type of the file.
	 *
	 * @param file
	 * @return the icon
	 */
	protected ImageResource getFileIcon(File file) {
		String mimetype = file.getContentType();
		boolean shared = file.isShared();
		if (mimetype == null)
			return shared ? images.documentShared() : images.document();
		mimetype = mimetype.toLowerCase();
		if (mimetype.startsWith("application/pdf"))
			return shared ? images.pdfShared() : images.pdf();
		else if (mimetype.endsWith("excel"))
			return shared ? images.spreadsheetShared() : images.spreadsheet();
		else if (mimetype.endsWith("msword"))
			return shared ? images.wordprocessorShared() : images.wordprocessor();
		else if (mimetype.endsWith("powerpoint"))
			return shared ? images.presentationShared() : images.presentation();
		else if (mimetype.startsWith("application/zip") ||
					mimetype.startsWith("application/gzip") ||
					mimetype.startsWith("application/x-gzip") ||
					mimetype.startsWith("application/x-tar") ||
					mimetype.startsWith("application/x-gtar"))
			return shared ? images.zipShared() : images.zip();
		else if (mimetype.startsWith("text/html"))
			return shared ? images.htmlShared() : images.html();
		else if (mimetype.startsWith("text/plain"))
			return shared ? images.txtShared() : images.txt();
		else if (mimetype.startsWith("image/"))
			return shared ? images.imageShared() : images.image();
		else if (mimetype.startsWith("video/"))
			return shared ? images.videoShared() : images.video();
		else if (mimetype.startsWith("audio/"))
			return shared ? images.audioShared() : images.audio();
		return shared ? images.documentShared() : images.document();
	}

	/**
	 * Fill the file cache with data.
	 */
	public void setFiles(final List<File> _files) {
		files = new ArrayList<File>();
    	for (File fres : _files)
			files.add(fres);
		Collections.sort(files, new Comparator<File>() {

			@Override
			public int compare(File arg0, File arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}

		});
		folderFileCount = files.size();
		
		nameHeader.setSorted(true);
		nameHeader.toggleReverseSort();
		for (SortableHeader otherHeader : allHeaders) {
	        if (otherHeader != nameHeader) {
	            otherHeader.setSorted(false);
	            otherHeader.setReverseSort(true);
	        }
	    }

        provider.setList(files);
        selectionModel.clear();
        app.showFolderStatistics(folderFileCount);
        celltable.setPageSize(folderFileCount);
	}

	/**
	 * Does the list contains the requested filename
	 *
	 * @param fileName
	 * @return true/false
	 */
	public boolean contains(String fileName) {
		for (int i = 0; i < files.size(); i++)
			if (files.get(i).getName().equals(fileName))
				return true;
		return false;
	}

	public void clearSelectedRows() {
		Iterator<File> it = selectionModel.getSelectedSet().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),false);
		}
	}
	
	/**
	 *
	 */
	public void selectAllRows() {
		Iterator<File> it = provider.getList().iterator();
		while(it.hasNext()){
			selectionModel.setSelected(it.next(),true);
		}
	}

	protected void sortFiles(final String sortingProperty, final boolean sortingType){
		Collections.sort(files, new Comparator<File>() {

            @Override
            public int compare(File arg0, File arg1) {
                    if (sortingType){
                            if (sortingProperty.equals("version")) {
                                    return arg0.getVersion() - arg1.getVersion();
                            } else if (sortingProperty.equals("owner")) {
                                    return arg0.getOwner().compareTo(arg1.getOwner());
                            } else if (sortingProperty.equals("date")) {
                            		if (arg0.getLastModified() != null && arg1.getLastModified() != null)
                            			return arg0.getLastModified().compareTo(arg1.getLastModified());
                            		return 0;
                            } else if (sortingProperty.equals("size")) {
                                    return (int) (arg0.getBytes() - arg1.getBytes());
                            } else if (sortingProperty.equals("name")) {
                                    return arg0.getName().compareTo(arg1.getName());
                            } else if (sortingProperty.equals("path")) {
                                    return arg0.getUri().compareTo(arg1.getUri());
                            } else {
                                    return arg0.getName().compareTo(arg1.getName());
                            }
                    }
                    else if (sortingProperty.equals("version")) {
                            
                            return arg1.getVersion() - arg0.getVersion();
                    } else if (sortingProperty.equals("owner")) {
                            
                            return arg1.getOwner().compareTo(arg0.getOwner());
                    } else if (sortingProperty.equals("date")) {
                            
                            return arg1.getLastModified().compareTo(arg0.getLastModified());
                    } else if (sortingProperty.equals("size")) {
                            return (int) (arg1.getBytes() - arg0.getBytes());
                    } else if (sortingProperty.equals("name")) {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    } else if (sortingProperty.equals("path")) {
                            
                            return arg1.getUri().compareTo(arg0.getUri());
                    } else {
                            
                            return arg1.getName().compareTo(arg0.getName());
                    }
            }

		});
	}
	
	final class FileValueUpdater implements ValueUpdater<String>{
		private String property;
		private SortableHeader header;
		/**
		 * 
		 */
		public FileValueUpdater(SortableHeader header,String property) {
			this.property=property;
			this.header=header;
		}
		@Override
		public void update(@SuppressWarnings("unused") String value) {
			header.setSorted(true);
			header.toggleReverseSort();

	        for (SortableHeader otherHeader : allHeaders) {
	          if (otherHeader != header) {
	            otherHeader.setSorted(false);
	            otherHeader.setReverseSort(true);
	          }
	        }
	        celltable.redrawHeaders();
	        sortFiles(property, header.getReverseSort());
	        FileList.this.update(true);			
		}
		
	}

	/**
	 * Shows the files in the cellTable 
     */
	private void showCellTable(){
		provider.setList(files);
		
		provider.refresh();
		
		//celltable.redraw();
		celltable.redrawHeaders();		
	}
}
