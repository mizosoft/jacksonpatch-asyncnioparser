Note: results aren't updated

# Overview

Looking for possible https://github.com/FasterXML/jackson-core/issues/478 patches and regressions regarding Jackson's `NonBlockingJsonParser`.

# Run

```
mvn install
cd target
java -jar benchmark.jar
```

# Results

### Feeding byte[] chunks

```
Benchmark                                             (patch)            (payloadLocation)   Mode  Cnt      Score     Error  Units
AsyncParserBenchmark_byteArrayChunks.readChunks   NON_PATCHED   /payload/jarray_small.json  thrpt   10  34928.616 ±  58.493  ops/s
AsyncParserBenchmark_byteArrayChunks.readChunks   NON_PATCHED  /payload/jobject_large.json  thrpt   10   1020.022 ±   0.561  ops/s
AsyncParserBenchmark_byteArrayChunks.readChunks      TOGGLING   /payload/jarray_small.json  thrpt   10  35129.755 ± 282.432  ops/s
AsyncParserBenchmark_byteArrayChunks.readChunks      TOGGLING  /payload/jobject_large.json  thrpt   10   1002.220 ±   9.724  ops/s
AsyncParserBenchmark_byteArrayChunks.readChunks  NIO_WRAPPING   /payload/jarray_small.json  thrpt   10  28750.253 ± 291.629  ops/s
AsyncParserBenchmark_byteArrayChunks.readChunks  NIO_WRAPPING  /payload/jobject_large.json  thrpt   10    826.597 ±   5.906  ops/s
```

### Feeding `ByteBuffer` chunks

```
Benchmark                                              (patch)            (payloadLocation)   Mode  Cnt      Score     Error  Units
AsyncParserBenchmark_byteBufferChunks.readChunks  NIO_WRAPPING   /payload/jarray_small.json  thrpt   10  30354.540 ±  31.549  ops/s
AsyncParserBenchmark_byteBufferChunks.readChunks  NIO_WRAPPING  /payload/jobject_large.json  thrpt   10    794.247 ±   2.005  ops/s
AsyncParserBenchmark_byteBufferChunks.readChunks      TOGGLING   /payload/jarray_small.json  thrpt   10  30000.832 ± 259.581  ops/s
AsyncParserBenchmark_byteBufferChunks.readChunks      TOGGLING  /payload/jobject_large.json  thrpt   10    813.049 ±   5.646  ops/s
```

### Feeding mixed `byte[]` and `ByteBuffer` chunks

```
Benchmark                                         (patch)            (payloadLocation)   Mode  Cnt      Score     Error  Units
AsyncParserBenchmark_mixedChunks.readChunks  NIO_WRAPPING   /payload/jarray_small.json  thrpt   10  30863.264 ±  24.033  ops/s
AsyncParserBenchmark_mixedChunks.readChunks  NIO_WRAPPING  /payload/jobject_large.json  thrpt   10    813.162 ±   0.531  ops/s
AsyncParserBenchmark_mixedChunks.readChunks      TOGGLING   /payload/jarray_small.json  thrpt   10  27712.643 ± 244.589  ops/s
AsyncParserBenchmark_mixedChunks.readChunks      TOGGLING  /payload/jobject_large.json  thrpt   10    772.887 ±  17.037  ops/s
```

### Single-byte `byte[]/ByteBuffer` accessor

```
Benchmark                                 Mode  Cnt     Score    Error   Units
BufferAccessorBenchmark.byteArray        thrpt    5  1673.493 ± 10.693  ops/us
BufferAccessorBenchmark.directNioBuffer  thrpt    5  1208.890 ±  5.003  ops/us
BufferAccessorBenchmark.nioBuffer        thrpt    5  1207.956 ±  7.147  ops/us
```