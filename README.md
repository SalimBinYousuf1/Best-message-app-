# Salim — Loved, Premium Android Messaging App

**Salim** is a native Android messaging application crafted with an Apple-inspired native liquid glass design system, built on modern Android platform architecture: Jetpack Compose, Material Design 3, Room, DataStore, Kotlin Coroutines, and native Telephony APIs.

---

## 🌟 Highlights & Key Capabilities

1. **Apple-Inspired Native Liquid Glass Design**
   - Translucent frosted-glass surfaces (`.liquidGlass()`) with subtle specular inner borders and soft depth.
   - Refined Salim Blue accent (`#007AFF`) paired with warm-white canvas in Light mode, deep graphite in Dark mode, and true black in OLED mode.
   - Tactile animations with spring physics, haptics, and zero lag.

2. **Real Native SMS / MMS Telephony Integration**
   - Official Android Default SMS Handler integration using `RoleManager` (API 29+) and `Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT`.
   - Multipart SMS splitting and dispatch with real `SmsManager` and broadcast callbacks for SENT and DELIVERED delivery reports.
   - Seamless local import of existing device SMS (`content://sms`) via `SmsSyncManager` without duplicates.
   - `HeadlessSmsSendService` handling `RESPOND_VIA_MESSAGE` for instant responses to incoming calls.

3. **Smart Everyday Features**
   - **Local OTP Detection**: On-device regex detection of 4–8 digit verification codes with a prominent, 1-tap "Copy Code" glass banner.
   - **Scheduled SMS**: Native exact alarm scheduling (`AlarmManager.setExactAndAllowWhileIdle`) with reboot restoration (`BootReceiver`).
   - **Quick Reply Templates**: User-customizable canned responses ready to insert with a single tap.
   - **Draft Preservation**: Persistent auto-saved message drafts that survive process death and app navigation.
   - **Search**: Fast local search across contact names, phone numbers, and message bodies.

4. **Rich Notifications & Lock-Screen Privacy**
   - Android `MessagingStyle` notifications with avatar and contact identity.
   - Native inline reply directly from notification shade via `RemoteInput`.
   - 1-tap "Mark as Read" action.
   - Granular privacy settings: Full preview, Sender only, or Hidden.

5. **Private by Default**
   - Local-first architecture: All messages, contacts cache, drafts, and preferences stay on the user's device in SQLite/Room.
   - Zero remote telemetry, tracking, or unexpected background uploads.

---

## 🏗️ Architecture

```
com.example
├── SalimApplication.kt            // Application container & notification channel setup
├── MainActivity.kt                // Edge-to-edge entry point & SENDTO intent routing
├── data
│   ├── local
│   │   ├── SalimDatabase.kt       // Room DB with migrations & preloaded templates
│   │   ├── entity/                // ConversationEntity, MessageEntity, ScheduledMessageEntity, QuickReplyTemplateEntity
│   │   ├── dao/                   // ConversationDao, MessageDao, ScheduledMessageDao, TemplateDao
│   │   └── preferences/           // SalimPreferences (DataStore)
│   └── repository/                // ConversationRepository, MessagingRepository, ScheduleRepository, TemplateRepository
├── telephony
│   ├── DefaultSmsRoleManager.kt   // RoleManager & default SMS detection
│   ├── SmsTransport.kt            // Multipart SMSManager engine & segment calculator
│   ├── SmsReceiver.kt             // SMS_DELIVER & SMS_RECEIVED receivers
│   ├── MmsReceiver.kt             // WAP_PUSH_DELIVER receiver
│   ├── HeadlessSmsSendService.kt  // Respond via message service
│   ├── ScheduledSmsReceiver.kt    // Alarm-based scheduled message dispatcher
│   ├── BootReceiver.kt            // Boot restoration receiver
│   ├── ContactResolver.kt         // Android ContactsContract query engine
│   ├── OtpDetector.kt             // Privacy-first local verification code extractor
│   └── NotificationCoordinator.kt // Notification channels, RemoteInput inline reply
└── ui
    ├── theme/                     // Apple-like color palette, Theme modes, GlassModifier
    ├── components/                // LiquidGlassTopBar, ContactAvatar, OtpBanner, StatusBadges
    ├── inbox/                     // InboxScreen, ConversationItem, InboxViewModel
    ├── conversation/              // ConversationScreen, MessageBubble, ComposerDock, Attachments
    ├── compose/                   // ComposeScreen, ComposeViewModel
    ├── search/                    // SearchScreen, SearchViewModel
    ├── scheduled/                 // ScheduledListScreen, ScheduledViewModel
    ├── settings/                  // SettingsScreen, SettingsViewModel
    ├── onboarding/                // OnboardingScreen
    └── navigation/                // SalimNavGraph & Screen routes
```

---

## 🚀 Building & Running

### Compile APK
```bash
gradle assembleDebug
```

### Run Tests
```bash
gradle :app:testDebugUnitTest
```

---

## 🔒 Permissions & Security

- `android.permission.RECEIVE_SMS`: Receive incoming SMS
- `android.permission.READ_SMS`: Import device SMS into local database
- `android.permission.SEND_SMS`: Dispatch text messages
- `android.permission.RECEIVE_MMS` & `RECEIVE_WAP_PUSH`: MMS receipt
- `android.permission.READ_CONTACTS`: Display contact names and avatars
- `android.permission.POST_NOTIFICATIONS`: Message alerts with inline reply
- `android.permission.SCHEDULE_EXACT_ALARM`: Time-precise scheduled messages
- `android.permission.RECEIVE_BOOT_COMPLETED`: Reschedule pending messages after reboot
- `android.permission.CALL_PHONE`: Direct dialer action from conversation header
