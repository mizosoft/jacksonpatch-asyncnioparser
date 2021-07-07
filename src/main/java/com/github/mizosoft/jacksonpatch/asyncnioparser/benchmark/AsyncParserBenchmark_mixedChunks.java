package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.async.ByteBufferFeeder;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** Benchmark feeding mixed chunks to parser, to see what skewing JVM branch prediction would do. */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class AsyncParserBenchmark_mixedChunks extends AbstractAsyncParserBenchmark {
  @Param({"/payload/jarray_small.json", "/payload/jobject_large.json"})
  public String payloadLocation;

  @Param({"NIO_WRAPPING", "TOGGLING"}) // These implement both ByteArrayFeeder/ByteBufferFeeder
  public JsonFactoryPatchType patch;

  private List<byte[]> arrayChunks;
  private List<ByteBuffer> bufferChunks;
  private PatchedJsonFactory factory;

  @Setup
  public void setup() {
    byte[] data = load(payloadLocation);
    List<byte[]> allChunks = ChunkTokenizer.tokenizeToArrays(ByteBuffer.wrap(data), CHUNK_SIZES);
    arrayChunks = new ArrayList<>();
    bufferChunks = new ArrayList<>();
    for (int i = 0, sz = allChunks.size(); i < sz; i++) {
      byte[] arrayChunk = allChunks.get(i);
      if (i % 2 == 0) {
        arrayChunks.add(arrayChunk);
      } else {
        bufferChunks.add(ByteBuffer.wrap(arrayChunk));
      }
    }
    factory = patch.createFactory();
  }

  @Benchmark
  public void readChunks(Blackhole blackhole) throws IOException {
    JsonParser parser = factory.createNonBlockingByteBufferParser();
    ByteArrayFeeder arrayFeeder = ((ByteArrayFeeder) parser.getNonBlockingInputFeeder());
    ByteBufferFeeder bufferFeeder = ((ByteBufferFeeder) parser.getNonBlockingInputFeeder());
    Iterator<byte[]> arrays = arrayChunks.iterator();
    Iterator<ByteBuffer> buffers = bufferChunks.iterator();
    for (int i = 0, sz = arrayChunks.size() + bufferChunks.size(); i < sz; i++) {
      if (i % 2 == 0) {
        byte[] arr = arrays.next();
        arrayFeeder.feedInput(arr, 0, arr.length);
      } else {
        bufferFeeder.feedInput(buffers.next());
      }
      consumeTokens(parser, blackhole);
    }
    arrayFeeder.endOfInput(); // enough to call from one feeder
    consumeTokens(parser, blackhole);
  }

  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder()
            .include(AsyncParserBenchmark_mixedChunks.class.getSimpleName())
            .shouldFailOnError(true)
            .build();
    new Runner(options).run();
  }
}
