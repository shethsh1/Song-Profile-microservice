package com.csc301.profilemicroservice;

import static org.neo4j.driver.v1.Values.parameters;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	/**
	 * Given userName and songId adds relationship to user and song using neo4j
	 * 
	 */
	@Override
	public DbQueryStatus likeSong(String userName, String songId) {

		Session session = driver.session();
		StatementResult result = null;

		result = session.run("MATCH (a:profile {userName: $userName}) return a", parameters("userName", userName));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Username doesn't exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:song {songId:$songId}) return a", parameters("songId", songId));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("songId not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:`" + userName + "-favorites`)" + "-[r:includes]- (b:song {songId:$songId}) return a",
				parameters("songId", songId));

		if (result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Song is already liked", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:`" + userName + "-favorites`)" + ", (b:song {songId:$songId}) MERGE (a)-[r:includes]->(b)",
				parameters("userName", userName, "songId", songId));
		session.close();
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return status;
	}

	/**
	 * Given userName and songId removes relationship for user and song with id
	 * Using neo4j
	 */
	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {

		Session session = driver.session();
		StatementResult result = null;

		result = session.run("MATCH (a:profile {userName: $userName}) return a", parameters("userName", userName));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Username doesn't exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run("MATCH (a:song {songId:$songId}) return a", parameters("songId", songId));
		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("songId not found", DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:`" + userName + "-favorites`)" + "-[r:includes]- (b:song {songId:$songId}) return a",
				parameters("songId", songId));

		if (!result.hasNext()) {
			DbQueryStatus status = new DbQueryStatus("Song is already not liked",
					DbQueryExecResult.QUERY_ERROR_GENERIC);
			session.close();
			return status;
		}

		result = session.run(
				"MATCH (a:`" + userName + "-favorites`)" + "-[r:includes]- (b:song {songId:$songId}) DELETE r",
				parameters("songId", songId));
		session.close();
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return status;

	}

	/**
	 * Given songId deletes it from the neo4j database
	 * 
	 */
	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {

		Session session = driver.session();
		StatementResult result = null;

		result = session.run("MATCH (a:song {songId:$songId}) DETACH delete a", parameters("songId", songId));
		session.close();
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return status;

	}
}
