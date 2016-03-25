package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O Symbolic Table command.
 */
public class SymTab extends AbstractMachOCommand {

    private static final int SYMBOL_TABLE_OFFSET_POSITION = 8;
    private static final int STRING_TABLE_OFFSET_POSITION = 16;

    public SymTab(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.SYM_TAB;
    }

    @Override
    public void parseCommand(BinaryWrapper binary) throws IOException {
        super.parseCommand(binary);
        this.offsetEntries.put(SYMBOL_TABLE_OFFSET_POSITION, binary.getSingleWordAtRelativePosition(SYMBOL_TABLE_OFFSET_POSITION));
        this.offsetEntries.put(STRING_TABLE_OFFSET_POSITION, binary.getSingleWordAtRelativePosition(STRING_TABLE_OFFSET_POSITION));
    }

}
