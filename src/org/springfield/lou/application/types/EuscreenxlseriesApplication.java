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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import jakarta.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springfield.fs.FSList;
import org.springfield.fs.FSListManager;
import org.springfield.fs.Fs;
import org.springfield.fs.FsNode;
import org.springfield.lou.application.Html5Application;
import org.springfield.lou.euscreen.config.Config;
import org.springfield.lou.euscreen.config.ConfigEnvironment;
import org.springfield.lou.euscreen.config.SettingNotExistException;
import org.springfield.lou.homer.LazyHomer;
import org.springfield.lou.screen.Screen;


public class EuscreenxlseriesApplication extends Html5Application{
	
	private boolean wantedna = true;
	private HashMap<String, String> countriesForProviders;
	private Config config;
	public String ipAddress="";
	public static boolean isAndroid;
	
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
		this.addReferid("analytics", "/euscreenxlelements/analytics");
		this.addReferid("config", "/euscreenxlelements/config");
		this.addReferid("urltransformer", "/euscreenxlelements/urltransformer");
		
		this.addReferidCSS("fontawesome", "/euscreenxlelements/fontawesome");
		this.addReferidCSS("bootstrap", "/euscreenxlelements/bootstrap");
		this.addReferidCSS("theme", "/euscreenxlelements/theme");
		this.addReferidCSS("genericadditions", "/euscreenxlelements/generic");
		this.addReferidCSS("all", "/euscreenxlelements/all");
		this.addReferidCSS("terms", "/euscreenxlelements/terms");
		
