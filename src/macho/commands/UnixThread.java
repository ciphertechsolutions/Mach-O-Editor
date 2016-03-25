package macho.commands;

import java.io.IOException;

import macho.MachOCommandTypeEnum;
import editor.BinaryWrapper;

/**
 * A class representing the Mach-O Unix Thread command. Currently does nothing.
 */
public class UnixThread extends AbstractMachOCommand {

    public UnixThread(BinaryWrapper binary) throws IOException {
        super(binary);
        this.commandType = MachOCommandTypeEnum.UNIX_THREAD;
    }

}
