package org.plus.net;

import org.telegram.tgnet.AbstractSerializedData;
import org.telegram.tgnet.NativeByteBuffer;

public class SuperObject {

        public int networkType;

        public boolean disableFree = false;

        private static final ThreadLocal<NativeByteBuffer> sizeCalculator = new ThreadLocal<NativeByteBuffer>() {
            @Override
            protected NativeByteBuffer initialValue() {
                return new NativeByteBuffer(true);
            }
        };


        public SuperObject() {

        }

        public void readParams(AbstractSerializedData stream, boolean exception) {

        }

        public void serializeToStream(AbstractSerializedData stream) {

        }

        public SuperObject deserializeResponse(AbstractSerializedData stream, int constructor, boolean exception) {
            return null;
        }

        public void freeResources() {

        }

        public int getObjectSize() {
            NativeByteBuffer byteBuffer = sizeCalculator.get();
            byteBuffer.rewind();
            serializeToStream(sizeCalculator.get());
            return byteBuffer.length();
        }

}
