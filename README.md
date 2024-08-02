# TiktokLiveServer

## 功能概述

- 实现了将 `TikTokLiveClient` 中部分功能通过服务端接口调用的能力。
- 提供了基于jpa的数据存储实现。

## 1. 服务端接口实现

### 目标

将 `TikTokLiveClient` 的功能模块通过服务端接口的形式暴露，以便于在服务器端进行调用和管理。

### 接口列表

以下是一些示例接口，具体实现可能根据实际需求进行调整：

- `POST /api/connect`
    - 描述：连接到 TikTok 直播。
    - 请求参数：直播间ID。
    - 返回：连接状态。

- `DELETE /api/disconnect`
    - 描述：断开与 TikTok 直播的连接。
    - 返回：断开状态。

  