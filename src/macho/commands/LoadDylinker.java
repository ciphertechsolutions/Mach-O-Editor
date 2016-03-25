package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;


/**
 * A class representing the Mach-O load dynamic linker command.
 */
public class LoadDylinker extends AbstractMachOCommand {

    public LoadDylinker(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.LOAD_DYLINKER;
    }

}
