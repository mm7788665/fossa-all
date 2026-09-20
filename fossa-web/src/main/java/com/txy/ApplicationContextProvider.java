package com.txy;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Ban on 2015/5/26.
 */
@Component
public class ApplicationContextProvider implements ApplicationContextAware {
	private static ApplicationContext context;

	public static ApplicationContext getApplicationContext() {
		return context;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) {
		context = ctx;
	}

	public static <T> List<T> getBeansByClass(Class<T> c) {
		Map<String, T> map = ApplicationContextProvider.getApplicationContext().getBeansOfType(c);
		List<T> beans = new ArrayList<T>();
		for (Map.Entry<String, T> entry : map.entrySet()) {
			beans.add(entry.getValue());
		}
		return beans;
	}

	public static <T> T getBeanByClass(Class<T> c) {
		Map<String, T> map = ApplicationContextProvider.getApplicationContext().getBeansOfType(c);
		List<T> beans = new ArrayList<T>();
		for (Map.Entry<String, T> entry : map.entrySet()) {
			beans.add(entry.getValue());
		}
		return beans.get(0);
	}

	public static String getProperty(String propertyName) {
		String propertyValue = context.getEnvironment().getProperty(propertyName);
		return propertyValue;
	}
}
