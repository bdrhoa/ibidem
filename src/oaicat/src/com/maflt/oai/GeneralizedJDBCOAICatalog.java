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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import ORG.oclc.oai.server.catalog.ExtendedJDBCOAICatalog;
import ORG.oclc.oai.server.catalog.ExtendedJDBCRecordFactory;
import ORG.oclc.oai.server.verb.OAIInternalServerError;


public class GeneralizedJDBCOAICatalog extends ExtendedJDBCOAICatalog {
	private static final boolean debug = false;
	
	
    private String itemTagQuery = null;
    
    private String setSpecListLabel = null;
    
    private ExtendedJDBCRecordFactory rf = null;
    
	public GeneralizedJDBCOAICatalog(Properties properties) throws IOException {
		
		super(properties);
		
		rf = new ExtendedJDBCRecordFactory(properties);
		
		itemTagQuery = properties.getProperty("ExtendedJDBCOAICatalog.itemTagQuery");
        if (itemTagQuery == null) {
            throw new IllegalArgumentException("ExtendedJDBCOAICatalog.itemTagQuery is missing from the properties file");
        }
        if (debug) System.out.println(itemTagQuery.toString());
        
        setSpecListLabel = properties.getProperty("ExtendedJDBCOAICatalog.setSpecListLabel");
        if (setSpecListLabel == null) {
            throw new IllegalArgumentException("ExtendedJDBCOAICatalog.setSpecListLabel is missing from the properties file");
        }
        if (debug) System.out.println(setSpecListLabel.toString());      
	}
	
	 /**
     * Get remainder of the nativeItem. By default, it is assumed
     * the entire nativeItem is produced by the default query.
     * 
     * For this implementation there are two HasMaps. The normal "coreResults" map
     * contains the local identifier (item_id) and the time stamp (last_update). Here we add
     * the itemTagResult map. We retrieve a ResultSet of tags (e.g. dc.title) and tag values (e.g. Pilgrims Progress).
     * A tag have any value and can be repeated any number of times which follows the OAI-PMH standard.
     */
    @SuppressWarnings("unchecked")
	protected void extendItem(Connection con, HashMap nativeItem) {
          
            Statement stmt;
            
            
            try {
            	stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				        ResultSet.CONCUR_READ_ONLY);
				//ResultSet rs = stmt.executeQuery(populateItemTagQuery(getItemId(nativeItem)));
				ResultSet rs = stmt.executeQuery(populateItemTagQuery(rf.getLocalIdentifier(nativeItem)));
				nativeItem.put("itemTagResult", getItemTagColumnValues(rs));
				
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (OAIInternalServerError e) {
				e.printStackTrace();
			}     
    }
    
    /**
     * Get item_tag records for the given item.
     *
     * @param item_id the OAI local identifier
     * @return a String containing an SQL query
     */

    private String populateItemTagQuery(String item_id)
    throws OAIInternalServerError {
        StringBuffer sb = new StringBuffer();
        StringTokenizer tokenizer;
        
        tokenizer = new StringTokenizer(itemTagQuery, "\\");

        if (tokenizer.hasMoreTokens())
            sb.append(tokenizer.nextToken());
        else
            throw new OAIInternalServerError("Invalid query");
        sb.append(item_id);
        if (debug) System.out.println(sb.toString());
        return sb.toString();
    }
  
    /**
     * get the item tags 
     *
     * @param rs ResultSet
     * @return a map of tags and corresponding values, e.g (dc.title,Pilgrims Progress)
     */
    @SuppressWarnings("unchecked")
	private HashMap getItemTagColumnValues(ResultSet rs)
    throws SQLException {
    	HashMap itemTagMap = new HashMap();    	
    	while(rs.next()){
    		itemTagMap.put(rs.getString("tagtype"), rs.getString("tag"));
    	}
    	return itemTagMap;
    }
    
    /**
     * get the setSpec XML string. 
     *
     * @param rs ResultSet
     * @return an XML String containing the &lt;setSpec&gt; content
     */
    @SuppressWarnings("unchecked")
	protected String getSetSpec(HashMap setItem) {
        try {
            return URLEncoder.encode(setItem.get(setSpecListLabel).toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return "UnsupportedEncodingException";
        }
    }
    
    /**
     * Change the String from UTC to MySQL format
     * If this method doesn't suit your needs, extend this class and override
     * the method rather than change this code directly. 
     */
    protected String formatDate(String date) {
        StringBuffer sb = new StringBuffer();
        sb.append(date.substring(0, 4));
        sb.append("-");
        sb.append(date.substring(5, 7));
        sb.append("-");
        sb.append(date.substring(8));
        
        if (debug) System.out.println("GeneralizedJDBCOAICatalog.formatDate: from " + date + " to " + sb.toString());
        return sb.toString();
    }
}
