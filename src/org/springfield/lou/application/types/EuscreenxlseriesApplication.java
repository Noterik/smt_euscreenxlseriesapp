/* 
* EuscreenxlpreviewApplication.java
* 
* Copyright (c) 2012 Noterik B.V.
* 
* This file is part of Lou, related to the Noterik Springfield project.
*
* Lou is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Lou is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Lou.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.springfield.lou.application.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONObject;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;


public class EuscreenxlseriesApplication extends Html5Application{
	
	private boolean wantedna = true;
	private HashMap<String, String> countriesForProviders;
	
	/*
	 * Constructor for the euscreenxl series page application.
	 */
	
	public EuscreenxlseriesApplication(String id) {
		super(id);
		
		//Add refers to other resources
		this.addReferid("mobilenav", "/euscreenxlelements/mobilenav");
		this.addReferid("header", "/euscreenxlelements/header");
		this.addReferid("footer", "/euscreenxlelements/footer");
		this.addReferid("linkinterceptor", "/euscreenxlelements/linkinterceptor");
		this.addReferid("warning", "/euscreenxlelements/warning");
		this.addReferid("videocopyright", "/euscreenxlelements/videocopyright");
		
		this.addReferidCSS("elements", "/euscreenxlelements/generic");
		this.addReferidCSS("bootstrap", "/euscreenxlelements/bootstrap");
		
		this.countriesForProviders = new HashMap<String, String>();
	}
	
	public void init(Screen s){
		//EXAMPLE SERIES ID: EUS_23670630327FACFCB5B6912617F95447
		String seriesId = s.getParameter("id");
		String uri = "/domain/euscreenxl/user/*/*";
		
		System.out.println("SERIES ID: " + seriesId);
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(seriesId.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode seriesNode = (FsNode)nodes.get(0);
			FSList videos = FSListManager.get(seriesNode.getPath() + "/video");
			
			s.setProperty("seriesNode", seriesNode);
			s.setProperty("seriesVideos", videos);
			setMetadata(s);
			setVideos(s);
		}
	}
	
	public void doDesktopSpecificStuff(Screen s){
		System.out.println("doDesktopSpecificStuff()");
		s.putMsg("template", "", "createTooltips()");
	}
	
	public String getFavicon() {
        return "/eddie/apps/euscreenxlelements/img/favicon.png";
    }
	
	private void setMetadata(Screen s){
		System.out.println("setMetadata()");
		FsNode seriesNode = (FsNode) s.getProperty("seriesNode");
		JSONObject metadata = new JSONObject();
		
		HashMap<String, String> mappings = FieldMappings.getMappings();
		String provider = seriesNode.getProperty(FieldMappings.getSystemFieldName("provider"));
		
		if(!this.countriesForProviders.containsKey(provider)){
			FsNode providerNode = Fs.getNode("/domain/euscreenxl/user/" + provider + "/account/default");
			try{
				String fullProviderString = providerNode.getProperty("birthdata");
				this.countriesForProviders.put(provider, fullProviderString);
			}catch(NullPointerException npe){
				this.countriesForProviders.put(provider, provider);
			}
		}
		
		for(Iterator<String> i = seriesNode.getKeys(); i.hasNext();){
			String key = i.next();
			
			if(mappings.containsValue(key)){
				metadata.put(FieldMappings.getReadable(key), seriesNode.getProperty(key));
			}
		}
		
		metadata.put("provider", this.countriesForProviders.get(provider));
		
		System.out.println(metadata);
	}
	
	private void setVideos(Screen s){
		FSList videos = (FSList) s.getProperty("seriesVideos");
		List<FsNode> videosList = videos.getNodes();
		
		for(Iterator<FsNode> i = videosList.iterator(); i.hasNext();){
			FsNode video = i.next();
		}
		
	}
	
	private boolean inDevelMode() {
    	return LazyHomer.inDeveloperMode();
    }
	
	private String setEdnaMapping(String screenshot) {
		if(screenshot != null){
			if (!wantedna) {
				screenshot = screenshot.replace("edna/", "");
			} else {
				int pos = screenshot.indexOf("edna/");
				if 	(pos!=-1) {
					screenshot = "http://images.euscreenxl.eu/"+screenshot.substring(pos+5);
				}
			}
		}
		return screenshot;
	}
}
