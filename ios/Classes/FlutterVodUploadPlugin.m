#import "FlutterVodUploadPlugin.h"
#import <VODUpload/VODUploadClient.h>

@implementation FlutterVodUploadPlugin {
    VODUploadClient* uploader;
}
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"flutter_vod_upload_plugin"
                                     binaryMessenger:[registrar messenger]];
    FlutterVodUploadPlugin* instance = [[FlutterVodUploadPlugin alloc] init];
    instance.channel = channel;
    [registrar addMethodCallDelegate:instance channel:channel];
}
- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    if ([@"setUploadAuthAndAddress" isEqualToString:call.method]) {
        //        [self setUploadAuthAndAddress:<#(UploadFileInfo *)#> uploadAuth:<#(NSString *)#> uploadAddress:<#(NSString *)#>]
    } else if ([@"start" isEqualToString:call.method]) {
        [self start];
    } else if ([@"stop" isEqualToString:call.method]){
        [self pause];
    } else if ([@"pause" isEqualToString:call.method]){
        [self stop];
    } else if ([@"resume" isEqualToString:call.method]){
        [self resume];
    } else if ([@"setListener" isEqualToString:call.method]){
        [self setListener:call.arguments[@"uploadAuth"] uploadAddress:call.arguments[@"uploadAddress"]];
    } else if ([@"addFile" isEqualToString:call.method]){
        NSString* filePath = call.arguments[@"filePath"];
        [self addFile:filePath vodInfo:createVodInfo(call.arguments[@"vodInfo"])];
    } else {
        result(FlutterMethodNotImplemented);
    }
    
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        uploader =[VODUploadClient new];
    }
    return self;
}


static VodInfo* createVodInfo(NSDictionary *dict) {
    VodInfo* vodInfo = [VodInfo new];
    
    vodInfo.title = dict[@"title"];
    if (dict[@"desc"] && dict[@"desc"] != [NSNull null]) {
        vodInfo.desc = dict[@"desc"];
    }
    if (dict[@"cateId"] && dict[@"cateId"] != [NSNull null]) {
        vodInfo.cateId = dict[@"cateId"];
    }
    if (dict[@"tags"] && dict[@"tags"] != [NSNull null]) {
        vodInfo.tags = dict[@"tags"];
    }
    if (dict[@"userData"] && dict[@"userData"] != [NSNull null]) {
        vodInfo.userData = dict[@"userData"];
    }
    if (dict[@"coverUrl"] && dict[@"coverUrl"] != [NSNull null]) {
        vodInfo.coverUrl = dict[@"coverUrl"];
    }
    if (dict[@"isProcess"] && dict[@"isProcess"] != [NSNull null]) {
        vodInfo.isProcess = dict[@"isProcess"];
    }
    
    if (dict[@"isShowWaterMark"] && dict[@"isShowWaterMark"] != [NSNull null]) {
        vodInfo.isShowWaterMark = dict[@"isShowWaterMark"];
    }
    if (dict[@"priority"] && dict[@"priority"] != [NSNull null]) {
        vodInfo.priority = dict[@"priority"];
    }
    
    return vodInfo;
}

static NSDictionary* vodInfoToMap(VodInfo* vodInfo) {
    NSMutableDictionary* dict = @{}.mutableCopy;
    
    if (vodInfo.title) {
        dict[@"title"] = vodInfo.title;
    }
    if (vodInfo.desc) {
        dict[@"desc"] = vodInfo.desc;
    }
    if (vodInfo.cateId) {
        dict[@"cateId"] = vodInfo.cateId;
    }
    if (vodInfo.tags) {
        dict[@"tags"] = vodInfo.tags;
    }
    if (vodInfo.userData) {
        dict[@"userData"] = vodInfo.userData;
    }
    if (vodInfo.coverUrl) {
        dict[@"coverUrl"] = vodInfo.coverUrl;
    }
    if (vodInfo.isProcess) {
        dict[@"isProcess"] = [NSNumber numberWithBool:vodInfo.isProcess] ;
    }
    
    if (vodInfo.isShowWaterMark) {
        dict[@"isShowWaterMark"] = [NSNumber numberWithBool:vodInfo.isShowWaterMark] ;
    }
    if (vodInfo.priority) {
        dict[@"priority"] = vodInfo.priority;
    }
    
    
    return dict;
    
}

