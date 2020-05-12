// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugins.webviewflutter.Shared;
import android.webkit.CookieSyncManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


class FlutterCookieManager implements MethodCallHandler {
  private final MethodChannel methodChannel;

  FlutterCookieManager(BinaryMessenger messenger) {
    methodChannel = new MethodChannel(messenger, "plugins.flutter.io/cookie_manager");
    methodChannel.setMethodCallHandler(this);

  }

  @Override
  public void onMethodCall(MethodCall methodCall, Result result) {
    switch (methodCall.method) {
      case "clearCookies":
        clearCookies(result);
        break;
      case "setCookie": {
        String url = (String) methodCall.argument("url");
        String name = (String) methodCall.argument("name");
        String value = (String) methodCall.argument("value");
        String domain = (String) methodCall.argument("domain");
        String path = (String) methodCall.argument("path");
        String expiresDateString = (String) methodCall.argument("expiresDate");
        Long expiresDate = (expiresDateString != null ? new Long(expiresDateString) : null);
        Integer maxAge = (Integer) methodCall.argument("maxAge");
        Boolean isSecure = (Boolean) methodCall.argument("isSecure");
        setCookie(url, name, value, domain, path, expiresDate, maxAge, isSecure, result);
      }
      break;
      default:
        result.notImplemented();
    }
  }

  void dispose() {
    methodChannel.setMethodCallHandler(null);
  }

  private static void clearCookies(final Result result) {
    CookieManager cookieManager = CookieManager.getInstance();
    final boolean hasCookies = cookieManager.hasCookies();
    if (Build.VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      cookieManager.removeAllCookies(
          new ValueCallback<Boolean>() {
            @Override
            public void onReceiveValue(Boolean value) {
              result.success(hasCookies);
            }
          });
    } else {
      cookieManager.removeAllCookie();
      result.success(hasCookies);
    }
  }

  public static void setCookie(String url,
                               String name,
                               String value,
                               String domain,
                               String path,
                               Long expiresDate,
                               Integer maxAge,
                               Boolean isSecure,
                               final MethodChannel.Result result) {
    CookieManager cookieManager = CookieManager.getInstance();
    String cookieValue = name + "=" + value + "; Domain=" + domain + "; Path=" + path;

    if (expiresDate != null)
      cookieValue += "; Expires=" + getCookieExpirationDate(expiresDate);

    if (maxAge != null)
      cookieValue += "; Max-Age=" + maxAge.toString();

    if (isSecure != null && isSecure)
      cookieValue += "; Secure";

    cookieValue += ";";

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      cookieManager.setCookie(url, cookieValue, new ValueCallback<Boolean>() {
        @Override
        public void onReceiveValue(Boolean aBoolean) {
          result.success(true);
        }
      });
      cookieManager.flush();
    }
    else {
      CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(Shared.applicationContext);
      cookieSyncMngr.startSync();
      cookieManager.setCookie(url, cookieValue);
      result.success(true);
      cookieSyncMngr.stopSync();
      cookieSyncMngr.sync();
    }
  }

  public static String getCookieExpirationDate(Long timestamp) {
    final SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy hh:mm:ss z");
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(new Date(timestamp));
  }
}
