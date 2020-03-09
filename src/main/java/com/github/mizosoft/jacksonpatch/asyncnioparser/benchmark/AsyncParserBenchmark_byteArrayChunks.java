package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectReadContext;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.json.JsonFactory;
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

/** Benchmark for feeding input via {@code byte[]} chunks. */
@SuppressWarnings("unused")
public class AsyncParserBenchmark_byteArrayChunks extends AbstractAsyncParserBenchmark {

  @Param({"/payload/jarray_small.json", "/payload/jobject_large.json"})
  public String payloadLocation;

  @Param
  public JsonFactoryPatchType patch;

  private List<byte[]> chunks;
  private JsonFactory factory;

  @Setup
  public void setup() {
    byte[] data = load(payloadLocation);
    chunks = ChunkTokenizer.tokenizeToArrays(ByteBuffer.wrap(data), CHUNK_SIZES);
    factory = patch.createFactory();
  }

  @Benchmark
  public void readChunks(Blackhole blackhole) throws IOException {
    JsonParser parser = factory.createNonBlockingByteArrayParser(ObjectReadContext.empty());
    ByteArrayFeeder feeder = ((ByteArrayFeeder) parser.getNonBlockingInputFeeder());
    for (byte[] chunk : chunks) {
      feeder.feedInput(chunk, 0, chunk.length);
      consumeTokens(parser, blackhole);
    }
    feeder.endOfInput();
    consumeTokens(parser, blackhole);
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder()
        .include(AsyncParserBenchmark_byteArrayChunks.class.getSimpleName())
        .shouldFailOnError(true)
        .build();
    new Runner(options).run();
  }
}
