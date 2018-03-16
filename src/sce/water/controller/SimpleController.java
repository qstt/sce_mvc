package sce.water.controller;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class SimpleController extends HttpServlet {

	private static final long serialVersionUID = 1L;

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html;charset=utf-8");

		// 获取请求action的名称
		StringBuffer requestURL = request.getRequestURL();
		System.out.println("url  ===   " + requestURL);
		String requestActionName = requestURL.substring(requestURL.lastIndexOf("/") + 1, requestURL.indexOf("."));

		try {
			
			// 解析controller.xml
			InputStream inputStream = this.getClass().getResourceAsStream("/controller.xml");
			SAXReader saxReader = new SAXReader();
			Document document = saxReader.read(inputStream);
			
			//获取根节点对象  获取action  迭代action
			Element rootElement = document.getRootElement();
			Element controllerElement = rootElement.element("controller");
			List<Element> actionElements = controllerElement.elements("action");
			Iterator<Element> iterator = actionElements.iterator();
			
			//判断controller.xml中是否有相应的action
			int sign = 0;
			while(iterator.hasNext()) {
				Element actionElement = iterator.next();
				String actionName = actionElement.attributeValue("name");

				if(actionName.equals(requestActionName)) {
					sign =1;
					
					String classValue = actionElement.attributeValue("class");
					Class<?> aClass = Class.forName(classValue);
					String methodValue = actionElement.attributeValue("method");
					Method method = aClass.getMethod(methodValue, null);
					Object returnValue = method.invoke(aClass.newInstance());
					
					List<Element> resultElements = actionElement.elements("result");
					Iterator<Element> iterator2 = resultElements.iterator();
					
					int sign2 = 0;
					while(iterator2.hasNext()) {
						Element resultElement = iterator2.next();
						String resultName = resultElement.attributeValue("name");
						String value = resultElement.attributeValue("value");
						
						if(resultName.equals(returnValue)) {
							sign2 = 1;
							String typeValue = resultElement.attributeValue("type");
							if(typeValue.equals("forward")) {
								request.getRequestDispatcher(value).forward(request, response);
							}else {
								response.sendRedirect(value);
							}
							
						}
						
					}
					
					if(sign2 == 0) {
						response.getWriter().println("没有请求的资源！！！");
					}
					
				}
				
			}
			
			if(sign == 0) {
				response.getWriter().println("不可识别的action请求！！！");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

}