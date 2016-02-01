/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.model;

import io.github.vocabhunter.analysis.session.WordState;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public final class SessionModel {
    private static final Comparator<WordModel> WORD_COMPARATOR = Comparator.comparing(WordModel::getSequenceNo);

    private final List<WordModel> allWords;

    private final ObservableSet<WordModel> selectedWords = FXCollections.observableSet(new TreeSet<>(WORD_COMPARATOR));

    private final ObservableList<WordModel> wordList =  FXCollections.observableArrayList(WordModel.PROPERTY_EXTRACTOR);

    private final ObservableList<String> useList = FXCollections.observableArrayList();

    private final SimpleObjectProperty<WordModel> currentWord;

    private final SimpleStringProperty useCount = new SimpleStringProperty();

    private final SimpleBooleanProperty editable = new SimpleBooleanProperty(true);

    private final SimpleStringProperty documentName;

    private final SimpleBooleanProperty changesSaved = new SimpleBooleanProperty(true);

    public SessionModel(final String documentName, final List<WordModel> words) {
        this.documentName = new SimpleStringProperty(documentName);
        allWords = words;
        selectedWords.addAll(words.stream()
                .filter(w -> w.getState().equals(WordState.UNKNOWN))
                .collect(Collectors.toList()));

        updateEditState(true);
        currentWord = new SimpleObjectProperty<>(InitialSelectionTool.nextWord(allWords));
    }

    public void addSelectedWord(final WordModel word) {
        selectedWords.add(word);
    }

    public void removeDeselectedWord(final WordModel word) {
        selectedWords.remove(word);
    }

    public void processWordUpdate(final WordModel word) {
        List<String> uses = word.getUses();

        useList.clear();
        useList.addAll(uses);
        useCount.set(String.format("(%d uses)", uses.size()));
    }

    public void updateEditState(final boolean isEditable) {
        wordList.clear();
        if (isEditable) {
            wordList.addAll(allWords);
        } else {
            wordList.addAll(selectedWords);
        }
    }

    public ObservableSet<WordModel> getSelectedWords() {
        return selectedWords;
    }

    public boolean isSelected(final int index) {
        WordModel word = allWords.get(index);

        return selectedWords.contains(word);
    }

    public WordModel getWord(final int index) {
        return allWords.get(index);
    }

    public SimpleObjectProperty<WordModel> currentWordProperty() {
        return currentWord;
    }

    public SimpleStringProperty useCountProperty() {
        return useCount;
    }

    public SimpleBooleanProperty editableProperty() {
        return editable;
    }

    public WordModel getCurrentWord() {
        return currentWord.get();
    }

    public ObservableList<WordModel> getWordList() {
        return wordList;
    }

    public int getWordListSize() {
        return wordList.size();
    }

    public int getAllWordsSize() {
        return allWords.size();
    }

    public ObservableList<String> getUseList() {
        return useList;
    }

    public SimpleStringProperty documentNameProperty() {
        return documentName;
    }

    public SimpleBooleanProperty changesSavedProperty() {
        return changesSaved;
    }

    public void setChangesSaved(final boolean changesSaved) {
        this.changesSaved.set(changesSaved);
    }
}
