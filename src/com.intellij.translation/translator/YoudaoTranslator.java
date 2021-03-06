/*
 * Copyright 2016 Yuyou Chow
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.translation.translator;

import com.google.gson.Gson;
import com.intellij.ide.BrowserUtil;
import com.intellij.translation.icons.TranslationIcons;
import com.intellij.translation.settings.TranslationSettings;
import com.intellij.util.ResourceUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static com.intellij.translation.TranslationConstants.REQUEST_TIMEOUT;

/**
 * Created by zyuyou on 16/6/27.
 *
 * https://github.com/Skykai521/ECTranslation/blob/master/src/RequestRunnable.java
 * @author loucyin
 */
public class YoudaoTranslator extends TranslatorEx{
	static final String HTTP_STYLE;

	static {
		String css;
		try{
			css = ResourceUtil.loadText(ResourceUtil.getResource(Translator.class, "/css", "youdao.css"));
		}catch (IOException e){
			throw (AssertionError) new AssertionError().initCause(e);
		}
		HTTP_STYLE = "<style type=\"text/css\">\n" + css + "</style>\n";
	}

	@NotNull
	@Override
	public String getTitle() {
		return "Youdao Translation";
	}

	@NotNull
	@Override
	public Icon getIcon() {
		return TranslationIcons.YOUDAO_SMALL;
	}

	@Nullable
	@Override
	public String fetchInfo(String query) {
		return TranslatorUtil.fetchInfo(query, this);
	}

	@Nullable
	@Override
	public String getExternalUrl(String query) {
		try{
			String finalQuery = URLEncoder.encode(query, "UTF-8");
			return "http://dict.youdao.com/w/"+ finalQuery + "/#keyfrom=dict2.top";
		} catch (UnsupportedEncodingException ignore) {
		}
		return null;
	}

	@NotNull
	@Override
	public URI createUrl(String query) throws URISyntaxException {
		URIBuilder builder = new URIBuilder();
		builder.setScheme("http")
			.setHost("fanyi.youdao.com")
			.setPath("/openapi.do")
			.addParameter("keyfrom", TranslationSettings.getInstance().getYoudaoKeyfrom())
			.addParameter("key", TranslationSettings.getInstance().getYoudaoApiKey())
			.addParameter("type", "data")
			.addParameter("version", "1.1")
			.addParameter("doctype", "json")
			.addParameter("q", query);
		return builder.build();
	}

	@Nullable
	@Override
	public String generateSuccess(HttpEntity entity) throws IOException {
		String json = EntityUtils.toString(entity, "UTF-8");
		Gson gson = new Gson();
		YoudaoTranslation result = gson.fromJson(json, YoudaoTranslation.class);
		return decorateHtml(result.toString());
	}

	@Nullable
	@Override
	public String generateFail(HttpResponse response) {
		return response.getStatusLine().getReasonPhrase();
	}

	@NotNull
	private static String decorateHtml(@NotNull String retrievedHtml) {
		return "<html>" + HTTP_STYLE + "<body>" + retrievedHtml + "</body></html>";
	}
}
