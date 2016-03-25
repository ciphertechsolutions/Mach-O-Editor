package macho;

import java.io.IOException;

import macho.commands.AbstractMachOCommand;
import editor.BinaryWrapper;

/**
 * A class to construct the correct {@link AbstractMachOCommand} subclass from the command starting at the
 * current position in the {@link BinaryWrapper}.
 */
public class MachOCommandFactory {

    /**
     * Constructs the correct {@link AbstractMachOCommand} subclass from the command starting at the
     * current position in the {@link BinaryWrapper}.
     * @param binary The {@link BinaryWrapper} to read from.
     * @return A {@link AbstractMachOCommand} representing the command at the current position in the binary.
     * @throws IOException
     */
    public static AbstractMachOCommand createMachOCommand(BinaryWrapper binary) throws IOException {
        int commandType = binary.getSingleWordAtRelativePosition(0);
        return MachOCommandTypeEnum.getTypeForValue(commandType).instantiate(binary);
    }
}
