package com.test.denis.uploader.network

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

typealias ProgressHandler = (Long, Long) -> Unit

class CountingRequestBody(private val delegate: RequestBody, private val handler: ProgressHandler?) : RequestBody() {

    var numWriteToCalls = 0

    override fun contentType(): MediaType? {
        return delegate.contentType()
    }

    override fun contentLength(): Long {
        return delegate.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        val countingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(countingSink)

        delegate.writeTo(bufferedSink)

        bufferedSink.flush()
    }

    internal inner class CountingSink(delegate: Sink) : ForwardingSink(delegate) {

        private var bytesWritten: Long = 0

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount

            if (numWriteToCalls > 0) handler?.invoke(bytesWritten, contentLength())

            numWriteToCalls++
        }
    }
}
