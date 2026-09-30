# 真机诊断交接：#3 / #4（2026-10-01，Asia/Shanghai）

## 活跃工作项

用户已授权修复既有 #3（清后台残留）与 #4（静音断连），测试组合为平板发送到手机。当前阶段完成 #4 现场诊断，尚未修改实现或完成修复验收。#3 尚未执行清后台复现。

后续状态：#3 已完成本轮现场清后台观察并恢复路由；启动发送继承系统静音也已记录。用户随后要求比较最新 GitHub 代码和设备版本并审查未知 bug，本轮只读审查已完成，完整差异、CI 结果与修复顺序见 [2026-10-01-version-review.md](2026-10-01-version-review.md)。最新远端开发提交为 2caa437，本地 checkout 未被覆盖；仍未实施产品代码修复。

源码 checkout：chore/project-governance-v1，2d72885；开工时工作区干净。已阅读仓库 AGENTS.md、PROJECT_STATE、全部 open Issues、PR #2、ARCHITECTURE、RELEASE、SIGNING、TESTING。手机安装包来源尚未与该提交精确匹配。

## 环境

- 平板：25053RP5CC / violin，Android 16，192.168.10.2，已连接无线 ADB 调试端口 46373。
- 手机：23049RAD8C / marble，Android 15，192.168.10.3，本次使用 ADB transport 3。
- 两端永久包 io.github.mu23xr.audiobridge，versionName 1.3.1-test.13，versionCode 2300013。
- 两端另装历史 lab 包；平板旧 lab 应用进程存活，尚无证据证明其引起本次断连。
- 用户明确表示未手动打开旧 lab。进一步核对：平板 ActivityManager 在 09-30 22:35:36.481 启动 pid 28933，原因为访问 rikka.shizuku.ShizukuProvider（for content provider，caller=null）。无法从该行确定具体发起者。当前旧包无运行服务，进程 empty=true / procState=19，未见旧包音频桥进程；不能把进程存在描述为用户正在使用旧版或旧版正在发送音频。
- ADB 连接端口/transport 会变化，每次采集前重新确认；配对码未保存。

## #4 直接证据与因果链

1. 01:20:42 平板 Shizuku sender 连接手机 192.168.10.3:47662，并持续发送；手机 WFAS metrics 最后记录 8616 包，丢包约 0.70%。
2. 01:21:41.862 手机 AudioTrackImpl 报 isLongTimeZeroData，持续全零数据约 60 秒。
3. 01:21:41.879 手机 PowerKeeper.Audio 报 pause zero data process，uid 10644、pid 16983、session 21201，指出该应用有前台服务仍被处理。
4. 01:21:41.881 AudioFlinger 报 pauseAudioTracks,pause AudioTrack success。
5. 音频系统快照显示该轨道 paused，4800-frame buffer 已满；手机 UDP :47662 Recv-Q=2099520。
6. 01:22:12.308 手机 SERVER_ACTIVITY_TIMEOUT：audioAgeMs=30279、pingAgeMs=30337。
7. 01:22:12.711 平板 client heartbeat timed out；平板服务保留，等待重连。

直接原因已确认：HyperOS 的连续零音频策略主动暂停接收端轨道。代码检查发现单播 socket 收包、PONG 回复和 AudioTrack.WRITE_BLOCKING 同处一个处理循环，暂停后的满缓冲可阻塞循环，继而阻断保活；取消 Job 无法可靠打断原生阻塞写入，因此恢复/重连也受影响。未获取线程栈，具体卡住的调用点仍属于由系统快照和源码支持的推断。

手机进程快照 isFrozen=false，有前台服务；不应归因为应用进程已被杀死。也不应归因为平板完全未发送或未获得 Shizuku 授权。

## 本地证据与采集

原始日志/快照在 Git 忽略的 out/device-debug/：
- phone-zero-audio-pause-evidence.log
- phone-session-history.log
- tablet-bridge-history.log
- phone-audio-flinger.txt / phone-audio.txt / phone-processes.txt
- 持续采集 tablet.log / phone.log（包含系统日志，勿直接上传完整文件）

debuggerd -j 被设备拒绝，提示 root is required；未尝试提权。

## 下一步

### 用户要求重新连接：已成功

本轮旧端口在沙盒账户 server 下失败；按用户重新连接的要求，通过正常 Windows 用户上下文重启电脑 ADB server，server-status 确认使用 w1741107948 的既有 adbkey / adb_known_hosts（未读取或复制密钥）。随后平板 192.168.10.2:46373、手机 192.168.10.3:37049 均 connected，devices 显示 device；shell getprop 分别返回 25053RP5CC、23049RAD8C。手机另有 mDNS 自动连接条目，属于同一台设备。此次重启是明确的恢复操作，不是已识别此前反复重启的发起者；后续避免在沙盒账户启动替代 server。未修改产品代码或设备设置。

### 无线 ADB 断开后自动重连（新增诊断）

用户报告手机和平板无线调试间歇断开、自动重连。本轮首次 adb devices 提示本机 daemon 未运行并启动 5037 server，随后只见模拟器；不将此提示当作此前所有断开的已确认原因。mdns services 同时发现平板 192.168.10.2:39877 / :46373、手机 192.168.10.3:37049 / :42355，但四个端点逐一 connect 均失败（其中 :39877 和 :42355 明确拒绝连接）。广播可能包含失效记录，尚无法确认设备端 adbd 重启、Wi-Fi 切换或电脑端 ADB server 更替的具体原因。电脑进程/网络 CIM 查询因访问拒绝未取得证据，设备未连接也无法读取当下系统日志。

