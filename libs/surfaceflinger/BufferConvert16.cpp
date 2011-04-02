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
/** Class BufferConvert16, convert YUV420P to RGB16 in 5-6-5 format. */

#include "BufferConvert16.h"

namespace android {

BufferConvert16::BufferConvert16()
{
    mCoefTbl = (uint8_t*)mCoefTbl32;
    mInitialized = false;
    mRGBBuffer = 0;
    mRGBBufferIndex = 0;
}

BufferConvert16::~BufferConvert16()
{
    if (mRGBBuffer != 0)
        free(mRGBBuffer);

}

int32_t BufferConvert16::Init(int32_t width, int32_t height)
{
    uint8_t *clip;
    mWidth = width;
    mHeight = height;
    mSize = width * height;

    *((uint32_t*)mCoefTbl) = (int)(65536 * 0.4681); 
    *((uint32_t*)(mCoefTbl + 4)) = (int)(65536 * 1.5748);
    *((uint32_t*)(mCoefTbl + 8)) = (int)(65536 * 0.1873);
    *((uint32_t*)(mCoefTbl + 12)) = (int)(65536 * 1.8556);

    clip = mCoefTbl + 400;

    /* do 5 bit conversion */
    memset(&clip[-384], 0, 385*sizeof(*clip));
    memset(&clip[ 640], 0, 385*sizeof(*clip));

    for (int i = 1; i < 255; i++)   
    {
        clip[i] = i >> 3;
        clip[i+1024] = i >> 2;
    }
    memset(&clip[255], 31, 385*sizeof(*clip));
    memset(&clip[1279], 63, 385*sizeof(*clip));



    if (mRGBBuffer != 0) free(mRGBBuffer);
    mRGBBuffer = (uint8_t *)malloc(width * height * 6);

    mInitialized = true;
    return 1;
}

bool BufferConvert16::GetInitState()
{
    return mInitialized;
}

uint8_t* BufferConvert16::GetRGBBuffer(uint8_t *yuvBuf)
{
    uint8_t *TmpYuvBuf[3];

    TmpYuvBuf[0]    =   yuvBuf;
    TmpYuvBuf[1]    =   yuvBuf + (mSize);
    TmpYuvBuf[2]    =   TmpYuvBuf[1] + (mSize / 4);

    if (++mRGBBufferIndex == 3) mRGBBufferIndex = 0;    
    cc16(TmpYuvBuf);
   
    return mRGBBuffer + (mRGBBufferIndex * mSize * 2);
}


int32_t BufferConvert16::cc16(uint8_t **src) const
{

    uint8_t   *pCb, *pCr;
    uint16_t  *pY;
    uint16_t  *pDst;
    int32_t   Y, Cb, Cr, Cg;
    int32_t   row, col;
    int32_t   tmp0, tmp1, tmp2;
    uint32_t  rgb;
    uint8_t   *clip = mCoefTbl + 400;
    int32_t   cc1 = (*((int32_t*)(clip - 400)));
    int32_t   cc3 = (*((int32_t*)(clip - 396)));
    int32_t   cc2 = (*((int32_t*)(clip - 392)));
    int32_t   cc4 = (*((int32_t*)(clip - 388)));

    pY         = (uint16_t *) src[0];
    pCb        = src[1];
    pCr        = src[2];

    pDst       = (uint16_t *)(mRGBBuffer + (mRGBBufferIndex * mSize * 2));

    for (row = mHeight; row > 0; row -= 2)
    {

        for (col = mWidth - 1; col >= 0; col -= 2)
        {

            Cb = *pCb++;
            Cr = *pCr++;
            Y = pY[mWidth >> 1];

            Cb -= 128;
            Cr -= 128;
            Cg  =   Cr * cc1;
            Cr  *= cc3;

            Cg  +=  Cb * cc2;
            Cb  *=  cc4;

            tmp0    = (Y & 0xFF) + 2;  
            //tmp0    += 2;

            tmp1    =   tmp0 - (Cg >> 16);
            tmp2    =   tmp0 + (Cb >> 16);
            tmp0    =   tmp0 + (Cr >> 16);

            tmp0    =   clip[tmp0];
            tmp1    =   clip[tmp1 + 1023];
            tmp2    =   clip[tmp2];
            //RGB_565

            rgb     =   tmp1 | (tmp0 << 6);
            rgb     =   tmp2 | (rgb << 5);

            Y   = ((Y >> 8) & 0xFF) + 6;

            //Y   += 6;
            tmp1    = (Y) - (Cg >> 16);
            tmp2    = (Y) + (Cb >> 16);
            tmp0    = (Y) + (Cr >> 16);

            tmp0    =   clip[tmp0];
            tmp1    =   clip[tmp1 + 1021];
            tmp2    =   clip[tmp2];

            //RGB_565

            tmp0    =   tmp1 | (tmp0 << 6);
            tmp0    =   tmp2 | (tmp0 << 5);

            rgb     |= (tmp0 << 16);

            *((uint32_t*)(pDst + mWidth))  = rgb;

            //load the top two pixels
            Y = *pY++;

            tmp0    = (Y & 0xFF) + 6;   //Low endian    left pixel
            //tmp0    += 6;

            tmp1    =   tmp0 - (Cg >> 16);
            tmp2    =   tmp0 + (Cb >> 16);
            tmp0    =   tmp0 + (Cr >> 16);

            tmp0    =   clip[tmp0];
            tmp1    =   clip[tmp1 + 1021];
            tmp2    =   clip[tmp2];

            //RGB_565
            rgb     =   tmp1 | (tmp0 << 6);
            rgb     =   tmp2 | (rgb << 5);

            Y   = ((Y >> 8) & 0xFF) + 2;

            //Y   += 2;
            tmp1    = (Y) - (Cg >> 16);
            tmp2    = (Y) + (Cb >> 16);
            tmp0    = (Y) + (Cr >> 16);

            tmp0    =   clip[tmp0];
            tmp1    =   clip[tmp1 + 1023];
            tmp2    =   clip[tmp2];

            //RGB_565
            tmp0    =   tmp1 | (tmp0 << 6);
            tmp0    =   tmp2 | (tmp0 << 5);

            rgb     |= (tmp0 << 16);
            *((uint32_t *)pDst)   = rgb;
            pDst += 2;

        }//end of COL

        pY  += (mWidth >> 1);
        pDst += (mWidth); 
    }
    return 1;
};
};

