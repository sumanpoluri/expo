/**
 * Copyright (c) 2015-present, Facebook, Inc.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

#import "ABI27_0_0RCTInputAccessoryViewManager.h"

#import "ABI27_0_0RCTInputAccessoryView.h"

@implementation ABI27_0_0RCTInputAccessoryViewManager

ABI27_0_0RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
  return NO;
}

- (UIView *)view
{
  return [[ABI27_0_0RCTInputAccessoryView alloc] initWithBridge:self.bridge];
}

ABI27_0_0RCT_REMAP_VIEW_PROPERTY(backgroundColor, content.inputAccessoryView.backgroundColor, UIColor)

@end
