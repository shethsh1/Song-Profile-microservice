package com.csc301.profilemicroservice;

import java.io.IOException;

public interface ProfileDriver {
	DbQueryStatus createUserProfile(String userName, String fullName, String password);
	DbQueryStatus followFriend(String userName, String frndUserName);
	DbQueryStatus unfollowFriend(String userName, String frndUserName );
	DbQueryStatus getAllSongFriendsLike(String userName) throws IOException;
	DbQueryStatus createSong(String songId, String songName, String songArtistFullName, String songAlbum);
	void deleteEverything();
}