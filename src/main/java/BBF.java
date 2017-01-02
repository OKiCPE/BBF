import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class BBF {
    private static final Logger log = LogManager.getLogger(BBF.class);
    //private static final int FILE_SIZE_FROM_HEADER_OFFSET = 0x08;
    //private static final int NUMBER_OF_UNITS_IN_FILE_OLD = 0x0c;
    private static final int NUMBER_OF_UNITS_IN_FILE_V3 = 0x0e;
    private static final int UNITS_LENGTH_TYPE_V3_OFFSET = 0x0d;
    private static byte[] BBF_MAGIC = {0x00, 0x42, 0x42, 0x46};//in hex, BBF in string
    private static byte PATTERN_LENGTH;


    static {
        PATTERN_LENGTH = (byte) BBF_MAGIC.length;
    }

    private short version;
    private int fileSize;
    private byte[] data;
    private boolean isBBF;
    private short numberOfUnits;
    private int numberOfSubUnits;
    private int startOffset;
    private Map<Integer, String> unitsNamesMap = new LinkedHashMap<>();
    private List<String> subUnitsNamesList = new LinkedList<>();

    BBF(byte[] data) {
        this.data = data;
    }

    public int getNumberOfSubUnits() {
        return numberOfSubUnits;
    }

    short getNumberOfUnits() {
        return numberOfUnits;
    }

    short getVersion() {
        return version;
    }

    int getFileSize() {
        return fileSize;
    }

    Map<Integer, String> getUnitsNamesMap() {
        return unitsNamesMap;
    }

    void init() {
        //check BBF magic
        byte count = 0;
        for (byte position = 0; position < PATTERN_LENGTH; position++) {
            if (data[position] != BBF_MAGIC[position]) {
                break;
            } else {
                count++;
            }
        }
        if (count == PATTERN_LENGTH) {
            isBBF = true;
        }


        //get version
        version = (short) ((data[0x04] & 0xff) | ((data[0x04 + 1] & 0xff) << 8));


        //get size
        fileSize = ((data[0x08] & 0xff) | ((data[0x08 + 1] & 0xff) << 8)) + 12;


        //get number of units in file
        if (version == 3) {
            short unitsLengthType = (short) (data[UNITS_LENGTH_TYPE_V3_OFFSET] & 0xff);
            if (unitsLengthType == 0x41) {
                startOffset = 0x0f;
                numberOfUnits = (short) ((data[NUMBER_OF_UNITS_IN_FILE_V3] & 0xff));
            } else if (unitsLengthType == 0x81) {
                startOffset = 0x10;
                numberOfUnits = (short) ((data[numberOfUnits] & 0xff) | ((data[numberOfUnits + 1] & 0xff) << 8));
            } else {
                log.error("Wrong units length type");
            }
        }

    }

    private int hash(String name) {
        int hash = 0x05;
        byte length = (byte) name.length();
        for (byte index = 0; index < length; index++) {
            hash = (33 * hash + name.charAt(index)) & 0xff;
        }
        return hash;
    }


    void parse() {
        int currentPosition = unitsNamesParce();

        //4 byte alignment
        while (currentPosition % 4 != 0) {
            currentPosition++;
        }


        //get number of subunits
        int subUnitBlockLength = (data[currentPosition] & 0xff)
                | ((data[currentPosition + 1] & 0xff) << 8);

        if (subUnitBlockLength > 0) {
            currentPosition += 2;
            int subUnitBlockType = data[currentPosition + 1] & 0xff;
            currentPosition += 2;
            if (subUnitBlockType == 0x40) {
                numberOfSubUnits = (data[currentPosition] & 0xff);
                currentPosition++;
            } else if (subUnitBlockType == 0x80) {
                numberOfSubUnits = (data[currentPosition] & 0xff)
                        | ((data[currentPosition + 1] & 0xff) << 8);
                currentPosition += 2;
            } else {
                log.error("Error parsing sub units block type");
            }
        } else {
            while (currentPosition % 4 != 0) {
                currentPosition++;
            }
        }


        //get list of subunits names
        int subUnitLength;
        for (byte subUnit = 0; subUnit < numberOfSubUnits; subUnit++) {
            subUnitLength = (data[currentPosition] & 0xff);
            currentPosition++;
            if (subUnitLength >= 0x80) {
                currentPosition++;
                subUnitLength = (subUnitLength - 0x80) * 0x100 + (data[currentPosition] & 0xff);
                currentPosition++;
                String subUnitName = new String(data, currentPosition, subUnitLength);
                subUnitsNamesList.add(subUnitName);
            }
            currentPosition += subUnitLength;
        }


        //4 byte alignment
        while (currentPosition % 4 != 0) {
            currentPosition++;
        }

        parseData(currentPosition, subUnitsNamesList);


    }


    private void parseData(int position, List<String> subUnitsNamesList) {

    }


    private int unitsNamesParce() {
        int currentPosition = startOffset;
        short unitLength;
        for (short unit = 0; unit < numberOfUnits; unit++) {
            unitLength = (short) (data[currentPosition] & 0xff);
            currentPosition++;
            String name = new String(data, currentPosition, unitLength);
            int hash = hash(name);
            if (unitsNamesMap.containsKey(hash)) {
                hash += 0x100;
            }
            unitsNamesMap.put(hash, name);
            currentPosition += unitLength;
        }
        return currentPosition;
    }

    boolean isBBF() {
        return isBBF;
    }
}
