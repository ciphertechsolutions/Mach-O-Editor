package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O function starts command. Currently does nothing.
 */
public class FunctionStarts extends AbstractMachOCommand {

    public FunctionStarts(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.FUNCTION_STARTS;
    }


}
