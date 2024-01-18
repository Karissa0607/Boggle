import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * The Controller class of BoggleUI, it handles all the button input, and the display of the UI.
 * This class uses BoggleAdapter to exchange data with BoggleGame for the game.
 * <p>
 */
public class BoggleController {
    @FXML
    Button B1, B2, B3, B4, B5, B6, B7, B8, B9, B10, B11, B12, B13, B14, B15, B16;

    ArrayList<Button> boardList = new ArrayList<Button>();
    Button[][] boardGrid;

    @FXML
    Button NewBoard;

    @FXML
    Button Undo, Redo;

    @FXML
    GridPane GameBoard;

    @FXML
    TextField TopWordBox;

    @FXML
    TextArea ScoreWordBox;

    @FXML
    TextArea TrackerWordBox;

    private boolean gameStart = false;

    // Stage reserved for pop-up windows
    Stage popupStage;

    BoggleAdapter adapter = new BoggleAdapter(this);

    /**
     * Add normal highlight to button.
     *
     * @param button the button to add highlight
     */
    private void highlightButton(Button button) {
        button.getStyleClass().add("highlight1");
    }

    /**
     * Add extra highlight to the button.
     *
     * @param button the button to add extra highlight
     */
    private void highlightButtonExtra(Button button) {
        button.getStyleClass().add("highlight2");
    }

    /**
     * Remove the highlight of a button.
     */
    private void clearButtonHighlight(Button b) {
        b.getStyleClass().remove("highlight1");
        b.getStyleClass().remove("highlight2");
        b.getStyleClass().remove("remove-highlight");
    }

    /**
     * Called when a grid button is clicked
     */
    public void buttonClickHandler(ActionEvent evt) {
        Button clickedButton = (Button) evt.getTarget();
        gridButtonClicked(clickedButton);
    }

    private String formingWord = "";

    // A list to track each grid button the player has clicked.
    private ArrayList<Button> buttonClicked = new ArrayList<Button>();

    private void gridButtonClicked(Button clickedButton) {
        // If the game has not started, do nothing
        if (gameStart) {
            if (buttonValid(clickedButton)) {
                if (buttonClicked.size() > 0) {
                    // Remove extra highlight from last button
                    Button lastButton = buttonClicked.get(buttonClicked.size() - 1);
                    clearButtonHighlight(lastButton);
                    highlightButton(lastButton);
                }
                // Highlight the button
                highlightButton(clickedButton);
                highlightButtonExtra(clickedButton);
                // Add it to the list of the grid buttons the player has clicked
                buttonClicked.add(clickedButton);
                // Update top word box
                updateFormWordText(formingWord + clickedButton.getText());
                // Check if formingWord is a word
                checkWord();
            } else if (sameAsLastButton(clickedButton)) {
                removeLastLetter();
            }
        }
    }

    /**
     * Check if formingWord is a valid word. If it is, let the game system know a word is found and update the score.
     * Then update the UI display the change.
     */
    private void checkWord() {
        // If formingWord is a valid word
        if (adapter.checkWord(formingWord)) {
            // Let the adapter know a word is found and update the score
            adapter.wordFound(formingWord);
            // Remove highlight from clicked grid buttons
            for (int i = 0; i < buttonClicked.size(); i++) {
                clearButtonHighlight(buttonClicked.get(i));
            }
            // Clear clicked grid buttons
            buttonClicked.clear();
            // Update the UI
            updateScoreText();
            updateFormWordText("");
            // Clear the undo stack
            adapter.clearUndoStack();
        }
    }

    /**
     * Remove the last letter the player has clicked.
     */
    private void removeLastLetter() {
        updateFormWordText(formingWord.substring(0, formingWord.length() - 1));
        Button lastButton = buttonClicked.get(buttonClicked.size() - 1);
        buttonClicked.remove(lastButton);
        clearButtonHighlight(lastButton);
        if (buttonClicked.size() > 0) {
            lastButton = buttonClicked.get(buttonClicked.size() - 1);
            highlightButtonExtra(lastButton);
        }
    }

    /**
     * Check if the grid button's position is valid.
     * A grid button's position is valid if:
     * It is not has not been clicked, and it is next the last button clicked.
     * If no button has been clicked (game just started), any position is valid.
     *
     * @param button the button to check
     */
    private boolean buttonValid(Button button) {
        if (buttonClicked.contains(button)) {
            return false;
        }
        if (buttonClicked.isEmpty()) {
            return true;
        }
        Button lastButton = buttonClicked.get(buttonClicked.size() - 1);
        int[] lastButtonXY = getButtonXY(lastButton);
        int[] buttonXY = getButtonXY(button);
        if (Math.abs(lastButtonXY[0] - buttonXY[0]) <= 1 && Math.abs(lastButtonXY[1] - buttonXY[1]) <= 1) {
            return true;
        }
        //System.out.println(lastButtonXY[0] + " " + lastButtonXY[1]);
        //System.out.println(buttonXY[0] + " " + buttonXY[1]);
        return false;
    }

