/*
Copyright (c) 2011, Cornell University
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright notice,
      this list of conditions and the following disclaimer in the documentation
      and/or other materials provided with the distribution.
    * Neither the name of Cornell University nor the names of its contributors
      may be used to endorse or promote products derived from this software
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package edu.cornell.mannlib.vitro.webapp.controller.visualization.freemarker;

import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import edu.cornell.mannlib.vitro.webapp.visualization.freemarker.visutils.VisualizationRequestHandler;

public class VisualizationsDependencyInjector {
	
	private static Map<String, VisualizationRequestHandler> visualizationIDsToClass;
	
	/**
	 * This method is used to inject vis dependencies i.e. the vis algorithms that are 
     * being implemented into the vis controller. Modified Dependency Injection pattern is 
     * used here. XML file containing the location of all the vis is saved in accessible folder. 
	 * @param servletContext
	 * @return
	 */
	private synchronized static Map<String, VisualizationRequestHandler> initVisualizations(
			ServletContext servletContext) {
		
		/*
		 * A visualization request has already been made causing the visualizationIDsToClass to be
		 * initiated & populated with visualization ids to its request handlers.
		 * */
		if (visualizationIDsToClass != null) {
			return visualizationIDsToClass;
		}
		
		String resourcePath = 
			servletContext
				.getRealPath(VisualizationFrameworkConstants
						.RELATIVE_LOCATION_OF_FM_VISUALIZATIONS_BEAN);
		
		ApplicationContext context = new ClassPathXmlApplicationContext(
											"file:" + resourcePath);

		BeanFactory factory = context;
		
		VisualizationInjector visualizationInjector = 
				(VisualizationInjector) factory.getBean("visualizationInjector");
		
		visualizationIDsToClass = visualizationInjector.getVisualizationIDToClass();
		
		
		return visualizationIDsToClass;
	}
	
	public static Map<String, VisualizationRequestHandler> getVisualizationIDsToClassMap(
			ServletContext servletContext) {
		if (visualizationIDsToClass != null) {
			return visualizationIDsToClass;
		} else {
			return initVisualizations(servletContext);
		}
	}

}
