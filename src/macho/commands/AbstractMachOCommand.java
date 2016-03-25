package macho.commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class to serve as the basis of classes representing Mach-O commands.
 * Specifies methods for interacting with Mach-O commands.
 */
public abstract class AbstractMachOCommand {

    protected MachOCommandTypeEnum commandType = null;
    protected long startOffset;
    protected int commandSize;
    protected Map<Integer, Integer> offsetEntries;
    protected Map<Integer, Integer> addressEntries;

    /**
     * Construct a Mach-O command from the given {@link BinaryWrapper} starting at the current
     * position in the binary. This class will contain very little of value until
     * {@link #parseCommand(BinaryWrapper)} is called.
     * @param binary The {@link BinaryWrapper}
     * @throws IOException
     */
    public AbstractMachOCommand(BinaryWrapper binary) throws IOException{
        startOffset = binary.getPosition();
        offsetEntries = new HashMap<>();
        addressEntries = new HashMap<>();
    }

    /**
     * Get the {@link MachOCommandTypeEnum} associated with this command.
     * @return The {@link MachOCommandTypeEnum).
     */
    public MachOCommandTypeEnum getCommandType() {
        return commandType;
    }

    /**
     * Get the size of this command, in bytes.
     * @return The size, in bytes, as an int.
     */
    public int getCommandSize() {
        return commandSize;
    }

    /**
     * Get the offset from the start of the {@link BinaryWrapper} that was used to construct this command
     * to the start of this command.
     * @return The offset, as a long.
     */
    public long getCommandStartOffset() {
        return startOffset;
    }

    /**
     * Gets the lists of offsets associated with this command.
     * @return The list of offsets.
     */
    public List<Integer> getOffsetsList(){
        return new ArrayList<>(offsetEntries.values());
    }

    /**
     * Updates any offsets that point to addresses after the one modified.
     * @param binary The file to modify
     * @param modifiedStartOffset Where the modification was made.
     * @param diffFromOriginal The change in size the modification caused.
     * @throws IOException
     */
    public void updateOffsetsIfNeeded(BinaryWrapper binary, int modifiedStartOffset, int diffFromOriginal) throws IOException {
        for (Entry<Integer,Integer> entry : offsetEntries.entrySet()){
            if (entry.getValue() > modifiedStartOffset) {
                binary.setSingleWordAtRelativePosition(entry.getValue()+ diffFromOriginal, entry.getKey());
            }
        }
    }

    /**
     * Updates any addresses that are after the one modified.
     * @param binary The file to modify
     * @param modifiedStartAddress Where the modification was made.
     * @param diffFromOriginal The change in size the modification caused.
     * @throws IOException
     */
    public void updateAddressesIfNeeded(BinaryWrapper binary, int modifiedStartAddress, int diffFromOriginal) throws IOException {
        for (Entry<Integer,Integer> entry : addressEntries.entrySet()){
            if (entry.getValue() > modifiedStartAddress) {
                binary.setSingleWordAtRelativePosition(entry.getValue()+ diffFromOriginal, entry.getKey());
            }
        }
    }

    /**
     * Update any references in the compiled code to addresses after the one modified. <br>
     * <b> WARNING: The implementation of this is currently buggy </b>
     * @param binary The file to modify
     * @param modifiedStartAddress Where the modification was made.
     * @param diffFromOriginal The change in size the modification caused.
     * @throws IOException
     */
    public void updateObjCAddressesIfNeeded(BinaryWrapper binary, int modifiedStartAddress, int diffFromOriginal) throws IOException {
        // Do nothing by default. Only Segment needs to do this for our purposes.
    }

    /**
     * Update the size of this command, if needed. This will only be necessary if this Mach-O command
     * contained the modified portion of the binary.
     * @param binary The file to modify
     * @param modifiedStartOffset Where the modification was made.
     * @param diffFromOriginal The change in size the modification caused.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void updateSizeifNeeded(BinaryWrapper binary, int modifiedStartOffset, int diffFromOriginal) throws IOException {
        // Do nothing by default. Only Segment needs to do this for our purposes.
    }

    /**
     * Parses this command from the {@link BinaryWrapper}. By default this only determines the size of this command.
     * @param binary The {@link BinaryWrapper}, the command must start at the same offset in this binary as the one
     * that was used to construct this command.
     * @throws IOException
     */
    public void parseCommand(BinaryWrapper binary) throws IOException {
        commandSize = binary.getSingleWordAtPosition(startOffset + 4);
    }

}
