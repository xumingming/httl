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
package httl.spi.parsers;

import httl.Resource;
import httl.Template;
import httl.spi.Parser;
import httl.spi.Translator;
import httl.spi.loaders.resources.StringResource;
import httl.util.LinkedStack;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommentParser. (SPI, Singleton, ThreadSafe)
 * 
 * @see httl.spi.engines.DefaultEngine#setParser(Parser)
 *
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public class CommentParser extends AbstractParser {
	
	protected static final Pattern STATEMENT_PATTERN = Pattern.compile("<!--\\s*#\\s*([a-z]+)\\s*\\(?(.*?)\\)?\\s*-->", Pattern.DOTALL);
	
	protected Pattern getPattern() {
		return STATEMENT_PATTERN;
	}
	
	@Override
	protected String getDiretive(String name, String value) {
		return "<!--#" + name + "(" + value + ")-->";
	}
	
	protected String doParse(Resource resource, boolean stream, String source, Translator translator, 
							 List<String> parameters, List<Class<?>> parameterTypes, 
							 Set<String> setVariables, Set<String> getVariables, Map<String, Class<?>> types, 
							 Map<String, Class<?>> returnTypes, Map<String, Class<?>> macros) throws IOException, ParseException {
		LinkedStack<String> nameStack = new LinkedStack<String>();
		LinkedStack<String> valueStack = new LinkedStack<String>();
		StringBuffer macro = null;
		int macroStart = 0;
		int macroParameterStart = 0;
		StringBuffer buf = new StringBuffer();
		Matcher matcher = getPattern().matcher(source);
		while (matcher.find()) {
			String name = matcher.group(1);
			String value = matcher.group(2);
			int offset;
			if (value == null) {
				offset = matcher.end(1);
			} else {
				offset = matcher.start(2);
			}
			if (endDirective.equals(name)) {
				if (nameStack.isEmpty()) {
					throw new ParseException("The #end directive without start directive.", matcher.start(1));
				}
				String startName = nameStack.pop();
				String startValue = valueStack.pop();
				while(elseifDirective.equals(startName) || elseDirective.equals(startName)) {
					if (nameStack.isEmpty()) {
						throw new ParseException("The #" + startName + " directive without #if directive.", matcher.start(1));
					}
					String oldStartName = startName;
					startName = nameStack.pop();
					startValue = valueStack.pop();  
					if (! ifDirective.equals(startName) && ! elseifDirective.equals(startName)) {
						throw new ParseException("The #" + oldStartName + " directive without #if directive.", matcher.start(1));
					}
				}
				if (macro != null) {
					if (macroDirective.equals(startName) && 
							! nameStack.toList().contains(macroDirective)) {
						int i = startValue.indexOf('(');
						String var;
						String param;
						if (i > 0) {
							if (! startValue.endsWith(")")) {
								throw new ParseException("Invalid macro parameters " + startValue, macroParameterStart + i);
							}
							var = startValue.substring(0, i).trim();
							param = startValue.substring(i + 1, startValue.length() - 1).trim();
						} else {
							var = startValue;
							param = null;
						}
						String out = null;
						String set = null;
						if (var.startsWith("$")) {
							if (var.startsWith("$!")) {
								out = var.substring(0, 2);
								var = var.substring(2).trim();
							} else {
								out = var.substring(0, 1);
								var = var.substring(1).trim();
							}
						} else if (var.contains("=")) {
							int l = var.indexOf("=");
							set = var.substring(0, l + 1).trim();
							var = var.substring(l + 1).trim();
						}
						matcher.appendReplacement(macro, "");
						String key = getMacroPath(resource.getName(), var);
						String es = macro.toString();
						if (param != null && param.length() > 0) {
							es = getDiretive(varDirective, param) + es;
						}
						macros.put(var, parseClass(new StringResource(engine, key, resource.getLocale(), resource.getEncoding(), resource.getLastModified(), es), stream, macroParameterStart));
						Class<?> cls = types.get(var);
						if (cls != null && ! cls.equals(Template.class)) {
							throw new ParseException("Duplicate macro variable " + var + ", conflict types: " + cls.getName() + ", " + Template.class.getName(), macroParameterStart);
						}
						types.put(var, Template.class);
						buf.append(LEFT);
						buf.append(matcher.end() - macroStart);
						if (out != null && out.length() > 0) {
							getVariables.add(var);
							String code = getExpressionCode(out, var, Template.class, stream, getVariables);
							buf.append(code);
						} else if (set != null && set.length() > 0) {
							getVariables.add(var);
							String setValue = set + " " + var + ".evaluate()";
							String code = getStatementCode(setDirective, setValue, matcher.start(1), offset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
							buf.append(code);
						}
						buf.append(RIGHT);
						macro = null;
						macroStart = 0;
						macroParameterStart = 0;
					} else {
						matcher.appendReplacement(macro, "$0");
					}
				} else {
					String end = getStatementEndCode(startName);
					if (end != null && end.length() > 0) {
						matcher.appendReplacement(buf, "");
						buf.append(LEFT);
						buf.append(matcher.group().length());
						buf.append(end);
						buf.append(RIGHT);
					} else {
						throw new ParseException("The #end directive without start directive.", matcher.start(1));
					}
				}
			} else {
				if (ifDirective.equals(name) || elseifDirective.equals(name) 
						|| elseDirective.equals(name) || foreachDirective.equals(name)
						|| macroDirective.equals(name)) {
					nameStack.push(name);
					valueStack.push(value);
				}
				if (macro != null) {
					matcher.appendReplacement(macro, "$0");
				} else {
					matcher.appendReplacement(buf, "");
					if (macroDirective.equals(name)) {
						if (value == null || value.trim().length() == 0) {
							throw new ParseException("Macro name == null!", matcher.start(1));
						}
						macro = new StringBuffer();
						macroStart = matcher.start();
						macroParameterStart = matcher.start(1);
					} else {
						buf.append(LEFT);
						buf.append(matcher.group().length());
						String code = getStatementCode(name, value, matcher.start(1), offset, translator, setVariables, getVariables, types, returnTypes, parameters, parameterTypes, true);
						buf.append(code);
						buf.append(RIGHT);
					}
				}
			}
		}
		matcher.appendTail(buf);
		return buf.toString();
	}
	
}