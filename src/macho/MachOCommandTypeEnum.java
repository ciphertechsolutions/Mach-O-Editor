package macho;

import macho.commands.AbstractMachOCommand;
import macho.commands.DataInCode;
import macho.commands.DySymTab;
import macho.commands.FunctionStarts;
import macho.commands.LoadDylib;
import macho.commands.LoadDylinker;
import macho.commands.Segment;
import macho.commands.SymTab;
import macho.commands.UnixThread;
import macho.commands.VersionMinMacOSX;
import editor.BinaryWrapper;

/**
 * An enum listing the supported Mach-O commands. Each one is tied to a corresponding
 * subclass of {@link AbstractMachOCommand}.
 *
 */
public enum MachOCommandTypeEnum {

    SEGMENT(1, "SEGMENT", Segment.class),

    SYM_TAB(2, "SYM_TAB", SymTab.class),

    UNIX_THREAD(5, "UNIX_THREAD", UnixThread.class),

    DY_SYM_TAB(11, "DY_SYM_TAB", DySymTab.class),

    LOAD_DYLIB(12, "LOAD_DYLIB", LoadDylib.class),

    LOAD_DYLINKER(14, "LOAD_DYLINKER", LoadDylinker.class),

    UUID(27, "UUID", macho.commands.UUID.class),

    VERSION_MIN_MAC_OSX(36, "VERSION_MIN_MAC_OSX", VersionMinMacOSX.class),

    FUNCTION_STARTS(38, "FUNCTION_STARTS", FunctionStarts.class),

    DATA_IN_CODE(41, "DATA_IN_CODE", DataInCode.class),
    /**
     * Represents any unsupported command type.
     */
    UNSUPPORTED(-1, "ERROR_UNSUPPORTED", AbstractMachOCommand.class);


    private int commandValue;
    private String name;
    private Class<? extends AbstractMachOCommand> toInstantiate;


    /**
     * Gets the appropriate Mach-O enum member given the Mach-O command type code.
     * Returns {@link #UNSUPPORTED} if the command type is not supported.
     * @param value The command type code.
     * @return The corresponding {@link MachOCommandTypeEnum} member.
     */
    public static MachOCommandTypeEnum getTypeForValue(int value){
        for (MachOCommandTypeEnum type : MachOCommandTypeEnum.values()) {
            if (type.getCommandValue() == value) {
                return type;
            }
        }
        return UNSUPPORTED;
    }

    /**
     * Instantiates a {@link MachOCommandTypeEnum} with the given command code, name, and backing class.
     * @param commandValue The command type code.
     * @param name The name of the command.
     * @param toInstantiate The class representing this command.
     */
    MachOCommandTypeEnum(int commandValue, String name, Class<? extends AbstractMachOCommand> toInstantiate) {
        this.commandValue = commandValue;
        this.name = name;
        this.toInstantiate = toInstantiate;
    }

    /**
     * Gets the command type code.
     * @return The command type code.
     */
    public int getCommandValue() {
        return commandValue;
    }

    /**
     * Gets the name of this Mach-O command.
     * @return The name.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Instantiate an instance of the class that represents this type of command. This will instantiate the command
     * assuming that the current position of the {@link BinaryWrapper} is aligned to the start of this command.
     * @param binary The {@link BinaryWrapper} to read this command from.
     * @return The {@link AbstractMachOCommand} subclass representing this command.
     */
    public AbstractMachOCommand instantiate(BinaryWrapper binary){
        // Instantiate our corresponding command class.
        try {
            return toInstantiate.getConstructor(binary.getClass()).newInstance(binary);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Failed to instantiate " + toInstantiate.toString() +  " from type " + this.toString());
    }
}