static NSDictionary* uploadFileInfoToMap(UploadFileInfo* fileInfo) {
    NSMutableDictionary* dict = @{}.mutableCopy;
    if (fileInfo.filePath) {
        dict[@"filePath"] = fileInfo.filePath;
    }
    if (fileInfo.endpoint) {
        dict[@"endpoint"] = fileInfo.endpoint;
    }
    if (fileInfo.bucket) {
        dict[@"bucket"] = fileInfo.bucket;
    }
    if (fileInfo.object) {
        dict[@"object"] = fileInfo.object;
    }
    if (fileInfo.vodInfo) {
        dict[@"vodInfo"] = vodInfoToMap(fileInfo.vodInfo);
    }
    if (fileInfo.state) {
        switch (fileInfo.state) {
            case VODUploadFileStatusSuccess:
                dict[@"state"] = @"SUCCESS";
                break;
            case VODUploadFileStatusUploading:
                dict[@"state"] = @"Uploading";
                break;
            case VODUploadFileStatusCanceled:
                dict[@"state"] = @"Canceled";
                break;
            case VODUploadFileStatusFailure:
                dict[@"state"] = @"Failure";
            case VODUploadFileStatusPaused:
                dict[@"state"] = @"Paused";
                break;
            default:
                break;
        }
    }
    return dict;
    
}

// MARK: 上传地址和凭证方式

- (void)setListener:(NSString* )uploadAuth
      uploadAddress:(NSString* )uploadAddress {
    
    // weakself
    __weak typeof(self) weakSelf = self;
    // setup callback
    OnUploadFinishedListener onUploadSucceed = ^(UploadFileInfo* fileInfo, VodUploadResult* result){
        [weakSelf.channel invokeMethod:@"onUploadSucceed" arguments:@{
            @"info":uploadFileInfoToMap(fileInfo)
        }];
    };
    OnUploadFailedListener onUploadFailed = ^(UploadFileInfo* fileInfo, NSString *code, NSString* message){
        NSLog(@"upload failed callback code = %@, error message = %@", code, message);
        
        [weakSelf.channel invokeMethod:@"onUploadFailed" arguments:[NSDictionary dictionaryWithObjectsAndKeys:
                                                                    uploadFileInfoToMap(fileInfo),@"info",
                                                                    code,@"code",
                                                                    message, @"message",
                                                                    nil]];
        
    };
    OnUploadProgressListener onUploadProgress = ^(UploadFileInfo* fileInfo, long uploadedSize, long totalSize) {
        
        [weakSelf.channel invokeMethod:@"onUploadProgress" arguments:[NSDictionary dictionaryWithObjectsAndKeys:
                                                                      uploadFileInfoToMap(fileInfo),@"info",
                                                                      [NSNumber numberWithLong: uploadedSize],@"uploadedSize",
                                                                      [NSNumber numberWithLong: totalSize], @"totalSize",
                                                                      nil]];
    };
    OnUploadTokenExpiredListener onUploadTokenExpired = ^{
        NSLog(@"upload token expired callback.");
        [weakSelf.channel invokeMethod:@"onUploadTokenExpired" arguments:nil];
        // token过期，设置新的上传凭证，继续上传
        // [uploader resumeWithAuth:@""]
    };
    OnUploadRertyListener onUploadRetry = ^{
        [weakSelf.channel invokeMethod:@"onUploadRetry" arguments:nil];
    };
    OnUploadRertyResumeListener onUploadRetryResume = ^{
        [weakSelf.channel invokeMethod:@"onUploadRetryResume" arguments:nil];
    };
    OnUploadStartedListener onUploadStarted = ^(UploadFileInfo* fileInfo) {
        [weakSelf.channel invokeMethod:@"onUploadStarted" arguments:@{
            @"info":uploadFileInfoToMap(fileInfo)
        }];
        [self->uploader setUploadAuthAndAddress:fileInfo uploadAuth: uploadAuth uploadAddress:uploadAddress];
    };
    VODUploadListener *listener = [[VODUploadListener alloc] init];
    listener.finish = onUploadSucceed;
    listener.failure = onUploadFailed;
    listener.progress = onUploadProgress;
    listener.expire = onUploadTokenExpired;
    listener.retry = onUploadRetry;
    listener.retryResume = onUploadRetryResume;
    listener.started = onUploadStarted;
    // init with upload address and upload auth
    [uploader setListener:listener];
}

// MARK: 设置上传地址 和 上传凭证
- (BOOL)setUploadAuthAndAddress:(UploadFileInfo *)uploadFileInfo
                     uploadAuth:(NSString *)uploadAuth
                  uploadAddress:(NSString *)uploadAddress {
    return [uploader setUploadAuthAndAddress:uploadFileInfo uploadAuth:uploadAuth uploadAddress:uploadAddress];
}
// MARK: 添加文件到上传列表
- (BOOL)addFile:(NSString *)filePath
        vodInfo:(VodInfo *)vodInfo{
    return [uploader addFile:filePath vodInfo:vodInfo];
}
// MARK: 上传控制
- (BOOL) start {
    return [uploader start];
}
- (BOOL) stop {
    return [uploader stop];
}
- (BOOL) pause {
    return [uploader pause];
}
- (BOOL) resume {
    return [uploader resume];
}
@end
