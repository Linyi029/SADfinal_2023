import javafx.scene.control.TextArea;



public class RankUpdateCallback implements RankCallback {
	
	
    private TextArea textArea;

    public RankUpdateCallback(TextArea textArea) {
        this.textArea = textArea;
    }

    @Override
    public void onRankUpdate(String result) {
        textArea.appendText(result + "\n");
    }
}