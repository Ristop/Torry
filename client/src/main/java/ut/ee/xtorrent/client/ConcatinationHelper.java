package ut.ee.xtorrent.client;

import java.lang.reflect.Array;

public class ConcatinationHelper {

    // taken from https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java
    public byte[]concatenate(byte[]a,byte[]b){
        int aLen=a.length;
        int bLen=b.length;

        byte[]c=(byte[]) Array.newInstance(a.getClass().getComponentType(),aLen+bLen);
        System.arraycopy(a,0,c,0,aLen);
        System.arraycopy(b,0,c,aLen,bLen);

        return c;
    }
}
