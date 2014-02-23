/**
 * Copyright (c) 2011-2013, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.render;

import java.io.IOException;

import com.google.common.base.Strings;
import com.jfinal.core.JFinal;
import com.jfinal.sog.kit.StringPool;

/**
 * RedirectRender with status: 302 Found.
 */
public class RedirectRender extends Render {
	
	private static final long serialVersionUID = 1812102713097864255L;
	private String url;
	private boolean withQueryString;
	private static final String contextPath = getContxtPath();
	
	static String getContxtPath() {
		String cp = JFinal.me().getContextPath();
		return (Strings.isNullOrEmpty(cp)) ? null : cp;
	}
	
	public RedirectRender(String url) {
		this.url = url;
		this.withQueryString = false;
	}
	
	public RedirectRender(String url, boolean withQueryString) {
		this.url = url;
		this.withQueryString =  withQueryString;
	}
	
	public void render() {
		if (contextPath != null && !url.contains("://"))
			url = contextPath + url;
		
		if (withQueryString) {
			String queryString = request.getQueryString();
			if (queryString != null)
				if (!url.contains(StringPool.QUESTION_MARK))
					url = url + StringPool.QUESTION_MARK + queryString;
				else
					url = url + StringPool.AMPERSAND + queryString;
		}
		
		try {
			response.sendRedirect(url);	// always 302
		} catch (IOException e) {
			throw new RenderException(e);
		}
	}
}

