import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;

public class BBFTest {
    private static final Logger log = LogManager.getLogger(BBFTest.class);
    private BBF first;
    private BBF second;
    private static byte[] firstArray;
    private static byte[] secondArray;

    @BeforeClass
    public static void readTestFile() {
        firstArray = readFile("TestFiles\\first");
        secondArray = readFile("TestFiles\\second");
    }

    @Before
    public void init() {
        first = new BBF(firstArray);
        second = new BBF(secondArray);
        first.init();
        second.init();
        first.parse();
        second.parse();
    }

    public static void main(String[] args) {

    }

    @Test
    public void checkVersion() {
        Assert.assertEquals(first.getVersion(), 3);
        Assert.assertEquals(first.getVersion(), 3);
    }

    @Test
    public void checkSize() {
        Assert.assertEquals(first.getFileSize(), 560);
        Assert.assertEquals(second.getFileSize(), 372);
    }

    @Test
    public void isBBF() {
        Assert.assertEquals(first.isBBF(), true);
        Assert.assertEquals(second.isBBF(), true);
    }

    @Test
    public void parseNames() {

        Assert.assertEquals(first.getUnitsNamesMap().size(), 31);
        Assert.assertEquals(second.getUnitsNamesMap().size(), 19);
    }

    @Test
    public void checkNumberOfUnits() {
        Assert.assertEquals(first.getNumberOfUnits(), 31);
        Assert.assertEquals(second.getNumberOfUnits(), 19);
    }

    @Test
    public void checkNumberofSubUnits() {
        Assert.assertEquals(first.getNumberOfSubUnits(), 8);
        Assert.assertEquals(second.getNumberOfSubUnits(), 2);
    }

    private static byte[] readFile(String filename) {
        File file = new File(filename);
        FileInputStream inputStream;
        BufferedInputStream bufferedStream;
        ByteArrayOutputStream outputStream;
        try {
            inputStream = new FileInputStream(file);
            bufferedStream = new BufferedInputStream(inputStream);
            short i;
            outputStream = new ByteArrayOutputStream();
            while ((i = (short) bufferedStream.read()) != -1) {
                outputStream.write(i);
            }
            byte[] outputArray;
            outputStream.flush();
            outputArray = outputStream.toByteArray();
            outputStream.close();
            bufferedStream.close();
            inputStream.close();
            return outputArray;
        } catch (IOException exception) {
            log.error("Test file read error " + exception);
            return new byte[1];
        }
    }
}