    /**
     * Check if the button is the same as the last button clicked.
     *
     * @param button the button to check
     */
    private boolean sameAsLastButton(Button button) {
        Button lastButton = buttonClicked.get(buttonClicked.size() - 1);
        return button.equals(lastButton);
    }

    /**
     * Get the position of a button in boardGrid as an int array of [x, y] form.
     * If the button does not exist in boardGrid, return [-1, -1].
     *
     * @param b the button to check the position
     * @return an int array of [x, y] form, representing the button's position in boardGrid.
     */
    private int[] getButtonXY(Button b) {
        for (int x = 0; x < boardGrid.length; x++) {
            for (int y = 0; y < boardGrid[x].length; y++) {
                if (b.equals(boardGrid[x][y])) {
                    int[] xy = {x, y};
                    return xy;
                }
            }
        }
        int[] xy = {-1, -1};
        return xy;
    }

    /**
     * Called when the "New Game" button is clicked, reset the score, word tracker, undo/redo stack, and create a new
     * board.
     */
    public void startNewGame() {
        gameStart = true;
        // Clear clicked grid buttons
        buttonClicked.clear();
        // Clear formingWord and update the UI
        updateFormWordText("");
        // Reset the score and update the UI
        adapter.resetScore();
        updateScoreText();
        // Create the board
        createBoard();
        // Remove the highlight of all buttons
        for (int i = 0; i < boardList.size(); i++) {
            clearButtonHighlight(boardList.get(i));
        }
        // Clear word tracker
        adapter.clearTracker();
        // Clear undo/redo stack
        adapter.clearUndoStack();
        // Print all words, used for debugging
        for (String s : adapter.allWords.keySet()) {
            System.out.println(s);
        }
    }

    private void createBoard() {
        setupButtonList();
        String s = adapter.getBoardString();
        // Update the grid buttons
        for (int i = 0; i < s.length(); i++) {
            boardList.get(i).setText(s.charAt(i) + "");
        }
        // Find all words in the board
        adapter.createAllWords(s);
//        for (int x = 0; x < boardGrid.length; x++) {
//            for (int y = 0; y < boardGrid[x].length; y++) {
//                System.out.print(boardGrid[x][y].getText());
//            }
//            System.out.println();
//        }
    }

    /**
     * Set up boardList and boardGrid.
     */
    private void setupButtonList() {
        boardList = new ArrayList<Button>();
        boardList.clear();
        boardList.add(B1);
        boardList.add(B2);
        boardList.add(B3);
        boardList.add(B4);
        boardList.add(B5);
        boardList.add(B6);
        boardList.add(B7);
        boardList.add(B8);
        boardList.add(B9);
        boardList.add(B10);
        boardList.add(B11);
        boardList.add(B12);
        boardList.add(B13);
        boardList.add(B14);
        boardList.add(B15);
        boardList.add(B16);
        boardGrid = new Button[4][4];
        int i = 0;
        for (int x = 0; x < boardGrid.length; x++) {
            for (int y = 0; y < boardGrid[x].length; y++) {
                boardGrid[x][y] = boardList.get(i);
                i++;
            }
        }
    }

    /**
     * return the grid as a string. Useful for debugging.
     */
    public String currentGrid() {
        String s = "";
        s += B1.getText();
        s += B2.getText();
        s += B3.getText();
        s += B4.getText();
        s += B5.getText();
        s += B6.getText();
        s += B7.getText();
        s += B8.getText();
        s += B9.getText();
        return s;
    }

    /**
     * Update the score text to display the score in the game system.
     */
    public void updateScoreText() {
        ScoreWordBox.setText("Score: " + adapter.getScore());
    }

    /**
     * Set formingWord to s, and update the top text box.
     *
     * @param s the word to be formingWord
     */
    private void updateFormWordText(String s) {
        formingWord = s;
        TopWordBox.setText("Word: " + s);
    }

    /*
     * Word tracker methods
     */

    /**
     * Update the word tracker to diaplay string s
     *
     * @param s the string to display
     */
    public void updateWordTrackerText(String s) {
        // TODO: test this
        TrackerWordBox.setText(s);
    }

    /*
     * Score saver methods
     * Score saver's feature might require separate windows
     */

    /**
     * Called when the "Save Score" button is clicked.
     */
    public void saveScore() {
        // Require more detail, how does the user input their name?
    }

    /**
     * Called when the "Leaderboard" button is clicked.
     */
    public void displayLeaderboard() {
        // Require more detail, Where to display the leaderboard?
    }

    /*
     * Undo/redo methods
     */

    /**
     * Called when the "Undo" button is clicked.
     */
    public void undo() {
        adapter.undo();
    }

    /**
     * Called when the "Redo" button is clicked.
     */
    public void redo() {
        adapter.redo();
    }
}
