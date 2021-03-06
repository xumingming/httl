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
package httl;

import httl.util.DelegateMap;

import java.util.Map;

/**
 * Context. (API, Prototype, ThreadLocal, ThreadSafe)
 * 
 * <pre>
 * Context context = Context.getContext();
 * Object value = context.get(key);
 * </pre>
 * 
 * Lookup variable:
 * 
 * <pre>
 * if (value == null) value = puts.get(key); // context.put(key, value);
 * if (value == null) value = parameters.get(key); // render(parameters);
 * if (value == null) value = parent.get(key); // recursive above
 * 
 * if (value == null) value = servletResovler.get(key); // httl.properties: resovlers+=ServletResovler
 * if (value == null) value = globalResovler.get(key); // GlobalResovler.put(key, value)
 * 
 * if (value == null) value = engineConfig.get(key); // httl.properties
 * </pre>
 * 
 * @see httl.Template#evaluate(Map)
 * @see httl.Template#render(Map, java.io.Writer)
 * @see httl.Template#render(Map, java.io.OutputStream)
 * @see httl.spi.parsers.templates.WriterTemplate#render(Map, java.io.Writer)
 * @see httl.spi.parsers.templates.OutputStreamTemplate#render(Map, java.io.OutputStream)
 * 
 * @author Liang Fei (liangfei0201 AT gmail DOT com)
 */
public final class Context extends DelegateMap<String, Object> {

	private static final long serialVersionUID = 1L;

	// The context thread local holder.
	private static final ThreadLocal<Context> LOCAL = new ThreadLocal<Context>();

	/**
	 * Get the current context from thread local.
	 * 
	 * @return current context
	 */
	public static Context getContext() {
		Context context = LOCAL.get();
		if (context == null) {
			context = new Context(null, null, null, null);
			LOCAL.set(context);
		}
		return context;
	}

	/**
	 * Push the current context to thread local.
	 * 
	 * @param template - current template
	 * @param parameters - current parameters
	 */
	public static Context pushContext(Template template, Map<String, Object> parameters, Object out) {
		Context parent = getContext();
		if (template != null && parent.parent == null) {
			parent.engine = template.getEngine(); // set root context engine
		}
		Context context = new Context(parent, template, parameters, out);
		LOCAL.set(context);
		return context;
	}

	/**
	 * Pop the current context from thread local, and restore parent context to thread local.
	 */
	public static void popContext() {
		Context context = LOCAL.get();
		if (context != null) {
			Context parent = context.getParent();
			if (parent != null) {
				LOCAL.set(parent);
			} else {
				LOCAL.remove();
			}
		}
	}

	/**
	 * Remove the current context from thread local.
	 */
	public static void removeContext() {
		LOCAL.remove();
	}

	// The parent context.
	private final Context parent;
	
	// The current template.
	private final Template template;

	// The current out.
	private final Object out;

	// The context level.
	private final int level;

	// The current engine.
	private Engine engine;

	private Context(Context parent, Template template, Map<String, Object> parameters, Object out) {
		super(parent, parameters);
		this.parent = parent;
		this.template = template;
		this.out = out;
		this.level = parent == null ? 0 : parent.getLevel() + 1;
	}

	/**
	 * Get the parent context.
	 * 
	 * @see #getContext()
	 * @return parent context
	 */
	public Context getParent() {
		return parent;
	}

	/**
	 * Get the super template.
	 * 
	 * @see #getContext()
	 * @return super template
	 */
	public Template getSuper() {
		return parent == null ? null : parent.getTemplate();
	}

	/**
	 * Get the current template.
	 * 
	 * @see #getContext()
	 * @return current template
	 */
	public Template getTemplate() {
		return template;
	}

	/**
	 * Get the current engine.
	 * 
	 * @see #getContext()
	 * @return current engine
	 */
	public Engine getEngine() {
		if (engine == null && template != null)
			engine = template.getEngine();
		return engine;
	}

	/**
	 * Get the current out.
	 * 
	 * @see #getContext()
	 * @return current out
	 */
	public Object getOut() {
		return out;
	}

	/**
	 * Get the context level.
	 * 
	 * @see #getContext()
	 * @return context level
	 */
	public int getLevel() {
		return level;
	}

	// The context key
	private static final String CONTEXT_KEY = "context";

	// The current context key
	private static final String CURRENT_KEY = "current";

	// The parent context key
	private static final String PARENT_KEY = "parent";

	// The template key
	private static final String TEMPLATE_KEY = "template";

	// The this template key
	private static final String THIS_KEY = "this";

	// The super template key
	private static final String SUPER_KEY = "super";

	// The engine key
	private static final String ENGINE_KEY = "engine";

	// The render out key
	private static final String OUT_KEY = "out";

	// The context level key
	private static final String LEVEL_KEY = "level";

	// Get the special variables after the user variables.
	// Allows the user to override these special variables.
	@Override
	protected Object doGet(Object key) {
		if (SUPER_KEY.equals(key)) {
			return getSuper();
		} else if (TEMPLATE_KEY.equals(key) || THIS_KEY.equals(key)) {
			return getTemplate();
		} else if (ENGINE_KEY.equals(key)) {
			return getEngine();
		} else if (OUT_KEY.equals(key)) {
			return getOut();
		} else if (LEVEL_KEY.equals(key)) {
			return getLevel();
		} else if (PARENT_KEY.equals(key)) {
			return getParent();
		} else if (CONTEXT_KEY.equals(key) || CURRENT_KEY.equals(key)) {
			return this;
		} else if (getParent() == null) {
			Engine engine = getEngine();
			return engine == null ? null : engine.getProperty((String) key);
		} else {
			return null;
		}
	}

}