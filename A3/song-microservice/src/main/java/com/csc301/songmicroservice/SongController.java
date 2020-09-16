package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

	@Autowired
	private final SongDal songDal;

	private OkHttpClient client = new OkHttpClient();

	public SongController(SongDal songDal) {
		this.songDal = songDal;
	}

	/*
	 * Gets the song from mongodb given the song id
	 */
	@RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = songDal.findSongById(songId);

		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;
	}

	/*
	 * Gets the title of the song given song id
	 */
	@RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("GET %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		return response;

	}

	/*
	 * delete Song from database given its id
	 */
	@RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
	public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
			HttpServletRequest request) throws IOException {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("DELETE %s", Utils.getUrl(request)));

		DbQueryStatus dbQueryStatus = songDal.deleteSongById(songId);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());

		if (dbQueryStatus.getMessage().equals("OK")) {

			RequestBody formBody = new FormBody.Builder().build();

			Request putRequest = new Request.Builder().url("http://localhost:3002/deleteAllSongsFromDb/" + songId)
					.put(formBody).build();

			client.newCall(putRequest).execute().close();
			client.connectionPool().evictAll();

		}

		return response;
	}

	/*
	 * Add songs given song name, artist name, and which album it belongs to.
	 */
	@RequestMapping(value = "/addSong", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
			HttpServletRequest request) throws IOException {

		Map<String, Object> response = new HashMap<String, Object>();

		Song song = new Song(params.get("songName"), params.get("songArtistFullName"), params.get("songAlbum"));
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus status = songDal.addSong(song);

		if (status.getMessage().equals("OK")) {
			response.put("data", song.getJsonRepresentation());
			RequestBody formBody = new FormBody.Builder().add("songId", song.getId())
					.add("songName", song.getSongName()).add("songArtistFullName", song.getSongArtistFullName())
					.add("songAlbum", song.getSongAlbum()).build();

			Request postRequest = new Request.Builder().url("http://localhost:3002/addSong").post(formBody).build();

			client.newCall(postRequest).execute().close();
			response.put("status", status.getMessage());

		} else {
			response.put("status", status.getMessage());
		}

		return response;
	}

	/*
	 * Adds +1 or -1 on a sing given its id and whether its true or false on
	 * decrease
	 * 
	 */
	@RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> updateFavouritesCount(@PathVariable("songId") String songId,
			@RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("data", String.format("PUT %s", Utils.getUrl(request)));

		boolean decrement = false;
		if (shouldDecrement.equals("true")) {
			decrement = true;
		}
		DbQueryStatus dbQueryStatus = songDal.updateSongFavouritesCount(songId, decrement);
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		response.put("message", dbQueryStatus.getMessage());

		return response;
	}
}