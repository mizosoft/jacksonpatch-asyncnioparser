package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/** General benchmark for per-byte access options (array, heap buffer, direct buffer). */
@Fork(value = 1)
@Threads(Threads.MAX)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 5, time = 5)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@BenchmarkMode(Mode.Throughput)
@SuppressWarnings("unused")
public class BufferAccessorBenchmark {
  private static final int BATCH_SIZE = 4 * 1024;

  @Benchmark
  @OperationsPerInvocation(BATCH_SIZE)
  public void nioBuffer(ByteBufferHandle handle, Blackhole blackhole) {
    for (int i = 0; i < BATCH_SIZE; i++) {
      blackhole.consume(handle.buffer.get(i));
    }
  }

  @Benchmark
  @OperationsPerInvocation(BATCH_SIZE)
  public void directNioBuffer(ByteBufferHandle handle, Blackhole blackhole) {
    for (int i = 0; i < BATCH_SIZE; i++) {
      blackhole.consume(handle.buffer.get(i));
    }
  }

  @Benchmark
  @OperationsPerInvocation(BATCH_SIZE)
  public void byteArray(ByteArrayHandle handle, Blackhole blackhole) {
    for (int i = 0; i < BATCH_SIZE; i++) {
      blackhole.consume(handle.array[i]);
    }
  }

  @State(Scope.Thread)
  public static class ByteArrayHandle {
    byte[] array;

    @Setup
    public void setup() {
      array = new byte[BATCH_SIZE];
    }
  }

  @State(Scope.Thread)
  public static class ByteBufferHandle {
    ByteBuffer buffer;

    @Setup
    public void setup() {
      buffer = ByteBuffer.allocate(BATCH_SIZE);
    }
  }

  @State(Scope.Thread)
  public static class DirectByteBufferHandle {
    ByteBuffer buffer;

    @Setup
    public void setup() {
      buffer = ByteBuffer.allocateDirect(BATCH_SIZE);
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options options =
        new OptionsBuilder()
            .include(BufferAccessorBenchmark.class.getSimpleName())
            .shouldFailOnError(true)
            .build();
    new Runner(options).run();
  }
}
