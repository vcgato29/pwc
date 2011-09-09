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

package gr.grnet.pithos.web.client.foldertree;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

public class Folder extends Resource {
    /*
     * The name of the folder. If the folder is a container this is its name. If it is a virtual folder this is the
     * last part of its path
     */
    private String name = null;

    private Date lastModified = null;

    private long bytesUsed = 0;

    private Folder parent = null;
    
    private Set<Folder> subfolders = new LinkedHashSet<Folder>();
    /*
     * The name of the container that this folder belongs to. If this folder is container, this field equals name
     */
    private String container = null;

    /*
     * This is the full path of the folder (prefix is a misnomer but it was named so because this is used as a prefix=
     * parameter in the request that fetches its children). If the folder is a cointainer this is empty string
     */
    private String prefix = "";

    private Set<File> files = new LinkedHashSet<File>();

    private boolean inTrash = false;

    /*
     * Flag that indicates that this folder is the Trash
     */
    private boolean trash = false;

    private Set<String> tags = new LinkedHashSet<String>();

    private String owner;

    private Map<String, Boolean[]> permissions = new HashMap<String, Boolean[]>();

    private String inheritedPermissionsFrom;

    public Folder() {};

    public Folder(String name) {
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public long getBytesUsed() {
        return bytesUsed;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Set<Folder> getSubfolders() {
        return subfolders;
    }

    public void setSubfolders(Set<Folder> subfolders) {
        this.subfolders = subfolders;
    }

    public String getContainer() {
        return container;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    private void parsePermissions(String rawPermissions) {
        String[] readwrite = rawPermissions.split(";");
        for (String s : readwrite) {
            String[] part = s.split("=");
            String perm = part[0].trim();
            String[] users = part[1].split(",");
            for (String u : users) {
                String user = u.trim();
                Boolean[] userPerm = permissions.get(u);
                if (userPerm == null) {
                    userPerm = new Boolean[2];
                    permissions.put(user, userPerm);
                }
                if (perm.equals("read")) {
                    userPerm[0] = Boolean.TRUE;
                }
                else if (perm.equals("write")) {
                    userPerm[1] = Boolean.TRUE;
                }
            }
        }
    }

    public void populate(String _owner, Response response) {
        this.owner = _owner;
        String header = response.getHeader("Last-Modified");
        if (header != null)
            lastModified = DateTimeFormat.getFormat(PredefinedFormat.RFC_2822).parse(header);

        header = response.getHeader("X-Container-Bytes-Used");
        if (header != null)
            bytesUsed = Long.valueOf(header);

        header = response.getHeader("X-Object-Meta-Trash");
        if (header != null && header.equals("true"))
            inTrash = true;

        header = response.getHeader("X-Container-Object-Meta");
        if (header != null && header.length() > 0) {
            for (String t : header.split(",")) {
                tags.add(t.toLowerCase().trim());
            }
        }

        inheritedPermissionsFrom = response.getHeader("X-Object-Shared-By");
        String rawPermissions = response.getHeader("X-Object-Sharing");
        if (rawPermissions != null)
            parsePermissions(rawPermissions);

        subfolders.clear(); //This is necessary in case we update a pre-existing Folder so that stale subfolders won't show up
        files.clear();
        JSONValue json = JSONParser.parseStrict(response.getText());
        JSONArray array = json.isArray();
        if (array != null) {
            for (int i=0; i<array.size(); i++) {
                JSONObject o = array.get(i).isObject();
                if (o != null) {
                    String contentType = unmarshallString(o, "content_type");
                    if (o.containsKey("subdir") || (contentType != null && (contentType.startsWith("application/directory") || contentType.startsWith("application/folder")))) {
                        Folder f = new Folder();
                        f.populate(this, o, _owner, container);
                        subfolders.add(f);
                    }
                    else if (!(o.containsKey("x_object_meta_trash") && o.get("x_object_meta_trash").isString().stringValue().equals("true"))) {
                        File file = new File();
                        file.populate(this, o, _owner, container);
                        files.add(file);
                    }
                }
            }
            //This step is necessary to remove the trashed folders. Trashed folders are added initially because we need to
            //avoid having in the list the virtual folders of the form {"subdir":"folder1"} which have no indication of thrash
            Iterator<Folder> iter = subfolders.iterator();
            while (iter.hasNext()) {
                Folder f = iter.next();
                if (f.isInTrash())
                    iter.remove();
            }
        }
    }

    public void populate(Folder _parent, JSONObject o, String _owner, String aContainer) {
        this.parent = _parent;
        String path = null;
        if (o.containsKey("subdir")) {
            path = unmarshallString(o, "subdir");
        }
        else {
            path = unmarshallString(o, "name");
            lastModified = unmarshallDate(o, "last_modified");
        }
        if (path.endsWith("/"))
            path = path.substring(0, path.length() - 1);
        if (path.contains("/"))
            name = path.substring(path.lastIndexOf("/") + 1, path.length()); //strip the prefix
        else
            name = path;
        if (aContainer != null) {
            container = aContainer;
            prefix = path;
        }
        else {
            container = name;
            prefix = "";
        }
        this.owner = _owner;
        if (o.containsKey("x_object_meta_trash") && o.get("x_object_meta_trash").isString().stringValue().equals("true"))
            inTrash = true;

        inheritedPermissionsFrom = unmarshallString(o, "x_object_shared_by");
        String rawPermissions = unmarshallString(o, "x_object_sharing");
        if (rawPermissions != null)
            parsePermissions(rawPermissions);
    }

    public static Folder createFromResponse(String owner, Response response, Folder result) {
        Folder f = null;
        if (result == null)
            f = new Folder();
        else
            f = result;

        f.populate(owner, response);
        return f;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Folder) {
            Folder o = (Folder) other;
            return getUri().equals(o.getUri());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    public Set<File> getFiles() {
        return files;
    }

    public Folder getParent() {
        return parent;
    }

    public String getUri() {
        return "/" + container + (prefix.length() == 0 ? "" : "/" + prefix);
    }

    public boolean isInTrash() {
        return inTrash;
    }

    public boolean isContainer() {
        return parent == null;
    }

    public boolean isTrash() {
        return trash;
    }

    public void setTrash(boolean trash) {
        this.trash = trash;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getInheritedPermissionsFrom() {
        return inheritedPermissionsFrom;
    }

    public Map<String, Boolean[]> getPermissions() {
        return permissions;
    }

    public String getOwner() {
        return owner;
    }

    public boolean existChildrenPermissions() {
        for (File f : files)
            if (!f.getPermissions().isEmpty() && f.getInheritedPermissionsFrom() == null)
                return true;

        for (Folder fo : subfolders)
            if ((!fo.getPermissions().isEmpty() && fo.getInheritedPermissionsFrom() == null) || fo.existChildrenPermissions())
                return true;
        return false;
    }

	public boolean isShared() {
		return !permissions.isEmpty();
	}
}
