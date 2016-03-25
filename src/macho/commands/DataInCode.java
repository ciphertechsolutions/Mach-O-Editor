package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;


/**
 * A class representing the Mach-O Data in Code command. Currently does nothing.
 */
public class DataInCode extends AbstractMachOCommand {

    public DataInCode(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.DATA_IN_CODE;
    }

}
