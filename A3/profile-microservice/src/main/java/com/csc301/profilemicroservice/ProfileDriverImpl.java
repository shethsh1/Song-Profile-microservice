package com.csc301.profilemicroservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.neo4j.driver.v1.Transaction;
import static org.neo4j.driver.v1.Values.parameters;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}

	/**
	 * Given userName and fullName and password creates user node using neo4j
	 */
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {

		if (userName == null || userName.isEmpty()) {
			DbQueryStatus status = new DbQueryStatus("Username cant be empty", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return status;
		} else if (fullName == null || fullName.isEmpty()) {
			DbQueryStatus status = new DbQueryStatus("Full name cant be empty", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return status;
		} else if (password == null || password.isEmpty()) {
			DbQueryStatus status = new DbQueryStatus("Password cant be empty", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return status;
		}

		Session session = driver.session();

		StatementResult result = session.run("MATCH (n:profile {userName: $userName}) RETURN n",
				parameters("userName", userName));
		if (result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Username already exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"CREATE (n:profile {userName: $userName, fullName: $fullName, password: $password}) RETURN n.name, n.id",
				parameters("userName", userName, "fullName", fullName, "password", password));

		result = session.run("CREATE (n:`" + userName + "-favorites`)", parameters("userName", userName));

		result = session.run("MATCH (a:profile {userName:$userName}), (b:`" + userName + "-favorites`)"
				+ "MERGE (a)-[r:created]->(b)", parameters("userName", userName));

		session.close();

		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);

		return status;
	}

	/**
	 * Given userName and friends userNames creates relationship follows between
	 * them using neo4j
	 */
	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {

		StatementResult result = null;
		Session session = driver.session();

		if (userName.equals(frndUserName)) {
			DbQueryStatus status = new DbQueryStatus("Cant follow yourself", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:profile {userName: $userName}) return a", parameters("userName", userName));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Username doesn't exist exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:profile {userName: $frndUserName}) return a",
				parameters("frndUserName", frndUserName));

		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("friend username doesn't exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:profile { userName: $userName })-[r:follows]->(b:profile { userName: $frndUserName}) return a",
				parameters("userName", userName, "frndUserName", frndUserName));

		if (result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("relationship already exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:profile {userName:$userName}), (b:profile {userName:$frndUserName}) MERGE (a)-[r:follows]->(b)",
				parameters("userName", userName, "frndUserName", frndUserName));

		session.close();

		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		status.setData(result);

		return status;
	}

	/**
	 * Given userName and frndUserName removes relationship follows between each
	 * other if there is one.
	 */
	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {

		StatementResult result = null;
		Session session = driver.session();

		if (userName.equals(frndUserName)) {
			DbQueryStatus status = new DbQueryStatus("Cant unfollow yourself", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:profile {userName: $userName}) return a", parameters("userName", userName));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Username doesn't exist exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:profile {userName: $frndUserName}) return a",
				parameters("frndUserName", frndUserName));

		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("friend username doesn't exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:profile { userName: $userName })-[r:follows]->(b:profile { userName: $frndUserName}) return a",
				parameters("userName", userName, "frndUserName", frndUserName));

		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("relationship doesn't exist",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:profile { userName: $userName })-[r:follows]->(b:profile { userName: $frndUserName}) DELETE r",
				parameters("userName", userName, "frndUserName", frndUserName));

		session.close();

		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		status.setData(result);

		return status;
	}

	/**
	 * Given userName gets all songs friends have included in their playlist
	 */
	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) throws IOException {

		Session session = driver.session();
		OkHttpClient client = new OkHttpClient();
		StatementResult result = null;

		result = session.run("MATCH (a:profile {userName: $userName}) return a", parameters("userName", userName));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("NOT FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			return status;
		}

		result = session.run("MATCH (a:profile {userName: $userName}) -[:follows]-> (b:profile) RETURN b",
				parameters("userName", userName));

		StatementResult songs = null;
		HashMap<String, List<String>> data = new HashMap<String, List<String>>();

		while (result.hasNext()) {
			String currUserName = result.next().get(0).asNode().get("userName").asString();
			List<String> titles = new ArrayList<String>();

			songs = session.run("MATCH (a:`" + currUserName + "-favorites`)-[r:includes]->(b:song) return b");
			while (songs.hasNext()) {
				String currId = songs.next().get(0).asNode().get("songId").asString();

				Request putRequest = new Request.Builder().url("http://localhost:3001/getSongTitleById/" + currId)
						.build();

				ResponseBody response = client.newCall(putRequest).execute().body();
				String resStr = response.string();
				JSONObject json = new JSONObject(resStr);
				titles.add(json.get("data").toString());
				response.close();

			}
			data.put(currUserName, titles);
		}
		session.close();

		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		status.setData(data);

		return status;
	}

	/**
	 * given songId, songName, songArtistFullName, songAlbum creates a neo4j song
	 * node
	 */
	public DbQueryStatus createSong(String songId, String songName, String songArtistFullName, String songAlbum) {
		// TODO Auto-generated method stub

		Session session = driver.session();
		StatementResult result = session.run(
				"CREATE (n:song {songId: $songId, songName: $songName, songArtistFullName: $songArtistFullName, songAlbum: $songAlbum})",
				parameters("songId", songId, "songName", songName, "songArtistFullName", songArtistFullName,
						"songAlbum", songAlbum));

		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		session.close();
		status.setData(result);

		return status;

	}

	/**
	 * Deletes all nodes - used for testing
	 */
	public void deleteEverything() {
		// TODO Auto-generated method stub

		Session session = driver.session();
		session.run("MATCH (n) detach delete n");
		session.close();

	}

}
