import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.JOptionPane;

public class loginSetting {
	TextField tfUserName = new TextField();
	PasswordField pfPassword = new PasswordField();
	Connection conn;
	Functions  data;
	String username, password;
	
	public loginSetting(Connection conn) {
		this.conn = conn;
		data = new Functions(conn);
		
	}


	public Scene loginPage(Connection conn ) {
		Button btnEnroll = new Button("Enroll");
		Button btnLogin = new Button("Login");
		Button btnCheckPW = new Button("查看密碼");
		Label lblUserName = new Label("Username:");
		Label lblPassword = new Label("Password:");
		Label lblInputPW = new Label("This is password : ");		
		User user = new User();
		Middle userData = new Middle();

		// 功能嵌入
		
			
			btnEnroll.setOnAction(event -> {
				try {
					user.checkAdd(conn, tfUserName.getText(), pfPassword.getText());
					data.register(conn, tfUserName.getText(), pfPassword.getText());
				} catch (PasswordError e1) {
					data.showMessageDialog("Password should be 8 words!");
				} catch (UserError e2) {
					data.showMessageDialog("User name can't be empty!");
				} catch (SQLException e) {
					data.showMessageDialog(e.getMessage());
				}
			});

			btnLogin.setOnAction(event -> {
				try {
					user.checkUserExist(conn, tfUserName.getText());
					try {
						user.checkPassword(conn,tfUserName.getText(), pfPassword.getText());
						try {
							username = tfUserName.getText();
							password = pfPassword.getText();
							if(username.contains("@")) {
								userData.setEmail(username);
							}else {
								userData.setUsername(username);
							}
							userData.setPassword(password);
							HomePagesetting home = new HomePagesetting(conn);
							Stage stage = (Stage) btnLogin.getScene().getWindow();
					        stage.setScene(home.HomeScene());
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (PasswordError e1) {
						data.showMessageDialog("Worng Password!");
					}
				} catch (UserError | SQLException e2) {
					data.showMessageDialog("User doesn't exist!");
				}

			});

		// 佈局

		GridPane root1 = new GridPane();
		root1.add(lblUserName, 10, 20);
		root1.add(tfUserName, 11, 20);
		root1.add(lblPassword, 10, 40);
		root1.add(pfPassword, 11, 40);
		//root1.add(lblInputPW, 10, 20);

		GridPane gridPane = new GridPane();
		gridPane.setPadding(new Insets(10));
		gridPane.setHgap(10);
		gridPane.setVgap(10);
		gridPane.setAlignment(Pos.CENTER);
		gridPane.add(lblUserName, 0, 0);
		gridPane.add(tfUserName, 1, 0);
		gridPane.add(lblPassword, 0, 1);
		gridPane.add(pfPassword, 1, 1);
		gridPane.add(btnCheckPW, 2, 1);

		HBox buttonBox = new HBox(10);
		buttonBox.setAlignment(Pos.CENTER);
		buttonBox.getChildren().addAll(btnEnroll, btnLogin);

		VBox root = new VBox(10);
		root.setAlignment(Pos.CENTER);
		root.getChildren().addAll(gridPane, lblInputPW, buttonBox);

		btnCheckPW.setOnAction(event -> {
			this.updateLblPW(lblInputPW, pfPassword);
		});
		Scene page1 = new Scene(root, 500, 500);
		return page1;
	}
	

	private void updateLblPW(Label label, PasswordField pfPassword) {
		label.setText("This is password : " + pfPassword.getText());
	}
	
}