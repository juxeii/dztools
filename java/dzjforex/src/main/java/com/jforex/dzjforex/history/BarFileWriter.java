package com.jforex.dzjforex.history;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dukascopy.api.IBar;
import com.jforex.dzjforex.ZorroLogger;
import com.jforex.dzjforex.datetime.DateTimeUtils;

public class BarFileWriter {
    private final List<IBar> bars;
    private ByteBuffer byteBuffer;
    private FileOutputStream outStream;
    private final String fileName;

    private final static Logger logger = LogManager.getLogger(BarFileWriter.class);

    public BarFileWriter(final String fileName,
                         final List<IBar> bars) {
        this.fileName = fileName;
        this.bars = bars;
    }

    public boolean isWriteBarsToTICKsFileOK() {
        initByteBuffer();
        writeBarsToBuffer();
        return isWriteBufferToFileOK();
    }

    private void initByteBuffer() {
        byteBuffer = ByteBuffer.allocate(24 * bars.size());
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    private void writeBarsToBuffer() {
        for (final IBar bar : bars) {
            byteBuffer.putFloat((float) bar.getOpen());
            byteBuffer.putFloat((float) bar.getClose());
            byteBuffer.putFloat((float) bar.getHigh());
            byteBuffer.putFloat((float) bar.getLow());
            byteBuffer.putDouble(DateTimeUtils.getUTCTimeFromBar(bar));
        }
    }

    private boolean isWriteBufferToFileOK() {
        try {
            outStream = new FileOutputStream(fileName);
            outStream.write(byteBuffer.array(), 0, byteBuffer.limit());
            outStream.close();
            return true;
        } catch (final FileNotFoundException e) {
            logger.error("FileNotFoundException: " + e.getMessage());
            ZorroLogger.indicateError();

        } catch (final IOException e) {
            logger.error("IOException while writing TICKs file! " + e.getMessage());
            ZorroLogger.indicateError();
        }
        return false;
    }
}
