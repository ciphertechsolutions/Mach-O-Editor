package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O UUID command. Currently does nothing.
 */
public class UUID extends AbstractMachOCommand {

    public UUID(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.UUID;
    }

}