		this.countriesForProviders = new HashMap<String, String>();
		
	}
	
	public void init(Screen s){
		System.out.println("EuscreenxlseriesApplication.init()");
		//EXAMPLE SERIES ID: EUS_23670630327FACFCB5B6912617F95447
		String seriesId = s.getParameter("id");
		String uri = "/domain/euscreenxl/user/*/*";
		s.setProperty("orderDirection", "up");
		System.out.println("seriesId = "+seriesId);
		
		JSONObject startupParameters = new JSONObject();
		startupParameters.put("id", seriesId);
		s.putMsg("history", "", "setStartupParameters(" + startupParameters + ")");
		FSList fslist = FSListManager.get(uri);
		List<FsNode> nodes = fslist.getNodesFiltered(seriesId.toLowerCase()); // find the item
		
		FsNode seriesNode = null;
		
		//find the series node
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			FsNode n = (FsNode)iter.next();	
			System.out.println("node path "+n.getPath());
			if (n.getPath().contains("/series/")) {
				seriesNode = n;
				break;
			}
		}
		
		if (seriesNode != null) {
			FSList videos = FSListManager.get(seriesNode.getPath() + "/video");	
			FSList audios = FSListManager.get(seriesNode.getPath() + "/audio");
			FSList pictures = FSListManager.get(seriesNode.getPath() + "/picture");
			addOrderFieldEpisodes(videos);
			addOrderFieldEpisodes(audios);
			addOrderFieldEpisodes(pictures);
			
			if(!this.inDevelMode()){
				videos = filterPublicEpisodes(videos);
				audios = filterPublicEpisodes(audios);
				pictures = filterPublicEpisodes(pictures);
			}
			
			s.setProperty("seriesNode", seriesNode);

			if(videos.size() > 0){
			    	s.setProperty("seriesVideos", videos);
			    	s.setProperty("seriesType", "video");
			    	System.out.println("SeriesType = "+s.getProperty("seriesType"));

			    	String activeId = s.getParameter("activeItem");
				FsNode activeVideo;
				if(activeId != null){
					activeVideo = videos.getNodesById(activeId).get(0);
				}else{
					activeVideo = videos.getNodes().get(0);
				}
				
				FsNode firstVideo = this.getSortedEpisodes(s).get(0);
				setActiveItem(s, activeVideo);
				
				getNextChunk(s);
			} else if(audios.size() > 0){
			    	s.setProperty("seriesVideos", audios);
			    	s.setProperty("seriesType", "audio");
			    	System.out.println("SeriesType = "+s.getProperty("seriesType"));
			    	
			    	String activeId = s.getParameter("activeItem");
				FsNode activeVideo;
				if(activeId != null){
					activeVideo = audios.getNodesById(activeId).get(0);
				}else{
					activeVideo = audios.getNodes().get(0);
				}
				
				FsNode firstVideo = this.getSortedEpisodes(s).get(0);
				setActiveItem(s, activeVideo);
				
				getNextChunk(s);
			} else if (pictures.size() > 0) {
				s.setProperty("seriesVideos", pictures);
		    	s.setProperty("seriesType", "picture");
		    	System.out.println("SeriesType = "+s.getProperty("seriesType"));
		    	
		    	String activeId = s.getParameter("activeItem");
		    	FsNode activeVideo;
		    	if(activeId != null){
		    		activeVideo = pictures.getNodesById(activeId).get(0);
		    	}else{
		    		activeVideo = pictures.getNodes().get(0);
		    	}
			
		    	FsNode firstVideo = this.getSortedEpisodes(s).get(0);
		    	setActiveItem(s, activeVideo);
		    	
		    	getNextChunk(s);
			}
			
			setMetadata(s);
			
			JSONObject socialSettings = new JSONObject();
			socialSettings.put("text", seriesNode.getProperty(FieldMappings.getSystemFieldName("series")));
			
			try {
				if(this.inDevelMode()){
					config = new Config(ConfigEnvironment.DEVEL);
				}else{
					config = new Config(ConfigEnvironment.PROD);
				}
			}catch(SettingNotExistException snee){
				snee.printStackTrace();
			}
			s.putMsg("urltransformer", "", "run()");
			try {
				s.putMsg("config", "", "update(" + this.config.getSettingsJSON() + ")");
			} catch (SettingNotExistException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			s.putMsg("social", "", "setSharingSettings(" + socialSettings + ")");
		}
		
		if(!this.inDevelMode()){
			System.out.println("NOT IN DEVEL, DISABLE STUFF");
			s.putMsg("linkinterceptor", "", "interceptLinks()");
			s.putMsg("template", "", "hideBookmarking()");
		}
		
	}
	
	private FSList filterPublicEpisodes(FSList fslist) {
		System.out.println("filterPublicEpisodes()");
		FSList filteredList = new FSList();
		List<FsNode> nodes = fslist.getNodes();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();
			System.out.println("PUBLIC: " + n.getProperty("public"));
			if(n.getProperty("public") != null && n.getProperty("public").equals("true")){
				filteredList.addNode(n);
			}
		}
		
		return filteredList;
	}
	
	private void addOrderFieldEpisodes(FSList fslist) {
		int basecounter = 1;
		List<FsNode> nodes = fslist.getNodes();
		for(Iterator<FsNode> iter = nodes.iterator() ; iter.hasNext(); ) {
			// get the next node
			FsNode n = (FsNode)iter.next();
			int value = basecounter++;
			int evalue = -1;
			int svalue = -1;
			try {
				String tmp = n.getProperty("episodeNumber");
				if (tmp!=null) {
					tmp = tmp.replace("OC", "00");
					System.out.println("EPI="+tmp);
					evalue = Integer.parseInt(tmp);
				}
			} catch(Exception e) {}
			try {
				String tmp = n.getProperty("Series/season");
				if (tmp!=null) {
					System.out.println("SEA="+tmp);
					svalue = Integer.parseInt(tmp);
				}
			} catch(Exception e) {}
			
			if (evalue!=-1) {
				if (svalue!=-1) {
					value = evalue*10000;
					value += svalue*1000000;
				} else {
					value = evalue*10000;
				}
			} 
			
			String valueString = String.format("%010d", value);
			System.out.println("N="+n.getId()+" V="+valueString);
			n.setProperty("ordervalue", valueString);
		}
	}
	
	public void setDeviceMobile(Screen s){
		JSONObject params = new JSONObject();
		params.put("device", "mobile");
		s.putMsg("viewer", "", "setDevice(" + params + ")");
		s.putMsg("template", "", "setDevice(" + params + ")");
		s.putMsg("social", "", "setDevice(" + params + ")");
	}
	
	public void setDeviceIpad(Screen s){
		JSONObject params = new JSONObject();
		params.put("device", "ipad");
		s.putMsg("viewer", "", "setDevice(" + params + ")");
		s.putMsg("template", "", "setDevice(" + params + ")");
		s.putMsg("social", "", "setDevice(" + params + ")");
	}
	
	public void doDesktopSpecificStuff(Screen s){
		System.out.println("doDesktopSpecificStuff()");
		s.putMsg("template", "", "createTooltips()");
	}
		
	public String getFavicon() {
        return "/eddie/apps/euscreenxlelements/img/favicon.png";
    }
	
	public void setActiveItem(Screen s, int number){
		
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
	
	public void changeSorting(Screen s, String direction){
		s.setProperty("orderDirection", direction);
		s.setProperty("chunk", 0);
		s.putMsg("episodelist", "", "clear()");
		this.getNextChunk(s);
	}
	
	private List<FsNode> getSortedEpisodes(Screen s){
		System.out.println("getSortedEpisodes()");
		String direction = (String) s.getProperty("orderDirection");
		System.out.println("DIRECTION: " + direction);
		return ((FSList) s.getProperty("seriesVideos")).getNodesSorted("ordervalue", direction);
	}
	
	private JSONArray getItems(Screen s){
		List<FsNode> videos = ((FSList) s.getProperty("seriesVideos")).getNodes();
		return getItems(s, 0, videos.size());
	}
	
	private JSONArray getItems(Screen s, int start, int stop){
		JSONArray items = new JSONArray();
		
		List<FsNode> videos = this.getSortedEpisodes(s);
		
		for(int i = start; i < stop; i++){
			FsNode video = videos.get(i);
			JSONObject videoJSON = new JSONObject();
			videoJSON.put("id", video.getId());
			videoJSON.put("title", video.getProperty(FieldMappings.getSystemFieldName("title")));
			videoJSON.put("screenshot", this.setEdnaMapping(video.getProperty(FieldMappings.getSystemFieldName("screenshot"))));
			videoJSON.put("type", s.getProperty("seriesType"));
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
		fields.add("summaryOriginal");
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
				
		String path = seriesNode.getPath();
		String[] splits = path.split("/");
		String provider = splits[4];
		
		if(!this.countriesForProviders.containsKey(provider)){
			FsNode providerNode = Fs.getNode("/domain/euscreenxl/user/" + provider + "/account/default");
			try{
				String fullProviderString = providerNode.getProperty("birthdata");
				this.countriesForProviders.put(provider, fullProviderString);
			}catch(NullPointerException npe){
				this.countriesForProviders.put(provider, seriesNode.getProperty(FieldMappings.getSystemFieldName("provider")));
			}
		}
		
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
		
		String p = node.getPath();
		String[] splits = p.split("/");
		String provider = splits[4];
						
		if(name.equals("video")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawvideo/1");
			String[] videos = rawNode.getProperty("mount").split(",");
			JSONObject objectToSend = new JSONObject();
			JSONArray sourcesArray = new JSONArray();
			String extension = rawNode.getProperty("extension");
			objectToSend.put("screenshot", this.setEdnaMapping(node.getProperty(FieldMappings.getSystemFieldName("screenshot"))));
			objectToSend.put("aspectRatio", node.getProperty(FieldMappings.getSystemFieldName("aspectRatio")));
			objectToSend.put("sources", sourcesArray);
				
			//for(int i = 0; i < videos.length; i++){ //Temp workaround to only have 1 video instead of multiple
			//This to prevent downloading of the second stream as the browser only plays out the first stream.
			for (int i = 0; i < 1; i++) { 
				JSONObject src = new JSONObject();
				String video = videos[i];
				
				if (video.indexOf("http://") == -1 && video.indexOf("https://") == -1) {
					Random randomGenerator = new Random();
					Integer random= randomGenerator.nextInt(100000000);
					String ticket = Integer.toString(random);

					String videoFile= "/" + video + node.getPath()+ "/rawvideo/1/raw."+ extension;
					
					try{						
						//System.out.println("CallingSendTicket");						
						sendTicket(videoFile,ipAddress,ticket);}
					catch (Exception e){}
					
					video = "https://" + video + ".noterik.com/progressive/" + video + node.getPath() + "/rawvideo/1/raw."+ extension+"?ticket="+ticket;
				} else if (video.indexOf(".noterik.com/progressive/") > -1) {
					Random randomGenerator = new Random();
					Integer random= randomGenerator.nextInt(100000000);
					String ticket = Integer.toString(random);
					
					String videoFile = video.substring(video.indexOf("progressive")+11);
					videoFile = videoFile.indexOf("http://") == 0 ? videoFile.replaceFirst("http", "https") : videoFile;
					
					try{						
						//System.out.println("CallingSendTicket");						
						sendTicket(videoFile,ipAddress,ticket);}
					catch (Exception e){}
					
					video = video.startsWith("http://") ? video.replaceFirst("http", "https") : video;
					video = video+"?ticket="+ticket;

				}
				
				FsNode maggieNode = Fs.getNode(node.getPath());
				String duration = maggieNode.getProperty(FieldMappings.getSystemFieldName("duration"));
				src.put("duration",""+timeToSeconds(duration));
				src.put("maggieid", maggieNode.getPath());
				
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
			if(!audio.startsWith("http://") && !audio.startsWith("https://")) {
				audio = "http://" + audio + ".noterik.com" + node.getPath() + "/rawaudio/1/raw." + extension;
				if(extension.equalsIgnoreCase("wav")) {
					mimeType = "audio/wav";
				} else if(extension.equalsIgnoreCase("ogg")) {
					mimeType = "audio/ogg";
				}
			}
			JSONObject objectToSend = new JSONObject();
			objectToSend.put("mime", mimeType);
			objectToSend.put("src", audio);
			objectToSend.put("provider", provider);
			
			FsNode maggieNode = Fs.getNode(node.getPath());
			String duration = maggieNode.getProperty(FieldMappings.getSystemFieldName("duration"));
			System.out.println("DURATION="+timeToSeconds(duration));
			objectToSend.put("duration",""+timeToSeconds(duration));
			objectToSend.put("maggieid", maggieNode.getPath());
			
			s.putMsg("viewer", "", "setAudio(" + objectToSend + ")");
		}else if(name.equals("picture")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawpicture/1");
			String rawpicture = rawNode.getProperty("mount");
			String extension = rawNode.getProperty("extension");
			if(!rawpicture.startsWith("http://") && !rawpicture.startsWith("https://")) {
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
			
			System.out.println("Setting picture "+objectToSend);
			s.putMsg("viewer", "", "setPicture(" + objectToSend + ")");
		}else if(name.equals("doc")){
			FsNode rawNode = Fs.getNode(node.getPath() + "/rawdoc/1");
			String doc = rawNode.getProperty("mount");
			String extension = rawNode.getProperty("extension");
			if(!doc.startsWith("http://") && !doc.startsWith("https://")) {
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
				screenshot = screenshot.startsWith("http://") ? screenshot.replaceFirst("http", "https")  : screenshot;
			} else {
				int pos = screenshot.indexOf("edna/");
				if 	(pos!=-1) {
					screenshot = "https://images.euscreenxl.eu/"+screenshot.substring(pos+5);
				} else {
					screenshot = screenshot.startsWith("http://") ? screenshot.replaceFirst("http", "https")  : screenshot;
				}
			}
		}
		return screenshot;
	}
	
	public String getMetaHeaders(HttpServletRequest request) {
		
		ipAddress=getClientIpAddress(request);
		
		String browserType = request.getHeader("User-Agent");
		if(browserType.indexOf("Mobile") != -1) {
			String ua = request.getHeader("User-Agent").toLowerCase();
			isAndroid = ua.indexOf("android") > -1; //&& ua.indexOf("mobile");	
		}
		
		return "";
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	//Themis NISV
	/////////////////////////////////////////////////////////////////////////////////////
	private static void sendTicket(String videoFile, String ipAddress, String ticket) throws IOException {
		URL serverUrl = new URL("http://ticket.noterik.com:8080/lenny/acl/ticket");
		HttpURLConnection urlConnection = (HttpURLConnection)serverUrl.openConnection();

		Long Sytime = System.currentTimeMillis();
		Sytime = Sytime / 1000;
		String expiry = Long.toString(Sytime+(15*60));
		
		// Indicate that we want to write to the HTTP request body
		
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Content-Type", "text/xml");
		urlConnection.setRequestMethod("POST");
		videoFile=videoFile.substring(1);

		//System.out.println("I send this video address to the ticket server:"+videoFile);
		//System.out.println("And this ticket:"+ticket);
		//System.out.println("And this EXPIRY:"+expiry);
		
		// Writing the post data to the HTTP request body
		BufferedWriter httpRequestBodyWriter = 
		new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
		String content="";
		if (isAndroid){
			content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>4</maxRequests></properties></fsxml>";
			isAndroid=false;
			//System.out.println("Android ticket!");
		}
		else {
			content = "<fsxml><properties><ticket>"+ticket+"</ticket>"
			+ "<uri>/"+videoFile+"</uri><ip>"+ipAddress+"</ip> "
			+ "<role>user</role>"
			+ "<expiry>"+expiry+"</expiry><maxRequests>1</maxRequests></properties></fsxml>";
		}
		//System.out.println("sending content!!!!"+content);
		httpRequestBodyWriter.write(content);
		httpRequestBodyWriter.close();

		// Reading from the HTTP response body
		Scanner httpResponseScanner = new Scanner(urlConnection.getInputStream());
		while(httpResponseScanner.hasNextLine()) {
				System.out.println(httpResponseScanner.nextLine());
		}
		httpResponseScanner.close();		
	}

	private static final String[] HEADERS_TO_TRY = { 
		"X-Forwarded-For",
		"Proxy-Client-IP",
		"WL-Proxy-Client-IP",
		"HTTP_X_FORWARDED_FOR",
		"HTTP_X_FORWARDED",
		"HTTP_X_CLUSTER_CLIENT_IP",
		"HTTP_CLIENT_IP",
		"HTTP_FORWARDED_FOR",
		"HTTP_FORWARDED",
		"HTTP_VIA",
		"REMOTE_ADDR" 
	}; 

	public static String getClientIpAddress(HttpServletRequest request) {
		for (String header : HEADERS_TO_TRY) {
			String ip = request.getHeader(header);
			if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
				return ip;
			}
		}
		return request.getRemoteAddr();
	}
	
	private int timeToSeconds(String time) {
		String[] parts = time.split(":");
		if (parts.length==3) {
			try {
				int sec = Integer.parseInt(parts[2]);
				int min = Integer.parseInt(parts[1]);
				int hour = Integer.parseInt(parts[0]);
				return (sec+(min*60)+(hour*3600));
			} catch(Exception e) {
				return 3600; // default to a hour?
			}
		} else if (parts.length==2) {
			try {
				int sec = Integer.parseInt(parts[1]);
				int min = Integer.parseInt(parts[0]);
				return (sec+(min*60));
			} catch(Exception e) {
				return 3600; // default to a hour?
			}
		}
		return 3600;
	}
}
