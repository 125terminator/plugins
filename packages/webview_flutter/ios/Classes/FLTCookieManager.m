// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "FLTCookieManager.h"

@implementation FLTCookieManager {
}

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar> *)registrar {
  FLTCookieManager *instance = [[FLTCookieManager alloc] init];

  FlutterMethodChannel *channel =
      [FlutterMethodChannel methodChannelWithName:@"plugins.flutter.io/cookie_manager"
                                  binaryMessenger:[registrar messenger]];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall *)call result:(FlutterResult)result {
  if ([[call method] isEqualToString:@"clearCookies"]) {
    [self clearCookies:result];
  }
  else if (([[call method] isEqualToString:@"setCookie"])){
    let url = arguments!["url"] as! String
    let name = arguments!["name"] as! String
    let value = arguments!["value"] as! String
    let domain = arguments!["domain"] as! String
    let path = arguments!["path"] as! String
    let expiresDate = arguments!["expiresDate"] as? Int
    let maxAge = arguments!["maxAge"] as? Int
    let isSecure = arguments!["isSecure"] as? Bool

    MyCookieManager.setCookie(url: url, name: name, value: value, domain: domain, path: path, expiresDate: expiresDate, maxAge: maxAge, isSecure: isSecure, result: result)
  }
  else if(([[call method] isEqualToString:@"getCookies"]))
  {
          let url = arguments!["url"] as! String
          MyCookieManager.getCookies(url: url, result: result)
  }
  else {
  }
    result(FlutterMethodNotImplemented);
  }
}

- (void)clearCookies:(FlutterResult)result {
  if (@available(iOS 9.0, *)) {
    NSSet<NSString *> *websiteDataTypes = [NSSet setWithObject:WKWebsiteDataTypeCookies];
    WKWebsiteDataStore *dataStore = [WKWebsiteDataStore defaultDataStore];

    void (^deleteAndNotify)(NSArray<WKWebsiteDataRecord *> *) =
        ^(NSArray<WKWebsiteDataRecord *> *cookies) {
          BOOL hasCookies = cookies.count > 0;
          [dataStore removeDataOfTypes:websiteDataTypes
                        forDataRecords:cookies
                     completionHandler:^{
                       result(@(hasCookies));
                     }];
        };

    [dataStore fetchDataRecordsOfTypes:websiteDataTypes completionHandler:deleteAndNotify];
  } else {
    // support for iOS8 tracked in https://github.com/flutter/flutter/issues/27624.
    NSLog(@"Clearing cookies is not supported for Flutter WebViews prior to iOS 9.");
  }
}
- (void)setCookie:(FlutterResult)result
j2: (NSString *) url
j3: (NSString *) name
j4: (NSString *) value
j5: (NSString *) domain
j6: (NSString *) path
j7: (NSInteger) expiresDate
j8: (NSInteger) maxAge
j9: (BOOL) isSecure
{
    if (@available(iOS 11.0, *)) {

        let cookie = HTTPCookie(properties: [
            .domain: domain,
            .path: "/",
            .name: name,
            .value: value,
            .secure: "TRUE",
            .expires: NSDate(timeIntervalSince1970: (double)expiresDate)
        ])!
        webView.configuration.websiteDataStore.httpCookieStore.setCookie(cookie)

    } else {
        NSLog(@"Clearing cookies is not supported for Flutter WebViews prior to iOS 9.");
      }
}
@end