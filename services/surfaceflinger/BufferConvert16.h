/* ------------------------------------------------------------------
 * Copyright (C) 1998-2009 PacketVideo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 * -------------------------------------------------------------------
 */

/**
*   This class is for 16 bit color conversion. The APIs usage is the same as
*   ColorConvertBase. The output format for RGB is 5-6-5 bits.
*/
#include <stdlib.h>
#include <stdint.h>
#include <sys/types.h>
#include <utils/Log.h>

namespace android {

class BufferConvert16
{
    public:
        BufferConvert16();
        ~BufferConvert16();

        int32_t Init(int32_t width, int32_t height);
        uint8_t* GetRGBBuffer(uint8_t *srcBuf);
        bool GetInitState();

    private:
        

        uint32_t mCoefTbl32[516];
        uint8_t *mCoefTbl;

        int32_t cc16(uint8_t **src) const;

        int32_t mWidth, mHeight, mSize;
        bool mInitialized;
        uint8_t* mRGBBuffer;
        int mRGBBufferIndex;
};
};