ADB 使用 mDNS 发现已配对设备并自动连接，自动重连本身不代表 AudioBridge 重启。依据：https://developer.android.com/tools/adb 。已询问两台是否同时断开及是否与熄屏相关；下一步取得设备无线调试页面当前连接端口，对照故障时刻的 AdbDebuggingManager / adbd / Wi-Fi 日志。诊断期间未修改设备设置或应用代码。

用户补充两台同时断开。进一步读取电脑 adb.log：02:20:20、02:20:53、02:20:58、02:22:02、02:22:23、02:22:55、02:23:18、02:24:15、02:24:47 多次出现不同 PID 的 adb starting；02:20:54 明确 adb server killed by remote request。证据支持电脑 ADB 服务反复结束/启动能够解释两台同时掉线和重新发现，但尚未识别发起关闭请求的进程，也不能将所有重启均归为同一原因。当前只见一个 adb 37.0.1，未证实版本冲突；仓库脚本搜索未发现 kill-server 等配置。

本轮 server-status 显示密钥/known-hosts 属于 CodexSandboxOffline 账户；02:25:23 手机 :37049、02:25:34 平板 :46373 的 TLS 日志为 SSLV3_ALERT_CERTIFICATE_UNKNOWN。更正前述失败的解释：两个端口仍响应 TLS，失败属于当前沙盒账户密钥不获设备认可，不能据其推断网络不可达。其余两个端口明确拒绝连接。当前建议固定 Windows 用户账户及同一个持久 ADB server，排查外部工具/任务进程清理是否关闭 server；不通过反复配对掩盖 server 生命周期问题。未终止其他进程、调整网络或复制密钥。

### #3 用户清后台监控已就绪

用户要求先开始监控，收到就绪通知后再亲自清发送端平板后台。已保存操作前基线并启动实时生命周期 logcat 和每约 4–5 秒的进程、服务、dumpsys audio、media.audio_policy 快照，快照采集限时 10 分钟；数据目录 out/device-debug/recents-run1/。执行会话：快照 92522，logcat 91536。已确认至少 3 组快照落盘。当前尚未宣称用户已执行清后台。

基线：平板永久包普通进程 26693、shell 音频桥 14562 存活；STREAM_MUSIC 当前路由 remote_submix(8000)，speaker 音量索引 0/150，remote_submix 索引 120/150。因此应观察退出后是否切回 speaker 及其独立音量，不能把系统音量索引直接等同于应用软件增益。监控期间未停止服务、未强行结束应用、未调音量，避免干扰用户复现。

用户清后台后，先比对最后操作前后快照及 lifecycle.log 的 task removal / bridge destroy / synchronous unregister / policy route 事件；如 10 分钟窗口已过，重新开始采集并告知。完成此次复现后停止本次采集进程，保留证据。

### #3 本次复现结果与新发现：启动发送继承静音

用户报告清后台恢复正常。采集与之相符：01:44:11、01:45:15 出现 SwipeUpClean 杀死主进程，桥进程收到 owner death，并在约 0.7 秒后完成清理退出；快照 043 的媒体路由切回 speaker，speaker 音量仍为 0，virtual submix 索引仍为 130。说明本轮退出路径恢复路由，尚不能据单次会话关闭 #3。

用户随后报告每次新开应用点击发送无声，按一次音量键才恢复，并保留现场请求诊断。已保存 send-start-audio.txt、send-start-policy.txt、send-start-services.txt、send-start-bridge.log。

直接观察：STREAM_MUSIC Muted=true、Muted Internally=false、streamVolume=0，但 remote_submix 索引为 130/150，当前媒体路由为 remote_submix。01:45:20.995 在 speaker 调到 0 时，系统记录 STREAM_MUSIC muting by VGS.applyAllVolumes#1；随后启动发送没有解除此静音标记。快照 044 speaker/Muted=true → 047 remote_submix/Muted=true。

启动抓取日志显示三个 AudioPolicy createAudioRecordSink 均返回 null（已记录明确错误），实际采用 REMOTE_SUBMIX fallback。当前源码的 Shizuku 启动设置的是软件 PCM gain，未对系统媒体流解除静音。因此现有证据支持：speaker 零音量触发的系统媒体静音标记跨路由保留，导致 virtual submix 虽有非零设备音量仍无声。已请用户只按一次音量加并报告声音变化，以对比 mute 状态；尚未宣称该对照验证完成，未修改代码或设备音量。

随后的只读对照快照 send-start-volume-check.txt 已见 remote_submix 从 130 → 140，媒体 Muted 从 true → false，streamVolume 从 0 → 140，而 speaker 索引仍为 0。系统层解除静音对照已观察到；用户对听感恢复的回复尚待确认。修复应仅在发送路由已建立后同步 virtual route 音量/静音状态，并保留退出后的 speaker 音量，避免通过提高外放音量掩盖问题。

1. 修复接收端阻塞写入/取消路径，确保播放被暂停时收包和保活仍可运行，取消会释放实际 track/socket。处理静音数据，避免持续写入零 PCM 触发已确认的 OEM 行为；不得用伪造可听噪声掩盖。
2. 核对 test.13 来源，取得保持永久签名的测试构建，避免卸载清数据或替换签名。
3. 平板持续静音超过 2–3 分钟，再恢复媒体，验证不断连与自动恢复；Home/锁屏也要覆盖。
4. 独立复现 #3：平板发送时划掉 Recents，比较桥进程、音频路由及本地音量前后状态。
5. 当前 Issues 不关闭，全局 PROJECT_STATE 未改；只有现场诊断结论，尚无修复通过结论。
