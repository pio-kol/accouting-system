package pl.coderstrust.database.file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.coderstrust.database.DbException;
import pl.coderstrust.database.ExceptionMsg;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileHelper {

  private final Logger logger = LoggerFactory.getLogger(FileHelper.class);
  private Configuration dbConfig;
  private File dbFile;
  private File tempFile;
  private FileStateCheck canWrite = (File file) -> file.canWrite();
  private FileStateCheck isDeleted = (File file) -> !file.exists();

  public FileHelper(Configuration dbConfig) {
    this.dbConfig = dbConfig;
    dbFile = new File(dbConfig.getDbFilePath());
    tempFile = new File(dbConfig.getDbTempFilePath());
    initializeDatabaseFile();
  }

  private void initializeDatabaseFile() {
    if (!dbFile.exists()) {
      try {
        dbFile.createNewFile();
      } catch (IOException e) {
        logger.warn(" from initializeDatabaseFile in FileHelper (File): "
            + e + ExceptionMsg.IO_ERROR_WHILE_INITIALIZING);
        throw new DbException(ExceptionMsg.IO_ERROR_WHILE_INITIALIZING, e);
      }
    }
  }

  public void addLine(String lineContent) {
    lineContent += System.lineSeparator();
    try {
      Files.write(dbFile.toPath(), lineContent.getBytes(), StandardOpenOption.APPEND);
    } catch (IOException e) {
      logger.warn(" from addLine in FileHelper (File): "
          + e + ExceptionMsg.IO_ERROR_WHILE_ADDING);
      throw new DbException(ExceptionMsg.IO_ERROR_WHILE_ADDING, e);
    }
  }

  public void deleteLine(String lineKey) {
    try {
      deleteLineAndSaveToTempFile(lineKey);
      updateDatabaseFromTempFile();
    } catch (InterruptedException e) {
      logger.warn(" from deleteLine in FileHelper (File): "
          + e + ExceptionMsg.INVOICE_PROCESSING_INTERRUPT);
      throw new DbException(ExceptionMsg.INVOICE_PROCESSING_INTERRUPT, e);
    } catch (IOException e) {
      logger.warn(" from deleteLine in FileHelper (File): "
          + e + ExceptionMsg.IO_ERROR_WHILE_DELETING);
      throw new DbException(ExceptionMsg.IO_ERROR_WHILE_DELETING, e);
    }
  }

  private void deleteLineAndSaveToTempFile(String lineKey) throws IOException {
    try (
        Stream<String> inputStream = Files.lines(dbFile.toPath());
        PrintWriter tempFileWriter = new PrintWriter(new FileWriter(tempFile))
    ) {
      inputStream
          .filter(line -> !line.contains(lineKey))
          .forEach(tempFileWriter::println);
    }
  }

  private void updateDatabaseFromTempFile() throws IOException, InterruptedException {
    waitForFileSystem(dbFile, canWrite);
    Files.delete(dbFile.toPath());

    waitForFileSystem(dbFile, isDeleted);
    Files.copy(tempFile.toPath(), dbFile.toPath());

    waitForFileSystem(tempFile, canWrite);
    Files.delete(tempFile.toPath());
  }

  private void waitForFileSystem(File checkedFile, FileStateCheck stateChecker)
      throws InterruptedException {
    int maxChecksCount = dbConfig.getFileSystemWaitTimeMs() / dbConfig.getUnitSleepTimeMs();
    int checkNumber = 0;
    while (!stateChecker.fileState(checkedFile) && checkNumber < maxChecksCount) {
      Thread.sleep(dbConfig.getUnitSleepTimeMs());
      checkNumber++;
    }
  }

  public String getLine(String lineKey) {
    try (Stream<String> dbStream = Files.lines(dbFile.toPath())) {
      String lineFound = dbStream
          .filter(line -> line.contains(lineKey))
          .collect(Collectors.joining());
      return lineFound;
    } catch (IOException e) {
      logger.warn(" from getLine in FileHelper (File): "
          + e + ExceptionMsg.IO_ERROR_WHILE_READING);
      throw new DbException(ExceptionMsg.IO_ERROR_WHILE_READING, e);
    }
  }

  public List<String> getAllLines() {
    try (Stream<String> dbStream = Files.lines(dbFile.toPath())) {
      return dbStream.collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException e) {
      logger.warn(" from getAllLines in FileHelper (File): "
          + e + ExceptionMsg.IO_ERROR_WHILE_READING);
      throw new DbException(ExceptionMsg.IO_ERROR_WHILE_READING, e);
    }
  }

  interface FileStateCheck {

    boolean fileState(File file);
  }
}