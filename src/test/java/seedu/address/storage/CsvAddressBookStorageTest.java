package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.HOON;
import static seedu.address.testutil.TypicalPersons.IDA;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataConversionException;
import seedu.address.commons.util.FileUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;

public class CsvAddressBookStorageTest {
    private static final Path TEST_DATA_FOLDER = Paths.get("src", "test", "data", "CsvAddressBookStorageTest");

    @TempDir
    public Path testFolder;

    @Test
    public void readAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> readAddressBook(null));
    }

    private java.util.Optional<ReadOnlyAddressBook> readAddressBook(String filePath) throws Exception {
        return new CsvAddressBookStorage(Paths.get(filePath)).readAddressBook(addToTestDataPathIfNotNull(filePath));
    }

    private Path addToTestDataPathIfNotNull(String prefsFileInTestDataFolder) {
        return prefsFileInTestDataFolder != null
                ? TEST_DATA_FOLDER.resolve(prefsFileInTestDataFolder)
                : null;
    }

    @Test
    public void read_missingFile_emptyResult() throws Exception {
        assertFalse(readAddressBook("NonExistentFile.csv").isPresent());
    }

    @Test
    public void read_notCsvFormat_exceptionThrown() {assertThrows(DataConversionException.class, () -> readAddressBook("notCsvFormatAddressBook.csv"));
    }

    @Test
    public void readAddressBook_invalidPersonAddressBook_throwDataConversionException() {
        assertThrows(DataConversionException.class, () -> readAddressBook("invalidPersonAddressBook.csv"));
    }

    @Test
    public void readAddressBook_invalidAndValidPersonAddressBook_throwDataConversionException() {
        assertThrows(DataConversionException.class, () -> readAddressBook("invalidAndValidPersonAddressBook.csv"));
    }

    @Test
    public void readAndSaveAddressBook_allInOrder_success() throws Exception {
        // JSON for storing app data needs to be overwritten, except for in an Export Command.
        // Unlike JSON, CSV is NOT used for storage of app data, and is only used for exporting and importing.
        // CSV files are not allowed to overwrite existing CSV files, hence requiring a second filePath.
        Path filePath = testFolder.resolve("TempAddressBook.csv");
        Path filePathExported = testFolder.resolve("TempAddressBookExported.csv");
        AddressBook original = getTypicalAddressBook();
        CsvAddressBookStorage csvAddressBookStorage = new CsvAddressBookStorage(filePath);

        // Save in new file and read back
        csvAddressBookStorage.saveAddressBook(original, filePathExported);
        ReadOnlyAddressBook readBack = csvAddressBookStorage.readAddressBook(filePathExported).get();
        assertEquals(original, new AddressBook(readBack));
        FileUtil.deleteFileIfExists(filePathExported);

        // Modify data, overwrite exiting file, and read back
        original.addPerson(HOON);
        original.removePerson(ALICE);
        csvAddressBookStorage.saveAddressBook(original, filePathExported);
        readBack = csvAddressBookStorage.readAddressBook(filePathExported).get();
        assertEquals(original, new AddressBook(readBack));
        FileUtil.deleteFileIfExists(filePathExported);

        // Save and read without specifying file path. The original file will be read.
        original.addPerson(IDA);
        csvAddressBookStorage.saveAddressBook(original); // file path not specified
        readBack = csvAddressBookStorage.readAddressBook().get(); // file path not specified
        assertEquals(original, new AddressBook(readBack));

    }

    @Test
    public void saveAddressBook_nullAddressBook_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(null, "SomeFile.csv"));
    }

    /**
     * Saves {@code addressBook} at the specified {@code filePath}.
     */
    private void saveAddressBook(ReadOnlyAddressBook addressBook, String filePath) {
        try {
            new CsvAddressBookStorage(Paths.get(filePath))
                    .saveAddressBook(addressBook, addToTestDataPathIfNotNull(filePath));
        } catch (IOException ioe) {
            throw new AssertionError("There should not be an error writing to the file.", ioe);
        }
    }

    @Test
    public void saveAddressBook_nullFilePath_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> saveAddressBook(new AddressBook(), null));
    }
}
