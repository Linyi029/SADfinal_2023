import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class pl_addPage{
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet result;
	private Label listNameLbl = new Label();
	private Label genreLbl = new Label();
	private Label creatorLbl = new Label();
	private Label addSongLbl = new Label();
	private TextField addSongFld;
	private Button btnCommit, btnLike, btnDeleteList, btnEditList;
	private int index;
	private String playlistName;
	private Stage editStage;
	private Middle mid = new Middle();
	
	private ObservableList<String> songList;
	private ObservableList<String> playlistList1 = FXCollections.observableArrayList();
	private ListView<String> playlistView = new ListView<>(playlistList1);
	private ListView<String> listView;
	Functions func;
	private Font font = new Font("Courier New", 12);
	
	public pl_addPage(Connection conn) {
		this.conn = conn;
	}
	
	public Stage newP(Connection conn) {
		Stage newS = new Stage();
		newS.setWidth(800);
		newS.setHeight(800);
		playlistName = Middle.getPlaylistName();
		newS.setTitle(playlistName);
		Font fontTitle = Font.font("Verdana", FontWeight.BOLD, FontPosture.ITALIC, 22);
		Font fontSub = Font.font("Verdana", FontWeight.MEDIUM, FontPosture.ITALIC, 18);
		func = new Functions(conn);
		
		try {
			pstmt = conn.prepareStatement("SELECT `username`, `hashtag` FROM `playlist` WHERE `playlist_name` = ?");
			pstmt.setString(1, playlistName);
			result = pstmt.executeQuery();
			result.next();
			listNameLbl.setText(playlistName);
			genreLbl.setText(result.getString(2));
			mid.setPlaylistStyle(genreLbl.getText());
			creatorLbl.setText(result.getString(1));
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		addSongLbl.setText("Add Song:");
		addSongFld = new TextField();
		listNameLbl.setFont(fontTitle);
		genreLbl.setFont(fontSub);
		creatorLbl.setFont(fontSub);

		btnCommit = new Button("Commit");
		btnLike = new Button("Like Playlist");
		btnDeleteList = new Button("Delete Playlist");
		btnEditList = new Button("Edit Playlist");
		
		btnLike.setOnAction(event -> {
			try {
				func.like_playlist(conn, playlistName);
			} catch (SQLException e) {
				Functions.showMessageDialog(e.getMessage());
			}
		});
		
		btnEditList.setOnAction(event ->{
			editStage = new Stage();
			this.editPage(editStage);
		});
		
		btnDeleteList.setOnAction(event ->{
		       Alert alert = new Alert(Alert.AlertType.INFORMATION);
		        alert.setTitle("Delete Playlist");
		        alert.setHeaderText("Are you sure you want to delete the playlist?");		        
		        ButtonType buttonCancel = new ButtonType("Cancel");
		        ButtonType buttonDelete = new ButtonType("Delete");	        
		        alert.getButtonTypes().setAll(buttonCancel, buttonDelete);
		        alert.showAndWait();

		        if (alert.getResult() == buttonDelete) {
					try {
						func.delete_playlist(conn, creatorLbl.getText(), listNameLbl.getText());
						newS.close();
					} catch (SQLException e) {
						Functions.showMessageDialog(e.getMessage());
					}
		        }
		});
		
		songList = FXCollections.observableArrayList();
		listView = new ListView<>(songList);
		listView.setPrefHeight(300);

		listView.setVisible(false);

			addSongFld.textProperty().addListener((observable, oldValue, newValue) -> {
				updateSuggestions(newValue);
				listView.setOnMouseClicked(event -> {
					int selectedIndex = listView.getSelectionModel().getSelectedIndex();
					String selectedName = songList.get(selectedIndex);
					addSongFld.setText(selectedName);
				});
			});

		this.updatePlaylistSong(playlistView, playlistList1);

		VBox listPanel = new VBox(10);
		listPanel.setAlignment(Pos.TOP_CENTER);
		listPanel.setPadding(new Insets(20));
		listPanel.setPrefWidth(600);
		listPanel.setPrefHeight(600);

		FlowPane row0Pane = new FlowPane();
		row0Pane.setAlignment(Pos.TOP_RIGHT);
		row0Pane.getChildren().addAll(btnDeleteList);
		listPanel.getChildren().add(row0Pane);
		
		FlowPane row1Pane = new FlowPane();
		row1Pane.setAlignment(Pos.CENTER);
		row1Pane.getChildren().addAll(listNameLbl);
		listPanel.getChildren().add(row1Pane);

		HBox row2Pane = new HBox(30);
		row2Pane.setAlignment(Pos.CENTER);
		row2Pane.getChildren().addAll(genreLbl, creatorLbl, btnLike, btnEditList);
		listPanel.getChildren().add(row2Pane);

		FlowPane row3Pane = new FlowPane();
		row3Pane.setAlignment(Pos.CENTER);
		row3Pane.setHgap(10); 
		row3Pane.setVgap(10); 
		row3Pane.getChildren().addAll(addSongLbl, addSongFld, btnCommit);
		listPanel.getChildren().add(row3Pane);

		FlowPane row4Pane = new FlowPane();
		row4Pane.setAlignment(Pos.CENTER);
		listView.setItems(songList);
		row4Pane.getChildren().addAll(listView);
		listPanel.getChildren().add(row4Pane);
		
		Label lbl = new Label("現有歌曲：");
		VBox vbox = new VBox(10);
		vbox.getChildren().addAll(lbl, playlistView);
		FlowPane row5Pane = new FlowPane();
		row5Pane.setAlignment(Pos.CENTER);
		row4Pane.getChildren().addAll(vbox);
		listPanel.getChildren().add(row5Pane);

		btnCommit.setOnAction(event -> {
			result = this.getResult();
			boolean songExist = false;
			try {
				while (result.next()) {
					String inputSong = result.getString(1);
					if (inputSong.equals(addSongFld.getText())) {
						try {
							func.add_song_to_playlist(conn, playlistName, addSongFld.getText());
							this.updatePlaylistSong(playlistView, playlistList1);
							songExist = true;
							listView.setVisible(false);
							addSongFld.clear();
							break;
						} catch (SQLException e) {
							Functions.showMessageDialog(e.getMessage());
						}
					}
				}
				if (songExist == false) {
					Functions.showMessageDialog("This song doesn't exist. ");
				}
			} catch (SQLException e) {
				Functions.showMessageDialog(e.getMessage());
			}
		});
		listPanel.setStyle("-fx-background-color: " + Middle.getColor());
		Scene newsc = new Scene(listPanel);
		newS.setScene(newsc);
		return newS;
	}
	
	
	private void updateSuggestions(String keyword) {
		try {
			songList.clear();
			ArrayList<String> suggestions = new ArrayList<>();
			result = this.getResult();
			while (result.next()) {
				suggestions.add(result.getString("title"));
			}

			for (String suggestion : suggestions) {
				if (suggestion.startsWith(keyword)) {
					songList.add(suggestion);
				}
			}
			listView.setVisible(true);
		} catch (SQLException e) {
			Functions.showMessageDialog(e.getMessage());
		}
	}

	public ResultSet getResult() {
		ResultSet resultReturn = null;
		try {
			pstmt = conn.prepareStatement("SELECT `title` FROM `songs` WHERE 1");
			resultReturn = pstmt.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return resultReturn;
	}

	private void updatePlaylistSong(ListView<String> playlistSongView, ObservableList<String> playlistList) {
		ResultSet resultGet = null;
		ArrayList<String> songName = new ArrayList<>();
		ArrayList<String> songArtist = new ArrayList<>();
		ArrayList<String> songLanguage = new ArrayList<>();
		try {
			pstmt = conn.prepareStatement(
					"SELECT songs.title, songs.artist, songs.language FROM `songs` INNER JOIN `playlist_songs` ON songs.songID = playlist_songs.songID WHERE playlist_songs.playlistID = ?");
			pstmt.setInt(1, func.getPlaylistId(conn, playlistName));
			resultGet = pstmt.executeQuery();
			songName.add("menu");
			
			while(resultGet.next()) {
				songName.add(resultGet.getString(1));
	            songArtist.add(resultGet.getString(2));
	            songLanguage.add(resultGet.getString(3));
			}
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

		ObservableList<String> tempList = FXCollections.observableArrayList(); // 创建临时列表
		for (String song : songName) {
			tempList.add(song);
		}
		
		playlistList.setAll(tempList); // 更新playlistList
		playlistSongView.setItems(playlistList);
		playlistSongView.setPrefWidth(500);
		playlistSongView.setCellFactory(new Callback<ListView<String>, ListCell<String>>() {
			@Override
			public ListCell<String> call(ListView<String> param) {
				return new ListCell<String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
							if (item != null) {
								if(item.equals("menu")) {
									HBox hboxMenu = new HBox(50);
									Label name, artist, language;
									name = new Label("Song Name");
									name.setPrefWidth(80);
									name.setAlignment(Pos.CENTER_LEFT);
									name.setFont(font);
									artist = new Label("Artist");
									artist.setAlignment(Pos.CENTER_LEFT);
									artist.setPrefWidth(80);
									artist.setFont(font);
									language = new Label("Language");
									language.setPrefWidth(80);
									language.setFont(font);
									 language.setAlignment(Pos.CENTER_LEFT);
									hboxMenu.getChildren().addAll(name, artist, language);
									setGraphic(hboxMenu);
								}else {
									HBox hbox = new HBox(30);
									Label label1, label2, label3;
									Button btn = new Button("Delete");
									label1 = new Label(item);
									label2 = new Label("");
									label3 = new Label("");
									
									 label1.setPrefWidth(80);
						             label2.setPrefWidth(80);
						             label3.setPrefWidth(80);
						             btn.setPrefWidth(60);						     
						          
									index = getIndex() -1; 
									if(index>=0 && index < songArtist.size() && index < songLanguage.size()) {
									label2.setText(songArtist.get(index));
									label3.setText(songLanguage.get(index));
									}
									
									 label1.setAlignment(Pos.CENTER_LEFT);
						             label2.setAlignment(Pos.CENTER);
						             label3.setAlignment(Pos.CENTER);
						             btn.setAlignment(Pos.CENTER);
									btn.setOnAction(event -> {
										try {									
											func.delete_song_from_playlist(conn, playlistName, item.trim());									
										} catch (SQLException e) {
											Functions.showMessageDialog(e.getMessage());
										}
										updatePlaylistSong(playlistView, playlistList1);
									});							
									hbox.getChildren().addAll(label1, label2, label3, btn);
									setGraphic(hbox);
								}
							}
						}
				};
			}
		});
		playlistSongView.setVisible(true);
	}
	
	private void editPage(Stage stage) {
		stage.setTitle("Edit List");
		Label newNameLbl = new Label("Enter new playlist name：");
		Label newGenreLbl = new Label("Choose new playlist genre：");
		TextField newNameInput = new TextField();
		newNameInput.setPrefWidth(150);
		String[] genreOptions = {"K-pop", "J-pop", "Hip-Hop", "R&B", "華語流行", "Jazz", "Metal", "Classic", "Anime"};
		ComboBox<String> styleCmb = new ComboBox<>();
		styleCmb.getItems().addAll(genreOptions);
		styleCmb.setValue(Middle.getPlaylistStyle());
		styleCmb.setPrefWidth(150);
		Button btnEditName = new Button("Change Name");
		btnEditName.setOnAction(event ->{
			String newName = newNameInput.getText();			
			if(newName == null) {
				Functions.showMessageDialog("Playlist name can't be empty.");
			}else if(newName.equals(Middle.getPlaylistName())) {
				Functions.showMessageDialog("Playlist name should be changed.");
			}else {
				try {
					pstmt = conn.prepareStatement("UPDATE `playlist` SET `playlist_name`= ? WHERE `playlist_name`= ?");
					pstmt.setString(1, newName);
					pstmt.setString(2, Middle.getPlaylistName());
					pstmt.executeUpdate();
					mid.setPlaylistName(newName);		
					listNameLbl.setText(newName);
				} catch (SQLException e) {
					Functions.showMessageDialog(e.getMessage());
				}				
			}			
		});
		Button btnEditGenre = new Button("Change Genre");
		btnEditGenre.setOnAction(event ->{
			String newGenre = styleCmb.getSelectionModel().getSelectedItem();
			if(newGenre.equals(Middle.getPlaylistStyle())) {
				Functions.showMessageDialog("Playlist genre should be changed.");
			}else {
				try {
					pstmt = conn.prepareStatement("UPDATE `playlist` SET `hashtag`= ? WHERE `hashtag`= ?");
					pstmt.setString(1, newGenre);
					pstmt.setString(2, Middle.getPlaylistStyle());
					pstmt.executeUpdate();
					mid.setPlaylistStyle(newGenre);
					genreLbl.setText(newGenre);
				} catch (SQLException e) {
					Functions.showMessageDialog(e.getMessage());
				}	
			}
		});
		
		Button btnFinish = new Button("Finish");
		btnFinish.setOnAction(event ->{
			stage.close();
		});
		
		HBox hboxName = new HBox(10);
		hboxName.setAlignment(Pos.CENTER);
		hboxName.getChildren().addAll(newNameLbl, newNameInput, btnEditName);
		HBox hboxGenre = new HBox(10);
		hboxGenre.setAlignment(Pos.CENTER);
		hboxGenre.getChildren().addAll(newGenreLbl, styleCmb, btnEditGenre);
		HBox hboxFinish = new HBox(10);
		hboxFinish.setAlignment(Pos.BOTTOM_CENTER);
		hboxFinish.getChildren().addAll(btnFinish);
		
		VBox vbox = new VBox(30);
		vbox.getChildren().addAll(hboxName, hboxGenre, hboxFinish);
		vbox.setStyle("-fx-background-color:" + Middle.getColor());
		vbox.setAlignment(Pos.CENTER);
		Scene scene = new Scene(vbox, 500, 250);
		stage.setScene(scene);
		stage.show();
	}
}