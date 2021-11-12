
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils

import reactor.core.publisher.Flux

fun Flux<DataBuffer>.mergeDataBuffers() = DataBufferUtils.join(this)
    .map { buf ->
        ByteArray(buf.readableByteCount()).also {
            buf.read(it)
            DataBufferUtils.release(buf)
        }
    }
