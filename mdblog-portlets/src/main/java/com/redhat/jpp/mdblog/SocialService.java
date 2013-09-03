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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.gatein.api.PortalRequest;
import org.gatein.api.cdi.context.PortletLifecycleScoped;
import org.gatein.api.oauth.AccessToken;
import org.gatein.api.oauth.OAuthProvider;
import org.gatein.security.oauth.portlet.RequestContext;
import org.jsoup.Jsoup;

import com.petebevin.markdown.MarkdownProcessor;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.exception.FacebookNetworkException;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import javax.inject.Named;

@PortletLifecycleScoped
public class SocialService {
	@Inject
	MarkdownProcessor processor;
	
	@Inject
	private PortalRequest portalRequest;

	@Inject
	private RequestContext requestContext;
	
	@Inject @Named("config")
	private MDBlogConfig config;

	public void publishBlog(MDBlog blog, boolean create) {
		AccessToken accessToken = getAccessToken();

		if (accessToken == null) {
			System.err
					.println("accessToken not found! Maybe portlet session expired");
			return;
		}

		FacebookClient facebookClient = new DefaultFacebookClient(
				accessToken.getAccessToken());
		
		List<Parameter> params = new ArrayList<Parameter>();
		if (create) {
			params.add(Parameter.with("message", "I just posted a new blog on MDBlog"));
		} else {
			params.add(Parameter.with("message", "I just updated a blog on MDBlog"));
		}
		params.add(Parameter.with("name", blog.getTitle()));
		String link = config.getBaseUrl() + portalRequest.getURIResolver().resolveURI(portalRequest.getSiteId()) +
				portalRequest.getNodePath();
		params.add(Parameter.with("link", link));
		
		String description = Jsoup.parse(processor.markdown(blog.getContent())).text();
		description = ellipsis(description, 200);
		params.add(Parameter.with("description", description));

		try {
			FacebookType publishMessageResponse = facebookClient.publish(
					"me/feed", FacebookType.class,
					params.toArray(new Parameter[] {}));
			if (publishMessageResponse.getId() != null) {
				System.out.println("FB Status published");
			}
		} catch (FacebookOAuthException foe) {
			foe.printStackTrace();
		} catch (FacebookNetworkException fne) {
			fne.printStackTrace();
		}
	}

	protected String getOAuthProviderKey() {
		return OAuthProvider.FACEBOOK;
	}

	// Intended to be used by subclasses
	protected OAuthProvider getOAuthProvider() {
		return requestContext.getOAuthProvider(getOAuthProviderKey());
	}

	// Intended to be used by subclasses
	protected AccessToken getAccessToken() {
		return requestContext.getAccessToken(getOAuthProviderKey());
	}
	
	public static String ellipsis(final String text, int length)
	{
	    length += Math.ceil(text.replaceAll("[^iIl]", "").length() / 2.0d);

	    if (text.length() > length)
	    {
	        return text.substring(0, length - 3) + "...";
	    }

	    return text;
	}
}
