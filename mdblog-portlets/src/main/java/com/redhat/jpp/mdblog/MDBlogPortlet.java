/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.redhat.jpp.mdblog;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.ProcessAction;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.UnavailableException;

import org.gatein.api.PortalRequest;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.page.Page;

import com.petebevin.markdown.MarkdownProcessor;

public class MDBlogPortlet extends GenericPortlet {

	@Inject
	private MarkdownProcessor mdProcessor;
	
	@Inject
	private MDBlogService service;
	
	@Inject
	private SocialService socialService;
	
	@Inject
	private PortalRequest portalRequest;
	
	protected MDBlog findBlog(RenderRequest request) throws MDBlogException {
		String parent = getParentPath();
		String name = getName();
		return service.findBlog(parent, name);
	}
	
	protected void prepareView(RenderRequest request) throws MDBlogException {
		MDBlog blog = findBlog(request);
		if (blog == null)
			return;
		blog.setContent(mdProcessor.markdown(blog.getContent()));
		request.setAttribute("blog", blog);
	}
	
	protected void prepareEditView(RenderRequest request) throws MDBlogException {
		MDBlog blog = findBlog(request);
		
		if (blog == null)
			return;
		
		request.setAttribute("blog", blog);
		
		StringBuilder tags = new StringBuilder();
		for (String tag : blog.getTags()) {
			tags.append(tag);
			tags.append(", ");
		}
		System.out.println(blog.getTags().isEmpty() + ", " + tags.length());
		request.setAttribute("tags", (blog.getTags().isEmpty() ? "" : tags.substring(0, tags.length() - 2)));
	}
	
	@Override
	protected void doEdit(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		try {
			prepareEditView(request);
			
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/mdblog/blog_edit.jsp");
            prd.forward(request, response);
		} catch (MDBlogException e) {
			displayError("Unable to load blog", request, response);
		}
	}
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException, UnavailableException {
		MarkdownProcessor processor = new MarkdownProcessor();
		
		try {
			if (request.getAttribute("skipRender") != null) {
				return;
			}
			prepareView(request);
			
			Navigation navigation = portalRequest.getNavigation();
			PermissionsBean permissions = new PermissionsBean();
			if (request.getRemoteUser() != null) {
				permissions.setCanEdit(true);
				permissions.setCanCreate(true);
				
				Node node = navigation.getNode(portalRequest.getNodePath(), Nodes.visitChildren());
				if (node.getChildCount() == 0) {
					permissions.setCanDelete(true);
				}
			}
			
			Node node = navigation.getNode(portalRequest.getNodePath(), Nodes.visitChildren());
			List<NodeBean> nodes = new LinkedList<NodeBean>();
			for (int i = 0; i < node.getChildCount(); i++) {
				Node child = node.getChild(i);
				nodes.add(new NodeBean(portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId()) + child.getNodePath(), child.getDisplayName()));
			}
			
			request.setAttribute("nodes", nodes);
			
			request.setAttribute("permissions", permissions);
			PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/mdblog/blog.jsp");
            prd.forward(request, response);
		} catch (MDBlogException e) {
			displayError("Unable to load blog", request, response);
		}
	}
	
	protected void displayError(String message, RenderRequest request, RenderResponse response) throws PortletException, IOException {
		request.setAttribute("errorMessage", message);
        PortletRequestDispatcher prd = getPortletContext().getRequestDispatcher("/jsp/error/error.jsp");
        prd.forward(request, response);
	}
	
	@ProcessAction(name="delete")
	public void deleteAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
		String parentPath = getParentPath();
		String name = getName();
		
		Navigation navigation = portalRequest.getNavigation();
		Node parent = navigation.getNode(NodePath.fromString(parentPath), Nodes.visitChildren());
		parent.removeChild(name);
		navigation.saveNode(parent);
		service.delete(parentPath, name);
		request.setAttribute("skipRender", true);
		response.sendRedirect(portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId()) + parent.getNodePath());
	}
	
	@ProcessAction(name="add")
	public void addAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
		String name = request.getParameter("name");
		String title = request.getParameter("title");
		
		if (name == null || title == null || name.isEmpty() || title.isEmpty()) {
			return;
		}
		
		NodePath path = portalRequest.getNodePath();
		
		portalRequest.getPage();
		Page page = portalRequest.getPage();
		
		Navigation navigation = portalRequest.getNavigation();
		Node node = navigation.getNode(path, Nodes.visitChildren());
		Node child = node.addChild(name);
		child.setDisplayName(title);
		child.setPageId(page.getId());
		navigation.saveNode(node);
		
		MDBlog blog = new MDBlog();
		blog.setTitle(title);
		
		try {
			service.save(node.getNodePath().toString(), name, blog);
			request.setAttribute("skipRender", true);
			response.sendRedirect(portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId()) + child.getNodePath());
		} catch (MDBlogException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@ProcessAction(name="save")
	public void saveAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
		String parent = getParentPath();
		String name = getName();
		String title = request.getParameter("title");
		String content = request.getParameter("content");
		String tags = request.getParameter("tags");
		
		System.out.println("parent: " + parent);
		System.out.println("name: " + name);
		
		if (title == null || title.isEmpty()) {
			return;
		}
		
		MDBlog blog = service.findBlog(parent, name);
		boolean create = false;
		if (blog == null) {
			create = true;
			blog = new MDBlog();
		}
		
		blog.setTitle(title);
		blog.setContent(content);
		
		blog.getTags().clear();
		
		if (tags != null) {
			Matcher m = Pattern.compile(
			          "(\\w+\\s)?(\"[^\"]+\"|\\w+)(\\(\\w\\d(,\\w\\d)*\\))?").matcher(tags);
			while(m.find()) {
				String tag = m.group();
			    blog.getTags().add(tag);
			}
		}
		
		try {
			service.save(parent, name, blog);
			socialService.publishBlog(blog, create);
		} catch (MDBlogException e) {
			throw new PortletException("Unable to save blog", e);
		}
		
		response.setPortletMode(PortletMode.VIEW);
	}
	
	protected String getName() {
		return portalRequest.getNodePath().getLastSegment();
	}
	
	protected String getParentPath() {
		NodePath path = portalRequest.getNodePath();
		Navigation navigation = portalRequest.getNavigation();
		Node node = navigation.getNode(path);
		Node parentNode = node.getParent();
		return parentNode.getNodePath().toString();
	}
}
