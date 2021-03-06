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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ApplicationScoped
public class JsonDao {
	private Gson gson = new GsonBuilder()
		.setPrettyPrinting()
		.create();
	
	public void delete(String path) {
		File file = new File(path);
		file.delete();
	}
	public void write(String path, Object object) throws IOException {
		File file = new File(path);
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileWriter fw = new FileWriter(file);
		BufferedWriter bf = new BufferedWriter(fw);
		bf.write(gson.toJson(object));
		
		try {
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public <T> T read(String path, Class<T> c) throws FileNotFoundException {
		FileReader fr = new FileReader(path);
		
		T object = gson.fromJson(fr, c);
		try {
			fr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return object;
	}
}
