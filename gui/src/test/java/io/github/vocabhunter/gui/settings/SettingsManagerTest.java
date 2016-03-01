/*
 * Open Source Software published under the Apache Licence, Version 2.0.
 */

package io.github.vocabhunter.gui.settings;

import io.github.vocabhunter.test.utils.TestFileManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingsManagerTest {
    private static final int UPDATE_INT_VALUE = 12345;

    private final Path home = Paths.get(System.getProperty("user.home"));

    private TestFileManager files;

    private Path dummyPath;

    private SettingsManager target;

    @Before
    public void setUp() throws Exception {
        files = new TestFileManager(getClass());
        Path settingsFile = files.addFile("settings.json");
        dummyPath = files.addFile("dummy");
        target = new SettingsManagerImpl(settingsFile);
    }

    @After
    public void tearDown() throws Exception {
        files.cleanup();
    }

    @Test
    public void testGetDefaultDocumentsPath() {
        validateGetDefaultPath(target::getDocumentsPath);
    }

    @Test
    public void testUpdateDocumentsPath() throws Exception {
        validateUpdatePath(target::getDocumentsPath, target::setDocumentsPath);
    }

    @Test
    public void testMissingDocumentsPath() {
        validateMissingPath(target::getDocumentsPath, target::setDocumentsPath);
    }

    @Test
    public void testGetDefaultSessionsPath() {
        validateGetDefaultPath(target::getSessionsPath);
    }

    @Test
    public void testUpdateSessionsPath() throws Exception {
        validateUpdatePath(target::getSessionsPath, target::setSessionsPath);
    }

    @Test
    public void testMissingSessionsPath() {
        validateMissingPath(target::getSessionsPath, target::setSessionsPath);
    }

    @Test
    public void testGetDefaultExportPath() {
        validateGetDefaultPath(target::getExportPath);
    }

    @Test
    public void testUpdateExportPath() throws Exception {
        validateUpdatePath(target::getExportPath, target::setExportPath);
    }

    @Test
    public void testMissingExportPath() {
        validateMissingPath(target::getExportPath, target::setExportPath);
    }

    @Test
    public void testUpdateFilterMinimumLetters() {
        validateUpdateInt(target::getFilterMinimumLetters, target::setFilterMinimumLetters);
    }

    @Test
    public void testMissingFilterMinimumLetters() {
        validateMissingInt(target::getFilterMinimumLetters, VocabHunterSettings.DEFAULT_MINIMUM_LETTERS);
    }

    @Test
    public void testUpdateFilterMinimumOcurrences() {
        validateUpdateInt(target::getFilterMinimumOccurrences, target::setFilterMinimumOccurrences);
    }

    @Test
    public void testMissingFilterMinimumOcurrences() {
        validateMissingInt(target::getFilterMinimumOccurrences, VocabHunterSettings.DEFAULT_MINIMUM_OCCURRENCES);
    }

    private void validateGetDefaultPath(final Supplier<Path> getter) {
        Path path = getter.get();

        Assert.assertEquals("Default path", home, path);
    }

    private void validateUpdatePath(final Supplier<Path> getter, final Consumer<Path> setter) throws Exception {
        Files.createDirectories(dummyPath);
        validate(getter, setter, dummyPath);
    }

    private void validateMissingPath(final Supplier<Path> getter, final Consumer<Path> setter) {
        validate(getter, setter, home);
    }

    private void validate(final Supplier<Path> getter, final Consumer<Path> setter, final Path expected) {
        setter.accept(dummyPath);
        Path path = getter.get();

        Assert.assertEquals("Saved path", expected, path);
    }

    private void validateMissingInt(final Supplier<Integer> getter, final int expected) {
        int actual = getter.get();

        Assert.assertEquals("Default int", expected, actual);
    }

    private void validateUpdateInt(final Supplier<Integer> getter, final Consumer<Integer> setter) {
        setter.accept(UPDATE_INT_VALUE);
        int actual = getter.get();

        Assert.assertEquals("Updated int", UPDATE_INT_VALUE, actual);
    }
}
