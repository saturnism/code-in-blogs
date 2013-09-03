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
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;

public class JsonDaoTest {
	
	protected JsonDao dao = new JsonDao();

	@Test
	public void testWrite() throws IOException {
		MDBlog blog = new MDBlog();
		blog.setContent("**Hello**");
		blog.setTitle("Hello World");
		blog.setLastUpdatedDate(new Date());
		blog.getTags().add("hello");
		blog.getTags().add("world");
		blog.getTags().add("first");
		blog.getTags().add("jpp");
		dao.write("/tmp/test.json", blog);
	}

	@Test
	public void testRead() throws IOException {
		MDBlog blog = new MDBlog();
		blog.setContent("**Hello**");
		blog.setTitle("Hello World");
		blog.setLastUpdatedDate(new Date());
		blog.getTags().add("hello");
		blog.getTags().add("world");
		blog.getTags().add("first");
		blog.getTags().add("jpp");
		dao.write("/tmp/read.json", blog);
		
		MDBlog read = dao.read("/tmp/read.json", MDBlog.class);
		Assert.assertNotNull(read);
	}

}
