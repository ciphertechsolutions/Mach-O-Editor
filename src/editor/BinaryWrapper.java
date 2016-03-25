package editor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * A class designed to wrap a Mach-O binary to allow inspection and modification of it.
 */
public class BinaryWrapper {

    /**
     * The default size of a word. <br>
     * Note: changing this alone is not sufficient to change word size.
     * Some of the code assumes {@code word size = sizeof(int)}
     */
    private static final int WORD_SIZE = 4;
    private final FileChannel binary;

    /**
     * Creates a {@link BinaryWrapper} given a Path.
     * @param binaryLocation A Path to the Mach-O file to be wrapped
     * @throws IOException
     */
    public BinaryWrapper(Path binaryLocation) throws IOException {
        binary = FileChannel.open(binaryLocation, StandardOpenOption.READ, StandardOpenOption.WRITE);
    }

    /**
     * Gets the current position of the file channel.
     * @return The current position.
     * @throws IOException
     */
    public long getPosition() throws IOException {
        return binary.position();
    }

    /**
     * Sets the current position of the file channel.
     * @param position The position to seek to.
     * @throws IOException
     */
    public void setPosition(long position) throws IOException {
        binary.position(position);
    }

    /**
     * Inserts the entire contents of the given {@link FileChannel} into this file, starting at the given position. <br>
     * This will replace the contents of the wrapped file between {@code position} and {@code position + amountToOverwrite} with
     * the contents of {@code inputBytes}. Everything after that will be preserved. This means the file will change either shrink
     * or expand depending on if {@code inputBytes.size()} is smaller or larger than {@code amountToOverwrite}.
     *
     * @param position The position to start overwriting the wrapped file.
     * @param inputBytes The contents to write into this file.
     * @param amountToOverwrite The amount of this file to replace.
     * @throws IOException
     */
    public void insertFileAtOffsetOverriding(long position, FileChannel inputBytes, long amountToOverwrite) throws IOException {
        long originalPosition = binary.position();
        long originalInputPosition = inputBytes.position();
        ByteBuffer fileContentsToSave = saveOldBytes(position, amountToOverwrite);
        binary.position(position);
        long toTransfer = writeFile(inputBytes);
        binary.position(position + toTransfer);
        appendOldContents(fileContentsToSave);
        binary.position(originalPosition);
        inputBytes.position(originalInputPosition);
    }

    private void appendOldContents(ByteBuffer fileContentsToSave) throws IOException {
        binary.write(fileContentsToSave);
    }

    private long writeFile(FileChannel inputBytes) throws IOException {
        long toTransfer = inputBytes.size();
        long transferredBytes = inputBytes.transferTo(0, toTransfer, binary);
        if (transferredBytes != toTransfer) {
            throw new IOException("Did not write enough bytes!");
        }
        return toTransfer;
    }

    private ByteBuffer saveOldBytes(long position, long amountToOverwrite) throws IOException {
        long oldFileResume = position + amountToOverwrite;
        int amountToSave = (int) (binary.size() - oldFileResume);
        ByteBuffer fileContentsToSave = getLittleEndianByteBuffer(amountToSave);
        int consumedBytes = binary.read(fileContentsToSave, oldFileResume);
        if (consumedBytes != amountToSave) {
            throw new IOException("Did not read enough bytes!");
        }
        fileContentsToSave.position(0);
        return fileContentsToSave;
    }

    /**
     * Retrieves a single word from the given absolute position.
     * @param position The absolute position to start retrieval.
     * @return The word, as an int.
     * @throws IOException
     */
    public int getSingleWordAtPosition(long position) throws IOException{
        ByteBuffer readByte = getLittleEndianByteBuffer(WORD_SIZE);
        int consumedBytes = binary.read(readByte, position);
        readByte.position(0);
        if (consumedBytes == WORD_SIZE) {
            return readByte.getInt();
        }
        throw new IOException("Did not read enough bytes!");
    }

