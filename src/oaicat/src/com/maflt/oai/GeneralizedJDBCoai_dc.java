/*
Copyright 2009 Mission Aviation Fellowship
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package com.maflt.oai;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import ORG.oclc.oai.server.crosswalk.Crosswalk;
import ORG.oclc.oai.util.OAIUtil;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.http.HttpSession;


public class GeneralizedJDBCoai_dc extends Crosswalk {
	
	private Properties localProperties;
	
	public GeneralizedJDBCoai_dc(Properties properties) {
		super("http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd");
		localProperties = properties;
	}
	
	 /**
     * Perform the actual crosswalk.
     *
     * @param nativeItem the native "item". 
     * @return a String containing the XML to be stored within the <metadata> element.
     * Note that tag_type_vw appends "_" + rand() to the tagtype, to make it unique and thus
     * not get dropped from the HashMap here. So theKey.split("_")[0] strips off the _rand().
     */
    @SuppressWarnings("unchecked")
	public String createMetadata(Object nativeItem) {

	HashMap itemTagTable = (HashMap)((HashMap)nativeItem).get("itemTagResult");
	StringBuffer sb = new StringBuffer();
	sb.append("<oai_dc:dc xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\""
		  + getSchemaLocation()
		  + "\">");
	
	Iterator it = itemTagTable.entrySet().iterator();
	
	while(it.hasNext()){
		Map.Entry m 	= (Map.Entry) it.next();
		String theKey 	=  m.getKey().toString();
                
  		if (m.getValue()!= null) {
                        String theValue =  m.getValue().toString();
                        //TODO:Add check for valid URL. If not valid return original value.
			if (theKey.split("_")[0].equals("identifier") && (!theValue.toLowerCase().startsWith("http")) ) {
				theValue =localProperties.getProperty("Identifier.baseUrl")+theValue;
			}
			
			sb.append("<" + theKey.split("_")[0] + ">");
			sb.append(OAIUtil.xmlEncode(theValue));
			sb.append("</" + theKey.split("_")[0] + ">");
		}
	}
			
	sb.append("</oai_dc:dc>");
	return sb.toString();
    }

    /**
     * Can this nativeItem be represented in DC format?
     * @param nativeItem a record in native format
     * @return true if DC format is possible, false otherwise.
     */
    public boolean isAvailableFor(Object nativeItem) {
	return true; // all records must support oai_dc according to the OAI spec.
    }

}
