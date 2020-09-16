import unittest

import requests
import json 

def delete():
    '''
    deletes all the nodes
    '''
    res1 = requests.post('http://localhost:3002/deleteAllNodes')

class TestA3(unittest.TestCase):
    
    def test_allSongServices(self):
        d = {'songName': "song1", 'songArtistFullName': "song1", "songAlbum": "song1"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        firstSongId = res.json()['data']['id']
        
        
        
        d = {'songName': "song2", 'songArtistFullName': "song2", "songAlbum": "song2"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        SecondSongId = res.json()['data']['id']
        
        
        d = {'songName': "song3", 'songArtistFullName': "song3", "songAlbum": "song3"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        ThirdSongId = res.json()['data']['id']
        
        
        res = requests.get("http://localhost:3001/getSongTitleById/" + firstSongId)
        assert res.json()['status'] == 'OK'
        assert "song1" == res.json()['data']
        
        res = requests.get("http://localhost:3001/getSongTitleById/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        assert "song2" == res.json()['data']
        
        
        res = requests.get("http://localhost:3001/getSongTitleById/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        assert "song3" == res.json()['data']
        
        res = requests.get("http://localhost:3001/getSongTitleById/" + "song4")
        assert res.json()['status'] == 'NOT_FOUND'
        
        
        res = requests.put("http://localhost:3001/updateSongFavouritesCount/" + firstSongId + "?shouldDecrement=false")
        assert res.json()['status'] == 'OK'
        res = requests.put("http://localhost:3001/updateSongFavouritesCount/" + SecondSongId + "?shouldDecrement=true")
        assert res.json()['status'] == 'INTERNAL_SERVER_ERROR'
        res = requests.put("http://localhost:3001/updateSongFavouritesCount/" + ThirdSongId + "?shouldDecrement=false")
        assert res.json()['status'] == 'OK'
        
        
        res1 = requests.delete("http://localhost:3001/deleteSongById/" + firstSongId)
        res2 = requests.delete("http://localhost:3001/deleteSongById/" + SecondSongId)
        res3 = requests.delete("http://localhost:3001/deleteSongById/" + ThirdSongId)
        res4 = requests.delete("http://localhost:3001/deleteSongById/" + "song4")
        assert res1.json()['status'] == 'OK'
        assert res2.json()['status'] == 'OK'
        assert res3.json()['status'] == 'OK'
        assert res4.json()['status'] == 'NOT_FOUND'
        
        
    def test_profileservice_few(self):
        d = {'songName': "song1", 'songArtistFullName': "song1", "songAlbum": "song1"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        firstSongId = res.json()['data']['id']
        
        
        
        d = {'songName': "song2", 'songArtistFullName': "song2", "songAlbum": "song2"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        SecondSongId = res.json()['data']['id']
        
        
        d = {'songName': "song3", 'songArtistFullName': "song3", "songAlbum": "song3"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        ThirdSongId = res.json()['data']['id']
        
        
        d = {'userName': 'Shaahid', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] != 'OK'
        
        d = {'userName': 'Shaahid2', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        d = {'userName': 'Shaahid3', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        d = {'userName': 'Shaahid4', 'fullName': '', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] != 'OK'
        
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid3/Shaahid2")
        assert res.json()['status'] == 'OK'
        res = requests.put("http://localhost:3002/followFriend/Shaahid2/Shaahid3")
        assert res.json()['status'] == 'OK'
        res = requests.put("http://localhost:3002/followFriend/Shaahid2/Shaahid3")
        assert res.json()['status'] != 'OK'
        res = requests.put("http://localhost:3002/unfollowFriend/Shaahid2/Shaahid3")
        assert res.json()['status'] == 'OK'
        res = requests.put("http://localhost:3002/unfollowFriend/Shaahid2/Shaahid3")
        assert res.json()['status'] != 'OK'
        res = requests.put("http://localhost:3002/followFriend/Shaahid2/Shaahid3")
        assert res.json()['status'] == 'OK'
        
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + firstSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + ThirdSongId)
        assert res.json()['status'] != 'OK'
        
        
        res = requests.put("http://localhost:3002/unlikeSong/Shaahid3/" + firstSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/unlikeSong/Shaahid3/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/unlikeSong/Shaahid3/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/unlikeSong/Shaahid3/" + ThirdSongId)
        assert res.json()['status'] != 'OK'
        
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid2/" + firstSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid2/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid2/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaa/" + ThirdSongId)
        assert res.json()['status'] != 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid/" + firstSongId)
        assert res.json()['status'] == 'OK'
        
        
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid2")
        assert res.json()['data']['Shaahid3'] == []
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid3")
        assert res.json()['data']['Shaahid2'] == ['song3', 'song2', 'song1']
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid")
        assert res.json()['data'] == {}
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Sha")
        assert res.json()['status'] != "OK"   
        
        
        
        res1 = requests.delete("http://localhost:3001/deleteSongById/" + firstSongId)
        res2 = requests.delete("http://localhost:3001/deleteSongById/" + SecondSongId)
        res3 = requests.delete("http://localhost:3001/deleteSongById/" + ThirdSongId)        
        
        
        
        
        
        delete()
        
        
    def test_getFriendsSongs(self):


        
        d = {'songName': "song1", 'songArtistFullName': "song1", "songAlbum": "song1"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        firstSongId = res.json()['data']['id']
        
        
        
        d = {'songName': "song2", 'songArtistFullName': "song2", "songAlbum": "song2"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        SecondSongId = res.json()['data']['id']
        
        
        d = {'songName': "song3", 'songArtistFullName': "song3", "songAlbum": "song3"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        ThirdSongId = res.json()['data']['id']
        
        
        d = {'songName': "song4", 'songArtistFullName': "song4", "songAlbum": "song4"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        FourthSongId = res.json()['data']['id']
        
        
        d = {'songName': "song5", 'songArtistFullName': "song5", "songAlbum": "song5"}
        res = requests.post("http://localhost:3001/addSong/", data=d)
        assert res.json()['status'] == 'OK'
        FifthSongId = res.json()['data']['id']
        
        
        d = {'userName': 'Shaahid1', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        d = {'userName': 'Shaahid2', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        
        d = {'userName': 'Shaahid3', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        
        d = {'userName': 'Shaahid4', 'fullName': 'Shaahid Sheth', 'password': '123'}
        res = requests.post("http://localhost:3002/profile/", data=d)
        assert res.json()['status'] == 'OK'
        
        
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid2/Shaahid1")
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid3/Shaahid1")
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid4/Shaahid3")
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid4/Shaahid2")
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/followFriend/Shaahid4/Shaahid1")
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid1/" + firstSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid1/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid1/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid1/" + FourthSongId)
        assert res.json()['status'] == 'OK'
        
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid2/" + SecondSongId)
        assert res.json()['status'] == 'OK'
        
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + FifthSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + ThirdSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.put("http://localhost:3002/likeSong/Shaahid3/" + FourthSongId)
        assert res.json()['status'] == 'OK'
        
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid1")
        assert res.json()['data'] == {}
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid2")
        assert res.json()['data'] == {'Shaahid1': ['song4', 'song3', 'song2', 'song1']}
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid3")
        assert res.json()['data'] == {'Shaahid1': ['song4', 'song3', 'song2', 'song1']}
        assert res.json()['status'] == "OK"
        res = requests.get("http://localhost:3002/getAllFriendFavouriteSongTitles/Shaahid4")
        assert res.json()['data'] == {'Shaahid2': ['song2'], 'Shaahid3': ['song4', 'song3', 'song5'], 'Shaahid1': ['song4', 'song3', 'song2', 'song1']}
        assert res.json()['status'] == "OK"
        
        
        
        
        res1 = requests.delete("http://localhost:3001/deleteSongById/" + firstSongId)
        res2 = requests.delete("http://localhost:3001/deleteSongById/" + SecondSongId)
        res3 = requests.delete("http://localhost:3001/deleteSongById/" + ThirdSongId)
        res4 = requests.delete("http://localhost:3001/deleteSongById/" + FourthSongId)
        res5 = requests.delete("http://localhost:3001/deleteSongById/" + FifthSongId)
        res6 = requests.delete("http://localhost:3001/deleteSongById/" + "song4")
        assert res1.json()['status'] == 'OK'
        assert res2.json()['status'] == 'OK'
        assert res3.json()['status'] == 'OK'
        assert res4.json()['status'] == 'OK'
        assert res5.json()['status'] == 'OK'
        assert res6.json()['status'] != 'OK'        
        
        
        
        
        delete()
        



if __name__ == '__main__':
    unittest.main()