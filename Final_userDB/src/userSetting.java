import java.sql.Connection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class userSetting {
	private Button btnChangeName, btnChangeEmail, btnChangePassWord, btnMainPage, btnShowPlaylist;
	private ListView<String> listView;
	private Label lblEmail, lblName;
	private String email, name, password;
	
	private ResultSet result;
	private Stage listStage;
	Connection conn;
	public userSetting(Connection conn) {
		this.conn = conn;
	}

	public Scene sceneUser(Connection conn) throws SQLException {
		Functions func = new Functions(conn);
		Middle mid = new Middle();
		mid.userData(conn);
		email = Middle.getEmail();
		name = Middle.getUsername();
		password = Middle.getPassword();
		
		lblEmail = new Label(email);
		lblName = new Label(name); 
		Label lblPassword = new Label(password); 
		Label nameLabel = new Label("User Name: ");
		Label emailLabel = new Label("Email: ");
		Label passLabel = new Label("Password: ");

		// 設置按鈕們
		btnChangeName = new Button("Change User Name");
		Label ChangeNLabel = new Label("Input new User Name:");
		TextField NameTextField = new TextField();
		NameTextField.setPrefWidth(200);

		btnChangeEmail = new Button("Change Email");
		Label ChangeELabel = new Label("Input new Email:");
		TextField EmailTextField = new TextField();
		EmailTextField.setPrefWidth(200);

		btnChangePassWord = new Button("Change Password");
		Label ChangePLabel = new Label("Input new Password:");
		TextField PasswordTextField = new TextField();
		PasswordTextField.setPrefWidth(200);
		btnMainPage = new Button("Back to Main Page");
		btnShowPlaylist = new Button("Show Playlist");

		btnChangeName.setOnAction(event -> {
			if (lblName.getText().equals(NameTextField.getText()) || NameTextField.getText().equals("")) {
				Functions.showMessageDialog("You should input a 'new Name'");
				NameTextField.clear();
			} else {
				try {
					func.change_userName(conn, name, NameTextField.getText());
					Functions.showMessageDialog("Name has been changed to: " + NameTextField.getText());
					lblName.setText(NameTextField.getText());
					mid.setUsername(NameTextField.getText());
					NameTextField.clear();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		btnChangePassWord.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (password.equals(PasswordTextField.getText()) || PasswordTextField.getText().equals("")) {
					Functions.showMessageDialog("You should input a 'new Pass Word'");
				}else if(PasswordTextField.getText().length() != 8) {
					Functions.showMessageDialog("Password should be eight letters'");
				}else {
					try {
						func.change_password(conn, password, PasswordTextField.getText());
						Functions.showMessageDialog("Pass Word has been changed to: " + PasswordTextField.getText());
						lblPassword.setText(PasswordTextField.getText());
						mid.setPassword(PasswordTextField.getText());
						PasswordTextField.clear();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});

		btnChangeEmail.setOnAction(event ->{
			if(email == null) {
				email = "noEmail";
			}
			if (email.equals(EmailTextField.getText()) || EmailTextField.getText().equals("")) {
				Functions.showMessageDialog("You should input a 'new Email'");
				EmailTextField.clear();
			} else {
				try {
					func.change_email(conn, name, EmailTextField.getText());
					lblEmail.setText(EmailTextField.getText());
					Functions.showMessageDialog("Email has been changed to: " + EmailTextField.getText());
					mid.setEmail(EmailTextField.getText());
					EmailTextField.clear();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});

		btnMainPage.setOnAction(event -> {
			 HomePagesetting page2 = new HomePagesetting(conn);
		        Scene scene1 = null;
				try {
					scene1 = page2.HomeScene();
				} catch (SQLException | InterruptedException e) {
					e.printStackTrace();
				}
		        Stage stage = (Stage) btnMainPage.getScene().getWindow();
		        stage.setScene(scene1);
		});
		result = func.show_playlist(conn,Middle.getUsername());
		listView = new ListView<>();
		ArrayList<String> playlistNames = new ArrayList<>();

		while (result.next()) {
			playlistNames.add(result.getString(1));
		}
		listView.setPrefWidth(300);
		listView.getItems().addAll(playlistNames);

		listView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			public ListCell<String> call(ListView<String> param) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (item != null) {
							for (int i = 0; i < playlistNames.size(); i++) {
								HBox hbox = new HBox(70);
								Label label1;
								Label label2;
								Button btn = new Button("Detail");
								label1 = new Label(item);
								label1.setPrefWidth(50);
								label2 = new Label("");
								label2.setPrefWidth(50);
								btn.setPrefWidth(50);
								btn.setOnAction(event -> {
									mid.setPlaylistName(item);
									listStage = new Stage();
									pl_addPage playlistPage = new pl_addPage(conn);
									playlistPage.newP(conn).showAndWait();
								     Stage stage = (Stage) btn.getScene().getWindow();
								});
								
								try {
									PreparedStatement pstmt = conn.prepareStatement("SELECT `hashtag` FROM `playlist` WHERE `playlist_name` = ?");
									pstmt.setString(1, item);
									ResultSet result2 = pstmt.executeQuery();
									result2.next();
									updateLabel(label2, result2.getString(1));
								} catch (SQLException e) {
									e.printStackTrace();
								}
								hbox.getChildren().addAll(label1, label2, btn);
								setGraphic(hbox);
							}
						} else {
							setGraphic(null);
						}
					}
				};
			}
		});
		listView.setPrefHeight(200);
		listView.setVisible(false);

		btnShowPlaylist.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) 
			
			{
				listView.setVisible(true);
			}
		});

		// 排版
		VBox userPanel = new VBox(10);
		userPanel.setAlignment(Pos.TOP_CENTER);
		userPanel.setPadding(new Insets(20));
		userPanel.setPrefWidth(600);
		userPanel.setPrefHeight(600);

		FlowPane row1Pane = new FlowPane();
		row1Pane.setAlignment(Pos.CENTER);
		row1Pane.getChildren().addAll(nameLabel, lblName);
		userPanel.getChildren().add(row1Pane);

		FlowPane row2Pane = new FlowPane();
		row2Pane.setAlignment(Pos.CENTER);
		row2Pane.getChildren().addAll(passLabel, lblPassword);
		userPanel.getChildren().add(row2Pane);

		FlowPane row3Pane = new FlowPane();
		row3Pane.setAlignment(Pos.CENTER);
		row3Pane.getChildren().addAll(emailLabel, lblEmail);
		userPanel.getChildren().add(row3Pane);

		FlowPane row4Pane = new FlowPane();
		row4Pane.setAlignment(Pos.CENTER);
		row4Pane.setHgap(10); 
		row4Pane.setVgap(10);
		row4Pane.getChildren().addAll(ChangeNLabel, NameTextField, btnChangeName);
		userPanel.getChildren().add(row4Pane);

		FlowPane row5Pane = new FlowPane();
		row5Pane.setAlignment(Pos.CENTER);
		row5Pane.setHgap(10); 
		row5Pane.setVgap(10); 
		row5Pane.getChildren().addAll(ChangePLabel, PasswordTextField, btnChangePassWord);
		userPanel.getChildren().add(row5Pane);

		FlowPane row6Pane = new FlowPane();
		row6Pane.setAlignment(Pos.CENTER);
		row6Pane.setHgap(10); 
		row6Pane.setVgap(10); 
		row6Pane.getChildren().addAll(ChangeELabel, EmailTextField, btnChangeEmail);
		userPanel.getChildren().add(row6Pane);

		FlowPane row7Pane = new FlowPane();
		row7Pane.setAlignment(Pos.CENTER);
		row7Pane.getChildren().addAll(btnMainPage);
		userPanel.getChildren().add(row7Pane);

		FlowPane row8Pane = new FlowPane();
		row8Pane.setAlignment(Pos.CENTER);
		row8Pane.getChildren().addAll(btnShowPlaylist);
		userPanel.getChildren().add(row8Pane);

		FlowPane row9Pane = new FlowPane();
		row9Pane.setAlignment(Pos.CENTER);
		row9Pane.getChildren().addAll(listView);
		userPanel.getChildren().add(row9Pane);
		
		userPanel.setStyle("-fx-background-color: " + Middle.getColor());
		return new Scene(userPanel);
	}

	public void updateLabel(Label lbl, String genre) {
		lbl.setText("#" + genre);
	}
	
	public void changeColor(Node n, String color) {
		n.setStyle("-fx-background-color: " + color);
	}

}
