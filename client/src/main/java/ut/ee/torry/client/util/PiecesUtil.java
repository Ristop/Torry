package ut.ee.torry.client.util;

public class PiecesUtil {

    /**
     * @param currentPosition current byte location inside a file (filePointer)
     * @param pieceSize the maximum size of piece
     * @param fileLength the length of file in number of bytes
     * @return integer of how many bytes should be used inside piece.
     */
    public static int calcBytesCount(long currentPosition, long pieceSize, long fileLength) {
        if (currentPosition + pieceSize > fileLength) {
            return (int) (fileLength - currentPosition);
        } else {
            return (int) pieceSize;
        }
    }

    /**
     * @param currentPosition current byte location inside a file (filePointer)
     * @param bytesAlreadyRead number of bytes already read in other files
     * @param pieceSize the maximum size of piece
     * @param fileLength the length of file in number of bytes
     * @return integer of how many bytes should be used inside piece.
     */
    public static int calcBytesCount(long currentPosition, long bytesAlreadyRead, long pieceSize, long fileLength) {
        long bytesToRead = fileLength - currentPosition;
        if (bytesToRead + bytesAlreadyRead > pieceSize){  //can fill the whole piece (can't use all new bytes)
            return (int) (pieceSize - bytesAlreadyRead);
        } else { // can use all new bytes
            return (int) bytesToRead;
        }
    }
}