    /**
     * Retrieves a single byte from the relative position given.
     * @param position The position, relative to the current position of the channel.
     * @return The single byte, represented as a char.
     * @throws IOException
     */
    public char getSingleByteAtRelativePosition(long position) throws IOException{
        ByteBuffer readByte = getLittleEndianByteBuffer(1);
        int consumedBytes = binary.read(readByte,  binary.position() + position);
        readByte.position(0);
        if (consumedBytes == 1) {
            return (char) readByte.get();
        }
        throw new IOException("Did not read enough bytes!");
    }

    /**
     * Retrieves a single word from the relative position given.
     * @param position The position, relative to the current position of the channel.
     * @return The single word, represented as a int.
     * @throws IOException
     */
    public int getSingleWordAtRelativePosition(int position) throws IOException{
        ByteBuffer readByte = getLittleEndianByteBuffer(WORD_SIZE);
        int consumedBytes = binary.read(readByte, binary.position() + position);
        readByte.position(0);
        if (consumedBytes == WORD_SIZE) {
            return readByte.getInt();
        }
        throw new IOException("Did not read enough bytes!");
    }

    /**
     * Writes a single word to the given absolute position.
     * @param value The word, as an int.
     * @param position The absolute position to start writing.
     * @throws IOException
     */
    public void setSingleWordAtPosition(int value, long position) throws IOException{
        ByteBuffer writeByte = getLittleEndianByteBuffer(WORD_SIZE);
        writeByte.putInt(value);
        writeByte.position(0);
        int consumedBytes = binary.write(writeByte, position);
        if (consumedBytes != WORD_SIZE) {
            throw new IOException("Did not read enough bytes!");
        }
    }

    /**
     * Writes a single word to the relative position given.
     * @param value The word, as an int.
     * @param position The position, relative to the current position of the channel.
     * @throws IOException
     */
    public void setSingleWordAtRelativePosition(int value, int position) throws IOException{
        ByteBuffer writeByte = getLittleEndianByteBuffer(WORD_SIZE);
        writeByte.putInt(value);
        writeByte.position(0);
        int consumedBytes = binary.write(writeByte, binary.position() + position);
        if (consumedBytes != WORD_SIZE) {
            throw new IOException("Did not read enough bytes!");
        }
    }

    private static ByteBuffer getLittleEndianByteBuffer(int capacity) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        return byteBuffer;
    }

    /**
     * Inserts the entire contents of the given {@link FileChannel} into this file, starting at the given position. <br>
     * This will replace the contents of the wrapped file between {@code position} and {@code position + amountToOverwriteAndZero} with
     * the contents of {@code inputBytes}, followed by null bytes, if necessary. Everything after that will be preserved. This differs
     * from {@link #insertFileAtOffsetOverriding(long, FileChannel, long)} by always maintaining the file size.
     *
     * @param position The position to start overwriting the wrapped file.
     * @param inputBytes The contents to write into this file.
     * @param amountToOverwriteAndZero The amount of this file to replace.
     * @throws IOException
     */
    public void insertFileAtOffsetOverridingAndZeroing(long position, FileChannel inputBytes, long amountToOverwriteAndZero) throws IOException {
        // TODO Auto-generated method stub
        if (inputBytes.size() > amountToOverwriteAndZero) {
            throw new IOException("Input is too long for the given buffer size");
        }
        long originalPosition = binary.position();
        long originalInputPosition = inputBytes.position();
        binary.position(position);
        long toTransfer = writeFile(inputBytes);
        binary.position(position + toTransfer);
        zeroOldContents((int) (amountToOverwriteAndZero - toTransfer));
        binary.position(originalPosition);
        inputBytes.position(originalInputPosition);
    }

    private void zeroOldContents(int toZero) throws IOException {
        ByteBuffer zeroBuffer = getLittleEndianByteBuffer(toZero);
        binary.write(zeroBuffer);
    }


}
