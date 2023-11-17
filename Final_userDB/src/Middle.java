import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Middle {
	private static String username, password, email, playlistName, playlistStyle, color;
	
	public static String getPlaylistStyle() {
		return playlistStyle;
	}

	public void setPlaylistStyle(String playlistStyle) {
		this.playlistStyle = playlistStyle;
	}

	public static String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public static String getPlaylistName() {
		return playlistName;
	}

	public void setPlaylistName(String playlistName) {
		this.playlistName = playlistName;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

	public static String getEmail() {
		return email;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	public void userData(Connection conn) {
		try {
			
			PreparedStatement pstmt;
			ResultSet result;
			if (getUsername() == null) {
				pstmt = conn.prepareStatement("SELECT `username`, `password` FROM `users` WHERE `email` = ?");
				pstmt.setString(1, getEmail());
				result = pstmt.executeQuery();
				result.next();
				setUsername(result.getString("username"));
				setPassword(result.getString("password"));
			} else {
				pstmt = conn.prepareStatement("SELECT `password`, `email` FROM `users` WHERE `username` = ?");
				pstmt.setString(1, getUsername());
				result = pstmt.executeQuery();
				result.next();
				if(result!=null) {
					setEmail(result.getString("email"));
				}
				setPassword(result.getString("password"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
}
}
