import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;;

public class User {
	Connection conn;
	PreparedStatement pstmt;
	ResultSet result;
	
	public void checkAdd(Connection conn,String name, String pw) throws PasswordError, UserError, SQLException{
		pstmt = conn.prepareStatement("INSERT INTO users (username, password) VALUES (?, ?)");
		if (name.length() == 0) throw new UserError("Username can't be empty");
		if (pw.length() != 8) throw new PasswordError("Password should be 8 letter");
		return;
	}
	
	public void checkUserExist(Connection conn,String name) throws UserError, SQLException { 
		pstmt = conn.prepareStatement("SELECT `username`, `email` FROM `users` WHERE 1");
		result = pstmt.executeQuery();
		Boolean userCheck = false;
		while(result.next()) {
			if(result.getString("email") == null) {
				if (result.getString("username").equals(name)) {
					userCheck = true;
				}
			}else {
				if (result.getString("username").equals(name)) {
					userCheck = true;
				}else if(result.getString("email").equals(name)) {
					userCheck = true;
				}
			}
		}
		if(userCheck) return;
		throw new UserError("Can't find the user");
	}
	
	public void checkPassword(Connection conn,String name, String PW) throws PasswordError, SQLException {
		if (name.contains("@")) {
			pstmt = conn.prepareStatement("SELECT * FROM users WHERE email = ?");
			pstmt.setString(1, name);
		} else {
			pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
			pstmt.setString(1, name);
		}
		result = pstmt.executeQuery();
		Boolean pwCheck = false;
		while(result.next()) {
			if (result.getString("password").equals(PW)) {
				pwCheck = true;
			}
		}
		if(pwCheck) return;
		throw new PasswordError("Password is wrong");
	}

	
}
	
class UserError extends Exception { 
	public UserError(String Error){
		super(Error);
	}
}
class PasswordError extends Exception {	
	public PasswordError(String Error){ 
		super(Error);
	}
}
