//
//  ABI28_0_0AIRUrlTileOverlay.m
//  AirMaps
//
//  Created by cascadian on 3/19/16.
//  Copyright © 2016. All rights reserved.
//

#import "ABI28_0_0AIRMapUrlTile.h"
#import <ReactABI28_0_0/UIView+ReactABI28_0_0.h>

@implementation ABI28_0_0AIRMapUrlTile {
    BOOL _urlTemplateSet;
}


- (void)setUrlTemplate:(NSString *)urlTemplate{
    _urlTemplate = urlTemplate;
    _urlTemplateSet = YES;
    [self createTileOverlayAndRendererIfPossible];
    [self update];
}

- (void) createTileOverlayAndRendererIfPossible
{
    if (!_urlTemplateSet) return;
    self.tileOverlay = [[MKTileOverlay alloc] initWithURLTemplate:self.urlTemplate];
    self.tileOverlay.canReplaceMapContent = YES;
    if (self.maximumZ) {
        self.tileOverlay.maximumZ = self.maximumZ;
    }
    self.renderer = [[MKTileOverlayRenderer alloc] initWithTileOverlay:self.tileOverlay];
}

- (void) update
{
    if (!_renderer) return;
    
    if (_map == nil) return;
    [_map removeOverlay:self];
    [_map addOverlay:self level:MKOverlayLevelAboveLabels];
    for (id<MKOverlay> overlay in _map.overlays) {
        if ([overlay isKindOfClass:[ABI28_0_0AIRMapUrlTile class]]) {
            continue;
        }
        [_map removeOverlay:overlay];
        [_map addOverlay:overlay];
    }
}

#pragma mark MKOverlay implementation

- (CLLocationCoordinate2D) coordinate
{
    return self.tileOverlay.coordinate;
}

- (MKMapRect) boundingMapRect
{
    return self.tileOverlay.boundingMapRect;
}

- (BOOL)canReplaceMapContent
{
    return self.tileOverlay.canReplaceMapContent;
}

@end
