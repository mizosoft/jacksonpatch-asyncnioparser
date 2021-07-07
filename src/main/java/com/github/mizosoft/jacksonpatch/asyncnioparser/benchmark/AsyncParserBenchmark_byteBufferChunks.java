package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.async.ByteBufferFeeder;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** Benchmark for feeding input via {@code ByteBuffer} chunks. */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class AsyncParserBenchmark_byteBufferChunks extends AbstractAsyncParserBenchmark {
  @Param({"/payload/jarray_small.json", "/payload/jobject_large.json"})
  public String payloadLocation;

  @Param({"NIO_WRAPPING", "TOGGLING"})
  public JsonFactoryPatchType patch;

  private List<ByteBuffer> chunks;
  private PatchedJsonFactory factory;

  @Setup
  public void setup() {
    byte[] data = load(payloadLocation);
    chunks = ChunkTokenizer.tokenizeToBuffers(ByteBuffer.wrap(data), CHUNK_SIZES);
    factory = patch.createFactory();
  }

  @Benchmark
  public void readChunks(Blackhole blackhole) throws IOException {
    JsonParser parser = factory.createNonBlockingByteBufferParser();
    ByteBufferFeeder feeder = (ByteBufferFeeder) parser.getNonBlockingInputFeeder();
    for (ByteBuffer chunk : chunks) {
      feeder.feedInput(chunk);
      consumeTokens(parser, blackhole);
    }
    feeder.endOfInput();
    consumeTokens(parser, blackhole);
  }

  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder()
            .include(AsyncParserBenchmark_byteBufferChunks.class.getSimpleName())
            .shouldFailOnError(true)
            .build();
    new Runner(options).run();
  }
}
