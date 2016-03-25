package macho.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O Segment command.
 */
public class Segment extends AbstractMachOCommand {


    private static final char[] OBJ_C = "__OBJC".toCharArray();
    private static final char[] MODULE_INFO = "__module_info".toCharArray();
    private static final char[] META_CLASS = "__meta_class".toCharArray();
    private static final char[] INSTANCE_VARS = "__instance_vars".toCharArray();
    private static final char[] CLASS = "__class".toCharArray();
    private static final char[] SYMBOLS = "__symbols".toCharArray();
    private static final int SEGMENT_NAME_POSITION = 8;
    private static final int NAME_SIZE = 16;
    private static final int FILE_OFFSET_POSITION = 32;
    private static final int FILE_SIZE_POSITION = 36;
    private static final int VM_ADDRESS_POSITION = 24;
    private static final int VM_SIZE_POSITION = 28;
    private static final int NUMBER_OF_SECTIONS_POSITION = 48;
    private static final int FIRST_SECTION_POSITION = 56;
    private static final int SECTION_HEADER_SIZE = 68;
    private int numberOfSections;
    private List<Section> sections;

    public Segment(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.SEGMENT;
    }

    @Override
    public void updateSizeifNeeded(BinaryWrapper binary, int modifiedStartOffset, int diffFromOriginal) throws IOException {
        int originalSize = binary.getSingleWordAtRelativePosition(FILE_SIZE_POSITION);
        if (modifiedStartOffset >= this.offsetEntries.get(FILE_OFFSET_POSITION) &&
            modifiedStartOffset < this.offsetEntries.get(FILE_OFFSET_POSITION) + originalSize){
            binary.setSingleWordAtRelativePosition(originalSize + diffFromOriginal, FILE_SIZE_POSITION);
            // Pretty sure VM_SIZE always equals FILE_SIZE...
            binary.setSingleWordAtRelativePosition(originalSize + diffFromOriginal, VM_SIZE_POSITION);
            for (Section section : sections) {
                section.updateSizeifNeeded(binary, modifiedStartOffset, diffFromOriginal) ;
            }
        }
    }

    @Override
    public void updateObjCAddressesIfNeeded(BinaryWrapper binary, int modifiedStartAddress, int diffFromOriginal) throws IOException {
        char[] name = getName(binary, SEGMENT_NAME_POSITION);
        if (!compareCharArrays(name, OBJ_C)) {
            return;
        }
        for (Section section : sections) {
            section.updateObjCAddressesifNeeded(binary, modifiedStartAddress, diffFromOriginal) ;
        }
    }

    private static char[] getName(BinaryWrapper binary, int position) throws IOException {
        char[] name = new char[NAME_SIZE];
        for (int i = 0; i < NAME_SIZE; i++) {
            name[i] = binary.getSingleByteAtRelativePosition(position + i);
        }
        return name;
    }

    private static boolean compareCharArrays(char[] raw, char[] nonPadded) {
        if (raw.length < nonPadded.length) {
            return false;
        }
        for (int i = 0; i < raw.length; i++) {
            if ((i < nonPadded.length && raw[i] != nonPadded[i]) || (i >= nonPadded.length && raw[i] != '\0')) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void parseCommand(BinaryWrapper binary) throws IOException {
        super.parseCommand(binary);
        this.offsetEntries.put(FILE_OFFSET_POSITION, binary.getSingleWordAtRelativePosition(FILE_OFFSET_POSITION));
        this.addressEntries.put(VM_ADDRESS_POSITION, binary.getSingleWordAtRelativePosition(VM_ADDRESS_POSITION));
        getSections(binary);

    }

    private void getSections(BinaryWrapper binary) throws IOException {
        this.numberOfSections = binary.getSingleWordAtRelativePosition(NUMBER_OF_SECTIONS_POSITION);
        sections = new ArrayList<>(numberOfSections);
        while (sections.size() < numberOfSections) {
            sections.add(getNextSection(sections.size(), binary));
        }
    }

    private Section getNextSection(int sectionNumber, BinaryWrapper binary) throws IOException {
        Section section = new Section(sectionNumber);
        section.parseSection(binary);
        return section;
    }

    /**
     * This class represents a Section within a Segment in a Mach-O file.
     */
    class Section {
        private static final int SECTION_NAME_POSITION = 0;
        private static final int OFFSET_POSITION = 40;
        private static final int ADDRESS_POSITION = 32;
        private static final int SIZE_POSITION = 36;
        private final int sectionOffset;

        /**
         * Construct a section, given the section number.
         * @param sectionNumber
         */
        Section(int sectionNumber){
            this.sectionOffset = SECTION_HEADER_SIZE * sectionNumber + FIRST_SECTION_POSITION;
        }

        @SuppressWarnings("synthetic-access")
        public void updateObjCAddressesifNeeded(BinaryWrapper binary, int modifiedStartAddress, int diffFromOriginal) throws IOException {
            if (binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(ADDRESS_POSITION)) > modifiedStartAddress &&
                    isSectionToUpdate(getName(binary, getOffsetRelativeToCommandStart(SECTION_NAME_POSITION)))){
                scanAndUpdateBinary(binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(OFFSET_POSITION)),
                        binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(SIZE_POSITION)),
                        modifiedStartAddress, diffFromOriginal, binary);
            }
        }

        private void scanAndUpdateBinary(int startingOffset, int sizeToScan, int modifiedStartAddress, int addressDiff, BinaryWrapper binary) throws IOException {
            // TODO: This is very buggy, fix this to actually attempt some level of instruction decoding perhaps.
            int currentOffset = startingOffset;
            int endOffset = startingOffset + sizeToScan;
            while (currentOffset < endOffset) {
                int currentWordValue = binary.getSingleWordAtPosition(currentOffset);
                // We pray it is actually an address value.
                if (currentWordValue > modifiedStartAddress) {
                    binary.setSingleWordAtPosition(currentWordValue + addressDiff, currentOffset);
                }
                currentOffset += 4;
            }

        }

        @SuppressWarnings("synthetic-access")
        private boolean isSectionToUpdate(char[] name){
            return compareCharArrays(name, MODULE_INFO) ||
                compareCharArrays(name, META_CLASS) ||
                compareCharArrays(name, INSTANCE_VARS) ||
                compareCharArrays(name, CLASS) ||
                compareCharArrays(name, SYMBOLS);
        }

        @SuppressWarnings("synthetic-access")
        public void updateSizeifNeeded(BinaryWrapper binary, int modifiedStartOffset, int diffFromOriginal) throws IOException {
            int originalSize = binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(SIZE_POSITION));
            if (modifiedStartOffset >= offsetEntries.get(getOffsetRelativeToCommandStart(OFFSET_POSITION)) &&
                    modifiedStartOffset < offsetEntries.get(getOffsetRelativeToCommandStart(OFFSET_POSITION)) + originalSize){
                binary.setSingleWordAtRelativePosition(originalSize + diffFromOriginal, getOffsetRelativeToCommandStart(SIZE_POSITION));
            }
        }

        private int getOffsetRelativeToCommandStart(int relativeToSectionStart) {
            return sectionOffset + relativeToSectionStart;
        }

        @SuppressWarnings("synthetic-access")
        public void parseSection(BinaryWrapper binary) throws IOException {
            offsetEntries.put(getOffsetRelativeToCommandStart(OFFSET_POSITION),
                    binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(OFFSET_POSITION)));
            addressEntries.put(getOffsetRelativeToCommandStart(ADDRESS_POSITION),
                    binary.getSingleWordAtRelativePosition(getOffsetRelativeToCommandStart(ADDRESS_POSITION)));
        }

    }

}
