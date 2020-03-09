package com.github.mizosoft.jacksonpatch.asyncnioparser.benchmark;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory.NioWrappingParserPatch;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory.NonPatchedFactory;
import com.github.mizosoft.jacksonpatch.asyncnioparser.PatchedJsonFactory.TogglingParserPatch;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

// These annotations are inherited
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 1)
@Threads(1)
@Warmup(iterations = 4, time = 5)
@Measurement(iterations = 6, time = 5)
@OutputTimeUnit(TimeUnit.SECONDS)
@BenchmarkMode(Mode.Throughput)
public class AbstractAsyncParserBenchmark {

  // arbitrary sizes
  static final int[] CHUNK_SIZES = {512, 555, 11, 2048, 555, 12};

  static byte[] load(String payloadLocation) {
    InputStream in = AbstractAsyncParserBenchmark.class.getResourceAsStream(payloadLocation);
    if (in == null) {
      throw new AssertionError("couldn't find resource: " + payloadLocation);
    }
    try (InputStream _in = in) {
      ByteArrayOutputStream outBuff = new ByteArrayOutputStream();
      byte[] tempBuff = new byte[8 * 1024];
      int read;
      while ((read = _in.read(tempBuff)) != -1) {
        outBuff.write(tempBuff, 0, read);
      }
      return outBuff.toByteArray();
    } catch (IOException ioe) {
      throw new UncheckedIOException(ioe);
    }
  }

  // convenient wrapper over consumeCurrentToken(...)
  static void consumeTokens(JsonParser parser, Blackhole blackhole) throws IOException {
    JsonToken token;
    while ((token = parser.nextToken()) != null && token != JsonToken.NOT_AVAILABLE) {
      consumeCurrentToken(parser, blackhole);
    }
  }

  // This method consumes parser tokens to trigger actual parsing, tricking the JVM into thinking
  // we actually need parsed tokens via JMH's Blackhole, for preventing DCE. Modified from
  // TokenBuffer::copyCurrentEvent.
  private static void consumeCurrentToken(
      JsonParser parser, Blackhole blackhole) throws IOException {
    switch (parser.currentToken()) {
      case START_OBJECT:
        blackhole.consume("{");
        break;
      case END_OBJECT:
        blackhole.consume("}");
        break;
      case START_ARRAY:
        blackhole.consume("[");
        break;
      case END_ARRAY:
        blackhole.consume("]");
        break;
      case FIELD_NAME:
        blackhole.consume(parser.currentName());
        break;
      case VALUE_STRING:
        if (parser.hasTextCharacters()) {
          blackhole.consume(parser.getTextCharacters());
          blackhole.consume(parser.getTextOffset());
          blackhole.consume(parser.getTextLength());
        } else {
          blackhole.consume(parser.getText());
        }
        break;
      case VALUE_NUMBER_INT:
        switch (parser.getNumberType()) {
          case INT:
            blackhole.consume(parser.getIntValue());
            break;
          case BIG_INTEGER:
            blackhole.consume(parser.getBigIntegerValue());
            break;
          default:
            blackhole.consume(parser.getLongValue());
        }
        break;
      case VALUE_NUMBER_FLOAT:
        switch (parser.getNumberType()) {
          case BIG_DECIMAL:
            blackhole.consume(parser.getDecimalValue());
            break;
          case FLOAT:
            blackhole.consume(parser.getFloatValue());
            break;
          default:
            blackhole.consume(parser.getDoubleValue());
        }
        break;
      case VALUE_TRUE:
        blackhole.consume(true);
        break;
      case VALUE_FALSE:
        blackhole.consume(false);
        break;
      case VALUE_NULL:
        blackhole.consume(null);
        break;
      case VALUE_EMBEDDED_OBJECT:
        blackhole.consume(parser.getEmbeddedObject());
        break;
      default:
        throw new RuntimeException("Internal error: unexpected token: " + parser.currentToken());
    }
  }

  // Factory for each patch variant, including non-patched one
  @SuppressWarnings("unused")
  public enum JsonFactoryPatchType {
    NIO_WRAPPING {
      @Override
      PatchedJsonFactory createFactory() {
        return new NioWrappingParserPatch();
      }
    },
    TOGGLING {
      @Override
      PatchedJsonFactory createFactory() {
        return new TogglingParserPatch();
      }
    },
    NON_PATCHED {
      @Override
      PatchedJsonFactory createFactory() {
        return new NonPatchedFactory();
      }
    };

    abstract PatchedJsonFactory createFactory();
  }
}
