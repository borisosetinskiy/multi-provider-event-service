/*
 * QDS - Quick Data Signalling Library
 * Copyright (C) 2002-2016 Devexperts LLC
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 */
package com.ob;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Created by boris on 19.04.2016.
 */
public class UnsafeHolder {
	public static final Unsafe UNSAFE;

	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe)unsafeField.get(null);
		} catch (Exception e) {
			throw new ExceptionInInitializerError(e);
		}
	}
}
