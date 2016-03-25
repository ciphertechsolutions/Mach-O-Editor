package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;


/**
 * A class representing the Mach-O Dynamic Symbolic Table command.
 */
public class DySymTab extends AbstractMachOCommand {

    private static final int INDIRECT_SYMBOL_TABLE_OFFSET_POSITION = 56;
    private static final int EXTERNAL_RELOCATION_ENTRIES_OFFSET_POSITION = 64;

    public DySymTab(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.DY_SYM_TAB;
    }

    @Override
    public void parseCommand(BinaryWrapper binary) throws IOException {
        super.parseCommand(binary);
        this.offsetEntries.put(INDIRECT_SYMBOL_TABLE_OFFSET_POSITION, binary.getSingleWordAtRelativePosition(INDIRECT_SYMBOL_TABLE_OFFSET_POSITION));
        this.offsetEntries.put(EXTERNAL_RELOCATION_ENTRIES_OFFSET_POSITION, binary.getSingleWordAtRelativePosition(EXTERNAL_RELOCATION_ENTRIES_OFFSET_POSITION));
    }

}
