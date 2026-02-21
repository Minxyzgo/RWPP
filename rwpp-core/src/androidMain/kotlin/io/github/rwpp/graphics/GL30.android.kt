/*
 * Copyright 2023-2025 RWPP contributors
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 * https://github.com/Minxyzgo/RWPP/blob/main/LICENSE
 */

package io.github.rwpp.graphics

import java.nio.Buffer
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.nio.LongBuffer

@Suppress(names = ["unused"])
actual object GL30 {
    actual fun glReadBuffer(mode: Int) {
    }

    actual fun glDrawRangeElements(
        mode: Int,
        start: Int,
        end: Int,
        count: Int,
        type: Int,
        indices: Buffer?
    ) {
    }

    actual fun glDrawRangeElements(
        mode: Int,
        start: Int,
        end: Int,
        count: Int,
        type: Int,
        offset: Int
    ) {
    }

    actual fun glTexImage3D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        depth: Int,
        border: Int,
        format: Int,
        type: Int,
        pixels: Buffer?
    ) {
    }

    actual fun glTexImage3D(
        target: Int,
        level: Int,
        internalformat: Int,
        width: Int,
        height: Int,
        depth: Int,
        border: Int,
        format: Int,
        type: Int,
        offset: Int
    ) {
    }

    actual fun glTexSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        width: Int,
        height: Int,
        depth: Int,
        format: Int,
        type: Int,
        pixels: Buffer?
    ) {
    }

    actual fun glTexSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        width: Int,
        height: Int,
        depth: Int,
        format: Int,
        type: Int,
        offset: Int
    ) {
    }

    actual fun glCopyTexSubImage3D(
        target: Int,
        level: Int,
        xoffset: Int,
        yoffset: Int,
        zoffset: Int,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
    }

    actual fun glGenQueries(n: Int, ids: IntBuffer?) {
    }

    actual fun glDeleteQueries(n: Int, ids: IntBuffer?) {
    }

    actual fun glIsQuery(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun glBeginQuery(target: Int, id: Int) {
    }

    actual fun glEndQuery(target: Int) {
    }

    actual fun glGetQueryiv(target: Int, pname: Int, params: IntBuffer?) {
    }

    actual fun glGetQueryObjectuiv(id: Int, pname: Int, params: IntBuffer?) {
    }

    actual fun glUnmapBuffer(target: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun glGetBufferPointerv(target: Int, pname: Int): Buffer? {
        TODO("Not yet implemented")
    }

    actual fun glDrawBuffers(n: Int, bufs: IntBuffer?) {
    }

    actual fun glUniformMatrix2x3fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glUniformMatrix3x2fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glUniformMatrix2x4fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glUniformMatrix4x2fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glUniformMatrix3x4fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glUniformMatrix4x3fv(
        location: Int,
        count: Int,
        transpose: Boolean,
        value: FloatBuffer?
    ) {
    }

    actual fun glBlitFramebuffer(
        srcX0: Int,
        srcY0: Int,
        srcX1: Int,
        srcY1: Int,
        dstX0: Int,
        dstY0: Int,
        dstX1: Int,
        dstY1: Int,
        mask: Int,
        filter: Int
    ) {
    }

    actual fun glRenderbufferStorageMultisample(
        target: Int,
        samples: Int,
        internalformat: Int,
        width: Int,
        height: Int
    ) {
    }

    actual fun glFramebufferTextureLayer(
        target: Int,
        attachment: Int,
        texture: Int,
        level: Int,
        layer: Int
    ) {
    }

    actual fun glFlushMappedBufferRange(
        target: Int,
        offset: Int,
        length: Int
    ) {
    }

    actual fun glBindVertexArray(array: Int) {
    }

    actual fun glDeleteVertexArrays(n: Int, arrays: IntBuffer?) {
    }

    actual fun glGenVertexArrays(n: Int, arrays: IntBuffer?) {
    }

    actual fun glIsVertexArray(array: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun glBeginTransformFeedback(primitiveMode: Int) {
    }

    actual fun glEndTransformFeedback() {
    }

    actual fun glBindBufferRange(
        target: Int,
        index: Int,
        buffer: Int,
        offset: Int,
        size: Int
    ) {
    }

    actual fun glBindBufferBase(target: Int, index: Int, buffer: Int) {
    }

    actual fun glTransformFeedbackVaryings(
        program: Int,
        varyings: Array<String?>?,
        bufferMode: Int
    ) {
    }

    actual fun glVertexAttribIPointer(
        index: Int,
        size: Int,
        type: Int,
        stride: Int,
        offset: Int
    ) {
    }

    actual fun glGetVertexAttribIiv(
        index: Int,
        pname: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glGetVertexAttribIuiv(
        index: Int,
        pname: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glVertexAttribI4i(
        index: Int,
        x: Int,
        y: Int,
        z: Int,
        w: Int
    ) {
    }

    actual fun glVertexAttribI4ui(
        index: Int,
        x: Int,
        y: Int,
        z: Int,
        w: Int
    ) {
    }

    actual fun glGetUniformuiv(
        program: Int,
        location: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glGetFragDataLocation(program: Int, name: String?): Int {
        TODO("Not yet implemented")
    }

    actual fun glUniform1uiv(location: Int, count: Int, value: IntBuffer?) {
    }

    actual fun glUniform3uiv(location: Int, count: Int, value: IntBuffer?) {
    }

    actual fun glUniform4uiv(location: Int, count: Int, value: IntBuffer?) {
    }

    actual fun glClearBufferiv(
        buffer: Int,
        drawbuffer: Int,
        value: IntBuffer?
    ) {
    }

    actual fun glClearBufferuiv(
        buffer: Int,
        drawbuffer: Int,
        value: IntBuffer?
    ) {
    }

    actual fun glClearBufferfv(
        buffer: Int,
        drawbuffer: Int,
        value: FloatBuffer?
    ) {
    }

    actual fun glClearBufferfi(
        buffer: Int,
        drawbuffer: Int,
        depth: Float,
        stencil: Int
    ) {
    }

    actual fun glGetStringi(name: Int, index: Int): String? {
        TODO("Not yet implemented")
    }

    actual fun glCopyBufferSubData(
        readTarget: Int,
        writeTarget: Int,
        readOffset: Int,
        writeOffset: Int,
        size: Int
    ) {
    }

    actual fun glGetUniformIndices(
        program: Int,
        uniformNames: Array<String?>?,
        uniformIndices: IntBuffer?
    ) {
    }

    actual fun glGetActiveUniformsiv(
        program: Int,
        uniformCount: Int,
        uniformIndices: IntBuffer?,
        pname: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glGetUniformBlockIndex(
        program: Int,
        uniformBlockName: String?
    ): Int {
        TODO("Not yet implemented")
    }

    actual fun glGetActiveUniformBlockiv(
        program: Int,
        uniformBlockIndex: Int,
        pname: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glGetActiveUniformBlockName(
        program: Int,
        uniformBlockIndex: Int,
        length: Buffer?,
        uniformBlockName: Buffer?
    ) {
    }

    actual fun glUniformBlockBinding(
        program: Int,
        uniformBlockIndex: Int,
        uniformBlockBinding: Int
    ) {
    }

    actual fun glDrawArraysInstanced(
        mode: Int,
        first: Int,
        count: Int,
        instanceCount: Int
    ) {
    }

    actual fun glDrawElementsInstanced(
        mode: Int,
        count: Int,
        type: Int,
        indicesOffset: Int,
        instanceCount: Int
    ) {
    }

    actual fun glGetInteger64v(pname: Int, params: LongBuffer?) {
    }

    actual fun glGetBufferParameteri64v(
        target: Int,
        pname: Int,
        params: LongBuffer?
    ) {
    }

    actual fun glGenSamplers(count: Int, samplers: IntBuffer?) {
    }

    actual fun glDeleteSamplers(count: Int, samplers: IntBuffer?) {
    }

    actual fun glIsSampler(sampler: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun glBindSampler(unit: Int, sampler: Int) {
    }

    actual fun glSamplerParameteri(sampler: Int, pname: Int, param: Int) {
    }

    actual fun glSamplerParameteriv(
        sampler: Int,
        pname: Int,
        param: IntBuffer?
    ) {
    }

    actual fun glSamplerParameterf(sampler: Int, pname: Int, param: Float) {
    }

    actual fun glSamplerParameterfv(
        sampler: Int,
        pname: Int,
        param: FloatBuffer?
    ) {
    }

    actual fun glGetSamplerParameteriv(
        sampler: Int,
        pname: Int,
        params: IntBuffer?
    ) {
    }

    actual fun glGetSamplerParameterfv(
        sampler: Int,
        pname: Int,
        params: FloatBuffer?
    ) {
    }

    actual fun glVertexAttribDivisor(index: Int, divisor: Int) {
    }

    actual fun glBindTransformFeedback(target: Int, id: Int) {
    }

    actual fun glDeleteTransformFeedbacks(n: Int, ids: IntBuffer?) {
    }

    actual fun glGenTransformFeedbacks(n: Int, ids: IntBuffer?) {
    }

    actual fun glIsTransformFeedback(id: Int): Boolean {
        TODO("Not yet implemented")
    }

    actual fun glPauseTransformFeedback() {
    }

    actual fun glResumeTransformFeedback() {
    }

    actual fun glProgramParameteri(program: Int, pname: Int, value: Int) {
    }

    actual fun glInvalidateFramebuffer(
        target: Int,
        numAttachments: Int,
        attachments: IntBuffer?
    ) {
    }

    actual fun glInvalidateSubFramebuffer(
        target: Int,
        numAttachments: Int,
        attachments: IntBuffer?,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
    }
}