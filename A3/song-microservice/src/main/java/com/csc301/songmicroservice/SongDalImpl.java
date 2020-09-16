package com.csc301.songmicroservice;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	/**
	 * adds songs to mongodb given Song class
	 */
	@Override
	public DbQueryStatus addSong(Song songToAdd) {

		if (songToAdd.getSongName().isEmpty() || songToAdd.getSongArtistFullName().isEmpty()) {
			DbQueryStatus status = new DbQueryStatus("SONG NAME CANT BE EMPTY", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return status;
		}

		db.save(songToAdd);
		DbQueryStatus status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return status;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		Song song = db.findById(songId, Song.class);
		DbQueryStatus status = null;
		if (song != null) {
			status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			status.setData(song);
		} else {
			status = new DbQueryStatus("NOT_FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		return status;
	}

	/**
	 * Gets song title by id from mongodb given songId
	 */
	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		Song song = db.findById(songId, Song.class);
		DbQueryStatus status = null;
		if (song != null) {
			status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			status.setData(song.getSongName());
		} else {
			status = new DbQueryStatus("NOT_FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}

		return status;
	}

	/**
	 * Deletes song from mongodb given songId
	 */
	@Override
	public DbQueryStatus deleteSongById(String songId) {
		Song song = db.findById(songId, Song.class);
		DbQueryStatus status = null;
		if (song != null) {
			status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			db.remove(song);
		} else {
			status = new DbQueryStatus("NOT_FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}

		return status;
	}

	/**
	 * Updates favourite count given songId and boolean shouldDecrement
	 */
	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		Song song = db.findById(songId, Song.class);
		DbQueryStatus status = null;
		if (song != null) {

			if (shouldDecrement) {
				if (song.getSongAmountFavourites() == 0) {
					status = new DbQueryStatus("Favorite count cant be negative",
							DbQueryExecResult.QUERY_ERROR_GENERIC);
					return status;
				}
				song.setSongAmountFavourites(song.getSongAmountFavourites() - 1);
				db.save(song);
			} else {
				song.setSongAmountFavourites(song.getSongAmountFavourites() + 1);
				db.save(song);

			}
			status = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		} else {
			status = new DbQueryStatus("NOT_FOUND", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}
		return status;
	}
}