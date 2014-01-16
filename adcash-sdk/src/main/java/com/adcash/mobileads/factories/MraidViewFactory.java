/*
 * Copyright (c) 2010-2013, Adcash Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.adcash.mobileads.factories;

import android.content.Context;
import com.adcash.mobileads.AdConfiguration;
import com.adcash.mobileads.MraidView;
import com.adcash.mobileads.MraidView.NativeCloseButtonStyle;

public class MraidViewFactory {
    protected static MraidViewFactory instance = new MraidViewFactory();

    @Deprecated // for testing
    public static void setInstance(MraidViewFactory factory) {
        instance = factory;
    }

    public static MraidView create(Context context, AdConfiguration adConfiguration) {
        return instance.internalCreate(context, adConfiguration);
    }

    public static MraidView create(
            Context context,
            AdConfiguration adConfiguration,
            MraidView.ExpansionStyle expansionStyle,
            NativeCloseButtonStyle buttonStyle,
            MraidView.PlacementType placementType) {
        return instance.internalCreate(context, adConfiguration, expansionStyle, buttonStyle, placementType);
    }

    protected MraidView internalCreate(Context context, AdConfiguration adConfiguration) {
        return new MraidView(context, adConfiguration);
    }

    protected MraidView internalCreate(
            Context context,
            AdConfiguration adConfiguration,
            MraidView.ExpansionStyle expansionStyle,
            NativeCloseButtonStyle buttonStyle,
            MraidView.PlacementType placementType) {
        return new MraidView(context, adConfiguration, expansionStyle, buttonStyle, placementType);
    }
}
