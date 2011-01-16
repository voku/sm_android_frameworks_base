/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "SoftwareRenderer"
#include <utils/Log.h>

#include "../include/SoftwareRenderer.h"

#include <binder/MemoryHeapBase.h>
#include <binder/MemoryHeapPmem.h>
#include <media/stagefright/MediaDebug.h>
#include <surfaceflinger/ISurface.h>

//#define FORCE_TO_SW_REDERING

namespace android {

SoftwareRenderer::SoftwareRenderer(
        OMX_COLOR_FORMATTYPE colorFormat,
        const sp<ISurface> &surface,
        size_t displayWidth, size_t displayHeight,
        size_t decodedWidth, size_t decodedHeight)
    : mColorFormat(colorFormat),
      mConverter(colorFormat, OMX_COLOR_Format16bitRGB565),
      mISurface(surface),
      mDisplayWidth(displayWidth),
      mDisplayHeight(displayHeight),
      mDecodedWidth(decodedWidth),
      mDecodedHeight(decodedHeight),
#ifdef FORCE_TO_SW_REDERING
      mFrameSize(mDecodedWidth * mDecodedHeight * 2),  // RGB565
#else
      mFrameSize((mDecodedWidth * mDecodedHeight * 3) / 2),  // YUV420
#endif
      mIndex(0) {
    mMemoryHeap = new MemoryHeapBase("/dev/pmem_stream", 2 * mFrameSize);
    if (mMemoryHeap->heapID() < 0) {
        LOGI("Creating physical memory heap failed, reverting to regular heap.");
        mMemoryHeap = new MemoryHeapBase(2 * mFrameSize);
    } else {
        sp<MemoryHeapPmem> pmemHeap = new MemoryHeapPmem(mMemoryHeap);
        pmemHeap->slap();
        mMemoryHeap = pmemHeap;
    }

    CHECK(mISurface.get() != NULL);
    CHECK(mDecodedWidth > 0);
    CHECK(mDecodedHeight > 0);
    CHECK(mMemoryHeap->heapID() >= 0);
#ifdef FORCE_TO_SW_REDERING
    CHECK(mConverter.isValid());

    ISurface::BufferHeap bufferHeap(
            mDisplayWidth, mDisplayHeight,
            mDecodedWidth, mDecodedHeight,
            PIXEL_FORMAT_RGB_565,
            mMemoryHeap);
#else
    ISurface::BufferHeap bufferHeap(
            mDisplayWidth, mDisplayHeight,
            mDecodedWidth, mDecodedHeight,
            HAL_PIXEL_FORMAT_YCbCr_420_P,
            mMemoryHeap);
#endif

    status_t err = mISurface->registerBuffers(bufferHeap);
    CHECK_EQ(err, OK);
}

SoftwareRenderer::~SoftwareRenderer() {
    mISurface->unregisterBuffers();
}

void SoftwareRenderer::render(
        const void *data, size_t size, void *platformPrivate) {
    size_t offset = mIndex * mFrameSize;
    void *dst = (uint8_t *)mMemoryHeap->getBase() + offset;

#ifdef FORCE_TO_SW_REDERING
    mConverter.convert(
            mDecodedWidth, mDecodedHeight,
            data, 0, dst, 2 * mDecodedWidth);
#else
    memcpy(dst, data, mFrameSize);
#endif

    mISurface->postBuffer(offset);
    mIndex = 1 - mIndex;
}

}  // namespace android
