package com.guardianbaby.config;

import com.guardianbaby.entity.*;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("已存在用户数据，跳过初始化");
            return;
        }

        log.info("正在初始化演示数据...");

        // 创建演示用户
        User guardian = User.builder()
                .username("admin")
                .password(passwordEncoder.encode("123456"))
                .phone("13800138000")
                .userType(User.UserType.GUARDIAN)
                .ageGroup(null)
                .build();
        userRepository.save(guardian);

        User child = User.builder()
                .username("child")
                .password(passwordEncoder.encode("123456"))
                .phone("13800138001")
                .userType(User.UserType.PROTECTED)
                .ageGroup("12-15")
                .build();
        userRepository.save(child);

        // 创建演示设备
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

        // 创建时间设置
        timeSettingsRepository.save(TimeSettings.builder()
                .dailyHours(2).dailyMinutes(0)
                .startTime("09:00").endTime("21:00")
                .weeklyLimit(14).monthlyLimit(60)
                .user(guardian).build());

        // 创建禁止列表
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

        // 创建使用记录
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
                    .user(guardian)
                    .build());
        }

        log.info("演示数据初始化完成");
    }
}
