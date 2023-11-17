import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.layout.FlowPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import java.sql.Connection;
import java.sql.DriverManager;


public class MainPages extends Application{
	private Stage primaryStage;
	 String server = "jdbc:mysql://127.0.0.1:3306/";
     String database = "LLL";
     String url = server + database + "?useSSL=false";
     String userN = "root";
     String password = "";
     
    
	public void start(Stage primaryStage) throws Exception, UserError {
		
		Connection conn = DriverManager.getConnection(url, userN, password);
		loginSetting login = new loginSetting(conn);
		this.primaryStage = primaryStage;
	    primaryStage.setScene(login.loginPage(conn));
	    primaryStage.setTitle("Playlist Sharing App");
	    primaryStage.show();
	}
	
	 public static void main(String[] args) {
	    	launch(args);

	    }

}
