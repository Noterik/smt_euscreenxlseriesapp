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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
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
		this.addReferid("history", "/euscreenxlelements/history");
		this.addReferid("viewer", "/euscreenxlelements/viewer");
		this.addReferid("ads", "/euscreenxlelements/ads");
		
		this.addReferidCSS("elements", "/euscreenxlelements/generic");
		this.addReferidCSS("bootstrap", "/euscreenxlelements/bootstrap");
		
		this.countriesForProviders = new HashMap<String, String>();
	}
	
	public void init(Screen s){
		//EXAMPLE SERIES ID: EUS_23670630327FACFCB5B6912617F95447
		String seriesId = s.getParameter("id");
		String uri = "/domain/euscreenxl/user/*/*";
		
		JSONObject startupParameters = new JSONObject();
		startupParameters.put("id", seriesId);
		s.putMsg("history", "", "setStartupParameters(" + startupParameters + ")");
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(seriesId.toLowerCase()); // find the item
		if (nodes!=null && nodes.size()>0) {
			FsNode seriesNode = (FsNode)nodes.get(0);
			FSList videos = FSListManager.get(seriesNode.getPath() + "/video");
			
			s.setProperty("seriesNode", seriesNode);
			s.setProperty("seriesVideos", videos);
			setMetadata(s);
			
			String activeId = s.getParameter("activeItem");
			FsNode activeVideo;
			if(activeId != null){
				activeVideo = videos.getNodesById(activeId).get(0);
			}else{
				activeVideo = videos.getNodes().get(0);
			}
			
			FsNode firstVideo = videos.getNodes().get(0);
			setActiveItem(s, activeVideo);
			
			getNextChunk(s);
			
			JSONObject socialSettings = new JSONObject();
			socialSettings.put("text", seriesNode.getProperty(FieldMappings.getSystemFieldName("series")));
			
			s.putMsg("social", "", "setSharingSettings(" + socialSettings + ")");
		}
		
		
	}
	
	public void doDesktopSpecificStuff(Screen s){
		System.out.println("doDesktopSpecificStuff()");
		s.putMsg("template", "", "createTooltips()");
	}
	
	public String getFavicon() {
        return "/eddie/apps/euscreenxlelements/img/favicon.png";
    }
	
	public void setActiveItem(Screen s, String paramsJSON){
		System.out.println("setActiveItem(" + paramsJSON + ")");
		JSONObject params = (JSONObject) JSONValue.parse(paramsJSON);
		String id = (String) params.get("id");
		FSList videos = (FSList) s.getProperty("seriesVideos");
		List<FsNode> videoList = videos.getNodesById(id);		
		FsNode node = videoList.get(0);
		
		setActiveItem(s, node);
	}
	
	public void setActiveItem(Screen s, FsNode node){
		FsNode actualVideo = Fs.getNode(node.getReferid());
		this.setViewer(s, actualVideo);
		
		JSONObject params = new JSONObject();
		params.put("id", actualVideo.getId());
		s.putMsg("episodelist", "", "setActiveItem(" + params + ")");
		s.putMsg("itempagelink", "", "setIdentifier(" + params + ")");
		
		JSONObject historyParams = new JSONObject();
		historyParams.put("activeItem", node.getId());
		s.putMsg("history", "", "setParameter(" + historyParams + ")");
		
		JSONObject titleParams = new JSONObject();
		titleParams.put("title", actualVideo.getProperty(FieldMappings.getSystemFieldName("title")));
		s.putMsg("episodetitle", "", "setTitle(" + titleParams + ")");
		
		s.putMsg("social", "", "urlChanged()");
	}
	
	public void requestAll(Screen s){
		JSONObject chunkJSON = new JSONObject();
		chunkJSON.put("items", getItems(s));
		chunkJSON.put("clear", true);
		s.putMsg("episodelist", "", "handleChunk(" + chunkJSON + ")");
	}
	
	private JSONArray getItems(Screen s){
		List<FsNode> videos = ((FSList) s.getProperty("seriesVideos")).getNodes();
		return getItems(s, 0, videos.size());
	}
	
	private JSONArray getItems(Screen s, int start, int stop){
		JSONArray items = new JSONArray();
		
		List<FsNode> videos = ((FSList) s.getProperty("seriesVideos")).getNodes();
		
		for(int i = start; i < stop; i++){
			FsNode video = videos.get(i);
			JSONObject videoJSON = new JSONObject();
			
			videoJSON.put("id", video.getId());
			videoJSON.put("title", video.getProperty(FieldMappings.getSystemFieldName("title")));
			videoJSON.put("screenshot", this.setEdnaMapping(video.getProperty(FieldMappings.getSystemFieldName("screenshot"))));
			items.add(videoJSON);
		}
		
		return items;
	}
			
	public void getNextChunk(Screen s){
		Integer lastChunk = (Integer) s.getProperty("chunk");
		Integer chunk;
		if(lastChunk == null){
			lastChunk = 0;
			chunk = 1;
		}else{
			chunk = lastChunk + 1;
		}
		s.setProperty("chunk", chunk);
		
		JSONObject chunkJSON = new JSONObject();		
		List<FsNode> videos = ((FSList) s.getProperty("seriesVideos")).getNodes();
		
		int start = lastChunk * 9;
		int stop = chunk * 9;
		
		if(stop > videos.size()){
			stop = videos.size();
			s.putMsg("episodelist", "", "hideShowMore()");
		}
		
		JSONArray items = this.getItems(s, start, stop);
		chunkJSON.put("items", items);
		
		s.putMsg("episodelist", "", "handleChunk(" + chunkJSON + ")");
	};
	
	private void setMetadata(Screen s){
		System.out.println("setMetadata()");
		FsNode seriesNode = (FsNode) s.getProperty("seriesNode");
		JSONObject metadata = new JSONObject();
		
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("seriesEnglish");
		fields.add("series");
		fields.add("provider");
		fields.add("year");
		fields.add("language");
		fields.add("summaryEnglish");
		fields.add("publisher");
		fields.add("broadcastChannel");
		fields.add("broadcastDate");
		fields.add("lastBroadcastDate");
		fields.add("lastProductionYear");
		fields.add("contributors");
		fields.add("extendedDescription");
		fields.add("furtherInformation");
		fields.add("keywords");
		fields.add("materialType");
		fields.add("itemType");
		fields.add("landingPageURL");
		
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
		
		for(Iterator<String> i = fields.iterator(); i.hasNext();){
			String field = i.next();
			String fieldValue = seriesNode.getProperty(FieldMappings.getSystemFieldName(field));
			if(mappings.containsKey(field) && fieldValue != null){
				metadata.put(field, fieldValue);
			}else{
				metadata.put(field, "-");
			}
		}
		
		metadata.put("id", seriesNode.getId());
		metadata.put("provider", this.countriesForProviders.get(provider));
		
		s.putMsg("metadata", "", "setMetadata(" + metadata + ")");
		
		System.out.println(metadata);
	}
	
	private void setViewer(Screen s){
		FSList videos = (FSList) s.getProperty("seriesVideos");
		List<FsNode> videosList = videos.getNodes();
		
		FsNode firstVideo = videosList.get(0);
		String referId = firstVideo.getReferid();
		FsNode actualVideo = Fs.getNode(referId);
		setViewer(s, actualVideo);
	}
	
	private void setViewer(Screen s, FsNode node){
		System.out.println("EuscreenxlseriesApplication.startViewer()");
		System.out.println("NODE PATH: " + node.getPath());
				
		String name = node.getName();
		
						
		if(name.equals("video")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawvideo/1");
			String[] videos = rawNode.getProperty("mount").split(",");
			JSONObject objectToSend = new JSONObject();
			JSONArray sourcesArray = new JSONArray();
			String extension = rawNode.getProperty("extension");
			objectToSend.put("screenshot", this.setEdnaMapping(node.getProperty(FieldMappings.getSystemFieldName("screenshot"))));
			objectToSend.put("aspectRatio", node.getProperty(FieldMappings.getSystemFieldName("aspectRatio")));
			objectToSend.put("sources", sourcesArray);
				
			for(int i = 0; i < videos.length; i++){
				JSONObject src = new JSONObject();
				String video = videos[i];
				
				if (video.indexOf("http://")==-1) {
					video = "http://" + video + ".noterik.com/progressive/" + video + "/" + node.getPath() + "/rawvideo/1/raw."+ extension;
				}
				
				String mime = "video/mp4";
				src.put("src", video);
				src.put("mime", mime);
				sourcesArray.add(src);
			}
			s.putMsg("viewer", "", "setVideo(" + objectToSend + ")");
		}else if(name.equals("audio")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawaudio/1");
			String audio = rawNode.getProperty("mount");
			String extension = rawNode.getProperty("extension");
			String mimeType = "audio/mpeg";
			if(!audio.startsWith("http://")) {
				audio = "http://" + audio + ".noterik.com" + node.getPath() + "/rawaudio/1/raw." + extension;
				if(extension.equalsIgnoreCase("wav")) {
					mimeType = "audio/wav";
				} else if(extension.equalsIgnoreCase("ogg")) {
					mimeType = "audio/ogg";
				}
			}
			JSONObject objectToSend = new JSONObject();
			objectToSend.put("mime", mimeType);
			objectToSend.put("audio", audio);;
			s.putMsg("viewer", "", "setAudio(" + objectToSend + ")");
		}else if(name.equals("picture")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawpicture/1");
			String rawpicture = rawNode.getProperty("mount");
			String extension = rawNode.getProperty("extension");
			if(!rawpicture.startsWith("http://")) {
				rawpicture = "http://" + rawpicture + ".noterik.com" + node.getPath() + "/rawaudio/1/raw." + extension;
			} else {
				if(rawpicture.contains("/edna")) {
					rawpicture = rawpicture.replace("/edna", "");
				}
			}
			String picture = node.getProperty(FieldMappings.getSystemFieldName("screenshot"));
			if(picture==null) {
				picture=rawpicture;
			} else {
				if(picture.contains("/edna")) {
					picture = picture.replace("/edna", "");
				}
			}
			JSONObject objectToSend = new JSONObject();
			objectToSend.put("src", picture);
			objectToSend.put("alt", node.getProperty(FieldMappings.getSystemFieldName("title")));
			s.putMsg("viewer", "", "setPicture(" + objectToSend + ")");
		}else if(name.equals("doc")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawdoc/1");
			String doc = rawNode.getProperty("mount");
			String extension = rawNode.getProperty("extension");
			if(!doc.startsWith("http://")) {
				doc = "http://" + doc + ".noterik.com" + node.getPath() + "/rawaudio/1/raw." + extension;
			}
			JSONObject objectToSend = new JSONObject();
			objectToSend.put("src", doc);
			s.putMsg("viewer", "", "setDoc(" + objectToSend + ")");
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
