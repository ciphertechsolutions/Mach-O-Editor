package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O Minimum Mac OSX version command. Currently does nothing.
 */
public class VersionMinMacOSX extends AbstractMachOCommand {

    public VersionMinMacOSX(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.VERSION_MIN_MAC_OSX;
    }
}
