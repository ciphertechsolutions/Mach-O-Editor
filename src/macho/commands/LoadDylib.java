package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O load dynamic library command. Currently does nothing.
 */
public class LoadDylib extends AbstractMachOCommand {

    public LoadDylib(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.LOAD_DYLIB;
    }

}
