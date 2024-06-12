import com.simple.logging.application.utility.LogUtility;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LogUtilityTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set up UtilityObjects before each test
        LogUtility.UtilityObjects.setObjects(tempDir.toString(), "testApp");
    }

    @Test
    void testMoveFile() throws IOException {
        Path sourceFile = Files.createFile(tempDir.resolve("source.log"));
        Path targetFile = tempDir.resolve("target.log");

        LogUtility.moveFile(sourceFile, targetFile);

        assertTrue(Files.exists(targetFile));
        assertFalse(Files.exists(sourceFile));
    }

    @Test
    void testRenameFile() throws IOException {
        Path sourceFile = Files.createFile(tempDir.resolve("source.log"));
        Path renamedFile = tempDir.resolve("renamed.log");

        LogUtility.renameFile(sourceFile, "renamed.log");

        assertTrue(Files.exists(renamedFile));
        assertFalse(Files.exists(sourceFile));
    }

    @Test
    void testDeleteFile() throws IOException {
        Path fileToDelete = Files.createFile(tempDir.resolve("fileToDelete.log"));

        LogUtility.deleteFile(fileToDelete);

        assertFalse(Files.exists(fileToDelete));
    }

    @Test
    void testZipFile() throws IOException {
        Path fileToZip = Files.createFile(tempDir.resolve("fileToZip.log"));
        Files.write(fileToZip, List.of("log line 1", "log line 2"));

        LogUtility.zipFile(fileToZip);

        Path zippedFile = tempDir.resolve("fileToZip.log.zip");
        assertTrue(Files.exists(zippedFile));
    }

    @Test
    void testGetAllLogFiles() throws IOException {
        Files.createFile(tempDir.resolve("testApp-2023-01-01.log"));
        Files.createFile(tempDir.resolve("testApp-2023-01-02.log"));

        List<File> logFiles = LogUtility.getAllLogFiles();

        assertEquals(2, logFiles.size());
    }

    @Test
    void testGetLogFilesBetweenDates() throws IOException {
        Files.createFile(tempDir.resolve("testApp-2023-01-01.log"));
        Files.createFile(tempDir.resolve("testApp-2023-01-02.log"));
        Files.createFile(tempDir.resolve("testApp-2023-01-03.log"));

        LocalDate startDate = LocalDate.of(2023, 1, 1);
        LocalDate endDate = LocalDate.of(2023, 1, 2);

        List<File> logFiles = LogUtility.getLogFilesBetweenDates(startDate, endDate);

        assertEquals(2, logFiles.size());
    }

    @Test
    void testGetLogFilesForDate() throws IOException {
        Files.createFile(tempDir.resolve("testApp-2023-01-01.log"));
        Files.createFile(tempDir.resolve("testApp-2023-01-02.log"));

        LocalDate date = LocalDate.of(2023, 1, 1);

        List<File> logFiles = LogUtility.getLogFilesForDate(date);

        assertEquals(1, logFiles.size());
        assertEquals("testApp-2023-01-01.log", logFiles.get(0).getName());
    }

    @Test
    void testSearchLogFile() throws IOException {
        Path logFile = Files.createFile(tempDir.resolve("searchTest.log"));
        Files.write(logFile, List.of("this is a test line", "another test line", "no match here"));

        List<String> matchedLines = LogUtility.searchLogFile(logFile, "test");

        assertEquals(2, matchedLines.size());
    }

    @Test
    void testGenerateFileFromSearch() throws IOException {
        String fileName = "generatedFile";
        List<String> lines = List.of("line1", "line2", "line3");

        LogUtility.generateFileFromSearch(fileName, lines);

        Path generatedFile = tempDir.resolve(fileName + ".log");
        assertTrue(Files.exists(generatedFile));

        List<String> fileContent = Files.readAllLines(generatedFile);
        assertEquals(lines, fileContent);
    }
}