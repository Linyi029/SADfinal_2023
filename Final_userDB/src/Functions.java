import java.sql.Connection;

import java.util.TimerTask;

import javax.security.auth.callback.Callback;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Timer;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import java.util.ArrayList;
import java.util.Observable;

public class Functions {
	
	PreparedStatement pstmt;
	ResultSet rs;
	Connection conn;
	RankUpdateCallback callback;
	User user;
	Timer timer;
	TextArea textArea;
	ObservableList<String> plList= FXCollections.observableArrayList();
	ObservableList<String> songList= FXCollections.observableArrayList();
	
	public Functions(Connection conn) {
        this.conn = conn;
        timer = new Timer();
    }
	public void setRankUpdateCallback(RankUpdateCallback callback) {
        this.callback = callback;
    }
	
	public int getUserId(Connection conn, String username) throws SQLException {
       pstmt = conn.prepareStatement("SELECT userID FROM users WHERE username = ?");
        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("userID");
        } else {
            return -1;
        }
    }
	
	public String getUserName(Connection conn, String username) throws SQLException {
	       pstmt = conn.prepareStatement("SELECT username FROM users WHERE userID = ?");
	      int id = getUserId(conn, username);
	        pstmt.setInt(1,id);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("username");
	        } else {
	            return null;
	        }
	    }
	
	public int getPlaylistId(Connection conn, String playlistName) throws SQLException{
		pstmt = conn.prepareStatement("SELECT playlistID FROM playlist WHERE playlist_name = ?");
   	 pstmt.setString(1, playlistName);
   	 ResultSet rs = pstmt.executeQuery();
   	 if (rs.next()) {
            return rs.getInt("playlistID");
        } else {
            return -1;
        }
   }
   
  public int getSongId(Connection conn, String songName) throws SQLException{
	 pstmt = conn.prepareStatement("SELECT songID FROM songs WHERE title = ?");
  	 pstmt.setString(1, songName.trim());
  	 ResultSet rs = pstmt.executeQuery();
 
  	if (rs.next()) {
           return rs.getInt("songID");
       } else {
           return -1;
       }
  }
   
	
	public void  register(Connection conn, String userName, String userPassword) throws SQLException, UserError, PasswordError {
		boolean nameExist = false;
		pstmt = conn.prepareStatement("SELECT `username` FROM `users` WHERE 1");
		rs = pstmt.executeQuery();
		while (rs.next()) {
			if (userName.equals(rs.getString(1))) {
				nameExist = true;
				showMessageDialog("Username exist, please choose a new name.");
				break;
			}
		}
		if (!nameExist) {
			if (userName.contains("@")) {
				showMessageDialog("Username cannot contain '@', please choose a new name.");
			} else {
				pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
				pstmt.setString(1, userName);
				pstmt.setString(2, userPassword);
				pstmt.executeUpdate();
				showMessageDialog("User registered!");
				//System.out.println("user registered");
			}
		}
	}

	public void  add_song_to_playlist(Connection conn, String playlistName, String songName) throws SQLException {
		int playlistId = getPlaylistId(conn, playlistName);
		int songId = getSongId(conn, songName);
		boolean songExist = false;
		pstmt = conn.prepareStatement("SELECT `songID` FROM `playlist_songs` WHERE `playlistID` = ?");
		pstmt.setInt(1, playlistId);
		rs = pstmt.executeQuery();
		while(rs.next()) {
			if(rs.getInt(1) == songId) {
				songExist = true;
				break;
			}
		}
		if(!songExist) {
			pstmt = conn.prepareStatement("INSERT INTO playlist_songs (playlistID, songID) VALUES (?, ?)");
			pstmt.setInt(1, playlistId);
			pstmt.setInt(2, songId);
			pstmt.executeUpdate();
			//System.out.println("song added to playlist");
		}else {
			showMessageDialog("Song already exist.");
		}
	}


	public void  delete_song_from_playlist(Connection conn, String playlistName, String songName) throws SQLException {
		int playlistId = getPlaylistId(conn, playlistName);
		if (playlistId == -1) {
			//System.out.println("playlist does not exist");
			return;
		}
		int songId = getSongId(conn,songName.trim());
		if (songId == -1) {
			//System.out.println("song does not exist");
			return;
		}
		pstmt = conn.prepareStatement("DELETE FROM playlist_songs WHERE playlistID= ? AND songID = ?");
		pstmt.setInt(1, playlistId);
		pstmt.setInt(2, songId);
		pstmt.executeUpdate();
		
	}
	
	
	public boolean search_playlist(Connection conn, String searchTerm) throws SQLException {
	    pstmt = conn.prepareStatement("SELECT playlist_name FROM playlist WHERE playlist_name = ?");
	    pstmt.setString(1, searchTerm);
	    boolean e =false;
	    ResultSet rs = pstmt.executeQuery();
	    if (!rs.next()) {
	    	e = false;
	    }else {
	    	e = true;
	    }
	    return e;
	}

	public interface RankCallback {
	    void onRankUpdate(String result);
	}
		
	public void startRankUpdates() {
        timer.schedule(new RankTask(), 0, 5000); 
    }
	
	public void stopRankUpdates() {
        timer.cancel();
    }
	
	private class RankTask extends TimerTask {
		public void run() {
            try {
            	String result = rank(conn,callback); 
            	if (callback != null) {
            		callback.onRankUpdate(result); 
            	}
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
	}
	
	public String rank(Connection conn, RankUpdateCallback rcb) throws SQLException, InterruptedException  {
		while (true) {
		pstmt = conn.prepareStatement("SELECT playlist_name , likes, hashtag FROM playlist ORDER BY likes DESC LIMIT 10");
		ResultSet rs = pstmt.executeQuery();
		return showResultSet(rs);
		}
	}
	
	public String lot_drawing(Connection conn) throws SQLException{
		pstmt = conn.prepareStatement("SELECT MAX(songID) FROM songs");
	    rs = pstmt.executeQuery();
	    String result = null;
	   
	    int maxSongID = 0;          		    
	    if (rs.next()) {
	        maxSongID = rs.getInt(1);
	    }     		    
	    int randomSongID = (int) (Math.random() * maxSongID) + 1;
	    pstmt = conn.prepareStatement("SELECT title, artist FROM songs WHERE songID=?"); 
	    pstmt.setInt(1, randomSongID);
	    rs = pstmt.executeQuery();
	    String title = "";
	    String artist = "";
	    while(title.isBlank()) {  
	    	if (rs.next()) {
	    		title = rs.getString("title");
	    		artist = rs.getString("artist");
	    		result = "Your song of the day is: " + title + " by " + artist;
	    	}else {
	        result ="No songs found.";
	    	}
	    }
	 return result;
	}
	
	public static String showResultSet(ResultSet result) throws SQLException {
	    ResultSetMetaData metaData = result.getMetaData();
	    int columnCount = metaData.getColumnCount();
	    int[] columnWidths = { 20, 10, 20 }; // 自訂每個欄位的固定欄寬
	    StringBuilder output = new StringBuilder();

	    // 格式化輸出並對齊欄位標籤
	    for (int i = 1; i <= columnCount; i++) {
	        String label = metaData.getColumnLabel(i);
	        output.append(String.format("%-20s", label)); // 使用固定的欄寬 20
	    }
	    output.append("\n");

	    // 格式化輸出並對齊結果
	    while (result.next()) {
	        for (int i = 1; i <= columnCount; i++) {
	            String value = result.getString(i).trim();
	            output.append(String.format("%-20s", value)); // 使用固定的欄寬 20
	        }
	        output.append("\n");
	    }
	    return output.toString();
	}

	  
	  public ObservableList<String> updateSuggestions(Connection conn,String keyword)throws SQLException {       
	        try {
	        	pstmt = conn.prepareStatement("SELECT playlist_name FROM playlist WHERE 1");
	        	rs = pstmt.executeQuery();
	        	plList.clear();
	            ArrayList<String> suggestions = new ArrayList<>();
	            while(rs.next()) {     	
	            	suggestions.add(rs.getString("playlist_name"));
	            }
	            for (String suggestion : suggestions) {
	             if (suggestion.startsWith(keyword)) {
	              plList.add(suggestion);
	             }
	         }
	        } catch (SQLException e) {
	        	e.printStackTrace();
	        }        
	        return plList;
	    }
	  
	  public ObservableList<String> songsupdateSuggestions(Connection conn,String keyword)throws SQLException {       
	        try {
	        	pstmt = conn.prepareStatement("SELECT title FROM songs WHERE 1");
	        	rs = pstmt.executeQuery();
	        	songList.clear();
	            ArrayList<String> suggestions = new ArrayList<>();
	            while(rs.next()) {     	
	            	suggestions.add(rs.getString("title"));
	            }
	   
	            for (String suggestion : suggestions) {
	             if (suggestion.startsWith(keyword)) {
	              songList.add(suggestion);
	             }
	         }
	        } catch (SQLException e) {
	        	e.printStackTrace();
	        }        
	        return songList;
	    }
	  
	  public ResultSet show_playlist(Connection conn, String userName) throws SQLException {
			pstmt = conn.prepareStatement("SELECT `playlist_name`,`hashtag` FROM `playlist` WHERE `username` = ?");
			pstmt.setString(1, userName);
			rs = pstmt.executeQuery();
			return rs;
		}
	 
	  
	  public void change_password(Connection conn, String password, String newPassword) throws SQLException {
			pstmt = conn.prepareStatement("UPDATE `users` SET `password`=? WHERE `password`=?");
			pstmt.setString(1, newPassword);
			pstmt.setString(2, password);
			pstmt.executeUpdate();
			//System.out.println("Update password succeed.");
		}

		public void change_email(Connection conn, String userName, String email) throws SQLException {
			pstmt = conn.prepareStatement("UPDATE `users` SET `email`=? WHERE `username`=?");
			pstmt.setString(1, email);
			pstmt.setString(2, userName);
			pstmt.executeUpdate();
			//System.out.println("Update email succeed.");
		}
		
		public void change_userName(Connection conn, String userName, String newUserName) throws SQLException {
			pstmt = conn.prepareStatement("UPDATE `users` SET `username`=? WHERE `username`=?");
			pstmt.setString(1, newUserName);
			pstmt.setString(2, userName);
		
			pstmt.executeUpdate();
			pstmt = conn.prepareStatement(
					"UPDATE `playlist` INNER JOIN `users` ON playlist.userID = users.userID  SET playlist.username = users.username");
			pstmt.execute();
		}

		public void change_playlistName(Connection conn, String originalName, String newName) throws SQLException {
			pstmt = conn.prepareStatement("UPDATE `playlist_name`= ? WHERE `playlist_name`= ?");
			pstmt.setString(1, newName);
			pstmt.setString(2, originalName);
			pstmt.executeUpdate();
		}
		
		public void like_playlist(Connection conn, String playlistName) throws SQLException {
			int playlistId = getPlaylistId(conn, playlistName);
			if (playlistId == -1) {
				return;
			}
			pstmt = conn.prepareStatement("UPDATE playlist SET likes = likes + 1 WHERE playlistID = ?;");
			pstmt.setInt(1, playlistId);
			pstmt.executeUpdate();
			showMessageDialog("playlist liked!");
		}
		
		public void delete_playlist(Connection conn, String userName, String playlistName) throws SQLException {
			PreparedStatement pstmt = conn.prepareStatement("DELETE `playlist` FROM `playlist` INNER JOIN `users` ON playlist.userID = users.userID WHERE playlist.playlist_name = ? AND users.username = ?");
			pstmt.setString(1, playlistName);
			pstmt.setString(2, userName);
			int rowsAffected = pstmt.executeUpdate();
			/*if (rowsAffected > 0) {
				System.out.println("Playlist deleted");
			} else {
				System.out.println("Playlist not found");
			}*/
		}

		public void add_playlist(Connection conn, String userName, String playlistName, String genre) throws SQLException {
			PreparedStatement pstmt = conn. prepareStatement("INSERT INTO playlist (playlist_name, userID, hashtag, username) VALUES (?, ?, ?, ?)");
			pstmt.setString(1, playlistName);
			pstmt.setInt(2, getUserId(conn, userName));
			pstmt.setString(3, genre);
			pstmt.setString(4, userName);
			pstmt.executeUpdate();
						
		}
		
		public static void showMessageDialog(String message) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Message");
			alert.setHeaderText(null);
			alert.setContentText(message);
			alert.showAndWait();
		}

		
}
	

