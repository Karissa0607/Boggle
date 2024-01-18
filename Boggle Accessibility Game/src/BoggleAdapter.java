import boggle.BoggleGame;
import boggle.BoggleGrid;
import boggle.Dictionary;
import boggle.Position;

import java.util.*;

/**
 * The Adapter class of BoggleUI, it handles data from Controller and BoggleGame to update each other.
 */
public class BoggleAdapter {
    BoggleGame game;
    Dictionary dictionary;
    Map<String, ArrayList<Position>> allWords = new HashMap<String, ArrayList<Position>>();

    BoggleController controller;

    Receiver receiver;
    RedoCommand redo;
    UndoCommand undo;

    public BoggleAdapter(BoggleController controller) {
        this.controller = controller;
        this.game = new BoggleGame();
        this.dictionary = new Dictionary("src/wordlist.txt");
        this.receiver = new Receiver(this.game.wordsGuessed);
        this.redo = new RedoCommand(this.receiver);
        this.undo = new UndoCommand(this.receiver);

    }

    /**
     * Return a random 4x4 board made by the game system.
     */
    public String getBoardString() {
        return game.getRandomBoard();
    }

    /**
     * Reset the score of the game system.
     */
    public void resetScore() {
        game.resetStats();
    }

    /**
     * Populate allWords with the game system.
     */
    public void createAllWords(String board) {
        BoggleGrid grid = new BoggleGrid(4);
        grid.initalizeBoard(board);
        game.populateAllWords(allWords, dictionary, grid);
    }

    public boolean checkWord(String word) {
        return allWords.keySet().contains(word) && !game.hasFoundWord(word) && !this.receiver.historyWords.contains(word);
    }

    public void wordFound(String word) {
        game.increaseScore(word);
        addWordToTracker(word);
    }

    public int getScore() {
        return game.getScore();
    }

    /*
     * Word tracker methods
     * They might be useful to link UI with the word tracker
     */

    /**
     * Add word to the word tracker, and update the UI to display it.
     *
     * @param word the word to add to the word tracker
     */
    public void addWordToTracker(String word) {
        this.game.wordsGuessed.push(word);
        this.updateWordTrackerUI(this.game.wordsGuessed);
    }

    /**
     * Make the word tracker empty, and update the UI to display it. Used when a new game start.
     */
    public void clearTracker() {
        this.game.wordsGuessed.clear();
        this.receiver.historyWords.clear();
        this.controller.updateWordTrackerText("");
    }

    /**
     * Update the UI of word tracker to display the wordList.
     *
     * @param wordList the list of words to display
     */
    private void updateWordTrackerUI(Stack<String> wordList) {
        Iterator<String> iterator = wordList.iterator();
        String acu = "Words Found:\n";
        while (iterator.hasNext()) {
            acu += iterator.next() + "\n";
        }
        controller.updateWordTrackerText(acu);
    }

    /*
     * Score saver methods
     * They might be useful to link UI with the score saver
     */

    /**
     * Add a new entry to the score saver, if the score saver already has an entry with the same name, and the score is
     * higher, replace the old score with new high score.
     *
     * @param name  the name given to high score
     * @param score the score to be saved
     */
    public void addEntry(String name, int score) {
        // TODO: implement this
        // entryNameExist and replaceEntry might be helpful
    }

    /**
     * Check if the score saver has an entry with name.
     *
     * @param name the name to check
     */
    private boolean entryNameExist(String name) {
        // TODO: implement this
        throw new UnsupportedOperationException();
    }

    /**
     * Replace the score of the entry with name in the score saver with score.
     *
     * @param name  the name given to high score
     * @param score the score to be saved
     */
    private void replaceEntry(String name, int score) {
        // TODO: implement this
    }

    /*
     * Undo/redo methods
     * They might be useful to link UI with the undo/redo system
     */

    /**
     * Revert the last action of the player.
     */
    public void undo() {
        String word = this.undo.execute();
        this.game.decreaseScore(word);
        this.controller.updateScoreText();
        this.updateWordTrackerUI(this.game.wordsGuessed);
    }

    /**
     * Revert the last undo action of the player.
     */
    public void redo() {
        String word = this.redo.execute();
        this.game.increaseScore(word);
        this.controller.updateScoreText();
        this.updateWordTrackerUI(this.game.wordsGuessed);
    }

    /**
     * Make the undo/redo stack empty. Used when a new game start.
     */
    public void clearUndoStack() {

    }
}
