package editor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import macho.MachOCommandFactory;
import macho.commands.AbstractMachOCommand;

/**
 * This class was designed specifically to replace the contents of one of the data sections
 * with custom data. As such, it is currently very tailored to that purpose and should be
 * generalized. Success when expanding the file is far from guaranteed, as references in the
 * executable may not be appropriately updated.
 *
 */
public class DataSegmentSwapper {

    private static final int FIRST_COMMAND_OFFSET = 28;
    private static final int HEADER_SEGMENTS_OFFSET = 16;

    // These addresses currently need to be manually set. Ideally this would not be the case.
    private static final int SEGMENT_OFFSET = 12314;
    private static final int SEGMENT_ADDRESS = 124215;
    private static final int OLD_SIZE = 12321;

    private long newSize = 0l;
    private int sizeDiff = 0;
    private final BinaryWrapper binary;
    private final FileChannel newDataSegment;
    private List<AbstractMachOCommand> commands;

    /**
     * Given a string representing the path to the Mach-O file and a path to the new file to
     * put in it, overwrites a portion of the Mach-O file with the other file. It will update
     * the Mach-O header as necessary.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        long time = System.currentTimeMillis();
        if (args.length < 2) {
            System.out.println("Invalid args. Needs pathToBinary pathToNewSegment");
        }
        Path binaryLocation = Paths.get(args[0]);
        Path newDataSegmentLocation = Paths.get(args[1]);
        DataSegmentSwapper tarSwapper = new DataSegmentSwapper(binaryLocation, newDataSegmentLocation);
        tarSwapper.parseHeader();
        tarSwapper.swapFile();
        tarSwapper.updateHeader();
        System.out.println(System.currentTimeMillis() - time);
    }

    private void updateHeader() throws IOException {
        if (sizeDiff <= 0) {
            return;
        }
        for (AbstractMachOCommand command : commands) {
            binary.setPosition(command.getCommandStartOffset());
            command.updateSizeifNeeded(binary, SEGMENT_OFFSET, sizeDiff);
            command.updateOffsetsIfNeeded(binary, SEGMENT_OFFSET, sizeDiff);
            command.updateAddressesIfNeeded(binary, SEGMENT_ADDRESS, sizeDiff);
            command.updateObjCAddressesIfNeeded(binary, SEGMENT_ADDRESS, sizeDiff);
        }
        binary.setPosition(0);
    }

    private void swapFile() throws IOException {
        if (sizeDiff > 0) {
            binary.insertFileAtOffsetOverriding(SEGMENT_OFFSET, newDataSegment, OLD_SIZE);
        }
        else {
            binary.insertFileAtOffsetOverridingAndZeroing(SEGMENT_OFFSET, newDataSegment, OLD_SIZE);
        }
    }

    /**
     * Given a Path to the Mach-O file and a Path to the new file to insert into it,
     * constructs a new {@link DataSegmentSwapper}.
     * @param binaryLocation The Path to the Mach-O file
     * @param newDataSegmentLocation The Path to the file to insert into the Mach-O file
     * @throws IOException
     */
    public DataSegmentSwapper(Path binaryLocation, Path newDataSegmentLocation) throws IOException {
        binary = new BinaryWrapper(binaryLocation);
        newDataSegment = FileChannel.open(newDataSegmentLocation, StandardOpenOption.READ, StandardOpenOption.WRITE);
        newSize = newDataSegment.size();
        sizeDiff = (int) (newSize - OLD_SIZE);
        if (sizeDiff > 0) {
            while (sizeDiff % 64 != 0) {
                sizeDiff++;
                ByteBuffer padding = ByteBuffer.allocate(1);
                padding.put((byte) 0);
                padding.position(0);
                newDataSegment.write(padding, newSize);
                newSize++;
            }
        }
    }

    private List<AbstractMachOCommand> parseHeader() throws IOException {
        int commandCount = binary.getSingleWordAtPosition(HEADER_SEGMENTS_OFFSET);
        binary.setPosition(FIRST_COMMAND_OFFSET);
        commands = new ArrayList<>(commandCount);
        while (commands.size() < commandCount) {
            commands.add(parseCommand());
        }
        binary.setPosition(0);
        return commands;
    }

    private AbstractMachOCommand parseCommand() throws IOException {
        AbstractMachOCommand command = MachOCommandFactory.createMachOCommand(binary);
        command.parseCommand(binary);
        binary.setPosition(command.getCommandStartOffset()  + command.getCommandSize());
        return command;
    }

}
