package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	/*
	 * 
	 * Creates a profile node on neo4j given username, password, and full name
	 * 
	 */
	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.createUserProfile(params.get("userName"), params.get("fullName"),
				params.get("password"));
		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		return response;
	}

	/*
	 * Follow a friend given your username and friends username
	 */
	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = profileDriver.followFriend(userName, friendUserName);

		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}

		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		return response;
	}

	/*
	 * Gets all songs of user that you follow given your username
	 */
	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) throws IOException {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = profileDriver.getAllSongFriendsLike(userName);

		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}

		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	/*
	 * unfollow friend given your username and friends username
	 */
	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = profileDriver.unfollowFriend(userName, friendUserName);

		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}

		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		return response;
	}

	/*
	 * 
	 * Like song by adding relationship to it on neo4j given songs id and your
	 * username.
	 * 
	 */

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) throws IOException {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = playlistDriver.likeSong(userName, songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		if (dbQueryStatus.getMessage().equals("OK")) {
			RequestBody formBody = new FormBody.Builder().add("shouldDecrement", "false").build();
			String stringSongId = songId + "";
			Request putRequest = new Request.Builder()
					.url("http://localhost:3001/updateSongFavouritesCount/" + stringSongId).put(formBody).build();

			client.newCall(putRequest).execute().close();
		}

		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}

		return response;
	}

	/*
	 * 
	 * Unlike song by giving your username and songs id
	 * 
	 */

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) throws IOException {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = playlistDriver.unlikeSong(userName, songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		if (dbQueryStatus.getMessage().equals("OK")) {
			RequestBody formBody = new FormBody.Builder().add("shouldDecrement", "true").build();
			String stringSongId = songId + "";
			Request putRequest = new Request.Builder()
					.url("http://localhost:3001/updateSongFavouritesCount/" + stringSongId).put(formBody).build();

			client.newCall(putRequest).execute().close();
		}

		if (dbQueryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_ERROR_GENERIC) {
			response.put("status", dbQueryStatus.getMessage());
			return response;
		}

		return response;
	}

	/*
	 * 
	 * Delete a song from neo4j given its id
	 * 
	 */

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = playlistDriver.deleteSongFromDb(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		return response;
	}

	/*
	 * 
	 * Add a song to neo4j given song id, name, and song album
	 */

	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.createSong(params.get("songId"), params.get("songName"),
				params.get("songArtistFullName"), params.get("songAlbum"));
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), null);

		return response;
	}

	/*
	 * Deletes all nodes from neo 4j used for testing
	 * 
	 */
	@RequestMapping(value = "/deleteAllNodes", method = RequestMethod.POST)
	public void deleteEverything() {
		profileDriver.deleteEverything();

	}
}