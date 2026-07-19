package com.guardianbaby.config;

import com.guardianbaby.entity.*;
import com.guardianbaby.entity.ApprovalRequest.ApprovalStatus;
import com.guardianbaby.entity.ApprovalRequest.ApprovalType;
import com.guardianbaby.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;
    private final TimeSettingsRepository timeSettingsRepository;
    private final BlockItemRepository blockItemRepository;
    private final UsageRecordRepository usageRecordRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final GuardianBindingRepository guardianBindingRepository;
    private final AlertRepository alertRepository;
    private final ContentFilterRuleRepository contentFilterRuleRepository;
    private final BiometricRecordRepository biometricRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("已存在用户数据，跳过初始化");
            return;
        }

        log.info("正在初始化演示数据...");

        // ── 创建用户 ──
        User guardian = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .phone("13800138000")
                .userType(User.UserType.GUARDIAN)
                .ageGroup(null)
                .build();
        userRepository.save(guardian);

        // 各年龄段被保护对象
        User child0_6 = createChildUser("child06", "0-6");
        User child6_12 = createChildUser("child612", "6-12");
        User child12_15 = createChildUser("child", "12-15");
        User child15_18 = createChildUser("child1518", "15-18");

        // ── 创建设备 ──
        deviceRepository.save(Device.builder()
                .name("小明的手机").deviceType(Device.DeviceType.PHONE)
                .status(Device.DeviceStatus.ONLINE).lastActive(LocalDateTime.now().minusMinutes(5))
                .owner(guardian).build());
        deviceRepository.save(Device.builder()
                .name("客厅电脑").deviceType(Device.DeviceType.COMPUTER)
                .status(Device.DeviceStatus.OFFLINE).lastActive(LocalDateTime.now().minusHours(2))
                .owner(guardian).build());
        deviceRepository.save(Device.builder()
                .name("学习平板").deviceType(Device.DeviceType.TABLET)
                .status(Device.DeviceStatus.ONLINE).lastActive(LocalDateTime.now().minusMinutes(30))
                .owner(guardian).build());

        // ── 时间设置（年龄分层默认值由 TimeSettingsServiceImpl 自动推断） ──
        // 监护人无年龄分层
        timeSettingsRepository.save(TimeSettings.builder()
                .dailyHours(2).dailyMinutes(0)
                .startTime("09:00").endTime("21:00")
                .weeklyLimit(14).monthlyLimit(60)
                .user(guardian).build());

        // ── 禁止列表 ──
        blockItemRepository.save(BlockItem.builder()
                .blockType(BlockItem.BlockType.WEBSITES).name("抖音").user(guardian).build());
        blockItemRepository.save(BlockItem.builder()
                .blockType(BlockItem.BlockType.WEBSITES).name("快手").user(guardian).build());
        blockItemRepository.save(BlockItem.builder()
                .blockType(BlockItem.BlockType.GAMES).name("王者荣耀").user(guardian).build());
        blockItemRepository.save(BlockItem.builder()
                .blockType(BlockItem.BlockType.GAMES).name("和平精英").user(guardian).build());
        blockItemRepository.save(BlockItem.builder()
                .blockType(BlockItem.BlockType.APPS).name("小红书").user(guardian).build());

        // ── 使用记录 ──
        LocalDateTime now = LocalDateTime.now();
        String[] apps = {"浏览器", "学习软件", "微信", "视频App", "游戏"};
        int[] durations = {600, 1800, 300, 1200, 900};

        for (int i = 0; i < 10; i++) {
            usageRecordRepository.save(UsageRecord.builder()
                    .device("小明的手机")
                    .app(apps[i % apps.length])
                    .startTime(now.minusDays(i % 7).minusHours(i))
                    .endTime(now.minusDays(i % 7).minusHours(i).plusSeconds(durations[i % durations.length]))
                    .duration(durations[i % durations.length])
                    .user(child12_15)
                    .build());
        }

        // ── 请求批准演示数据 ──
        approvalRequestRepository.save(ApprovalRequest.builder()
                .approvalType(ApprovalType.TIME_EXTENSION)
                .description("今天作业比较多，需要多用30分钟完成在线练习")
                .extraMinutes(30)
                .status(ApprovalStatus.PENDING)
                .requester(child12_15)
                .build());

        approvalRequestRepository.save(ApprovalRequest.builder()
                .approvalType(ApprovalType.UNBLOCK)
                .description("需要查资料，暂时访问百度文库")
                .targetName("百度文库")
                .status(ApprovalStatus.PENDING)
                .requester(child12_15)
                .build());

        approvalRequestRepository.save(ApprovalRequest.builder()
                .approvalType(ApprovalType.TIME_EXTENSION)
                .description("周末想多玩1小时")
                .extraMinutes(60)
                .status(ApprovalStatus.PENDING)
                .requester(child15_18)
                .build());

        // ── Phase2: 监护人-被保护人绑定关系 ──
        guardianBindingRepository.save(GuardianBinding.builder()
                .guardian(guardian).protectedUser(child6_12)
                .status(GuardianBinding.BindingStatus.ACTIVE).createdAt(now).build());
        guardianBindingRepository.save(GuardianBinding.builder()
                .guardian(guardian).protectedUser(child12_15)
                .status(GuardianBinding.BindingStatus.ACTIVE).createdAt(now).build());
        guardianBindingRepository.save(GuardianBinding.builder()
                .guardian(guardian).protectedUser(child15_18)
                .status(GuardianBinding.BindingStatus.ACTIVE).createdAt(now).build());

        // ── Phase2: 告警示例 ──
        alertRepository.save(Alert.builder()
                .protectedUser(child12_15).guardian(guardian)
                .type(Alert.AlertType.BLOCKED_CONTENT).severity(Alert.AlertSeverity.HIGH)
                .title("尝试访问禁止网站").message("被保护用户尝试访问禁止网站：抖音")
                .status(Alert.AlertStatus.NEW).createdAt(now.minusMinutes(10)).build());
        alertRepository.save(Alert.builder()
                .protectedUser(child6_12).guardian(guardian)
                .type(Alert.AlertType.TIME_EXCEEDED).severity(Alert.AlertSeverity.MEDIUM)
                .title("使用时间即将超标").message("被保护用户今日累计使用时长即将达到限额")
                .status(Alert.AlertStatus.NEW).createdAt(now.minusHours(1)).build());

        // ── Phase2: 内容过滤规则 ──
        contentFilterRuleRepository.save(ContentFilterRule.builder()
                .user(guardian).category(ContentFilterRule.FilterCategory.KEYWORD)
                .pattern("赌博").action(ContentFilterRule.FilterAction.BLOCK)
                .priority(1).enabled(true).createdAt(now).build());
        contentFilterRuleRepository.save(ContentFilterRule.builder()
                .user(guardian).category(ContentFilterRule.FilterCategory.KEYWORD)
                .pattern("色情").action(ContentFilterRule.FilterAction.BLOCK)
                .priority(1).enabled(true).createdAt(now).build());
        contentFilterRuleRepository.save(ContentFilterRule.builder()
                .user(guardian).category(ContentFilterRule.FilterCategory.WEBSITE)
                .pattern("tiktok.com").action(ContentFilterRule.FilterAction.BLOCK)
                .priority(2).enabled(true).createdAt(now).build());

        // ── Phase2: 生物特征注册 ──
        biometricRecordRepository.save(BiometricRecord.builder()
                .user(guardian).type(BiometricRecord.BiometricType.FACE)
                .dataHash("face_hash_guardian").confidenceThreshold(0.9)
                .status(BiometricRecord.BiometricStatus.ACTIVE).registeredAt(now).build());
        biometricRecordRepository.save(BiometricRecord.builder()
                .user(guardian).type(BiometricRecord.BiometricType.FINGERPRINT)
                .dataHash("fp_hash_guardian").confidenceThreshold(0.85)
                .status(BiometricRecord.BiometricStatus.ACTIVE).registeredAt(now).build());

        log.info("演示数据初始化完成：{} 个用户(含 {} 个年龄段被保护对象)", 5, 4);
    }

    private User createChildUser(String username, String ageGroup) {
        User child = User.builder()
                .username(username)
                .password(passwordEncoder.encode("123456"))
                .phone("1380013800" + (username.length() % 10))
                .userType(User.UserType.PROTECTED)
                .ageGroup(ageGroup)
                .build();
        return userRepository.save(child);
    }
}
