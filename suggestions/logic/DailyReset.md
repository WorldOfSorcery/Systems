# DailyReset

**Package:** `me.hektortm.woSSystems.utils`

## Logic Issues

### 1. Comment says "12:00" but code schedules for midnight (00:00)

Lines 21–22:
```java
// Calculate the delay until the next 12:00
LocalDateTime nextRun = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
```
The comment states the reset fires at **12:00** (noon), but `withHour(0)` schedules it at **00:00** (midnight). If the intent is noon, `withHour(12)` should be used. If the intent is midnight, the comment is simply wrong and misleading — either way, one of the two is incorrect.

### 2. Scheduler tick overflow for long periods

`delayTicks` and `periodTicks` are computed as:
```java
long delayTicks  = delaySeconds * 20;   // up to 86 400 * 20 = 1 728 000 ticks
long periodTicks = 24 * 60 * 60 * 20;  // = 1 728 000 ticks
```
The Bukkit scheduler accepts a `long` for these parameters, so overflow is not an issue. However, `delaySeconds` is the result of `ChronoUnit.SECONDS.between(now, nextRun)`. If `nextRun` has already passed (because `now.withHour(0)` produces a time earlier than `now` and the `plusDays(1)` branch is taken), the guard condition is:

```java
if (!now.isBefore(nextRun)) {
    nextRun = nextRun.plusDays(1);
}
```

When `now` equals `nextRun` exactly (e.g. the plugin starts at exactly midnight), `isBefore` returns `false` and `plusDays(1)` is applied — so the very first reset fires a full 24 hours later than expected instead of immediately. If a reset at exactly midnight is desired, the condition should be `!now.isBefore(nextRun)` changed to `now.isAfter(nextRun)` (strict greater-than) or the first run should be scheduled immediately via a separate `runTask`.
