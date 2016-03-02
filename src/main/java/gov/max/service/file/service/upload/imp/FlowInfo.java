package gov.max.service.file.service.upload.imp;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * by fanxu
 */
public class FlowInfo {

    private int flowChunkSize;
    private long flowTotalSize;
    private String flowIdentifier;
    private String flowFilename;
    private String flowRelativePath;
    private String flowFilePath;

    public FlowInfo(int flowChunkSize, long flowTotalSize, String flowIdentifier, String flowFilename, String flowRelativePath, String flowFilePath) {
        this.flowChunkSize = flowChunkSize;
        this.flowTotalSize = flowTotalSize;
        this.flowIdentifier = flowIdentifier;
        this.flowFilename = flowFilename;
        this.flowRelativePath = flowRelativePath;
        this.flowFilePath = flowFilePath;
        this.chunks = new ConcurrentSkipListSet<>((o1, o2) -> (int) (o1.start - o2.start));
        int count = (int) Math.ceil(((double) flowTotalSize) / ((double) flowChunkSize));
        this.chunkNumbers = new AtomicBitSet(count);
    }

    public int getFlowChunkSize() {
        return flowChunkSize;
    }

    public long getFlowTotalSize() {
        return flowTotalSize;
    }

    public static class FlowChunkNumber {

        public FlowChunkNumber(long start, long end) {
            this.start = start;
            this.end = end;
        }

        private long start;
        private long end;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof FlowChunkNumber ? ((FlowChunkNumber) obj).start == this.start : false;
        }

        @Override
        public int hashCode() {
            return (int) start;
        }
    }

    public String getFlowIdentifier() {
        return flowIdentifier;
    }

    public String getFlowFilePath() {
        return flowFilePath;
    }

    /**
     * Chunks uploaded.
     */
    private volatile NavigableSet<FlowChunkNumber> chunks;
    //	private volatile BitSet chunkNumbers = new BitSet();
    private volatile AtomicBitSet chunkNumbers;

    public void addChunk(int flowChunkNumber, long start, long end) {
        chunkNumbers.set(flowChunkNumber);
        chunks.add(new FlowChunkNumber(start, end));
    }

    public boolean containsChunk(int flowChunkNumber) {
        return chunkNumbers.get(flowChunkNumber);
    }

    public boolean isValid() {
        return (flowChunkSize > 0 && flowTotalSize > 0 && !(HttpUtils.isEmpty(flowIdentifier) || HttpUtils.isEmpty(flowFilename) || HttpUtils.isEmpty(flowRelativePath)));
    }

    public boolean checkIfUploadComplete() {
        Iterator<FlowChunkNumber> iterator = chunks.descendingIterator();
        if (iterator.hasNext()) {
            FlowChunkNumber last = iterator.next();
            if (last.end == flowTotalSize) {
                while (iterator.hasNext()) {
                    FlowChunkNumber chunk = iterator.next();
                    if (last.start != chunk.end) {
                        return false;
                    }
                    last = chunk;
                }
                if (last.start == 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
