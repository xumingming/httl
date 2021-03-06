/*
 * Copyright 2011-2012 HTTL Team.
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
package httl.spi.parsers.templates;

import httl.Context;
import httl.Engine;
import httl.Template;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Adaptive Template. (SPI, Prototype, ThreadSafe)
 * 
 * @see httl.Engine#getTemplate(String)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class AdaptiveTemplate implements Template, Serializable {

	private static final long serialVersionUID = 3094907176375413567L;

	private final Template writerTemplate;

	private final Template streamTemplate;

	public AdaptiveTemplate(Template writerTemplate, Template streamTemplate) {
		if (writerTemplate == null)
			throw new IllegalArgumentException("writer template == null");
		if (streamTemplate == null)
			throw new IllegalArgumentException("stream template == null");
		this.writerTemplate = writerTemplate;
		this.streamTemplate = streamTemplate;
	}

	public String getName() {
		return writerTemplate.getName();
	}

	public String getEncoding() {
		return writerTemplate.getEncoding();
	}

	public Locale getLocale() {
		return writerTemplate.getLocale();
	}

	public long getLastModified() {
		return writerTemplate.getLastModified();
	}

	public long getLength() {
		return writerTemplate.getLength();
	}

	public String getSource() {
		return writerTemplate.getSource();
	}

	public Reader getReader() throws IOException {
		return writerTemplate.getReader();
	}

	public InputStream getInputStream() throws IOException {
		return streamTemplate.getInputStream();
	}

	public Engine getEngine() {
		return writerTemplate.getEngine();
	}

	public Class<?> getReturnType() {
		if (Context.getContext().getOut() instanceof OutputStream) {
			return streamTemplate.getReturnType();
		} else {
			return writerTemplate.getReturnType();
		}
	}

	public Object evaluate() throws ParseException {
		if (Context.getContext().getOut() instanceof OutputStream) {
			return streamTemplate.evaluate();
		} else {
			return writerTemplate.evaluate();
		}
	}

	public Object evaluate(Map<String, Object> parameters) throws ParseException {
		if (Context.getContext().getOut() instanceof OutputStream) {
			return streamTemplate.evaluate(parameters);
		} else {
			return writerTemplate.evaluate(parameters);
		}
	}

	public void render(OutputStream stream) throws IOException, ParseException {
		streamTemplate.render(stream);
	}

	public void render(Map<String, Object> parameters, OutputStream stream)
			throws IOException, ParseException {
		streamTemplate.render(parameters, stream);
	}

	public void render(Writer writer) throws IOException, ParseException {
		writerTemplate.render(writer);
	}

	public void render(Map<String, Object> parameters, Writer writer)
			throws IOException, ParseException {
		writerTemplate.render(parameters, writer);
	}

	public Map<String, Class<?>> getParameterTypes() {
		return writerTemplate.getParameterTypes();
	}

	public Map<String, Class<?>> getContextTypes() {
		return writerTemplate.getContextTypes();
	}
	
	private Map<String, Template> macros;

	public Map<String, Template> getMacros() {
		if (macros == null) { // allow duplicate on concurrent
			Map<String, Template> map = new HashMap<String, Template>();
			Map<String, Template> writerMacros = writerTemplate.getMacros();
			Map<String, Template> streamMacros = streamTemplate.getMacros();
			for (Map.Entry<String, Template> entry : writerMacros.entrySet()) {
				map.put(entry.getKey(), new AdaptiveTemplate(entry.getValue(), streamMacros.get(entry.getKey())));
			}
			macros = Collections.unmodifiableMap(map);
		}
		return macros;
	}

	public String getCode() {
		return writerTemplate.getCode();
	}

	public int getOffset() {
		return writerTemplate.getOffset();
	}

	public boolean isMacro() {
		return writerTemplate.isMacro();
	}

	@Override
	public String toString() {
		return writerTemplate.toString();
	}

}