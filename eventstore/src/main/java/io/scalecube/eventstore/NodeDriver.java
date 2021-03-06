package io.scalecube.eventstore;

import java.nio.ByteBuffer;
import java.util.function.Function;

public class NodeDriver {

  private final NodeDriverManager driverMan;
  private final ByteBuffer buffer;
  private final int index;

  public NodeDriver(NodeDriverManager driverMan, int index, ByteBuffer buffer) {
    this.driverMan = driverMan;
    this.index = index;
    this.buffer = buffer;
  }

  public long getDriverOffset() {
    return this.index * driverMan.getNodeDriverSizeInBytes();
  }

  public int getMaxKeyCount() {
    return driverMan.getMaxKeyCount();
  }

  public KeyComparator getComparator() {
    return driverMan.getComparator();
  }

  public int getNodeKeyCount(int pos) {
    return buffer.getShort(pos + driverMan.getKeySegmentLen());
  }

  public void setNodeKeyCount(int pos, int count) {
    buffer.putShort(pos + driverMan.getKeySegmentLen(), (short) count);
  }

  public boolean isNodeFull(int pos) {
    return getNodeKeyCount(pos) >= driverMan.getMaxKeyCount();
  }

  public void putKey(int pos, int index, long data) {
    buffer.putLong(pos + (index * 8), data);
  }

  public void putValue(int pos, int index, long data) {
    buffer.putLong(pos + driverMan.getMainSegmentLen() + (index * 8), data);
  }

  public long getKey(int pos, int index) {
    return buffer.getLong(pos + (index * 8));
  }

  public long getValue(int pos, int index) {
    return buffer.getLong(pos + driverMan.getMainSegmentLen() + (index * 8));
  }

  public boolean isNodeEmpty(int pos) {
    return getNodeKeyCount(pos) == 0;
  }

  public Function<byte[], Long> getKeyProvider() {
    return driverMan.getKeyProvider();
  }

  public Function<byte[], Long> getValueProvider() {
    return driverMan.getValueProvider();
  }

  public int binarySearch(int pos, int keyCount, byte[] key) {
    return ByteBufferUtil.binarySearch(buffer, pos, 0, keyCount, key, driverMan.getComparator());
  }

  /**
   * shift 8 byte to right side from given index.
   * @param pos starting offset of the byte buffer
   * @param index index of the 8 byte that we need to shift
   * @param count  number of 8 bytes that we shifting
   */
  public void moveIndex(int pos, int index, int count) {
    int dest = count;
    while (dest > index) {
      buffer.putLong(pos + (dest * 8), buffer.getLong(pos + ((dest - 1) * 8)));
      dest--;
    }

  }

  public void moveKeyIndex(int pos, int index, int count) {
    moveIndex(pos, index, count);
  }

  public void moveValueIndex(int pos, int index, int count) {
    moveIndex(pos + driverMan.getMainSegmentLen(), index, count);
  }

  private int getNodeType(int pos) {
    return buffer.get(pos + driverMan.getNodeTypeOffset());
  }

  /**
   * load node from the given position of the file.
   * @param pos starting position of the node
   * @return node , depend on the type , it can be LeafNode or IndexNode
   */
  public Node loadNode(long pos) {
    if (pos == 0) {
      return null;
    }

    NodeDriver driver = driverMan.getNodeDriver(pos);
    int realPos = (int) (pos - driver.getDriverOffset());
    int nodeType = driver.getNodeType(realPos);
    switch (nodeType) {
      case 1:
        return new IndexNode(this, realPos);
      case 2:
        return new LeafNode(this, realPos);
      default:
        break;
    }
    throw new NodeException("unknow node type " + nodeType + " for pos " + pos);
  }

}
