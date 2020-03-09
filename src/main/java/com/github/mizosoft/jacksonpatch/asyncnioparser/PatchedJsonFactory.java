package com.github.mizosoft.jacksonpatch.asyncnioparser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectReadContext;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.json.JsonFactory;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;

public abstract class PatchedJsonFactory extends JsonFactory {

  PatchedJsonFactory() {}

  public abstract JsonParser createNonBlockingByteBufferParser(ObjectReadContext readCtxt);

  public static final class NioWrappingParserPatch extends PatchedJsonFactory {

    public NioWrappingParserPatch() {}

    @Override
    public JsonParser createNonBlockingByteArrayParser(ObjectReadContext readCtxt) {
      IOContext ioCtxt = _createNonBlockingContext(null);
      ByteQuadsCanonicalizer can = _byteSymbolCanonicalizer.makeChild(_factoryFeatures);
      return new NioWrappingNonBlockingJsonParser(readCtxt, ioCtxt,
          readCtxt.getStreamReadFeatures(_streamReadFeatures),
          readCtxt.getFormatReadFeatures(_formatReadFeatures),
          can);
    }

    @Override
    public JsonParser createNonBlockingByteBufferParser(ObjectReadContext readCtxt) {
      IOContext ioCtxt = _createNonBlockingContext(null);
      ByteQuadsCanonicalizer can = _byteSymbolCanonicalizer.makeChild(_factoryFeatures);
      return new NioWrappingNonBlockingJsonParser(readCtxt, ioCtxt,
          readCtxt.getStreamReadFeatures(_streamReadFeatures),
          readCtxt.getFormatReadFeatures(_formatReadFeatures),
          can);
    }
  }

  public static final class TogglingParserPatch extends PatchedJsonFactory {

    public TogglingParserPatch() {}

    @Override
    public JsonParser createNonBlockingByteArrayParser(ObjectReadContext readCtxt) {
      IOContext ioCtxt = _createNonBlockingContext(null);
      ByteQuadsCanonicalizer can = _byteSymbolCanonicalizer.makeChild(_factoryFeatures);
      return new TogglingNonBlockingJsonParser(readCtxt, ioCtxt,
          readCtxt.getStreamReadFeatures(_streamReadFeatures),
          readCtxt.getFormatReadFeatures(_formatReadFeatures),
          can);
    }

    @Override
    public JsonParser createNonBlockingByteBufferParser(ObjectReadContext readCtxt) {
      IOContext ioCtxt = _createNonBlockingContext(null);
      ByteQuadsCanonicalizer can = _byteSymbolCanonicalizer.makeChild(_factoryFeatures);
      return new TogglingNonBlockingJsonParser(readCtxt, ioCtxt,
          readCtxt.getStreamReadFeatures(_streamReadFeatures),
          readCtxt.getFormatReadFeatures(_formatReadFeatures),
          can);
    }
  }

  public static final class NonPatchedFactory extends PatchedJsonFactory {

    public NonPatchedFactory() {}

    @Override
    public JsonParser createNonBlockingByteBufferParser(ObjectReadContext readCtxt) {
      throw new UnsupportedOperationException();
    }
  }
}
