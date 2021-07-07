package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ChunkTokenizer {
  static List<ByteBuffer> tokenizeToBuffers(ByteBuffer input, int[] chunkSizes) {
    List<ByteBuffer> tokens = new ArrayList<>();
    for (int i = 0; input.hasRemaining(); i++) {
      ByteBuffer token = ByteBuffer.allocate(chunkSizes[i % chunkSizes.length]);
      copyRemaining(input, token);
      token.flip();
      tokens.add(token);
    }
    return tokens;
  }

  static List<byte[]> tokenizeToArrays(ByteBuffer input, int[] chunkSizes) {
    List<byte[]> tokens = new ArrayList<>();
    for (ByteBuffer buffer : tokenizeToBuffers(input, chunkSizes)) {
      byte[] arrayBuffer = new byte[buffer.remaining()];
      buffer.get(arrayBuffer);
      tokens.add(arrayBuffer);
    }
    return tokens;
  }

  private static void copyRemaining(ByteBuffer src, ByteBuffer dest) {
    int rem = Math.min(src.remaining(), dest.remaining());
    int prevLimit = src.limit();
    src.limit(src.position() + rem);
    dest.put(src);
    src.limit(prevLimit);
  }
}
