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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MDBlogService {
	private String rootDir = System.getProperty("java.io.tmpdir") + File.separator + "mdblog";
	
	@Inject
	private JsonDao dao;
	
	public MDBlogService() {
	}
	
	@PostConstruct
	public void init() {
		File root = new File(rootDir);
		root.mkdirs();
	}
	
	public void save(String parent, String name, MDBlog blog) throws MDBlogException {
		if (parent == null)
			parent = "/";
		
		blog.setLastUpdatedDate(new Date());
		String path = rootDir + parent + (parent.endsWith("/") ? "" : "/") + name + ".json";
		System.out.println("Saving blog: " + path);
		try {
			dao.write(path, blog);
		} catch (IOException e) {
			throw new MDBlogException("Unable to save blog: " + path, e);
		}
	}
	
	public MDBlog findBlog(String parent, String name) {
		if (parent == null)
			parent = "/";
		String path = rootDir + parent + (parent.endsWith("/") ? "" : "/") + name + ".json";
		System.out.println("Loading blog: " + path);
		try {
			return dao.read(path, MDBlog.class);
		} catch (FileNotFoundException e) {
			return null;
		}
	}
	
	public void delete(String parent, String name) {
		if (parent == null)
			parent = "/";
		
		String path = rootDir + parent + (parent.endsWith("/") ? "" : "/") + name + ".json";
		System.out.println("Deleting blog: " + path);
		dao.delete(path);
	}
}
