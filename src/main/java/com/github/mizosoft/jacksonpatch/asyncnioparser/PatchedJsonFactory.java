package com.github.mizosoft.jacksonpatch.asyncnioparser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.async.NonBlockingJsonParser;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;

public abstract class PatchedJsonFactory extends JsonFactory {
  PatchedJsonFactory() {}

  abstract boolean supportsByteBufferFeeder();

  abstract JsonParser newParser(IOContext ctxt, ByteQuadsCanonicalizer can);

  @Override
  public JsonParser createNonBlockingByteArrayParser() {
    IOContext ctxt = super._createNonBlockingContext(null);
    ByteQuadsCanonicalizer can = super._byteSymbolCanonicalizer.makeChild(super._factoryFeatures);
    return newParser(ctxt, can);
  }

  public JsonParser createNonBlockingByteBufferParser() {
    if (!supportsByteBufferFeeder()) {
      throw new UnsupportedOperationException();
    }
    return createNonBlockingByteArrayParser();
  }

  public static final class NioWrappingParserPatch extends PatchedJsonFactory {
    public NioWrappingParserPatch() {}

    @Override
    boolean supportsByteBufferFeeder() {
      return false;
    }

    @Override
    JsonParser newParser(IOContext ctxt, ByteQuadsCanonicalizer can) {
      return new NioWrappingParser(ctxt, super._parserFeatures, can);
    }
  }

  public static final class TogglingParserPatch extends PatchedJsonFactory {
    public TogglingParserPatch() {}

    @Override
    boolean supportsByteBufferFeeder() {
      return false;
    }

    @Override
    JsonParser newParser(IOContext ctxt, ByteQuadsCanonicalizer can) {
      return new TogglingParser(ctxt, super._parserFeatures, can);
    }
  }

  public static final class ChunkedParserPatch extends PatchedJsonFactory {
    public ChunkedParserPatch() {}

    @Override
    boolean supportsByteBufferFeeder() {
      return false;
    }

    @Override
    JsonParser newParser(IOContext ctxt, ByteQuadsCanonicalizer can) {
      return new ChunkedParser(ctxt, super._parserFeatures, can);
    }
  }

  public static final class NonPatchedFactory extends PatchedJsonFactory {
    public NonPatchedFactory() {}

    @Override
    boolean supportsByteBufferFeeder() {
      return false;
    }

    @Override
    JsonParser newParser(IOContext ctxt, ByteQuadsCanonicalizer can) {
      // This is the non-patched version
      return new NonBlockingJsonParser(ctxt, super._parserFeatures, can);
    }
  }
}
