/*
Copyright (c) 2010, Cornell University
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

package edu.cornell.mannlib.vedit.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vedit.beans.EditProcessObject;
import edu.cornell.mannlib.vitro.webapp.auth.policy.JenaNetidPolicy.ContextSetup;


public class OperationUtils{
	
	private static final Log log = LogFactory.getLog(OperationUtils.class.getName());

    public static void beanSetAndValidate(Object newObj, String field, String value, EditProcessObject epo){
        Class cls = (epo.getBeanClass() != null) ? epo.getBeanClass() : newObj.getClass();
        Class[] paramList = new Class[1];
        paramList[0] = String.class;
        boolean isInt = false;
        boolean isBoolean = false;
        Method setterMethod = null;
        try {
            setterMethod = cls.getMethod("set"+field,paramList);
        } catch (NoSuchMethodException e) {
            //let's try int
            paramList[0] = int.class;
            try {
                setterMethod = cls.getMethod("set"+field,paramList);
                isInt = true;
            } catch (NoSuchMethodException f) {
            	//let's try boolean
            	paramList[0]=boolean.class;
            	try {
            		setterMethod = cls.getMethod("set"+field,paramList);
            		isBoolean = true;
            		log.debug("found boolean field "+field);
            	} catch (NoSuchMethodException g) {
            		log.error("beanSet could not find an appropriate String, int, or boolean setter method for "+field);	
            	}
                
            }
        }
        Object[] arglist = new Object[1];
        if (isInt)
            arglist[0] = Integer.decode(value);
        else if (isBoolean) 
        	arglist[0] = (value.equalsIgnoreCase("TRUE"));
        else
            arglist[0] = value;
        try {
            setterMethod.invoke(newObj,arglist);
        } catch (Exception e) {
            System.out.println("Couldn't invoke method");
            System.out.println(e.getMessage());
            System.out.println(field+" "+arglist[0]);
        }
    }

    /**
     * Takes a bean and clones it using reflection.
     * Any fields without standard getter/setter methods will not be copied.
     * @param bean
     * @return
     */
    public static Object cloneBean (Object bean) {
        return cloneBean(bean, bean.getClass());
    }

    /**
     * Takes a bean and clones it using reflection.
     * Any fields without standard getter/setter methods will not be copied.
     * @param bean
     * @return
     */
    public static Object cloneBean (Object bean, Class beanClass){
        Object newBean = null;
        try {
            newBean = beanClass.newInstance();
            Method[] beanMeths = beanClass.getMethods();
            for (int i=0; i<beanMeths.length ; ++i){
                String methName = beanMeths[i].getName();
                if (methName.indexOf("get")==0){
                    String fieldName = methName.substring(3,methName.length());
                    Class returnType = beanMeths[i].getReturnType();
                    try {
                        Class[] args = new Class[1];
                        args[0] = returnType;
                        Method setterMethod = beanClass.getMethod("set"+fieldName,args);
                        try {
                            Object fieldVal = beanMeths[i].invoke(bean,(Object[])null);
                            try {
                                Object[] setArgs = new Object[1];
                                setArgs[0] = fieldVal;
                                setterMethod.invoke(newBean,setArgs);
                            } catch (IllegalAccessException iae) {
                                System.out.println("edu.cornell.mannlib.vitro.edit.utils.OperationUtils encountered IllegalAccessException invoking "+setterMethod.getName());
                            } catch (InvocationTargetException ite) {
                                System.out.println("edu.cornell.mannlib.vitro.edit.utils.OperationUtils encountered InvocationTargetException invoking "+setterMethod.getName());
                                System.out.println(ite.getTargetException().getClass().toString());
                            }
                        } catch (IllegalAccessException iae) {
                            System.out.println(OperationUtils.class.getName()+" encountered IllegalAccessException invoking "+beanMeths[i].getName());
                        } catch (InvocationTargetException ite) {
                            System.out.println(OperationUtils.class.getName()+" encountered InvocationTargetException invoking "+beanMeths[i].getName());
                            System.out.println(ite.getTargetException().getClass().toString());
                        } catch (IllegalArgumentException iae) {
                            // System.out.println(OperationUtils.class.getName()+" found that "+beanMeths[i].getName()+" requires one or more arguments. Skipping.");
                        }
                    } catch (NoSuchMethodException nsme){
                        // ignore this field because there is no setter method
                    }
                }
            }
        } catch (InstantiationException ie){
            System.out.println("edu.cornell.mannlib.vitro.edit.utils.OperationUtils.cloneBean("+bean.getClass().toString()+") could not instantiate new instance of bean.");
            System.out.println(ie.getStackTrace());
        } catch (IllegalAccessException iae){
            System.out.println("edu.cornell.mannlib.vitro.edit.utils.OperationUtils.cloneBean("+bean.getClass().toString()+") encountered illegal access exception instantiating new bean.");
            System.out.println(iae.getStackTrace());
        }
        return newBean;
    }

}